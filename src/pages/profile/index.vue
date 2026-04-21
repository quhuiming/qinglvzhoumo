<template>
  <view class="app-page">
    <view class="profile-hero soft-card">
      <view class="heart" aria-hidden="true"></view>
      <text class="title">{{ state.profile.me }} & {{ state.profile.partner }}</text>
      <text class="days">恋爱第 {{ loveDays }} 天</text>
    </view>

    <view class="profile-form soft-card">
      <view class="form-field">
        <text class="field-label">我的昵称</text>
        <input v-model="form.me" class="field-input" placeholder="我的昵称" />
      </view>
      <view class="form-field">
        <text class="field-label">对方昵称</text>
        <input v-model="form.partner" class="field-input" placeholder="对方昵称" />
      </view>
      <view class="form-field">
        <text class="field-label">恋爱开始日期</text>
        <input v-model="form.startDate" class="field-input" placeholder="YYYY-MM-DD" />
      </view>
      <button class="primary-button tap-target" hover-class="button-hover" @tap="handleSave">保存我们</button>
    </view>

    <view class="stats-grid">
      <view class="stat-card soft-card">
        <text class="stat-number">{{ undoneCount }}</text>
        <text class="stat-label">待完成愿望</text>
      </view>
      <view class="stat-card soft-card">
        <text class="stat-number">{{ state.memories.length }}</text>
        <text class="stat-label">段小回忆</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { countLoveDays } from '../../utils/date'
import { loadState, updateProfile } from '../../store/love'

const state = ref(loadState())
const form = reactive({ ...state.value.profile })

const loveDays = computed(() => countLoveDays(state.value.profile.startDate))
const undoneCount = computed(() => state.value.wishes.filter((item) => !item.done).length)

onShow(() => {
  state.value = loadState()
  Object.assign(form, state.value.profile)
})

function handleSave() {
  if (!form.me.trim() || !form.partner.trim()) {
    uni.showToast({ title: '昵称都填一下吧', icon: 'none' })
    return
  }
  state.value = updateProfile({
    me: form.me.trim(),
    partner: form.partner.trim(),
    startDate: form.startDate.trim()
  })
  uni.showToast({ title: '保存好了', icon: 'none' })
}
</script>

<style scoped>
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
</style>
