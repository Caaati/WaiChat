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

export function formatMessageTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
