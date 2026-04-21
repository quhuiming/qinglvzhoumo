<template>
  <view class="app-page">
    <view class="page-head">
      <text class="section-title">最近回忆</text>
      <text class="muted">不用隆重，日常也值得被保存。</text>
    </view>

    <view class="memory-form soft-card">
      <view class="form-field">
        <text class="field-label">标题</text>
        <input v-model="form.title" class="field-input" placeholder="例如：昨日的甜品约会" />
      </view>
      <view class="form-field">
        <text class="field-label">日期</text>
        <input v-model="form.date" class="field-input" placeholder="YYYY-MM-DD" />
      </view>
      <view class="form-field">
        <text class="field-label">记录</text>
        <textarea v-model="form.description" class="field-textarea" placeholder="写下一点当时的心情" />
      </view>
      <button class="primary-button tap-target" hover-class="button-hover" @tap="handleAdd">保存回忆</button>
    </view>

    <view v-if="state.memories.length" class="memory-list">
      <view v-for="memory in state.memories" :key="memory.id" class="memory-item soft-card">
        <view class="memory-cover" :style="{ background: coverBackground(memory.color) }">
          <view class="cover-heart" aria-hidden="true"></view>
        </view>
        <view class="memory-copy">
          <text class="memory-title">{{ memory.title }}</text>
          <text class="memory-date">{{ formatFriendlyDate(memory.date) }}</text>
          <text class="memory-description">{{ memory.description }}</text>
        </view>
      </view>
    </view>

    <view v-else class="empty-state soft-card">还没有回忆，下一次约会回来就写一笔。</view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { addMemory, loadState } from '../../store/love'
import { formatFriendlyDate, getTodayString } from '../../utils/date'

const colors = ['#f9b38d', '#ff8b75', '#e8ad79', '#f2c1b2']
const state = ref(loadState())
const form = reactive({
  title: '',
  date: getTodayString(),
  description: ''
})

onShow(() => {
  state.value = loadState()
})

function coverBackground(color) {
  return `linear-gradient(135deg, ${color || '#f9b38d'} 0%, #fff0df 100%)`
}

function handleAdd() {
  if (!form.title.trim() || !form.description.trim()) {
    uni.showToast({ title: '标题和记录都写一下吧', icon: 'none' })
    return
  }
  const color = colors[state.value.memories.length % colors.length]
  state.value = addMemory({ ...form, color })
  form.title = ''
  form.date = getTodayString()
  form.description = ''
}
</script>

<style scoped>
.memory-form {
  padding: 28rpx;
}

.memory-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 28rpx;
}

.memory-item {
  display: flex;
  gap: 22rpx;
  padding: 22rpx;
  border-color: rgba(169, 103, 82, 0.14);
}

.memory-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 148rpx;
  width: 148rpx;
  height: 148rpx;
  border-radius: 22rpx;
  color: rgba(255, 255, 255, 0.88);
}

.cover-heart {
  position: relative;
  width: 36rpx;
  height: 36rpx;
  transform: rotate(45deg);
  border-radius: 8rpx 8rpx 2rpx 8rpx;
  background: rgba(255, 255, 255, 0.88);
}

.cover-heart::before,
.cover-heart::after {
  position: absolute;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: inherit;
  content: '';
}

.cover-heart::before {
  left: -18rpx;
  top: 0;
}

.cover-heart::after {
  left: 0;
  top: -18rpx;
}

.memory-copy {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.memory-title {
  color: #553a35;
  font-size: 30rpx;
  font-weight: 800;
}

.memory-date {
  margin-top: 8rpx;
  color: #806257;
  font-size: 23rpx;
}

.memory-description {
  margin-top: 14rpx;
  color: #6f5149;
  font-size: 25rpx;
  line-height: 1.55;
}

@media screen and (max-width: 360px) {
  .memory-item {
    flex-direction: column;
  }

  .memory-cover {
    width: 100%;
    flex-basis: 148rpx;
  }
}
</style>
