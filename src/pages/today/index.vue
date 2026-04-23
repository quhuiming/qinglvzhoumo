<template>
  <view class="today-page">
    <view class="today-content">
      <view class="hero">
        <text class="eyebrow">情侣周末</text>
        <text class="hello">你好，{{ state.profile.me }} & {{ state.profile.partner }}</text>
        <view class="days-row">
          <view class="heart-mark"></view>
          <text class="line"></text>
          <text>恋爱第 {{ loveDays }} 天</text>
          <text class="line"></text>
          <view class="heart-mark"></view>
        </view>
        <text v-if="milestoneMessage" class="milestone">{{ milestoneMessage }}</text>
      </view>

      <view class="progress-card soft-card">
        <view>
          <text class="progress-label">今日陪伴</text>
          <text class="progress-title">{{ progressTitle }}</text>
        </view>
        <view class="progress-steps">
          <view class="step" :class="{ done: hasAnswer }">
            <view class="step-dot"></view>
            <text>回答</text>
          </view>
          <view class="step-line" :class="{ done: isPlanDone }"></view>
          <view class="step" :class="{ done: isPlanDone }">
            <view class="step-dot"></view>
            <text>计划</text>
          </view>
        </view>
      </view>

      <view class="question-card soft-card">
        <text class="card-kicker">今日问题</text>
        <text class="question-text">“{{ dailyQuestion }}”</text>
        <textarea
          v-model="answerDraft"
          class="answer-input"
          maxlength="160"
          placeholder="写下今天想告诉对方的话"
          cursor-spacing="140"
          :adjust-position="true"
          :show-confirm-bar="false"
        />
        <view class="answer-actions">
          <text class="answer-meta">{{ state.today.answer ? `已保存 ${answerTimeText}` : `${answerDraft.length}/160` }}</text>
          <button class="save-answer tap-target" :class="{ disabled: !canSaveAnswer }" hover-class="button-hover" @tap="handleSaveAnswer">
            保存回答
          </button>
        </view>
        <view class="answer-pair">
          <view class="answer-bubble">
            <text class="bubble-label">我的回答</text>
            <text class="bubble-text">{{ answerPair.mine ? answerPair.mine.text : '还没有写下今天的想法' }}</text>
          </view>
          <view class="answer-bubble partner">
            <text class="bubble-label">TA 的回答</text>
            <text class="bubble-text">{{ answerPair.partner ? answerPair.partner.text : '同步后会看到对方的回答' }}</text>
          </view>
        </view>
      </view>

      <view class="plan-card soft-card">
        <view class="spark spark-left" aria-hidden="true"></view>
        <view class="spark spark-right" aria-hidden="true"></view>
        <text class="plan-title">今天我们一起做点什么？</text>
        <text class="plan-subtitle">抽一个很小的计划，让关系多一点靠近。</text>
        <view v-if="currentPlan" class="picked-plan">
          <text class="picked-label">今日小计划</text>
          <text class="picked-title">{{ currentPlan.title }}</text>
          <text class="picked-detail">{{ currentPlan.detail }}</text>
        </view>
        <button class="draw-button tap-target" :class="{ locked: isPlanDone }" hover-class="button-hover" @tap="handleDraw">
          <view class="button-icon dice-icon" aria-hidden="true">
            <text></text>
            <text></text>
            <text></text>
            <text></text>
          </view>
          <text>{{ isPlanDone ? '今日计划已完成' : (currentPlan ? '换一个小计划' : '抽一个小计划') }}</text>
          <view class="arrow-icon" aria-hidden="true"></view>
        </button>
        <button v-if="currentPlan" class="done-button tap-target" :class="{ completed: isPlanDone }" hover-class="soft-hover" @tap="handleToggleDone">
          <view class="check-icon" aria-hidden="true"></view>
          <text>{{ isPlanDone ? '今天已经完成啦' : '标记今天完成' }}</text>
        </button>
      </view>

      <view v-if="isTodayComplete" class="complete-card soft-card" @tap="goMemories">
        <text class="complete-label">今天的陪伴已收好</text>
        <text class="complete-copy">回答和小计划已经进入每日记录，之后可以在回忆页慢慢翻。</text>
      </view>

      <view class="partner-card soft-card">
        <view class="quick-head">
          <view class="card-icon activity-icon" aria-hidden="true"></view>
          <text>对方动态</text>
        </view>
        <view v-if="partnerActivities.length" class="activity-list">
          <text v-for="activity in partnerActivities" :key="activity.id" class="activity-line">{{ activity.text }}</text>
        </view>
        <text v-else class="quick-text">绑定并同步后，这里会出现 TA 新增、完成和回答的动态。</text>
      </view>

      <view class="quick-grid">
        <view class="quick-card soft-card">
          <view class="quick-head">
            <view class="card-icon list-icon" aria-hidden="true"></view>
            <text>共同愿望</text>
          </view>
          <text class="quick-text">{{ nextWish ? nextWish.title : '写下一个想一起完成的小事' }}</text>
          <text class="tag">{{ pendingWishCount }} 个待完成</text>
        </view>

        <view class="quick-card soft-card memory-card">
          <view class="quick-head">
            <view class="card-icon memory-icon" aria-hidden="true"></view>
            <text>最近回忆</text>
          </view>
          <image v-if="latestMemory && latestMemory.image" class="memory-thumb memory-image" :src="latestMemory.image" mode="aspectFill" />
          <view v-else class="memory-thumb" aria-hidden="true">
            <view class="photo-sun"></view>
            <view class="photo-hill hill-a"></view>
            <view class="photo-hill hill-b"></view>
          </view>
          <text class="memory-title">{{ latestMemory ? latestMemory.title : '记录一次小约会' }}</text>
        </view>
      </view>

      <view v-if="latestDailyEntry" class="history-card soft-card" @tap="goMemories">
        <text class="history-label">最近每日记录</text>
        <text class="history-title">{{ latestDailyEntry.answer || latestDailyEntry.planTitle }}</text>
        <text class="history-date">{{ latestDailyEntry.date }}</text>
      </view>

      <view v-if="latestTimelineItem" class="history-card soft-card" @tap="goMemories">
        <text class="history-label">最近完成</text>
        <text class="history-title">{{ latestTimelineItem.text }}</text>
        <text class="history-date">{{ latestTimelineItem.date }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { countLoveDays, formatShortTime } from '../../utils/date'
import {
  drawPlan,
  getActiveMemories,
  getActiveWishes,
  getDailyQuestion,
  getLatestDailyEntry,
  getLatestTimelineItem,
  getMilestoneMessage,
  getPartnerActivities,
  getTodayAnswerPair,
  getTodaySharedEntry,
  loadState,
  saveDailyAnswer,
  toggleTodayPlanDone
} from '../../store/love'

const state = ref(loadState())
const answerDraft = ref(state.value.today.answer || '')

const loveDays = computed(() => countLoveDays(state.value.profile.startDate))
const dailyQuestion = computed(() => getDailyQuestion(state.value))
const todaySharedEntry = computed(() => getTodaySharedEntry(state.value))
const currentPlan = computed(() => {
  const planId = todaySharedEntry.value?.planId || state.value.today.planId
  const plan = state.value.plans.find((item) => item.id === planId)
  if (plan) return plan
  if (!todaySharedEntry.value?.planId) return null
  return {
    id: todaySharedEntry.value.planId,
    title: todaySharedEntry.value.planTitle,
    detail: todaySharedEntry.value.planDetail
  }
})
const isPlanDone = computed(() => {
  if (!currentPlan.value) return false
  if (todaySharedEntry.value?.planId === currentPlan.value.id) return Boolean(todaySharedEntry.value.planDone)
  return state.value.today.completedPlanIds.includes(currentPlan.value.id)
})
const hasAnswer = computed(() => Boolean(state.value.today.answer.trim()))
const canSaveAnswer = computed(() => Boolean(answerDraft.value.trim()) && answerDraft.value.trim() !== state.value.today.answer)
const isTodayComplete = computed(() => hasAnswer.value && isPlanDone.value)
const pendingWishes = computed(() => getActiveWishes(state.value).filter((item) => !item.done))
const pendingWishCount = computed(() => pendingWishes.value.length)
const nextWish = computed(() => pendingWishes.value[0])
const latestMemory = computed(() => getActiveMemories(state.value)[0])
const latestDailyEntry = computed(() => getLatestDailyEntry(state.value))
const latestTimelineItem = computed(() => getLatestTimelineItem(state.value))
const partnerActivities = computed(() => getPartnerActivities(state.value))
const answerPair = computed(() => getTodayAnswerPair(state.value))
const milestoneMessage = computed(() => getMilestoneMessage(loveDays.value))
const answerTimeText = computed(() => formatShortTime(state.value.today.answeredAt))
const progressTitle = computed(() => {
  if (isTodayComplete.value) return '今天已经完成，两个人都靠近了一点'
  if (hasAnswer.value) return '还差一个小计划'
  if (isPlanDone.value) return '还差一句今日回答'
  return '从一个问题和一个小计划开始'
})

onShow(() => {
  state.value = loadState()
  answerDraft.value = state.value.today.answer || ''
})

function handleSaveAnswer() {
  if (!answerDraft.value.trim()) {
    uni.showToast({ title: '先写下一点想法吧', icon: 'none' })
    return
  }
  if (!canSaveAnswer.value) {
    uni.showToast({ title: '已经保存过啦', icon: 'none' })
    return
  }
  state.value = saveDailyAnswer(answerDraft.value)
  answerDraft.value = state.value.today.answer
  uni.showToast({ title: '已保存', icon: 'none' })
}

function handleDraw() {
  if (isPlanDone.value) {
    uni.showToast({ title: '今天已经完成啦，明天再换新的', icon: 'none' })
    return
  }
  const result = drawPlan()
  state.value = result.state
  uni.showToast({
    title: '抽到啦',
    icon: 'none'
  })
}

function handleToggleDone() {
  if (!currentPlan.value) return
  state.value = toggleTodayPlanDone(currentPlan.value.id)
}

function goMemories() {
  uni.switchTab({ url: '/pages/memories/index' })
}
</script>

<style scoped>
.today-page {
  min-height: 100vh;
  width: 100%;
  background:
    radial-gradient(circle at 8% 12%, rgba(255, 126, 100, 0.18) 0, transparent 28%),
    radial-gradient(circle at 88% 24%, rgba(232, 169, 113, 0.18) 0, transparent 26%),
    linear-gradient(180deg, #ffe9dd 0%, #fff8f3 48%, #fff7f1 100%);
  padding: 34rpx 24rpx 150rpx;
}

.today-content {
  position: relative;
  width: 100%;
  max-width: 760rpx;
  margin: 0 auto;
}

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 42rpx 0 34rpx;
}

.eyebrow {
  color: #b66b58;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
}

.hello {
  margin-top: 14rpx;
  color: #5f3f39;
  font-size: 34rpx;
  font-weight: 800;
}

.days-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-top: 18rpx;
  color: #7e5a50;
  font-size: 25rpx;
  font-weight: 700;
}

.milestone {
  display: block;
  border-radius: 999rpx;
  background: rgba(255, 248, 243, 0.76);
  color: #c95b49;
  font-size: 23rpx;
  font-weight: 900;
  line-height: 1.35;
  margin-top: 18rpx;
  padding: 12rpx 22rpx;
}

.line {
  display: block;
  width: 48rpx;
  height: 2rpx;
  background: #e0a99b;
}

.heart-mark,
.memory-icon {
  position: relative;
  width: 18rpx;
  height: 18rpx;
  transform: rotate(45deg);
  background: #ff755f;
  border-radius: 4rpx 4rpx 2rpx 4rpx;
}

.heart-mark::before,
.heart-mark::after,
.memory-icon::before,
.memory-icon::after {
  position: absolute;
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: inherit;
  content: '';
}

.heart-mark::before,
.memory-icon::before {
  left: -9rpx;
  top: 0;
}

.heart-mark::after,
.memory-icon::after {
  left: 0;
  top: -9rpx;
}

.progress-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  padding: 26rpx 28rpx;
  margin-bottom: 22rpx;
}

.progress-label,
.card-kicker {
  color: #d06451;
  font-size: 22rpx;
  font-weight: 900;
}

.progress-title {
  display: block;
  margin-top: 8rpx;
  color: #553a35;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.35;
}

.progress-steps {
  display: flex;
  align-items: center;
  flex: 0 0 208rpx;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  color: #a78073;
  font-size: 21rpx;
  font-weight: 800;
}

.step.done {
  color: #d75d4b;
}

.step-dot {
  width: 28rpx;
  height: 28rpx;
  border: 4rpx solid currentColor;
  border-radius: 50%;
  background: #fff9f4;
}

.step.done .step-dot {
  background: currentColor;
}

.step-line {
  flex: 1;
  height: 4rpx;
  background: #efd0c4;
  margin: 0 10rpx 30rpx;
}

.step-line.done {
  background: #d75d4b;
}

.question-card {
  padding: 32rpx;
  margin-bottom: 22rpx;
}

.question-text {
  display: block;
  margin-top: 12rpx;
  color: #553a35;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.35;
}

.answer-input {
  display: block;
  position: relative;
  z-index: 2;
  width: 100%;
  height: 156rpx;
  min-height: 142rpx;
  border: 1rpx solid rgba(158, 98, 78, 0.18);
  border-radius: 22rpx;
  background: #fff8f3;
  color: #5f433d;
  font-size: 27rpx;
  line-height: 1.5;
  margin-top: 24rpx;
  padding: 22rpx 24rpx;
}

.answer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-top: 18rpx;
}

.answer-pair {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 20rpx;
}

.answer-bubble {
  border-radius: 18rpx;
  background: #fff8f3;
  padding: 18rpx;
}

.answer-bubble.partner {
  background: #fff0e9;
}

.bubble-label {
  display: block;
  color: #c95b49;
  font-size: 21rpx;
  font-weight: 900;
}

.bubble-text {
  display: block;
  margin-top: 8rpx;
  color: #5f433d;
  font-size: 24rpx;
  line-height: 1.45;
}

.answer-meta {
  color: #8a645a;
  font-size: 23rpx;
}

.save-answer {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 168rpx;
  min-height: 66rpx;
  border-radius: 999rpx;
  background: #fff0e9;
  color: #d75d4b;
  font-size: 25rpx;
  font-weight: 900;
}

.save-answer.disabled {
  opacity: 0.45;
}

.plan-card {
  position: relative;
  min-height: 418rpx;
  padding: 58rpx 44rpx 38rpx;
  text-align: center;
}

.plan-card::before,
.plan-card::after {
  position: absolute;
  bottom: 0;
  width: 240rpx;
  height: 104rpx;
  border-radius: 100% 100% 0 0;
  content: '';
}

.plan-card::before {
  left: -34rpx;
  background: linear-gradient(135deg, rgba(255, 134, 97, 0.28), rgba(255, 199, 167, 0.18));
}

.plan-card::after {
  right: -28rpx;
  background: linear-gradient(135deg, rgba(255, 92, 64, 0.22), rgba(249, 183, 123, 0.16));
}

.spark {
  position: absolute;
  width: 24rpx;
  height: 24rpx;
  transform: rotate(45deg);
  background: #f0a36f;
}

.spark::after {
  position: absolute;
  inset: 8rpx;
  background: #fffaf6;
  content: '';
}

.spark-left {
  left: 44rpx;
  top: 156rpx;
}

.spark-right {
  right: 48rpx;
  top: 54rpx;
}

.plan-title {
  position: relative;
  z-index: 1;
  display: block;
  color: #5f3f39;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.25;
}

.plan-subtitle {
  position: relative;
  z-index: 1;
  display: block;
  margin-top: 18rpx;
  color: #8a645a;
  font-size: 25rpx;
  line-height: 1.45;
}

.picked-plan {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  border: 1rpx solid rgba(255, 122, 98, 0.18);
  border-radius: 24rpx;
  background: rgba(255, 248, 243, 0.82);
  margin-top: 28rpx;
  padding: 22rpx 24rpx;
  text-align: left;
}

.picked-label {
  color: #d06451;
  font-size: 21rpx;
  font-weight: 800;
}

.picked-title {
  color: #5d3e38;
  font-size: 29rpx;
  font-weight: 800;
}

.picked-detail {
  color: #7d5b52;
  font-size: 24rpx;
  line-height: 1.45;
}

.draw-button {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  width: 82%;
  min-height: 88rpx;
  margin: 32rpx auto 0;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff785e 0%, #f4513c 100%);
  box-shadow: 0 14rpx 28rpx rgba(244, 81, 60, 0.3);
  color: #fff;
  font-size: 31rpx;
  font-weight: 800;
}

.draw-button.locked {
  background: linear-gradient(135deg, #caa296 0%, #b6897d 100%);
  box-shadow: none;
  opacity: 0.82;
}

.button-icon {
  position: relative;
  flex: 0 0 34rpx;
  width: 34rpx;
  height: 34rpx;
  border-radius: 9rpx;
  background: rgba(255, 255, 255, 0.96);
}

.dice-icon text {
  position: absolute;
  width: 6rpx;
  height: 6rpx;
  border-radius: 50%;
  background: #f4513c;
}

.dice-icon text:nth-child(1) {
  left: 8rpx;
  top: 8rpx;
}

.dice-icon text:nth-child(2) {
  right: 8rpx;
  top: 8rpx;
}

.dice-icon text:nth-child(3) {
  left: 8rpx;
  bottom: 8rpx;
}

.dice-icon text:nth-child(4) {
  right: 8rpx;
  bottom: 8rpx;
}

.arrow-icon {
  width: 18rpx;
  height: 18rpx;
  border-top: 4rpx solid currentColor;
  border-right: 4rpx solid currentColor;
  transform: rotate(45deg);
}

.done-button {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  min-height: 62rpx;
  border: 1rpx solid rgba(244, 99, 80, 0.2);
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.78);
  color: #d85f4d;
  font-size: 24rpx;
  font-weight: 800;
  margin: 18rpx auto 0;
  padding: 0 28rpx;
}

.done-button.completed {
  background: #fff0e9;
  color: #8f584d;
}

.check-icon {
  width: 22rpx;
  height: 12rpx;
  border-left: 4rpx solid currentColor;
  border-bottom: 4rpx solid currentColor;
  transform: rotate(-45deg);
}

.complete-card,
.history-card,
.partner-card {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 24rpx;
  padding: 24rpx;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-top: 18rpx;
}

.activity-line {
  color: #5f433d;
  font-size: 25rpx;
  font-weight: 800;
  line-height: 1.45;
}

.activity-icon {
  border: 3rpx solid currentColor;
  border-radius: 50%;
}

.activity-icon::after {
  position: absolute;
  left: 9rpx;
  top: 9rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: currentColor;
  content: '';
}

.complete-label,
.history-label {
  color: #d06451;
  font-size: 22rpx;
  font-weight: 900;
}

.complete-copy {
  color: #6f5149;
  font-size: 25rpx;
  line-height: 1.5;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 46rpx;
}

.quick-card {
  min-height: 258rpx;
  overflow: hidden;
  padding: 26rpx 18rpx 20rpx;
}

.quick-head {
  display: flex;
  align-items: center;
  gap: 9rpx;
  min-height: 44rpx;
  border-bottom: 1rpx solid rgba(171, 112, 91, 0.16);
  color: #67483f;
  font-size: 23rpx;
  font-weight: 900;
  padding-bottom: 15rpx;
}

.card-icon {
  position: relative;
  flex: 0 0 28rpx;
  width: 28rpx;
  height: 28rpx;
  color: #db715d;
}

.list-icon {
  border: 3rpx solid currentColor;
  border-radius: 7rpx;
}

.list-icon::before,
.list-icon::after {
  position: absolute;
  left: 6rpx;
  right: 6rpx;
  height: 3rpx;
  border-radius: 999rpx;
  background: currentColor;
  content: '';
}

.list-icon::before {
  top: 8rpx;
}

.list-icon::after {
  top: 16rpx;
}

.memory-icon {
  flex-basis: 24rpx;
  width: 24rpx;
  height: 24rpx;
  color: #db715d;
  background: currentColor;
}

.quick-text {
  display: block;
  margin-top: 22rpx;
  color: #5f433d;
  font-size: 25rpx;
  line-height: 1.55;
}

.quick-card .tag {
  margin-top: 22rpx;
}

.memory-card {
  padding-bottom: 0;
}

.memory-thumb {
  position: relative;
  height: 106rpx;
  margin-top: 18rpx;
  overflow: hidden;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #8d5b45 0%, #f1b37e 58%, #ff8069 100%);
}

.memory-image {
  display: block;
  width: 100%;
}

.photo-sun {
  position: absolute;
  right: 28rpx;
  top: 20rpx;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #fff1c9;
}

.photo-hill {
  position: absolute;
  bottom: -28rpx;
  border-radius: 100% 100% 0 0;
}

.hill-a {
  left: -18rpx;
  width: 110rpx;
  height: 70rpx;
  background: rgba(255, 240, 221, 0.86);
}

.hill-b {
  right: -20rpx;
  width: 136rpx;
  height: 82rpx;
  background: rgba(255, 124, 98, 0.42);
}

.memory-title {
  display: block;
  min-height: 48rpx;
  margin: 12rpx -18rpx 0;
  background: #fff1e8;
  color: #6b4a43;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 48rpx;
  padding: 0 16rpx;
}

.history-title {
  color: #5d3e38;
  font-size: 29rpx;
  font-weight: 900;
  line-height: 1.35;
}

.history-date {
  color: #806257;
  font-size: 23rpx;
}

@media screen and (max-width: 360px) {
  .progress-card,
  .answer-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .answer-pair {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
