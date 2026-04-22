const SYNC_CONFIG_KEY = 'qinglvzhoumo.sync.config.v1'
const DEVICE_ID_KEY = 'qinglvzhoumo.device.id'

function defaultApiBaseUrl() {
  let url = 'http://localhost:8080'
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
  return {
    apiBaseUrl: saved.apiBaseUrl || defaultApiBaseUrl(),
    token: saved.token || '',
    userId: saved.userId || '',
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
      url: `${baseUrl}${path}`,
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
        reject(new Error(message))
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
    coupleId: data.coupleId || '',
    enabled: true,
    lastError: ''
  })
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
