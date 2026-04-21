import { getTodayString } from '../utils/date'

const STORAGE_KEY = 'qinglvzhoumo.state.v1'

const defaultState = {
  profile: {
    me: '小帅',
    partner: '小明',
    startDate: '2025-07-29'
  },
  plans: [
    { id: 'plan-1', title: '一起散步 20 分钟', detail: '不看手机，只聊今天最开心的一件小事。', doneCount: 0 },
    { id: 'plan-2', title: '互相点一杯饮品', detail: '按对方今天的心情来选，不用问太多。', doneCount: 0 },
    { id: 'plan-3', title: '拍一张今日合照', detail: '不用很正式，留下此刻就很好。', doneCount: 0 },
    { id: 'plan-4', title: '交换一个小愿望', detail: '说一个这周想被对方照顾到的小地方。', doneCount: 0 },
    { id: 'plan-5', title: '做一顿简单夜宵', detail: '哪怕只是煮面，也一起完成。', doneCount: 0 }
  ],
  questions: [
    '今天最开心的事是什么？',
    '最近哪一刻觉得被我照顾到了？',
    '这个周末最想一起做什么？',
    '有什么小事想让我更懂你一点？',
    '今天想要一个怎样的拥抱？'
  ],
  wishes: [
    { id: 'wish-1', title: '周末去看一场日落', done: false, createdAt: '2026-04-01' },
    { id: 'wish-2', title: '一起做一本旅行相册', done: false, createdAt: '2026-04-05' },
    { id: 'wish-3', title: '去吃收藏很久的小店', done: true, createdAt: '2026-04-10' }
  ],
  memories: [
    {
      id: 'memory-1',
      title: '昨日的甜品约会',
      date: '2026-04-20',
      description: '点了两杯喝的和一块蛋糕，慢慢聊到天黑。',
      color: '#f9b38d'
    }
  ],
  today: {
    date: '',
    planId: '',
    completedPlanIds: []
  }
}

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function normalizeState(value) {
  const fallback = clone(defaultState)
  const state = value && typeof value === 'object' ? value : {}
  return {
    ...fallback,
    ...state,
    profile: { ...fallback.profile, ...(state.profile || {}) },
    plans: Array.isArray(state.plans) && state.plans.length ? state.plans : fallback.plans,
    questions: Array.isArray(state.questions) && state.questions.length ? state.questions : fallback.questions,
    wishes: Array.isArray(state.wishes) ? state.wishes : fallback.wishes,
    memories: Array.isArray(state.memories) ? state.memories : fallback.memories,
    today: { ...fallback.today, ...(state.today || {}) }
  }
}

export function loadState() {
  const saved = uni.getStorageSync(STORAGE_KEY)
  const state = normalizeState(saved)
  const today = getTodayString()
  if (state.today.date !== today) {
    state.today = {
      date: today,
      planId: '',
      completedPlanIds: []
    }
    saveState(state)
  }
  return state
}

export function saveState(state) {
  uni.setStorageSync(STORAGE_KEY, normalizeState(state))
}

export function updateProfile(profile) {
  const state = loadState()
  state.profile = { ...state.profile, ...profile }
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
  saveState(state)
  return { state, plan: next }
}

export function toggleTodayPlanDone(planId) {
  const state = loadState()
  const completed = new Set(state.today.completedPlanIds)
  if (completed.has(planId)) {
    completed.delete(planId)
  } else {
    completed.add(planId)
  }
  state.today.completedPlanIds = Array.from(completed)
  saveState(state)
  return state
}

export function addWish(title) {
  const state = loadState()
  state.wishes.unshift({
    id: `wish-${Date.now()}`,
    title: title.trim(),
    done: false,
    createdAt: getTodayString()
  })
  saveState(state)
  return state
}

export function toggleWish(id) {
  const state = loadState()
  state.wishes = state.wishes.map((wish) => (
    wish.id === id ? { ...wish, done: !wish.done } : wish
  ))
  saveState(state)
  return state
}

export function addMemory(memory) {
  const state = loadState()
  state.memories.unshift({
    id: `memory-${Date.now()}`,
    title: memory.title.trim(),
    date: memory.date || getTodayString(),
    description: memory.description.trim(),
    color: memory.color || '#f9b38d'
  })
  saveState(state)
  return state
}

export function getDailyQuestion(state) {
  const today = getTodayString()
  const index = today.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0) % state.questions.length
  return state.questions[index]
}
