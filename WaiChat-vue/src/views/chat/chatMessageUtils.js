export function mapHistoryMessage(msg, currentUserId, contactId, contactNickname) {
  const isSelf = msg.userId == currentUserId
  return {
    id: msg.id,
    senderId: isSelf ? currentUserId : msg.userId,
    targetId: isSelf ? contactId : msg.targetId,
    content: msg.content,
    type: msg.type,
    audioUrl: msg.audioUrl,
    duration: msg.duration,
    senderName: isSelf ? '我' : contactNickname,
    timestamp: msg.createTime,
    translatedContent: null,
  }
}

export function buildRealtimeMessage(data, senderName) {
  return {
    id: data.id || Date.now() + Math.random(),
    senderId: data.userId || data.senderId,
    targetId: data.targetId,
    content: data.content,
    type: data.type || 'TEXT',
    audioUrl: data.audioUrl,
    duration: data.duration,
    senderName,
    timestamp: data.createTime || new Date(),
    translatedContent: null,
    isTranslating: false,
    textContent: null,
    isTranscribing: false,
  }
}

/** 单条消息内简短时间（HH:mm），仍可供其它场景使用 */
export function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) return ''
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const TIME_DIVIDER_GAP_MS = 5 * 60 * 1000

const WEEKDAYS_CN = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

function sameCalendarDay(a, b) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  )
}

function startOfWeekMonday(d) {
  const x = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const day = x.getDay()
  const diff = day === 0 ? -6 : 1 - day
  x.setDate(x.getDate() + diff)
  x.setHours(0, 0, 0, 0)
  return x
}

function sameWeekMonday(a, b) {
  return startOfWeekMonday(a).getTime() === startOfWeekMonday(b).getTime()
}

/**
 * 与上一条相比是否应显示「大跨度」时间分隔（类微信：同日 5 分钟内不重复显示）
 * @param {string|number|Date|null|undefined} prevTimestamp
 * @param {string|number|Date|null|undefined} currTimestamp
 */
export function shouldShowMessageTimeDivider(prevTimestamp, currTimestamp, isFirst) {
  if (isFirst) return true
  const prev = new Date(prevTimestamp)
  const curr = new Date(currTimestamp)
  if (Number.isNaN(curr.getTime())) return false
  if (Number.isNaN(prev.getTime())) return true
  if (!sameCalendarDay(prev, curr)) return true
  return curr.getTime() - prev.getTime() >= TIME_DIVIDER_GAP_MS
}

/**
 * 时间分隔文案：今天 HH:mm → 昨天 HH:mm → 本周 星期X HH:mm → 同年 M月d日 HH:mm → yyyy年M月d日 HH:mm
 * @param {string|number|Date|null|undefined} timestamp
 * @param {Date} [now]
 */
export function formatWeChatTimeDivider(timestamp, now = new Date()) {
  if (!timestamp) return ''
  const d = new Date(timestamp)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  const hm = `${pad(d.getHours())}:${pad(d.getMinutes())}`

  if (sameCalendarDay(d, now)) return hm

  const y = new Date(now)
  y.setDate(y.getDate() - 1)
  if (sameCalendarDay(d, y)) return `昨天 ${hm}`

  if (d.getFullYear() === now.getFullYear()) {
    if (sameWeekMonday(d, now)) return `${WEEKDAYS_CN[d.getDay()]} ${hm}`
    return `${d.getMonth() + 1}月${d.getDate()}日 ${hm}`
  }

  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${hm}`
}
