import { getNowString, getTodayString } from '../utils/date'
import {
  createInvite,
  getCoupleStatus,
  getDeviceId,
  getSyncConfig,
  joinInvite,
  leaveCouple,
  pullSync,
  pushSync,
  saveSyncConfig
} from '../utils/backend'

const STORAGE_KEY = 'qinglvzhoumo.state.v2'
const LEGACY_STORAGE_KEY = 'qinglvzhoumo.state.v1'

let syncTimer = null
let syncRunning = false

const defaultPlans = [
  { id: 'plan-1', title: '一起散步 20 分钟', detail: '不看手机，只聊今天最轻松的一件小事。', doneCount: 0 },
  { id: 'plan-2', title: '互相点一杯饮品', detail: '按对方今天的心情来选，不用问太多。', doneCount: 0 },
  { id: 'plan-3', title: '拍一张今日合照', detail: '不用很正式，留下此刻就很好。', doneCount: 0 },
  { id: 'plan-4', title: '交换一个小愿望', detail: '说一个这周想被对方照顾到的小地方。', doneCount: 0 },
  { id: 'plan-5', title: '做一顿简单夜宵', detail: '哪怕只是煮面，也一起完成。', doneCount: 0 },
  { id: 'plan-6', title: '互写三句夸夸', detail: '写给对方今天最可爱的三个瞬间。', doneCount: 0 },
  { id: 'plan-7', title: '整理一张合照', detail: '选一张照片，给它起一个只有你们懂的名字。', doneCount: 0 },
  { id: 'plan-8', title: '交换今晚的歌单', detail: '每人选一首歌，说说为什么想到对方。', doneCount: 0 },
  { id: 'plan-9', title: '计划一次微旅行', detail: '只定一个地点和一家想吃的小店，不用马上出发。', doneCount: 0 },
  { id: 'plan-10', title: '一起做 10 分钟拉伸', detail: '动作不用标准，重点是一起慢下来。', doneCount: 0 }
]

const defaultQuestions = [
  '今天最开心的事是什么？',
  '最近哪一刻觉得被我照顾到了？',
  '这个周末最想一起做什么？',
  '有什么小事想让我更懂你一点？',
  '今天想要一个怎样的拥抱？',
  '如果今晚有一小时完全属于我们，想怎么过？',
  '最近有没有一件事想让我少担心一点？',
  '你觉得我们最近最默契的一刻是什么？',
  '下次约会想多一点热闹，还是多一点安静？',
  '这个月最想留下哪种回忆？'
]

const defaultState = {
  profile: withMeta({
    id: 'profile-main',
    me: '小清',
    partner: '小明',
    startDate: '2025-07-29',
    initialized: false
  }, 'profile-main'),
  plans: defaultPlans,
  questions: defaultQuestions,
  wishTemplates: [
    '一起看一场日落',
    '周末去逛一次花市',
    '拍一组不用修的生活照',
    '做一次两个人的早餐',
    '去一家收藏很久的小店',
    '一起完成一场短途旅行'
  ],
  wishes: [
    withMeta({ id: 'wish-1', title: '周末去看一场日落', done: false, createdAt: '2026-04-01' }, 'wish-1'),
    withMeta({ id: 'wish-2', title: '一起做一本旅行相册', done: false, createdAt: '2026-04-05' }, 'wish-2'),
    withMeta({ id: 'wish-3', title: '去吃收藏很久的小店', done: true, createdAt: '2026-04-10' }, 'wish-3')
  ],
  memories: [
    withMeta({
      id: 'memory-1',
      title: '昨日的甜品约会',
      date: '2026-04-20',
      description: '点了两杯喝的和一块蛋糕，慢慢聊到天黑。',
      color: '#f9b38d'
    }, 'memory-1')
  ],
  today: {
    date: '',
    planId: '',
    completedPlanIds: [],
    question: '',
    answer: '',
    answeredAt: ''
  },
  planHistory: [],
  dailyEntries: []
}

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function createId(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1000)}`
}

function withMeta(value, fallbackId = createId('item')) {
  const now = getNowString()
  return {
    ...value,
    id: value.id || fallbackId,
    createdAt: value.createdAt || now,
    updatedAt: value.updatedAt || value.createdAt || now,
    deletedAt: value.deletedAt || '',
    version: value.version || 1
  }
}

function currentActor() {
  const config = getSyncConfig()
  const userId = config.userId ? String(config.userId) : ''
  const deviceId = getDeviceId()
  return {
    userId,
    deviceId,
    actorKey: userId ? `user:${userId}` : `device:${deviceId}`
  }
}

function withOwner(value) {
  const actor = currentActor()
  return {
    ...value,
    ownerUserId: value.ownerUserId || actor.userId,
    ownerDeviceId: value.ownerDeviceId || actor.deviceId
  }
}

function touch(value) {
  return {
    ...value,
    updatedAt: getNowString(),
    version: (value.version || 1) + 1
  }
}

function answerKey(answer) {
  if (answer.actorKey) return answer.actorKey
  if (answer.userId) return `user:${answer.userId}`
  if (answer.deviceId) return `device:${answer.deviceId}`
  return ''
}

function createAnswer(text) {
  const actor = currentActor()
  const now = getNowString()
  return {
    actorKey: actor.actorKey,
    userId: actor.userId,
    deviceId: actor.deviceId,
    text,
    answeredAt: now,
    updatedAt: now
  }
}

function normalizeAnswers(entry) {
  const answers = Array.isArray(entry.answers) ? entry.answers : []
  const normalized = answers
    .map((answer) => ({
      actorKey: answerKey(answer),
      userId: answer.userId ? String(answer.userId) : '',
      deviceId: answer.deviceId || '',
      text: answer.text || answer.answer || '',
      answeredAt: answer.answeredAt || answer.updatedAt || '',
      updatedAt: answer.updatedAt || answer.answeredAt || ''
    }))
    .filter((answer) => answer.actorKey && answer.text)
  if (!normalized.length && entry.answer) {
    const actor = currentActor()
    const answeredAt = entry.answeredAt || entry.updatedAt || getNowString()
    normalized.push({
      actorKey: actor.actorKey,
      userId: actor.userId,
      deviceId: actor.deviceId,
      text: entry.answer,
      answeredAt,
      updatedAt: answeredAt
    })
  }
  return normalized
}

function upsertAnswer(answers, answer) {
  const key = answerKey(answer)
  const list = Array.isArray(answers) ? answers : []
  if (!key) return list
  if (!list.some((item) => answerKey(item) === key)) return [answer, ...list]
  return list.map((item) => (answerKey(item) === key ? answer : item))
}

function mergeAnswers(localAnswers, remoteAnswers) {
  const byKey = new Map()
  ;[...(localAnswers || []), ...(remoteAnswers || [])].forEach((answer) => {
    const key = answerKey(answer)
    if (!key) return
    const current = byKey.get(key)
    if (!current || compareUpdatedAt(answer.updatedAt || answer.answeredAt, current.updatedAt || current.answeredAt) >= 0) {
      byKey.set(key, answer)
    }
  })
  return Array.from(byKey.values())
}

function normalizeEntityList(list, fallback, prefix) {
  const source = Array.isArray(list) ? list : fallback
  return source.map((item, index) => withMeta(item, item.id || `${prefix}-${index + 1}`))
}

function normalizeToday(today, state) {
  const current = today && typeof today === 'object' ? today : {}
  const date = current.date || getTodayString()
  return {
    date,
    planId: current.planId || '',
    completedPlanIds: Array.isArray(current.completedPlanIds) ? current.completedPlanIds : [],
    question: current.question || pickDailyQuestion(state.questions, date),
    answer: current.answer || '',
    answeredAt: current.answeredAt || ''
  }
}

function normalizeDailyEntry(entry) {
  const answers = normalizeAnswers(entry)
  const myAnswer = answers.find((answer) => answer.actorKey === currentActor().actorKey) || answers[0]
  return withMeta({
    id: entry.id,
    date: entry.date || getTodayString(),
    question: entry.question || '',
    answer: myAnswer?.text || entry.answer || '',
    answeredAt: myAnswer?.answeredAt || entry.answeredAt || '',
    answers,
    planId: entry.planId || '',
    planTitle: entry.planTitle || entry.title || '',
    planDetail: entry.planDetail || '',
    planDone: Boolean(entry.planDone),
    planCompletedAt: entry.planCompletedAt || entry.completedAt || ''
  }, entry.id || `daily-${entry.date || getTodayString()}`)
}

function migratePlanHistory(planHistory) {
  if (!Array.isArray(planHistory)) return []
  return planHistory.map((item) => normalizeDailyEntry({
    id: item.id ? `daily-${item.id}` : `daily-${item.completedAt || getTodayString()}`,
    date: item.completedAt || getTodayString(),
    question: '',
    answer: '',
    planId: item.planId || '',
    planTitle: item.title || '',
    planDone: true,
    planCompletedAt: item.completedAt || ''
  }))
}

function normalizeState(value) {
  const fallback = clone(defaultState)
  const source = value && typeof value === 'object' ? value : {}
  const state = {
    ...fallback,
    ...source,
    profile: withMeta({ ...fallback.profile, ...(source.profile || {}) }, 'profile-main'),
    plans: Array.isArray(source.plans) && source.plans.length ? source.plans : fallback.plans,
    questions: Array.isArray(source.questions) && source.questions.length ? source.questions : fallback.questions,
    wishTemplates: Array.isArray(source.wishTemplates) && source.wishTemplates.length ? source.wishTemplates : fallback.wishTemplates,
    wishes: normalizeEntityList(source.wishes, fallback.wishes, 'wish'),
    memories: normalizeEntityList(source.memories, fallback.memories, 'memory'),
    planHistory: Array.isArray(source.planHistory) ? source.planHistory : fallback.planHistory,
    dailyEntries: Array.isArray(source.dailyEntries) && source.dailyEntries.length
      ? source.dailyEntries.map(normalizeDailyEntry)
      : migratePlanHistory(source.planHistory)
  }
  state.today = normalizeToday(source.today, state)
  return state
}

function pickDailyQuestion(questions, date = getTodayString()) {
  const pool = Array.isArray(questions) && questions.length ? questions : defaultQuestions
  const index = date.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0) % pool.length
  return pool[index]
}

function activeDailyEntries(state) {
  return state.dailyEntries
    .filter((entry) => !entry.deletedAt)
    .sort((a, b) => b.date.localeCompare(a.date) || b.updatedAt.localeCompare(a.updatedAt))
}

function findTodayEntry(state) {
  return state.dailyEntries.find((entry) => entry.date === state.today.date && !entry.deletedAt)
}

function upsertDailyEntry(state, patch) {
  const current = findTodayEntry(state)
  const base = current || normalizeDailyEntry({
    id: `daily-${state.today.date}`,
    date: state.today.date,
    question: state.today.question
  })
  const next = touch(normalizeDailyEntry({ ...base, ...patch, date: state.today.date }))
  if (current) {
    state.dailyEntries = state.dailyEntries.map((entry) => (entry.id === current.id ? next : entry))
  } else {
    state.dailyEntries.unshift(next)
  }
  return next
}

function rolloverToday(state) {
  const today = getTodayString()
  if (state.today.date === today) return state
  state.today = {
    date: today,
    planId: '',
    completedPlanIds: [],
    question: pickDailyQuestion(state.questions, today),
    answer: '',
    answeredAt: ''
  }
  return state
}

function compareUpdatedAt(a, b) {
  return String(a || '').localeCompare(String(b || ''))
}

function activeList(list) {
  return Array.isArray(list) ? list.filter((item) => !item.deletedAt) : []
}

const syncListTypes = {
  wishes: 'wish',
  memories: 'memory',
  dailyEntries: 'dailyEntry'
}

function toSyncItems(state) {
  const items = [
    {
      type: 'profile',
      clientId: state.profile.id || 'profile-main',
      payload: state.profile,
      version: state.profile.version || 1,
      updatedAt: state.profile.updatedAt || getNowString(),
      deletedAt: state.profile.deletedAt || ''
    }
  ]
  Object.entries(syncListTypes).forEach(([key, type]) => {
    ;(state[key] || []).forEach((item) => {
      const payload = withOwner(item)
      items.push({
        type,
        clientId: item.id,
        payload,
        version: item.version || 1,
        authorUserId: payload.ownerUserId || undefined,
        updatedAt: item.updatedAt || item.createdAt || getNowString(),
        deletedAt: item.deletedAt || ''
      })
    })
  })
  return items
}

function mergeItemList(localList, remoteItem, normalize) {
  const payload = remoteItem.payload || {}
  const remote = normalize({
    ...payload,
    id: remoteItem.clientId || payload.id,
    ownerUserId: payload.ownerUserId || (remoteItem.authorUserId ? String(remoteItem.authorUserId) : ''),
    version: remoteItem.version || payload.version || 1,
    updatedAt: remoteItem.updatedAt || payload.updatedAt || getNowString(),
    deletedAt: remoteItem.deletedAt || payload.deletedAt || ''
  })
  const index = localList.findIndex((item) => item.id === remote.id)
  if (index < 0) return [remote, ...localList]
  const local = localList[index]
  if (compareUpdatedAt(remote.updatedAt, local.updatedAt) < 0) return localList
  return localList.map((item) => (item.id === remote.id ? remote : item))
}

function mergeDailyEntryList(localList, remoteItem) {
  const payload = remoteItem.payload || {}
  const remote = normalizeDailyEntry({
    ...payload,
    id: remoteItem.clientId || payload.id,
    ownerUserId: payload.ownerUserId || (remoteItem.authorUserId ? String(remoteItem.authorUserId) : ''),
    version: remoteItem.version || payload.version || 1,
    updatedAt: remoteItem.updatedAt || payload.updatedAt || getNowString(),
    deletedAt: remoteItem.deletedAt || payload.deletedAt || ''
  })
  const index = localList.findIndex((item) => item.id === remote.id)
  if (index < 0) return [remote, ...localList]
  const local = localList[index]
  const merged = normalizeDailyEntry({
    ...local,
    ...remote,
    answers: mergeAnswers(local.answers, remote.answers),
    updatedAt: compareUpdatedAt(remote.updatedAt, local.updatedAt) >= 0 ? remote.updatedAt : local.updatedAt,
    version: Math.max(local.version || 1, remote.version || 1)
  })
  return localList.map((item) => (item.id === remote.id ? merged : item))
}

function mergeRemoteItems(state, items) {
  ;(items || []).forEach((item) => {
    if (item.type === 'profile') {
      const remote = withMeta({
        ...state.profile,
        ...(item.payload || {}),
        id: item.clientId || 'profile-main',
        version: item.version || 1,
        updatedAt: item.updatedAt || item.payload?.updatedAt || getNowString(),
        deletedAt: item.deletedAt || item.payload?.deletedAt || ''
      }, 'profile-main')
      if (compareUpdatedAt(remote.updatedAt, state.profile.updatedAt) >= 0) {
        state.profile = remote
      }
      return
    }
    if (item.type === 'wish') {
      state.wishes = mergeItemList(state.wishes, item, (value) => withMeta(value, value.id))
      return
    }
    if (item.type === 'memory') {
      state.memories = mergeItemList(state.memories, item, (value) => withMeta(value, value.id))
      return
    }
    if (item.type === 'dailyEntry') {
      state.dailyEntries = mergeDailyEntryList(state.dailyEntries, item)
    }
  })
  return normalizeState(state)
}

function markSyncError(error) {
  saveSyncConfig({ lastError: error?.message || String(error || 'sync failed') })
}

export async function syncNow(options = {}) {
  const config = getSyncConfig()
  if (!config.enabled || !config.token || !config.coupleId || syncRunning) return loadState()
  syncRunning = true
  try {
    let state = loadState()
    const pulled = await pullSync(options.full ? '' : config.lastPulledAt)
    if (pulled?.items?.length) {
      state = mergeRemoteItems(state, pulled.items)
      saveState(state)
    }
    const pushed = await pushSync(toSyncItems(state))
    if (pushed?.items?.length) {
      state = mergeRemoteItems(loadState(), pushed.items)
      saveState(state)
    }
    saveSyncConfig({
      lastPulledAt: pushed?.serverTime || pulled?.serverTime || getNowString(),
      lastSyncedAt: getNowString(),
      lastError: ''
    })
    return loadState()
  } catch (error) {
    markSyncError(error)
    if (!options.silent) throw error
    return loadState()
  } finally {
    syncRunning = false
  }
}

export function queueSync() {
  const config = getSyncConfig()
  if (!config.enabled || !config.token || !config.coupleId) return
  if (syncTimer) clearTimeout(syncTimer)
  syncTimer = setTimeout(() => {
    syncNow({ silent: true }).catch(() => {})
  }, 500)
}

export function getBackendSyncConfig() {
  return getSyncConfig()
}

export function setBackendApiBaseUrl(apiBaseUrl) {
  return saveSyncConfig({ apiBaseUrl: apiBaseUrl.trim() || undefined, lastError: '' })
}

export async function createCoupleInvite() {
  const state = loadState()
  const config = await createInvite(state.profile.me)
  await syncNow({ full: true, silent: true })
  return config
}

export async function refreshCoupleStatus() {
  const state = loadState()
  return getCoupleStatus(state.profile.me)
}

export async function joinCoupleByInvite(inviteCode) {
  const state = loadState()
  const config = await joinInvite(inviteCode, state.profile.me)
  await syncNow({ full: true, silent: true })
  return config
}

export async function leaveCoupleSpace() {
  return leaveCouple()
}

export function initializeBackendSync() {
  queueSync()
}

export function loadState() {
  const saved = uni.getStorageSync(STORAGE_KEY) || uni.getStorageSync(LEGACY_STORAGE_KEY)
  const state = rolloverToday(normalizeState(saved))
  saveState(state)
  return state
}

export function saveState(state) {
  uni.setStorageSync(STORAGE_KEY, normalizeState(state))
}

export function updateProfile(profile) {
  const state = loadState()
  state.profile = touch({ ...state.profile, ...profile })
  saveState(state)
  queueSync()
  return state
}

export function resetState() {
  const state = rolloverToday(clone(defaultState))
  saveState(state)
  return state
}

export function drawPlan() {
  const state = loadState()
  const currentId = state.today.planId
  const pool = state.plans.filter((item) => item.id !== currentId)
  const plans = pool.length ? pool : state.plans
  const next = plans[Math.floor(Math.random() * plans.length)]
  state.today.planId = next.id
  upsertDailyEntry(state, withOwner({
    question: state.today.question,
    answer: state.today.answer,
    answeredAt: state.today.answeredAt,
    answers: findTodayEntry(state)?.answers || [],
    planId: next.id,
    planTitle: next.title,
    planDetail: next.detail,
    planDone: state.today.completedPlanIds.includes(next.id)
  }))
  saveState(state)
  queueSync()
  return { state, plan: next }
}

export function saveDailyAnswer(answer) {
  const text = answer.trim()
  const state = loadState()
  const nextAnswer = createAnswer(text)
  const current = findTodayEntry(state)
  state.today.answer = text
  state.today.answeredAt = text ? nextAnswer.answeredAt : ''
  upsertDailyEntry(state, {
    question: state.today.question,
    answer: state.today.answer,
    answeredAt: state.today.answeredAt,
    answers: text ? upsertAnswer(current?.answers, nextAnswer) : current?.answers || []
  })
  saveState(state)
  queueSync()
  return state
}

export function toggleTodayPlanDone(planId) {
  const state = loadState()
  const completed = new Set(state.today.completedPlanIds)
  const wasDone = completed.has(planId)
  if (wasDone) {
    completed.delete(planId)
  } else {
    completed.add(planId)
  }
  state.today.completedPlanIds = Array.from(completed)
  const plan = state.plans.find((item) => item.id === planId)
  if (plan && !wasDone) {
    const alreadyLogged = state.planHistory.some((item) => item.planId === planId && item.completedAt === state.today.date)
    if (!alreadyLogged) {
      state.planHistory.unshift({
        id: createId('history'),
        planId,
        title: plan.title,
        completedAt: state.today.date
      })
      state.planHistory = state.planHistory.slice(0, 50)
    }
    state.plans = state.plans.map((item) => (
      item.id === planId ? { ...item, doneCount: (item.doneCount || 0) + 1 } : item
    ))
  }
  if (plan) {
    upsertDailyEntry(state, withOwner({
      question: state.today.question,
      answer: state.today.answer,
      answeredAt: state.today.answeredAt,
      answers: findTodayEntry(state)?.answers || [],
      planId,
      planTitle: plan.title,
      planDetail: plan.detail,
      planDone: !wasDone,
      planCompletedAt: wasDone ? '' : getNowString()
    }))
  }
  saveState(state)
  queueSync()
  return state
}

export function addWish(title) {
  const state = loadState()
  state.wishes.unshift(withMeta(withOwner({
    id: createId('wish'),
    title: title.trim(),
    done: false,
    createdAt: getTodayString()
  })))
  saveState(state)
  queueSync()
  return state
}

export function updateWish(id, title) {
  const state = loadState()
  state.wishes = state.wishes.map((wish) => (
    wish.id === id ? touch({ ...wish, title: title.trim() }) : wish
  ))
  saveState(state)
  queueSync()
  return state
}

export function deleteWish(id) {
  const state = loadState()
  state.wishes = state.wishes.map((wish) => (
    wish.id === id ? touch({ ...wish, deletedAt: getNowString() }) : wish
  ))
  saveState(state)
  queueSync()
  return state
}

export function toggleWish(id) {
  const state = loadState()
  state.wishes = state.wishes.map((wish) => (
    wish.id === id ? touch({ ...wish, done: !wish.done }) : wish
  ))
  saveState(state)
  queueSync()
  return state
}

export function addMemory(memory) {
  const state = loadState()
  state.memories.unshift(withMeta(withOwner({
    id: createId('memory'),
    title: memory.title.trim(),
    date: memory.date || getTodayString(),
    description: memory.description.trim(),
    image: memory.image || '',
    color: memory.color || '#f9b38d'
  })))
  saveState(state)
  queueSync()
  return state
}

export function updateMemory(id, memory) {
  const state = loadState()
  state.memories = state.memories.map((item) => (
    item.id === id
      ? touch({
          ...item,
          title: memory.title.trim(),
          date: memory.date || getTodayString(),
          description: memory.description.trim(),
          image: memory.image || item.image || '',
          color: memory.color || item.color || '#f9b38d'
        })
      : item
  ))
  saveState(state)
  queueSync()
  return state
}

export function deleteMemory(id) {
  const state = loadState()
  state.memories = state.memories.map((memory) => (
    memory.id === id ? touch({ ...memory, deletedAt: getNowString() }) : memory
  ))
  saveState(state)
  queueSync()
  return state
}

export function deleteDailyEntry(id) {
  const state = loadState()
  state.dailyEntries = state.dailyEntries.map((entry) => (
    entry.id === id ? touch({ ...entry, deletedAt: getNowString() }) : entry
  ))
  saveState(state)
  queueSync()
  return state
}

export function getDailyQuestion(state) {
  return state.today.question || pickDailyQuestion(state.questions, state.today.date)
}

export function getActiveDailyEntries(state) {
  return activeDailyEntries(state)
}

export function getLatestDailyEntry(state) {
  return activeDailyEntries(state)[0]
}

export function getActiveWishes(state) {
  return activeList(state.wishes)
}

export function getActiveMemories(state) {
  return activeList(state.memories)
}

function isMine(item) {
  const actor = currentActor()
  if (item.ownerUserId && actor.userId) return String(item.ownerUserId) === actor.userId
  if (item.ownerDeviceId) return item.ownerDeviceId === actor.deviceId
  return true
}

function isPartnerItem(item) {
  if (!getSyncConfig().coupleId) return false
  return !isMine(item)
}

export function getTodayAnswerPair(state) {
  const entry = findTodayEntry(state)
  const answers = entry?.answers || []
  const actor = currentActor()
  const mine = answers.find((answer) => answer.actorKey === actor.actorKey)
  const partner = answers.find((answer) => answer.actorKey !== actor.actorKey)
  return { mine, partner }
}

export function getPartnerActivities(state) {
  const activities = []
  getActiveWishes(state).forEach((wish) => {
    if (!isPartnerItem(wish)) return
    activities.push({
      id: `wish-${wish.id}`,
      text: wish.done ? `TA 完成了愿望：${wish.title}` : `TA 新增了愿望：${wish.title}`,
      time: wish.updatedAt || wish.createdAt,
      type: 'wish'
    })
  })
  getActiveMemories(state).forEach((memory) => {
    if (!isPartnerItem(memory)) return
    activities.push({
      id: `memory-${memory.id}`,
      text: `TA 记录了回忆：${memory.title}`,
      time: memory.updatedAt || memory.createdAt || memory.date,
      type: 'memory'
    })
  })
  getActiveDailyEntries(state).forEach((entry) => {
    ;(entry.answers || []).forEach((answer) => {
      if (answer.actorKey === currentActor().actorKey) return
      activities.push({
        id: `answer-${entry.id}-${answer.actorKey}`,
        text: `TA 回答了今日问题：${answer.text}`,
        time: answer.updatedAt || answer.answeredAt || entry.updatedAt,
        type: 'answer'
      })
    })
    if (entry.planDone && isPartnerItem(entry)) {
      activities.push({
        id: `plan-${entry.id}`,
        text: `TA 完成了小计划：${entry.planTitle}`,
        time: entry.planCompletedAt || entry.updatedAt,
        type: 'plan'
      })
    }
  })
  return activities
    .filter((item) => item.time)
    .sort((a, b) => String(b.time).localeCompare(String(a.time)))
    .slice(0, 3)
}

export function getLatestTimelineItem(state) {
  const items = []
  getActiveDailyEntries(state).forEach((entry) => {
    if (entry.planDone && entry.planTitle) {
      items.push({ text: `完成小计划：${entry.planTitle}`, time: entry.planCompletedAt || entry.updatedAt, date: entry.date })
    }
  })
  getActiveWishes(state).forEach((wish) => {
    if (wish.done) items.push({ text: `完成愿望：${wish.title}`, time: wish.updatedAt || wish.createdAt, date: wish.updatedAt || wish.createdAt })
  })
  getActiveMemories(state).forEach((memory) => {
    items.push({ text: `保存回忆：${memory.title}`, time: memory.updatedAt || memory.createdAt || memory.date, date: memory.date })
  })
  return items.filter((item) => item.time).sort((a, b) => String(b.time).localeCompare(String(a.time)))[0]
}

export function getMilestoneMessage(loveDays) {
  const messages = {
    100: '第 100 天，已经攒下一个小小里程碑。',
    365: '一周年快乐，今天很适合多留下一段回忆。',
    520: '第 520 天，适合说一句平时不好意思说的话。',
    999: '第 999 天，很多日常都变成了你们的故事。'
  }
  return messages[loveDays] || ''
}
