export function getTodayString() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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
