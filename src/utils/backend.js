const SYNC_CONFIG_KEY = 'qinglvzhoumo.sync.config.v1'
const DEVICE_ID_KEY = 'qinglvzhoumo.device.id'

function defaultApiBaseUrl() {
  let url = '/api'
  // #ifdef APP-PLUS
  url = 'http://10.0.2.2:8080'
  // #endif
  return url
}

function createDeviceId() {
  return `device-${Date.now()}-${Math.floor(Math.random() * 100000)}`
}

export function getDeviceId() {
  let deviceId = uni.getStorageSync(DEVICE_ID_KEY)
  if (!deviceId) {
    deviceId = createDeviceId()
    uni.setStorageSync(DEVICE_ID_KEY, deviceId)
  }
  return deviceId
}

export function getSyncConfig() {
  const saved = uni.getStorageSync(SYNC_CONFIG_KEY) || {}
  const apiBaseUrl = saved.apiBaseUrl || defaultApiBaseUrl()
  return {
    apiBaseUrl,
    token: saved.token || '',
    userId: saved.userId || '',
    phone: saved.phone || '',
    registered: Boolean(saved.registered),
    coupleId: saved.coupleId || '',
    inviteCode: saved.inviteCode || '',
    memberCount: saved.memberCount || 0,
    lastPulledAt: saved.lastPulledAt || '',
    enabled: Boolean(saved.enabled),
    lastError: saved.lastError || '',
    lastSyncedAt: saved.lastSyncedAt || ''
  }
}

export function saveSyncConfig(patch) {
  const next = { ...getSyncConfig(), ...patch }
  uni.setStorageSync(SYNC_CONFIG_KEY, next)
  return next
}

export function clearSyncError() {
  return saveSyncConfig({ lastError: '' })
}

export function requestBackend(path, options = {}) {
  const config = getSyncConfig()
  const baseUrl = (options.apiBaseUrl || config.apiBaseUrl || defaultApiBaseUrl()).replace(/\/$/, '')
  const headers = { ...(options.header || {}) }
  if (options.auth !== false && config.token) {
    headers.Authorization = `Bearer ${config.token}`
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseUrl.endsWith('/api') && path.startsWith('/api/')
        ? `${baseUrl}${path.slice(4)}`
        : `${baseUrl}${path}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'content-type': 'application/json',
        ...headers
      },
      timeout: options.timeout || 12000,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
          return
        }
        const message = res.data?.message || res.data?.error || `HTTP ${res.statusCode}`
        const error = new Error(message)
        error.code = res.data?.code || ''
        error.statusCode = res.statusCode
        reject(error)
      },
      fail: (error) => {
        reject(new Error(error.errMsg || 'Network request failed'))
      }
    })
  })
}

export async function ensureAnonymousAuth(nickname = '') {
  const config = getSyncConfig()
  if (config.token) return config
  const data = await requestBackend('/api/auth/anonymous', {
    method: 'POST',
    auth: false,
    data: {
      deviceId: getDeviceId(),
      nickname: nickname || 'qinglv'
    }
  })
  return saveSyncConfig({
    token: data.token,
    userId: data.userId,
    phone: data.phone || '',
    registered: Boolean(data.registered),
    coupleId: data.coupleId || '',
    enabled: true,
    lastError: ''
  })
}

export async function registerAccount({ phone, password, nickname = '' }) {
  await ensureAnonymousAuth(nickname)
  const data = await requestBackend('/api/auth/register', {
    method: 'POST',
    data: { phone: phone.trim(), password, nickname }
  })
  return saveSyncConfig({
    token: data.token,
    userId: data.userId,
    phone: data.phone || phone.trim(),
    registered: Boolean(data.registered),
    coupleId: data.coupleId || '',
    enabled: Boolean(data.coupleId),
    lastError: ''
  })
}

export async function loginAccount({ phone, password }) {
  const data = await requestBackend('/api/auth/login', {
    method: 'POST',
    auth: false,
    data: {
      phone: phone.trim(),
      password,
      deviceId: getDeviceId()
    }
  })
  return saveSyncConfig({
    token: data.token,
    userId: data.userId,
    phone: data.phone || phone.trim(),
    registered: Boolean(data.registered),
    coupleId: data.coupleId || '',
    enabled: Boolean(data.coupleId),
    lastPulledAt: '',
    lastError: ''
  })
}

export async function logoutAccount() {
  const config = getSyncConfig()
  if (config.token) {
    try {
      await requestBackend('/api/auth/logout', { method: 'POST' })
    } catch (error) {
      // Local logout should not be blocked by a stale token or offline backend.
    }
  }
  return saveSyncConfig({
    token: '',
    userId: '',
    phone: '',
    registered: false,
    coupleId: '',
    inviteCode: '',
    memberCount: 0,
    enabled: false,
    lastPulledAt: '',
    lastError: ''
  })
}

export function getCurrentAccount() {
  return requestBackend('/api/auth/me')
}

export async function createInvite(nickname = '') {
  await ensureAnonymousAuth(nickname)
  const data = await requestBackend('/api/couples/invite', { method: 'POST' })
  return saveSyncConfig({
    coupleId: data.coupleId,
    inviteCode: data.inviteCode,
    memberCount: data.memberCount,
    enabled: true,
    lastError: ''
  })
}

export async function getCoupleStatus(nickname = '') {
  await ensureAnonymousAuth(nickname)
  const data = await requestBackend('/api/couples/me')
  return saveSyncConfig({
    userId: data.userId,
    phone: data.phone || getSyncConfig().phone || '',
    registered: Boolean(data.registered || getSyncConfig().registered),
    coupleId: data.coupleId || '',
    inviteCode: data.inviteCode || '',
    memberCount: data.memberCount || 0,
    enabled: Boolean(data.coupleId),
    lastError: ''
  })
}

export async function joinInvite(inviteCode, nickname = '') {
  await ensureAnonymousAuth(nickname)
  const data = await requestBackend('/api/couples/join', {
    method: 'POST',
    data: { inviteCode: inviteCode.trim().toUpperCase() }
  })
  return saveSyncConfig({
    coupleId: data.coupleId,
    inviteCode: data.inviteCode,
    memberCount: data.memberCount,
    enabled: true,
    lastError: ''
  })
}

export async function leaveCouple() {
  const data = await requestBackend('/api/couples/leave', { method: 'POST' })
  return saveSyncConfig({
    userId: data.userId,
    coupleId: '',
    inviteCode: '',
    memberCount: 0,
    enabled: false,
    lastPulledAt: '',
    lastError: ''
  })
}

export function pullSync(since = '') {
  const query = since ? `?since=${encodeURIComponent(since)}` : ''
  return requestBackend(`/api/sync${query}`)
}

export function pushSync(items) {
  return requestBackend('/api/sync', {
    method: 'POST',
    data: { items }
  })
}
