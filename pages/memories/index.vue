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
        <picker mode="date" :value="form.date" @change="handleDateChange">
          <view class="picker-field">{{ form.date }}</view>
        </picker>
      </view>
      <view class="form-field">
        <text class="field-label">记录</text>
        <textarea v-model="form.description" class="field-textarea" placeholder="写下一点当时的心情" />
      </view>
      <view class="form-field">
        <text class="field-label">照片</text>
        <button class="photo-picker tap-target" hover-class="soft-hover" @tap="chooseImage">
          <image v-if="form.image" class="picked-image" :src="form.image" mode="aspectFill" />
          <view v-else class="photo-placeholder">
            <view class="photo-icon"></view>
            <text>选择一张本地照片</text>
          </view>
        </button>
      </view>
      <view class="form-actions">
        <button class="primary-button tap-target" :class="{ disabled: !canSubmit }" hover-class="button-hover" @tap="handleSubmit">
          {{ editingId ? '保存修改' : '保存回忆' }}
        </button>
        <button v-if="editingId" class="ghost-button tap-target" hover-class="soft-hover" @tap="cancelEdit">取消编辑</button>
      </view>
    </view>

    <view v-if="state.memories.length" class="memory-list">
      <view v-for="memory in state.memories" :key="memory.id" class="memory-item soft-card">
        <image v-if="memory.image" class="memory-cover image-cover" :src="memory.image" mode="aspectFill" />
        <view v-else class="memory-cover" :style="{ background: coverBackground(memory.color) }">
          <view class="cover-heart" aria-hidden="true"></view>
        </view>
        <view class="memory-copy">
          <text class="memory-title">{{ memory.title }}</text>
          <text class="memory-date">{{ formatFriendlyDate(memory.date) }}</text>
          <text class="memory-description">{{ memory.description }}</text>
          <view class="memory-actions">
            <button class="text-action" @tap="startEdit(memory)">编辑</button>
            <button class="text-action danger" @tap="confirmDelete(memory)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-state soft-card">还没有回忆，下一次约会回来就写一笔。</view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { addMemory, deleteMemory, loadState, updateMemory } from '../../store/love'
import { formatFriendlyDate, getTodayString } from '../../utils/date'

const colors = ['#f9b38d', '#ff8b75', '#e8ad79', '#f2c1b2']
const state = ref(loadState())
const editingId = ref('')
const form = reactive({
  title: '',
  date: getTodayString(),
  description: '',
  image: ''
})

const canSubmit = computed(() => form.title.trim() && form.description.trim())

onShow(() => {
  state.value = loadState()
})

function coverBackground(color) {
  return `linear-gradient(135deg, ${color || '#f9b38d'} 0%, #fff0df 100%)`
}

function handleDateChange(event) {
  form.date = event.detail.value
}

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      form.image = res.tempFilePaths[0] || ''
    }
  })
}

function handleSubmit() {
  if (!canSubmit.value) {
    uni.showToast({ title: '标题和记录都写一下吧', icon: 'none' })
    return
  }
  const color = colors[state.value.memories.length % colors.length]
  const payload = { ...form, color }
  state.value = editingId.value ? updateMemory(editingId.value, payload) : addMemory(payload)
  resetForm()
}

function startEdit(memory) {
  editingId.value = memory.id
  form.title = memory.title
  form.date = memory.date
  form.description = memory.description
  form.image = memory.image || ''
}

function cancelEdit() {
  resetForm()
}

function resetForm() {
  editingId.value = ''
  form.title = ''
  form.date = getTodayString()
  form.description = ''
  form.image = ''
}

function confirmDelete(memory) {
  uni.showModal({
    title: '删除回忆',
    content: `确定删除“${memory.title}”吗？`,
    confirmText: '删除',
    confirmColor: '#d75d4b',
    success: (res) => {
      if (!res.confirm) return
      state.value = deleteMemory(memory.id)
      if (editingId.value === memory.id) resetForm()
    }
  })
}
</script>

<style scoped>
.memory-form {
  padding: 28rpx;
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

.photo-picker {
  overflow: hidden;
  width: 100%;
  height: 220rpx;
  border: 1rpx dashed rgba(215, 93, 75, 0.34);
  border-radius: 22rpx;
  background: #fff8f3;
}

.picked-image {
  width: 100%;
  height: 220rpx;
}

.photo-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  height: 220rpx;
  color: #8c5f55;
  font-size: 25rpx;
  font-weight: 800;
  gap: 16rpx;
}

.photo-icon {
  position: relative;
  width: 62rpx;
  height: 46rpx;
  border: 4rpx solid #d75d4b;
  border-radius: 12rpx;
}

.photo-icon::before {
  position: absolute;
  right: 8rpx;
  top: 8rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #d75d4b;
  content: '';
}

.photo-icon::after {
  position: absolute;
  left: 8rpx;
  bottom: 8rpx;
  width: 32rpx;
  height: 20rpx;
  border-radius: 100% 100% 0 0;
  background: #d75d4b;
  content: '';
}

.form-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
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

.image-cover {
  background: #fff1e8;
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

.memory-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 18rpx;
}

.text-action {
  color: #c95b49;
  font-size: 24rpx;
  font-weight: 800;
}

.text-action.danger {
  color: #9f4c40;
}

.disabled {
  opacity: 0.5;
}

@media screen and (max-width: 360px) {
  .memory-item {
    flex-direction: column;
  }

  .memory-cover {
    width: 100%;
    flex-basis: 180rpx;
  }
}
</style>
