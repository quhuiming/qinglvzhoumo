<template>
  <view class="app-page">
    <view class="page-head">
      <text class="section-title">共同愿望</text>
      <text class="muted">把想一起完成的小事放在这里。</text>
    </view>

    <view class="input-card soft-card">
      <view class="wish-input-wrap">
        <text class="field-label">{{ editingId ? '编辑愿望' : '新的共同愿望' }}</text>
        <input v-model="wishText" class="field-input" type="text" placeholder="例如：周五晚上去散步" confirm-type="done" cursor-spacing="120" :adjust-position="true" @confirm="handleSubmit" />
      </view>
      <button class="primary-button tap-target" :class="{ disabled: !wishText.trim() }" hover-class="button-hover" @tap="handleSubmit">
        {{ editingId ? '保存' : '加入' }}
      </button>
      <button v-if="editingId" class="ghost-button tap-target cancel-button" hover-class="soft-hover" @tap="cancelEdit">取消编辑</button>
    </view>

    <view class="template-row">
      <button v-for="template in state.wishTemplates" :key="template" class="template-chip tap-target" hover-class="soft-hover" @tap="useTemplate(template)">
        {{ template }}
      </button>
    </view>

    <view v-if="activeWishes.length" class="wish-list">
      <view v-for="wish in activeWishes" :key="wish.id" class="wish-item soft-card" :class="{ done: wish.done }">
        <button class="check tap-target" hover-class="soft-hover" @tap="handleToggle(wish.id)">
          <view v-if="wish.done" class="check-mark"></view>
        </button>
        <view class="wish-body">
          <text class="wish-title">{{ wish.title }}</text>
          <text class="wish-date">{{ formatFriendlyDate(wish.createdAt) }}</text>
          <view class="wish-actions">
            <button class="text-action" @tap="startEdit(wish)">编辑</button>
            <button class="text-action danger" @tap="confirmDelete(wish)">删除</button>
          </view>
        </view>
        <text class="wish-status">{{ wish.done ? '完成' : '待完成' }}</text>
      </view>
    </view>

    <view v-else class="empty-state soft-card">还没有愿望，先写一个很小很小的也可以。</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { addWish, deleteWish, getActiveWishes, loadState, toggleWish, updateWish } from '../../store/love'
import { formatFriendlyDate, getTodayString } from '../../utils/date'

const MEMORY_DRAFT_KEY = 'qinglvzhoumo.memory.draft.v1'

const state = ref(loadState())
const wishText = ref('')
const editingId = ref('')
const activeWishes = computed(() => getActiveWishes(state.value))

onShow(() => {
  state.value = loadState()
})

function handleSubmit() {
  const title = wishText.value.trim()
  if (!title) {
    uni.showToast({ title: '先写下一个小愿望', icon: 'none' })
    return
  }
  state.value = editingId.value ? updateWish(editingId.value, title) : addWish(title)
  wishText.value = ''
  editingId.value = ''
}

function useTemplate(template) {
  wishText.value = template
}

function startEdit(wish) {
  editingId.value = wish.id
  wishText.value = wish.title
}

function cancelEdit() {
  editingId.value = ''
  wishText.value = ''
}

function handleToggle(id) {
  const wish = activeWishes.value.find((item) => item.id === id)
  state.value = toggleWish(id)
  if (!wish || wish.done) return
  uni.showModal({
    title: '记录成回忆吗？',
    content: `“${wish.title}”已经完成，要不要顺手保存成一段回忆？`,
    cancelText: '稍后',
    confirmText: '记录回忆',
    confirmColor: '#d75d4b',
    success: (res) => {
      if (!res.confirm) return
      uni.setStorageSync(MEMORY_DRAFT_KEY, {
        title: wish.title,
        date: getTodayString(),
        description: `完成了共同愿望：${wish.title}`
      })
      uni.switchTab({ url: '/pages/memories/index' })
    }
  })
}

function confirmDelete(wish) {
  uni.showModal({
    title: '删除愿望',
    content: `确定删除“${wish.title}”吗？`,
    confirmText: '删除',
    confirmColor: '#d75d4b',
    success: (res) => {
      if (!res.confirm) return
      state.value = deleteWish(wish.id)
      if (editingId.value === wish.id) cancelEdit()
    }
  })
}
</script>

<style scoped>
.input-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132rpx;
  align-items: end;
  gap: 18rpx;
  padding: 24rpx;
}

.wish-input-wrap {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.input-card .primary-button {
  min-height: 84rpx;
  font-size: 26rpx;
}

.cancel-button {
  grid-column: 1 / -1;
}

.template-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 22rpx;
}

.template-chip {
  min-height: 58rpx;
  border: 1rpx solid rgba(215, 93, 75, 0.18);
  border-radius: 999rpx;
  background: rgba(255, 248, 243, 0.86);
  color: #8c5f55;
  font-size: 23rpx;
  font-weight: 800;
  padding: 0 22rpx;
}

.wish-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 26rpx;
}

.wish-item {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  min-height: 124rpx;
  padding: 22rpx;
  border-color: rgba(169, 103, 82, 0.14);
}

.check {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 48rpx;
  width: 48rpx;
  height: 48rpx;
  border: 3rpx solid #ff927f;
  border-radius: 15rpx;
  color: #fff;
  background: #fff7f2;
  margin-top: 2rpx;
}

.check-mark {
  width: 22rpx;
  height: 12rpx;
  border-left: 4rpx solid currentColor;
  border-bottom: 4rpx solid currentColor;
  transform: rotate(-45deg);
}

.wish-body {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}

.wish-title {
  color: #553a35;
  font-size: 29rpx;
  font-weight: 800;
  line-height: 1.35;
}

.wish-date {
  color: #806257;
  font-size: 23rpx;
}

.wish-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 6rpx;
}

.text-action {
  color: #c95b49;
  font-size: 24rpx;
  font-weight: 800;
}

.text-action.danger {
  color: #9f4c40;
}

.wish-status {
  flex: 0 0 auto;
  color: #c95b49;
  font-size: 23rpx;
  font-weight: 800;
}

.wish-item.done {
  background: rgba(255, 248, 243, 0.72);
}

.wish-item.done .check {
  background: #ff8069;
}

.wish-item.done .wish-title {
  text-decoration: line-through;
}

.disabled {
  opacity: 0.5;
}

@media screen and (max-width: 360px) {
  .input-card {
    grid-template-columns: 1fr;
  }
}
</style>
