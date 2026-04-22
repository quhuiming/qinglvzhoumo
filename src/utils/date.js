export function getTodayString() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function getNowString() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

export function countLoveDays(startDate) {
  if (!startDate) return 1
  const start = new Date(`${startDate}T00:00:00`)
  if (Number.isNaN(start.getTime())) return 1
  const today = new Date()
  const current = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const diff = current.getTime() - start.getTime()
  return Math.max(1, Math.floor(diff / 86400000) + 1)
}

export function formatFriendlyDate(dateText) {
  if (!dateText) return '今天'
  const date = new Date(`${dateText}T00:00:00`)
  if (Number.isNaN(date.getTime())) return dateText
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

export function formatShortTime(dateTimeText) {
  if (!dateTimeText) return ''
  const time = dateTimeText.split(' ')[1]
  return time ? time.slice(0, 5) : ''
}
