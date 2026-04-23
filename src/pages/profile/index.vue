<template>
  <view class="app-page">
    <view v-if="!state.profile.initialized" class="guide-card soft-card">
      <text class="guide-title">先把你们写进来</text>
      <text class="guide-copy">保存后，首页会用你们的昵称和恋爱天数开始记录。</text>
    </view>

    <view class="profile-hero soft-card">
      <view class="heart" aria-hidden="true"></view>
      <text class="title">{{ state.profile.me }} & {{ state.profile.partner }}</text>
      <text class="days">恋爱第 {{ loveDays }} 天</text>
    </view>

    <view class="profile-form soft-card">
      <view class="form-field">
        <text class="field-label">我的昵称</text>
        <input v-model="form.me" class="field-input" type="text" placeholder="我的昵称" cursor-spacing="120" :adjust-position="true" />
      </view>
      <view class="form-field">
        <text class="field-label">对方昵称</text>
        <input v-model="form.partner" class="field-input" type="text" placeholder="对方昵称" cursor-spacing="120" :adjust-position="true" />
      </view>
      <view class="form-field">
        <text class="field-label">恋爱开始日期</text>
        <picker mode="date" :value="form.startDate" @change="handleDateChange">
          <view class="picker-field">{{ form.startDate }}</view>
        </picker>
      </view>
      <button class="primary-button tap-target" hover-class="button-hover" @tap="handleSave">保存我们</button>
    </view>

    <view class="sync-card soft-card">
      <view class="sync-head">
        <view>
          <text class="sync-title">共享空间</text>
          <text class="sync-copy">{{ syncStatusText }}</text>
        </view>
        <text class="sync-badge" :class="{ online: syncConfig.coupleId }">{{ syncConfig.coupleId ? '已绑定' : '未绑定' }}</text>
      </view>

      <view class="form-field">
        <text class="field-label">后端地址</text>
        <input v-model="apiBaseUrl" class="field-input" type="text" placeholder="http://localhost:8080" cursor-spacing="120" :adjust-position="true" />
      </view>
      <button class="ghost-button tap-target" hover-class="soft-hover" @tap="saveApiBaseUrl">保存后端地址</button>

      <view class="invite-box">
        <text class="invite-label">我的邀请码</text>
        <text class="invite-code">{{ syncConfig.inviteCode || '还没有创建' }}</text>
        <text class="sync-tip">成员：{{ syncConfig.memberCount || 0 }}/2</text>
      </view>

      <view class="sync-scope">
        <text class="scope-line">会同步：昵称、愿望、文字回忆、每日回答和小计划记录。</text>
        <text class="scope-line">仅本机：已选择照片的本地文件本身，后端只保存路径。</text>
      </view>

      <button class="primary-button tap-target" :class="{ disabled: syncing }" hover-class="button-hover" @tap="handleCreateInvite">
        {{ syncConfig.inviteCode ? '刷新邀请码' : '创建情侣空间' }}
      </button>

      <view class="join-row">
        <input v-model="inviteCode" class="field-input" type="text" placeholder="输入对方的邀请码" maxlength="12" cursor-spacing="120" :adjust-position="true" />
        <button class="ghost-button tap-target join-button" :class="{ disabled: syncing || !inviteCode.trim() }" hover-class="soft-hover" @tap="handleJoinInvite">加入</button>
      </view>

      <button class="ghost-button tap-target" :class="{ disabled: syncing || !syncConfig.coupleId }" hover-class="soft-hover" @tap="handleSyncNow">
        {{ syncing ? '同步中...' : '立即同步' }}
      </button>
      <button class="ghost-button tap-target" :class="{ disabled: syncing || !syncConfig.token }" hover-class="soft-hover" @tap="handleRefreshStatus">
        刷新空间状态
      </button>
      <button v-if="syncConfig.coupleId" class="ghost-button tap-target danger-button" :class="{ disabled: syncing }" hover-class="soft-hover" @tap="confirmLeaveCouple">
        退出情侣空间
      </button>

      <text v-if="syncConfig.lastSyncedAt" class="sync-tip">上次同步：{{ syncConfig.lastSyncedAt }}</text>
      <text v-if="syncConfig.lastError" class="sync-error">同步提示：{{ syncConfig.lastError }}</text>
    </view>

    <view class="stats-grid">
      <view class="stat-card soft-card">
        <text class="stat-number">{{ undoneCount }}</text>
        <text class="stat-label">待完成愿望</text>
      </view>
      <view class="stat-card soft-card">
        <text class="stat-number">{{ activeMemories.length }}</text>
        <text class="stat-label">段小回忆</text>
      </view>
      <view class="stat-card soft-card">
        <text class="stat-number">{{ state.planHistory.length }}</text>
        <text class="stat-label">完成小计划</text>
      </view>
      <view class="stat-card soft-card">
        <text class="stat-number">{{ doneWishCount }}</text>
        <text class="stat-label">完成愿望</text>
      </view>
    </view>

    <view class="danger-zone soft-card">
      <text class="danger-title">本地数据</text>
      <text class="danger-copy">所有内容只保存在这台设备。重置后会恢复默认示例数据。</text>
      <button class="ghost-button tap-target" hover-class="soft-hover" @tap="confirmReset">重置本地数据</button>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { countLoveDays } from '../../utils/date'
import {
  createCoupleInvite,
  getActiveMemories,
  getActiveWishes,
  getBackendSyncConfig,
  joinCoupleByInvite,
  leaveCoupleSpace,
  loadState,
  refreshCoupleStatus,
  resetState,
  setBackendApiBaseUrl,
  syncNow,
  updateProfile
} from '../../store/love'

const state = ref(loadState())
const form = reactive({ ...state.value.profile })
const syncConfig = ref(getBackendSyncConfig())
const apiBaseUrl = ref(syncConfig.value.apiBaseUrl)
const inviteCode = ref('')
const syncing = ref(false)

const loveDays = computed(() => countLoveDays(state.value.profile.startDate))
const activeWishes = computed(() => getActiveWishes(state.value))
const activeMemories = computed(() => getActiveMemories(state.value))
const undoneCount = computed(() => activeWishes.value.filter((item) => !item.done).length)
const doneWishCount = computed(() => activeWishes.value.filter((item) => item.done).length)
const syncStatusText = computed(() => {
  if (syncConfig.value.coupleId) return '本机内容会同步到 Java 后端，另一台设备加入后可共享查看。'
  return '先创建情侣空间，或输入对方的邀请码加入。'
})

onShow(() => {
  state.value = loadState()
  Object.assign(form, state.value.profile)
  syncConfig.value = getBackendSyncConfig()
  apiBaseUrl.value = syncConfig.value.apiBaseUrl
})

function handleDateChange(event) {
  form.startDate = event.detail.value
}

function handleSave() {
  if (!form.me.trim() || !form.partner.trim()) {
    uni.showToast({ title: '昵称都填一下吧', icon: 'none' })
    return
  }
  state.value = updateProfile({
    me: form.me.trim(),
    partner: form.partner.trim(),
    startDate: form.startDate.trim(),
    initialized: true
  })
  uni.showToast({ title: '保存好了', icon: 'none' })
}

function refreshSyncConfig() {
  syncConfig.value = getBackendSyncConfig()
  apiBaseUrl.value = syncConfig.value.apiBaseUrl
}

function saveApiBaseUrl() {
  if (!apiBaseUrl.value.trim()) {
    uni.showToast({ title: '先填后端地址', icon: 'none' })
    return
  }
  syncConfig.value = setBackendApiBaseUrl(apiBaseUrl.value)
  uni.showToast({ title: '已保存后端地址', icon: 'none' })
}

async function runSyncTask(task, successTitle) {
  if (syncing.value) return
  syncing.value = true
  try {
    await task()
    state.value = loadState()
    refreshSyncConfig()
    uni.showToast({ title: successTitle, icon: 'none' })
  } catch (error) {
    refreshSyncConfig()
    uni.showModal({
      title: '连接后端失败',
      content: error?.message || '请确认 Java 后端已经启动',
      showCancel: false
    })
  } finally {
    syncing.value = false
  }
}

function handleCreateInvite() {
  saveApiBaseUrl()
  runSyncTask(() => createCoupleInvite(), '已创建情侣空间')
}

function handleJoinInvite() {
  if (!inviteCode.value.trim()) {
    uni.showToast({ title: '先输入邀请码', icon: 'none' })
    return
  }
  saveApiBaseUrl()
  runSyncTask(() => joinCoupleByInvite(inviteCode.value), '已加入情侣空间')
}

function handleSyncNow() {
  if (!syncConfig.value.coupleId) {
    uni.showToast({ title: '先绑定情侣空间', icon: 'none' })
    return
  }
  saveApiBaseUrl()
  runSyncTask(() => syncNow({ full: true }), '同步完成')
}

function handleRefreshStatus() {
  runSyncTask(() => refreshCoupleStatus(), '状态已刷新')
}

function confirmLeaveCouple() {
  uni.showModal({
    title: '退出情侣空间',
    content: '退出后本机数据会保留，但不会继续和对方同步。确定退出吗？',
    confirmText: '退出',
    confirmColor: '#d75d4b',
    success: (res) => {
      if (!res.confirm) return
      runSyncTask(() => leaveCoupleSpace(), '已退出情侣空间')
    }
  })
}

function confirmReset() {
  uni.showModal({
    title: '重置本地数据',
    content: '会清空你新增的愿望、回忆和计划历史，确定继续吗？',
    confirmText: '重置',
    confirmColor: '#d75d4b',
    success: (res) => {
      if (!res.confirm) return
      state.value = resetState()
      Object.assign(form, state.value.profile)
      uni.showToast({ title: '已恢复默认', icon: 'none' })
    }
  })
}
</script>

<style scoped>
.guide-card {
  margin-bottom: 24rpx;
  padding: 26rpx;
}

.guide-title {
  display: block;
  color: #553a35;
  font-size: 31rpx;
  font-weight: 900;
}

.guide-copy {
  display: block;
  margin-top: 10rpx;
  color: #765a52;
  font-size: 25rpx;
  line-height: 1.55;
}

.profile-hero {
  display: flex;
  align-items: center;
  flex-direction: column;
  min-height: 268rpx;
  justify-content: center;
  background:
    radial-gradient(circle at 18% 26%, rgba(255, 126, 101, 0.2) 0, transparent 26%),
    linear-gradient(145deg, #fffaf6 0%, #ffe5d7 100%);
  margin-bottom: 26rpx;
  padding: 36rpx;
}

.heart {
  position: relative;
  width: 38rpx;
  height: 38rpx;
  transform: rotate(45deg);
  border-radius: 9rpx 9rpx 3rpx 9rpx;
  background: #ff755f;
}

.heart::before,
.heart::after {
  position: absolute;
  width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  background: inherit;
  content: '';
}

.heart::before {
  left: -19rpx;
  top: 0;
}

.heart::after {
  left: 0;
  top: -19rpx;
}

.title {
  margin-top: 26rpx;
  color: #553a35;
  font-size: 38rpx;
  font-weight: 900;
}

.days {
  margin-top: 14rpx;
  color: #73564e;
  font-size: 27rpx;
  font-weight: 800;
}

.profile-form {
  padding: 28rpx;
}

.sync-card {
  margin-top: 26rpx;
  padding: 28rpx;
}

.sync-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 24rpx;
}

.sync-title {
  display: block;
  color: #553a35;
  font-size: 30rpx;
  font-weight: 900;
}

.sync-copy,
.sync-tip,
.sync-error {
  display: block;
  margin-top: 8rpx;
  color: #765a52;
  font-size: 24rpx;
  line-height: 1.45;
}

.sync-badge {
  flex: 0 0 auto;
  min-height: 46rpx;
  border-radius: 999rpx;
  background: #fff0e9;
  color: #c95b49;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 46rpx;
  padding: 0 18rpx;
}

.sync-badge.online {
  background: #ffe2d4;
  color: #d75d4b;
}

.invite-box {
  border: 1rpx dashed rgba(215, 93, 75, 0.26);
  border-radius: 22rpx;
  background: #fff8f3;
  margin: 22rpx 0;
  padding: 22rpx;
}

.sync-scope {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  border-radius: 18rpx;
  background: #fff8f3;
  margin-bottom: 18rpx;
  padding: 18rpx 20rpx;
}

.scope-line {
  color: #765a52;
  font-size: 23rpx;
  line-height: 1.45;
}

.invite-label {
  display: block;
  color: #8a645a;
  font-size: 23rpx;
  font-weight: 800;
}

.invite-code {
  display: block;
  margin-top: 10rpx;
  color: #553a35;
  font-size: 42rpx;
  font-weight: 900;
  letter-spacing: 4rpx;
}

.join-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132rpx;
  gap: 16rpx;
  margin: 18rpx 0;
}

.join-button {
  min-height: 88rpx;
}

.sync-error {
  color: #a7483d;
}

.danger-button {
  color: #a7483d;
}

.disabled {
  opacity: 0.5;
}

.picker-field {
  width: 100%;
  border: 1rpx solid rgba(158, 98, 78, 0.2);
  border-radius: 20rpx;
  background: rgba(255, 250, 246, 0.96);
  color: #4f3732;
  font-size: 28rpx;
  line-height: 1.4;
  padding: 22rpx 24rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  margin-top: 26rpx;
}

.stat-card {
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  min-height: 160rpx;
  border-color: rgba(169, 103, 82, 0.14);
}

.stat-number {
  color: #d75d4b;
  font-size: 46rpx;
  font-weight: 900;
}

.stat-label {
  margin-top: 10rpx;
  color: #765a52;
  font-size: 24rpx;
  font-weight: 800;
}

.danger-zone {
  margin-top: 26rpx;
  padding: 28rpx;
}

.danger-title {
  display: block;
  color: #553a35;
  font-size: 30rpx;
  font-weight: 900;
}

.danger-copy {
  display: block;
  color: #765a52;
  font-size: 25rpx;
  line-height: 1.55;
  margin: 12rpx 0 22rpx;
}
</style>
