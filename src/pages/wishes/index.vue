<template>
  <view class="app-page">
    <view class="page-head">
      <text class="section-title">共同愿望</text>
      <text class="muted">把想一起完成的小事放在这里。</text>
    </view>

    <view class="input-card soft-card">
      <view class="wish-input-wrap">
        <text class="field-label">新的共同愿望</text>
        <input v-model="newWish" class="field-input" placeholder="例如：周五晚上去散步" confirm-type="done" @confirm="handleAdd" />
      </view>
      <button class="primary-button tap-target" hover-class="button-hover" @tap="handleAdd">加入</button>
    </view>

    <view v-if="state.wishes.length" class="wish-list">
      <view v-for="wish in state.wishes" :key="wish.id" class="wish-item soft-card tap-target" :class="{ done: wish.done }" hover-class="soft-hover" @tap="handleToggle(wish.id)">
        <view class="check" aria-hidden="true">
          <view v-if="wish.done" class="check-mark"></view>
        </view>
        <view class="wish-body">
          <text class="wish-title">{{ wish.title }}</text>
          <text class="wish-date">{{ formatFriendlyDate(wish.createdAt) }}</text>
        </view>
        <text class="wish-status">{{ wish.done ? '完成' : '待完成' }}</text>
      </view>
    </view>

    <view v-else class="empty-state soft-card">还没有愿望，先写一个很小很小的也可以。</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { addWish, loadState, toggleWish } from '../../store/love'
import { formatFriendlyDate } from '../../utils/date'

const state = ref(loadState())
const newWish = ref('')

onShow(() => {
  state.value = loadState()
})

function handleAdd() {
  const title = newWish.value.trim()
  if (!title) {
    uni.showToast({ title: '先写下一个小愿望', icon: 'none' })
    return
  }
  state.value = addWish(title)
  newWish.value = ''
}

function handleToggle(id) {
  state.value = toggleWish(id)
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

.wish-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 26rpx;
}

.wish-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-height: 112rpx;
  padding: 22rpx;
  border-color: rgba(169, 103, 82, 0.14);
}

.check {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 46rpx;
  width: 46rpx;
  height: 46rpx;
  border: 3rpx solid #ff927f;
  border-radius: 14rpx;
  color: #fff;
  background: #fff7f2;
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

.wish-status {
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

@media screen and (max-width: 360px) {
  .input-card {
    grid-template-columns: 1fr;
  }
}
</style>
