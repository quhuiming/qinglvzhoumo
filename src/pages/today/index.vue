<template>
  <view class="today-page">
    <view class="phone-shell">
      <view class="status-row" aria-hidden="true">
        <view class="signal-bars">
          <text></text>
          <text></text>
          <text></text>
        </view>
        <view class="battery"></view>
      </view>

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
        <button class="draw-button tap-target" hover-class="button-hover" @tap="handleDraw">
          <view class="button-icon dice-icon" aria-hidden="true">
            <text></text>
            <text></text>
            <text></text>
            <text></text>
          </view>
          <text>{{ currentPlan ? '换一个小计划' : '抽一个小计划' }}</text>
          <view class="arrow-icon" aria-hidden="true"></view>
        </button>
        <button v-if="currentPlan" class="done-button tap-target" :class="{ completed: isPlanDone }" hover-class="soft-hover" @tap="handleToggleDone">
          <view class="check-icon" aria-hidden="true"></view>
          <text>{{ isPlanDone ? '今天已经完成啦' : '标记今天完成' }}</text>
        </button>
      </view>

      <view class="quick-grid">
        <view class="quick-card soft-card">
          <view class="quick-head">
            <view class="card-icon question-icon" aria-hidden="true"></view>
            <text>今日问题</text>
          </view>
          <text class="quick-text">“{{ dailyQuestion }}”</text>
        </view>

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

      <view v-if="latestHistory" class="history-card soft-card">
        <text class="history-label">最近完成</text>
        <text class="history-title">{{ latestHistory.title }}</text>
        <text class="history-date">{{ latestHistory.completedAt }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { countLoveDays } from '../../utils/date'
import { drawPlan, getDailyQuestion, loadState, toggleTodayPlanDone } from '../../store/love'

const state = ref(loadState())

const loveDays = computed(() => countLoveDays(state.value.profile.startDate))
const dailyQuestion = computed(() => getDailyQuestion(state.value))
const currentPlan = computed(() => state.value.plans.find((item) => item.id === state.value.today.planId))
const isPlanDone = computed(() => currentPlan.value && state.value.today.completedPlanIds.includes(currentPlan.value.id))
const pendingWishes = computed(() => state.value.wishes.filter((item) => !item.done))
const pendingWishCount = computed(() => pendingWishes.value.length)
const nextWish = computed(() => pendingWishes.value[0])
const latestMemory = computed(() => state.value.memories[0])
const latestHistory = computed(() => state.value.planHistory[0])

onShow(() => {
  state.value = loadState()
})

function handleDraw() {
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
</script>

<style scoped>
.today-page {
  min-height: 100vh;
  background: #f5f3ef;
  padding: 24rpx;
}

.phone-shell {
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 760rpx;
  min-height: calc(100vh - 48rpx);
  border-radius: 50rpx;
  background:
    radial-gradient(circle at 8% 42%, rgba(255, 126, 100, 0.2) 0, transparent 24%),
    radial-gradient(circle at 86% 32%, rgba(232, 169, 113, 0.22) 0, transparent 24%),
    linear-gradient(180deg, #ffe9dd 0%, #fff8f3 46%, #fffaf7 100%);
  box-shadow: 0 24rpx 64rpx rgba(98, 65, 55, 0.16);
  margin: 0 auto;
  padding: 28rpx 24rpx 48rpx;
}

.status-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14rpx;
  height: 32rpx;
}

.signal-bars {
  display: flex;
  align-items: flex-end;
  gap: 5rpx;
  height: 18rpx;
}

.signal-bars text {
  display: block;
  width: 7rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.92);
}

.signal-bars text:nth-child(1) {
  height: 8rpx;
}

.signal-bars text:nth-child(2) {
  height: 13rpx;
}

.signal-bars text:nth-child(3) {
  height: 18rpx;
}

.battery {
  width: 42rpx;
  height: 18rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.92);
  border-radius: 6rpx;
}

.battery::after {
  display: block;
  width: 22rpx;
  height: 8rpx;
  border-radius: 3rpx;
  background: rgba(255, 255, 255, 0.92);
  content: '';
  margin: 2rpx 0 0 3rpx;
}

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 34rpx 0 42rpx;
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

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.question-icon {
  border: 3rpx solid currentColor;
  border-radius: 50%;
}

.question-icon::after {
  position: absolute;
  left: 10rpx;
  bottom: -7rpx;
  width: 8rpx;
  height: 8rpx;
  border-right: 3rpx solid currentColor;
  border-bottom: 3rpx solid currentColor;
  transform: rotate(35deg);
  content: '';
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

.history-card {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 24rpx;
  padding: 24rpx;
}

.history-label {
  color: #d06451;
  font-size: 22rpx;
  font-weight: 900;
}

.history-title {
  color: #5d3e38;
  font-size: 29rpx;
  font-weight: 900;
}

.history-date {
  color: #806257;
  font-size: 23rpx;
}

@media screen and (max-width: 360px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
