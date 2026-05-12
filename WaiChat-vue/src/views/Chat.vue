<template>
  <div class="app-container" :class="{ 'mobile-mode': isMobile }">
    <transition name="slide-side">
      <div class="contacts-sidebar" v-show="sidebarVisible" id="sidebar">
        <div class="sidebar-header">
          <h3>联系人</h3>
          <button type="button" class="terminology-sidebar-btn btn-reset" @click="$router.push('/terminology')">
            术语库
          </button>
        </div>
        <ul class="contacts-list">
          <li class="add-chat-item" @click="showAddContactModal = true">
            <div class="add-chat-icon">+</div>
            <div class="add-chat-text">新增聊天</div>
          </li>
          <li
            v-for="contact in contacts"
            :key="contact.id"
            :class="{ active: selectedContactId === contact.id }"
            @click="selectContact(contact)"
          >
            <div class="contact-avatar">
              <span>{{ contact.nickname.charAt(0) }}</span>
            </div>
            <div class="contact-info">
              <div class="contact-name">
                <span class="nickname">{{ contact.nickname }}</span>
                <span class="username">@{{ contact.username }}</span>
              </div>
              <div class="last-message">{{ contact.lastMessage }}</div>
            </div>
            <span
              v-if="unreadCounts[contact.id] && unreadCounts[contact.id] > 0"
              class="unread-badge"
            >
              {{ unreadCounts[contact.id] }}
            </span>
          </li>
        </ul>
      </div>
    </transition>

    <div class="chat-container">
      <transition name="slide-fade">
        <div v-if="notification.show" class="chat-notification" :class="notification.type">
          <span class="notify-icon">{{ notification.type === 'error' ? '⚠️' : 'ℹ️' }}</span>
          <span class="notify-text">{{ notification.message }}</span>
        </div>
      </transition>

      <div class="chat-header">
        <div class="header-left">
          <button class="toggle-sidebar-btn btn-reset" @click="toggleSidebar" title="切换侧边栏/返回列表">
            <span v-if="isMobile">⬅️</span> <span v-else>{{ sidebarVisible ? '◀' : '▶' }}</span>
          </button>

          <div class="contact-avatar" v-if="currentContactName">
            <span>{{ currentContactName.charAt(0) }}</span>
          </div>
          <h2 class="chat-title">{{ currentContactName || '未选择联系人' }}</h2>

          <div class="header-actions">
            <button
              v-if="selectedContactId"
              @click="handleClearHistory"
              class="icon-btn btn-reset"
              title="清空历史记录"
              type="button"
            >
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                <line x1="10" y1="11" x2="10" y2="17" />
                <line x1="14" y1="11" x2="14" y2="17" />
              </svg>
            </button>
            <button
              v-if="selectedContactId"
              @click="handleRecoverHistory"
              class="icon-btn btn-reset"
              title="恢复历史记录"
              type="button"
            >
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
                <path d="M21 3v5h-5" />
              </svg>
            </button>
            <button
              class="summary-btn mobile-hide-text"
              @click="handleSummarize"
              :disabled="aiProcessing || !filteredMessages.length"
              title="总结当前聊天记录"
              type="button"
            >
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
                <rect x="8" y="2" width="8" height="4" rx="1" ry="1" />
              </svg>
              <span class="btn-text">总结</span>
            </button>
            <button
              class="analysis-btn mobile-hide-text"
              @click="handleAnalysis"
              :disabled="aiProcessing || !filteredMessages.length"
              title="查看聊天数据分析看板"
              type="button"
            >
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <line x1="18" y1="20" x2="18" y2="10" />
                <line x1="12" y1="20" x2="12" y2="4" />
                <line x1="6" y1="20" x2="6" y2="14" />
              </svg>
              <span class="btn-text">分析</span>
            </button>
          </div>
        </div>

        <div class="header-right">
          <div class="translation-controls" v-if="selectedContactId && !isMobileSimple">
            <label class="switch-label" title="收到消息将自动翻译为指定语言">
              <input type="checkbox" v-model="autoTranslate" />
              <span class="switch-text">自动翻译</span>
            </label>

            <select v-model="targetLang" class="lang-select">
              <option v-for="lang in languages" :key="lang.code" :value="lang.code">
                {{ lang.flag }} {{ lang.name }}
              </option>
            </select>
          </div>

          <div class="current-user-wrap" ref="userMenuRoot">
            <div class="current-user-item">
              <button
                type="button"
                class="user-avatar-trigger btn-reset"
                :title="userMenuOpen ? '关闭菜单' : '账户菜单'"
                aria-haspopup="menu"
                :aria-expanded="userMenuOpen"
                @click.stop="toggleUserMenu"
              >
                <div class="contact-avatar">
                  <span>{{ nickname ? nickname.charAt(0) : '?' }}</span>
                </div>
              </button>
              <div class="current-user-meta" v-if="!isMobileSimple">
                <div class="current-user-nickname">{{ nickname || '未登录' }}</div>
              </div>
            </div>
            <transition name="dropdown-fade">
              <div
                v-show="userMenuOpen"
                class="user-dropdown"
                role="menu"
                @click.stop
              >
                <button type="button" class="user-dropdown-item" role="menuitem" @click="goProfile">
                  个人中心
                </button>
                <button type="button" class="user-dropdown-item user-dropdown-item-warn" role="menuitem" @click="logoutFromMenu">
                  退出登录
                </button>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <div class="chat-messages">
        <div v-if="messages.length === 0 && selectedContactId" class="empty-chat-hint">
        </div>
        <div v-else-if="!selectedContactId" class="empty-chat-hint">
          <p v-if="!isMobile">请点击左侧折叠按钮或选择一个联系人。</p>
          <p v-else>点击左上角返回联系人列表</p>
        </div>
        <div class="message-list">
          <div
            v-for="(msg, idx) in filteredMessages"
            :key="msg.id"
            class="message-block"
          >
            <div v-if="showTimeDividerForIndex(idx)" class="message-time-divider-wrap">
              <div class="message-time-divider">{{ formatWeChatDividerTime(msg.timestamp) }}</div>
            </div>

            <div
              :class="[
                'message-row',
                'message-item',
                msg.senderId === userId ? 'self-message' : 'other-message',
              ]"
            >
              <div class="message-peer-avatar" aria-hidden="true">
                <span>{{ messageAvatarLetter(msg) }}</span>
              </div>

              <div class="message-main">
                <!-- 文本等：保留气泡 -->
                <template v-if="msg.type !== 'VOICE'">
                  <div class="message-bubble">
                    <div class="message-content">
                      {{ msg.content }}
                    </div>

                    <div v-if="msg.translatedContent" class="translation-content">
                      <div class="divider"></div>
                      <div class="translation-line">
                        <div>
                          <span class="trans-icon">{{ getFlag(msg.translatedToLang) }}</span>
                          {{ msg.translatedContent }}
                        </div>
                        <button
                          class="clear-trans-btn"
                          @click.stop="clearTranslation(msg)"
                          title="清除翻译"
                        >
                          ❌
                        </button>
                      </div>
                    </div>
                    <div v-else-if="msg.isTranslating" class="translating-spinner">翻译中...</div>

                    <button
                      v-if="!msg.translatedContent && !msg.isTranslating"
                      class="manual-trans-btn"
                      @click="translateSingleMessage(msg)"
                    >
                      翻译
                    </button>
                  </div>
                </template>

                <!-- 语音：无外侧气泡，仅语音条 + 下方操作（类微信） -->
                <template v-else>
                  <div class="message-voice-column">
                    <div
                      class="voice-player"
                      :style="voiceBarStyle(msg.duration)"
                      @click="playAudio(msg.audioUrl)"
                    >
                      <span class="voice-icon" aria-hidden="true">
                        <svg class="voice-mic-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
                          <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
                          <line x1="12" y1="19" x2="12" y2="23" />
                          <line x1="8" y1="23" x2="16" y2="23" />
                        </svg>
                      </span>
                      <span class="voice-duration">{{ formatVoiceDuration(msg.duration) }}</span>
                      <audio :src="msg.audioUrl" ref="audioPlayer"></audio>
                    </div>
                    <button
                      v-if="!msg.textContent && !msg.isTranscribing"
                      class="voice-to-text-btn"
                      @click="convertVoiceToText(msg)"
                    >
                      转文字
                    </button>
                    <div v-if="msg.isTranscribing" class="transcribing-spinner">转文字中...</div>
                    <div v-if="msg.textContent" class="voice-text-content">
                      <div class="divider"></div>
                      <div class="voice-text-line">
                        📝 {{ msg.textContent }}
                        <button
                          class="clear-text-btn"
                          @click.stop="clearVoiceText(msg)"
                          title="清除文字"
                        >
                          ❌
                        </button>
                      </div>
                    </div>

                    <div v-if="msg.translatedContent" class="translation-content voice-translation">
                      <div class="divider"></div>
                      <div class="translation-line">
                        <div>
                          <span class="trans-icon">{{ getFlag(msg.translatedToLang) }}</span>
                          {{ msg.translatedContent }}
                        </div>
                        <button
                          class="clear-trans-btn"
                          @click.stop="clearTranslation(msg)"
                          title="清除翻译"
                        >
                          ❌
                        </button>
                      </div>
                    </div>
                    <div v-else-if="msg.isTranslating" class="translating-spinner">翻译中...</div>

                    <button
                      v-if="msg.textContent && !msg.translatedContent && !msg.isTranslating"
                      class="manual-trans-btn"
                      @click="translateSingleMessage(msg)"
                    >
                      翻译
                    </button>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-wrapper" v-if="selectedContactId">
        <transition name="slide-up">
          <div v-if="aiSuggestion" class="ai-suggestion-box">
            <div class="suggestion-text">
              <strong>{{ aiSuggestionType === 'polish' ? 'AI 润色建议:' : 'AI 智能回复:' }}</strong>
              {{ aiSuggestion }}
            </div>
            <div class="suggestion-actions">
              <button @click="applySuggestion" class="apply-btn">采纳</button>
              <button @click="cancelSuggestion" class="cancel-btn">取消</button>
            </div>
          </div>
        </transition>
        <div class="ai-toolbar">
          <button
            v-if="!isMobile"
            class="mic-trigger-btn pc-mic-btn btn-reset"
            @click="startPcRecording"
            title="点击开始录音"
            type="button"
          >
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
              <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
              <line x1="12" y1="19" x2="12" y2="23" />
              <line x1="8" y1="23" x2="16" y2="23" />
            </svg>
          </button>
          <button
            v-else
            class="mic-trigger-btn mobile-mic-btn btn-reset"
            @touchstart.prevent="handleTouchStart"
            @touchmove.prevent="handleTouchMove"
            @touchend.prevent="handleTouchEnd"
            type="button"
          >
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
              <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
              <line x1="12" y1="19" x2="12" y2="23" />
              <line x1="8" y1="23" x2="16" y2="23" />
            </svg>
          </button>
          <button
            class="ai-tool-btn"
            @click="handleSmartReply"
            :disabled="aiProcessing || !!aiSuggestion"
            type="button"
          >
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <rect x="5" y="10" width="14" height="10" rx="2" />
              <path d="M9 14v2M15 14v2" />
              <path d="M12 2v3M9 5h6" />
            </svg>
            <span class="btn-text">智能回复</span>
          </button>
          <button
            class="ai-tool-btn"
            @click="handleAiPolish('business')"
            :disabled="!message.trim() || aiProcessing || !!aiSuggestion"
            type="button"
          >
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M12 3l-1.5 4.5L6 9l4.5 1.5L12 15l1.5-4.5L18 9l-4.5-1.5L12 3z" />
              <path d="M5 19h14" />
            </svg>
            <span class="btn-text">商务润色</span>
          </button>
          <button
            class="ai-tool-btn"
            @click="handleAiPolish('casual')"
            :disabled="!message.trim() || aiProcessing || !!aiSuggestion"
            type="button"
          >
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 0 1-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            <span class="btn-text">语气软化</span>
          </button>

          <div class="ai-loading" v-if="aiProcessing">Thinking...</div>
        </div>

        <div class="chat-input-area">
          <div class="emoji-container">
            <button
              class="emoji-toggle-btn btn-reset"
              @click.stop="showEmojiPicker = !showEmojiPicker"
              title="选择表情"
            >
              😊
            </button>

            <transition name="slide-fade-fast">
              <div v-if="showEmojiPicker" class="emoji-picker-wrapper">
                <EmojiPicker :native="true" @select="insertEmoji" :theme="'light'" />
              </div>
            </transition>
          </div>
          <input
            type="text"
            v-model="message"
            ref="messageInput"
            @keyup.enter="!aiSuggestion && sendMessage()"
            placeholder="输入消息..."
            class="message-input"
          />
          <button @click="sendMessage" class="send-button">发送</button>
        </div>
      </div>

      <transition name="modal-fade">
        <div v-if="showSummaryModal" class="chat-summary-modal-overlay modal-overlay-common">
          <div class="chat-summary-modal panel-card-common">
            <div class="modal-header">
              <h3>聊天摘要</h3>
              <button @click="showSummaryModal = false" class="close-btn btn-reset">×</button>
            </div>
            <div class="modal-content">
              <div v-if="chatSummary" class="summary-text">{{ chatSummary }}</div>
              <div v-else>正在生成摘要...</div>
            </div>
            <div class="modal-footer">
              <button @click="copySummary" class="copy-btn">复制摘要</button>
            </div>
          </div>
        </div>
      </transition>
      <transition name="modal-fade">
        <div v-if="showAnalysisModal" class="analysis-modal-overlay modal-overlay-common">
          <div class="analysis-modal panel-card-common">
            <div class="modal-header">
              <h3>🤖 AI 聊天数据看板</h3>
              <button @click="showAnalysisModal = false" class="close-btn btn-reset">×</button>
            </div>

            <div class="modal-content analysis-content">
              <div v-if="analysisLoading" class="analysis-loading">
                <div class="loading-spinner"></div>
                <p>AI 正在分析你们的聊天数据...</p>
              </div>

              <div v-else-if="analysisData" class="dashboard-grid">
                <div class="dashboard-card summary-card">
                  <div class="card-title">✨ AI 关系透视</div>
                  <div class="ai-comment">{{ analysisData.summary }}</div>
                </div>

                <div class="dashboard-card">
                  <div class="chart-container" ref="keywordChart"></div>
                </div>

                <div class="dashboard-card">
                  <div class="chart-container" ref="sentimentChart"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
    <transition name="fade">
      <div v-if="isPcRecording" class="pc-recording-overlay">
        <div class="pc-record-modal">
          <div class="modal-title">正在录音...</div>
          <div class="visualizer-container pc-visualizer">
            <div
              v-for="(bar, index) in bars"
              :key="index"
              class="visualizer-bar"
              :style="{ height: bar + 'px' }"
            ></div>
          </div>
          <div class="record-timer">{{ recordDuration }}s</div>
          <div class="pc-record-actions">
            <button class="action-btn cancel" @click="handlePcAction('cancel')">
              <span class="icon">🗑️</span> 取消
            </button>
            <button class="action-btn transcribe" @click="handlePcAction('transcribe')">
              <span class="icon">📝</span> 转文字
            </button>
            <button class="action-btn send" @click="handlePcAction('send')">
              <span class="icon">🚀</span> 发送
            </button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="isMobileRecording" class="recording-overlay">
        <div
          class="recording-card"
          :class="{ pulse: isMobileRecording && !bars.some((b) => b > 10) }"
        >
          <div v-if="recordStatus === 'normal'" class="visualizer-container mobile-visualizer">
            <div
              v-for="(bar, index) in bars"
              :key="index"
              class="visualizer-bar"
              :style="{ height: bar * 0.6 + 'px' }"
            ></div>
          </div>
          <div v-else class="status-icon">
            {{ recordStatus === 'cancel' ? '🗑️' : '📝' }}
          </div>
          <p :class="['status-hint', { warning: recordStatus !== 'normal' }]">{{ statusHint }}</p>
          <div class="record-timer">{{ recordDuration }}s</div>
        </div>
        <div class="record-zones">
          <div class="zone left" :class="{ active: recordStatus === 'cancel' }">取消</div>
          <div class="zone right" :class="{ active: recordStatus === 'transcribe' }">转文字</div>
        </div>
      </div>
    </transition>
    <AddContactModal
      :is-visible="showAddContactModal"
      :current-user-id="userId"
      @close="showAddContactModal = false"
      @user-selected="handleUserSelected"
      @search-error="showNotification"
    />
  </div>
</template>

<script>
import axios from 'axios'
import AddContactModal from '@/components/chat/AddContactModal.vue'
import { CODES } from '@/constants/codes.js'
import EmojiPicker from 'vue3-emoji-picker'
import * as echarts from 'echarts'
import {
  buildRealtimeMessage,
  formatWeChatTimeDivider,
  mapHistoryMessage,
  shouldShowMessageTimeDivider,
} from '@/views/chat/chatMessageUtils.js'

export default {
  components: {
    AddContactModal,
    EmojiPicker,
  },
  data() {
    return {
      isPcRecording: false, // PC端录音状态
      isMobileRecording: false, // 手机端录音状态
      // 音频可视化相关
      audioContext: null,
      analyser: null,
      dataArray: null,
      animationId: null,
      bars: Array(20).fill(5), // 初始化20个槽位
      recordTimer: null,

      recordStatus: 'normal', // normal, cancel, transcribe
      startX: 0,
      startY: 0,
      mediaRecorder: null,
      audioChunks: [],
      recordStartTime: 0,
      isPlaying: null, // 当前播放的音频地址

      messages: [],
      languages: [],
      message: '',
      aiSuggestion: '',
      aiSuggestionType: '',
      ws: null,
      userId: null,
      username: null,
      nickname: null,
      selectedContactId: null,
      currentContactName: '',
      contacts: [],
      unreadCounts: {},
      showAddContactModal: false,

      // --- 状态 ---
      autoTranslate: false,
      targetLang: 'zh',
      aiProcessing: false,
      showSummaryModal: false,
      chatSummary: '',
      showEmojiPicker: false,

      // ***** 看板相关状态 *****
      showAnalysisModal: false,
      analysisData: null,
      analysisLoading: false,
      chartInstanceKeywords: null,
      chartInstanceSentiment: null,

      notification: {
        show: false,
        message: '',
        type: 'info',
        timer: null,
      },

      // --- 响应式适配新增 ---
      sidebarVisible: true, // 控制侧边栏是否可见
      isMobile: false, // 是否为移动端
      userMenuOpen: false,
    }
  },
  computed: {
    statusHint() {
      if (this.recordStatus === 'cancel') return '松开手指，取消发送'
      if (this.recordStatus === 'transcribe') return '松开手指，转文字'
      return '手指上划，取消或转文字'
    },
    filteredMessages() {
      if (!this.selectedContactId || !this.userId) return []
      return this.messages
        .filter(
          (msg) =>
            (msg.senderId == this.userId && msg.targetId == this.selectedContactId) ||
            (msg.senderId == this.selectedContactId && msg.targetId == this.userId),
        )
        .sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
    },
    // 极简模式（屏幕非常窄时）
    isMobileSimple() {
      return this.isMobile && window.innerWidth < 400
    },
  },

  methods: {
    // 语音转文字
    async convertVoiceToText(msg) {
      if (!msg.audioUrl) {
        this.showNotification('音频链接为空', 'error');
        return;
      }
      // 标记转文字中
      msg.isTranscribing = true;
      try {
        const response = await axios.post('/api/ai/voiceToText', {
          audioUrl: msg.audioUrl, // 音频文件URL
        });
        if (response.data.code === CODES.SUCCESS && response.data.data) {
          msg.textContent = response.data.data.text; // 保存转文字结果
          this.showNotification('语音转文字成功');
          if (this.autoTranslate && msg.textContent && !msg.translatedContent) {
            this.translateSingleMessage(msg)
          }
        } else {
          this.showNotification(response.data.msg || '语音转文字失败', 'error');
        }
      } catch (error) {
        console.error('语音转文字失败:', error);
        this.showNotification('语音转文字服务异常', 'error');
      } finally {
        // 取消加载状态
        msg.isTranscribing = false;
      }
    },
    // 清除语音转文字结果
    clearVoiceText(msg) {
      if (msg) {
        msg.textContent = null;
        msg.translatedContent = null
        msg.translatedToLang = null
        this.$forceUpdate();
      }
    },
    playAudio(url) {
      if (!url) return
      const audio = new Audio(url)
      audio.play().catch((err) => {
        console.error('播放失败:', err)
        this.showNotification('播放失败，请检查文件链接是否有效','error')
      })
    },
    async handleStrangerMessage(senderId, content) {
      try {
        // 复用 AddContactModal 中的接口逻辑
        const response = await axios.get('/api/user/search', {
          params: { key: String(senderId) }, // 确保转为字符串
        })

        if (response.data.code === CODES.SUCCESS) {
          const users = response.data.data
          // 精确匹配 ID (防止搜索结果返回多个相似 ID 的情况)
          const user = users.find((u) => u.id == senderId)

          if (user) {
            // 1. 构造新的联系人对象
            const newContact = {
              id: user.id,
              username: user.username,
              nickname: user.nickname,
              lastMessage: content,
              // 如果有头像字段也可以加在这里
            }

            // 2. 添加到左侧列表顶部
            this.contacts.unshift(newContact)

            // 3. 【视觉优化】更新刚才收到的那条显示为 "未知用户" 或 "ID" 的消息的名字
            // 遍历当前消息列表，把该用户发的消息名字修正过来
            this.messages.forEach((msg) => {
              if (msg.senderId == senderId) {
                msg.senderName = user.nickname
              }
            })

            // 4. 如果不需要强制更新视图，Vue 的响应式通常会自动处理
            // 但如果消息列表没有变化，可以尝试 this.$forceUpdate() (一般不需要)
          }
        }
      } catch (error) {
        console.error('获取陌生人信息失败', error)
      }
    },
    checkScreenSize() {
      this.isMobile = window.innerWidth <= 768
      // 如果切换到桌面端，默认展开侧边栏；如果切换到移动端，根据是否有选人决定
      if (!this.isMobile) {
        this.sidebarVisible = true
      } else {
        // 移动端：如果有选中联系人，侧边栏隐藏（显示聊天）；否则显示侧边栏
        this.sidebarVisible = !this.selectedContactId
      }
    },
    toggleSidebar() {
      if (this.isMobile) {
        // 移动端：此按钮充当“返回”键
        this.selectedContactId = null
        this.sidebarVisible = true
      } else {
        // 桌面端：此按钮充当折叠/展开键
        this.sidebarVisible = !this.sidebarVisible
      }
    },

    // 原有方法修改：选择联系人时
    selectContact(contact) {
      this.selectedContactId = contact.id
      this.currentContactName = contact.nickname
      this.messages = []
      if (this.unreadCounts[contact.id]) this.unreadCounts[contact.id] = 0

      // 移动端：选择后隐藏侧边栏进入聊天
      if (this.isMobile) {
        this.sidebarVisible = false
      }

      axios
        .get('/api/chat/history', {
          params: { userId: this.userId, targetId: contact.id },
        })
        .then((res) => {
          if (res.data.code === CODES.SUCCESS) {
            const historyData = Array.isArray(res.data.data) ? res.data.data : []
            this.messages = historyData.map((msg) =>
              mapHistoryMessage(msg, this.userId, contact.id, contact.nickname),
            )
            this.scrollToBottom()
          }
        })
    },

    // ... 其他原有方法保持不变 (getLanguages, handleAnalysis, initCharts, insertEmoji, etc.) ...
    getLanguages() {
      axios
        .get('/api/ai/languages')
        .then((res) => {
          if (res.data.code === CODES.SUCCESS) {
            this.languages = res.data.data
            if (this.languages.length > 0 && !this.targetLang) {
              this.targetLang = this.languages[0].code
            }
          }
        })
        .catch((e) => console.error('获取语言列表失败', e))
    },
    async handleAnalysis() {
      if (!this.selectedContactId || this.analysisLoading) return
      if (this.filteredMessages.length < 5) {
        this.showNotification('聊天记录太少，无法进行有效分析', 'warning')
        return
      }
      this.showAnalysisModal = true
      this.analysisLoading = true
      this.analysisData = null
      const chatsForAnalysis = this.filteredMessages.map((m) => ({
        userId: m.senderId === this.userId ? '我' : '对方',
        content: m.content,
      }))
      try {
        const response = await axios.post('/api/ai/analysis', chatsForAnalysis)
        if (response.data.code === CODES.SUCCESS && response.data.data) {
          const result = response.data.data
          if (!result.keywords || !result.sentiment) {
            this.showNotification('AI 数据不完整，无法显示图表', 'error')
            this.showAnalysisModal = false
            return
          }
          this.analysisData = result
          this.$nextTick(() => {
            setTimeout(() => {
              this.initCharts()
            }, 50)
          })
        } else {
          this.showNotification(response.data.msg || '分析失败，请重试', 'error')
          this.showAnalysisModal = false
        }
      } catch (e) {
        console.error(e)
        this.showNotification('AI 服务繁忙', 'error')
        this.showAnalysisModal = false
      } finally {
        this.analysisLoading = false
      }
    },
    initCharts() {
      if (!this.analysisData) return
      const keywordChartDom = this.$refs.keywordChart
      if (keywordChartDom && keywordChartDom.offsetWidth > 0 && keywordChartDom.offsetHeight > 0) {
        if (this.chartInstanceKeywords) {
          this.chartInstanceKeywords.dispose()
          this.chartInstanceKeywords = null
        }
        this.chartInstanceKeywords = echarts.init(keywordChartDom)
        const keywords = this.analysisData.keywords || []
        if (keywords.length === 0) {
          this.chartInstanceKeywords.setOption({
            title: { text: '💬 暂无关键词数据', left: 'center' },
          })
          this.chartInstanceKeywords.setOption({
            title: { text: '💬 暂无关键词数据', left: 'center' },
          })
        } else {
          this.chartInstanceKeywords.setOption({
            title: { text: '💬 高频热词 Top 8', left: 'center', textStyle: { fontSize: 14 } },
            tooltip: {},
            grid: { top: 40, bottom: 20, left: 10, right: 10, containLabel: true },
            xAxis: { type: 'value', show: false },
            yAxis: {
              type: 'category',
              data: keywords.map((k) => k.name).reverse(),
              axisLine: { show: false },
              axisTick: { show: false },
            },
            series: [
              {
                type: 'bar',
                data: keywords.map((k) => k.value).reverse(),
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                    { offset: 0, color: '#83bff6' },
                    { offset: 1, color: '#188df0' },
                  ]),
                  borderRadius: [0, 10, 10, 0],
                },
                label: { show: true, position: 'right' },
              },
            ],
          })
        }
      }
      const sentimentChartDom = this.$refs.sentimentChart
      if (
        sentimentChartDom &&
        sentimentChartDom.offsetWidth > 0 &&
        sentimentChartDom.offsetHeight > 0
      ) {
        if (this.chartInstanceSentiment) {
          this.chartInstanceSentiment.dispose()
          this.chartInstanceSentiment = null
        }
        this.chartInstanceSentiment = echarts.init(sentimentChartDom)
        const sentiments = this.analysisData.sentiment || []
        if (sentiments.length === 0) {
          this.chartInstanceSentiment.setOption({
            title: { text: '❤️ 暂无情感波动数据', left: 'center' },
          })
          return
        }
        this.chartInstanceSentiment.setOption({
          title: {
            text: '❤️ 情感波动趋势 (最近10条)',
            left: 'center',
            textStyle: { fontSize: 14 },
          },
          tooltip: { trigger: 'axis' },
          grid: { top: 40, bottom: 20, left: 10, right: 10, containLabel: true },
          xAxis: { type: 'category', data: sentiments.map((_, i) => i + 1) },
          yAxis: {
            type: 'value',
            min: -1,
            max: 1,
            splitNumber: 2,
            axisLabel: {
              formatter: function (value) {
                if (value === 1) return '😊'
                if (value === 0) return '😐'
                if (value === -1) return '😠'
                return ''
              },
            },
          },
          series: [
            {
              data: sentiments,
              type: 'line',
              smooth: true,
              lineStyle: { color: '#ff7043', width: 3 },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(255, 112, 67, 0.5)' },
                  { offset: 1, color: 'rgba(255, 112, 67, 0.1)' },
                ]),
              },
            },
          ],
        })
      }
    },
    insertEmoji(emojiObject) {
      const emoji = emojiObject.i
      const input = this.$refs.messageInput
      if (input && emoji) {
        const start = input.selectionStart
        const end = input.selectionEnd
        this.message = this.message.substring(0, start) + emoji + this.message.substring(end)
        this.$nextTick(() => {
          input.focus()
          input.setSelectionRange(start + emoji.length, start + emoji.length)
        })
      } else if (emoji) {
        this.message += emoji
      }
    },
    copySummary() {
      if (this.chatSummary) {
        navigator.clipboard
          .writeText(this.chatSummary)
          .then(() => {
            this.showNotification('摘要已复制到剪贴板')
          })
          .catch((err) => {
            this.showNotification('复制失败', 'error')
            console.error('无法复制文本: ', err)
          })
      }
    },
    async handleSummarize() {
      if (!this.selectedContactId || this.aiProcessing) return
      if (!this.filteredMessages.length) {
        this.showNotification('当前聊天记录为空，无法总结', 'warning')
        return
      }
      this.aiProcessing = true
      this.chatSummary = ''
      this.showSummaryModal = true
      const chatsForSummarize = this.filteredMessages.map((m) => ({
        userId: m.senderId === this.userId ? '我' : '对方',
        content: m.content,
      }))
      try {
        const response = await axios.post('/api/ai/summarize', chatsForSummarize)
        if (response.data.code === CODES.SUCCESS && response.data.data) {
          this.chatSummary = response.data.data.trim()
        } else {
          this.chatSummary = '摘要生成失败，请稍后再试。'
          this.showNotification(response.data.msg || '摘要生成失败', 'error')
        }
      } catch (e) {
        console.error(e)
        this.chatSummary = '摘要服务连接失败。'
        this.showNotification('AI 服务繁忙，请稍后再试', 'error')
      } finally {
        this.aiProcessing = false
      }
    },
    handleGlobalKeyup(event) {
      if (event.key === 'Escape' && this.userMenuOpen) {
        event.preventDefault()
        this.userMenuOpen = false
        return
      }
      if (this.aiSuggestion) {
        if (event.key === 'Enter') {
          event.preventDefault()
          this.applySuggestion()
        } else if (event.key === 'Escape') {
          event.preventDefault()
          this.cancelSuggestion()
        }
      }
    },
    clearTranslation(msg) {
      if (msg) {
        msg.translatedContent = null
        msg.translatedToLang = null
        this.$forceUpdate()
      }
    },
    applySuggestion() {
      if (!this.aiSuggestion) return
      this.message = this.aiSuggestion
      this.aiSuggestion = ''
      this.aiSuggestionType = ''
      this.$nextTick(() => document.querySelector('.message-input')?.focus())
    },
    cancelSuggestion() {
      this.aiSuggestion = ''
      this.aiSuggestionType = ''
      this.$nextTick(() => document.querySelector('.message-input')?.focus())
    },
    getFlag(code) {
      return code || '🌐'
    },
    async handleSmartReply() {
      if (!this.selectedContactId) return
      if (this.aiProcessing) {
        this.showNotification('AI 正在处理上一个请求，请稍候', 'warning')
        return
      }
      if (this.aiSuggestion) {
        this.showNotification('请先处理当前的 AI 建议 (Enter/Esc)', 'warning')
        return
      }
      const originalChats = this.filteredMessages.slice(-20).map((m) => ({
        userId: m.senderId === this.userId ? '我' : '对方',
        content: m.content,
      }))

      // 在数组最后（末尾）添加仅包含双方ID的对象
      const chatsForSmartReply = [
        ...originalChats, // 保留原有所有处理后的消息
        {
          // 末尾追加ID对象
          userId: this.userId,
          targetId: this.selectedContactId,
        },
      ]

      if (chatsForSmartReply.length === 0) {
        this.showNotification('没有足够的聊天记录来生成智能回复', 'warning')
        return
      }
      this.aiProcessing = true
      this.aiSuggestion = ''
      this.aiSuggestionType = ''
      try {
        const response = await axios.post('/api/ai/smartReply', chatsForSmartReply)
        if (response.data.code === CODES.SUCCESS && response.data.data) {
          this.aiSuggestion = response.data.data.trim()
          this.aiSuggestionType = 'smartReply'
          this.showNotification('已生成智能回复，请按 Enter 采纳')
        } else {
          this.showNotification(response.data.msg || 'AI智能回复服务返回错误或结果为空', 'error')
        }
      } catch (e) {
        console.error(e)
        this.showNotification('AI 服务繁忙', 'error')
      } finally {
        this.aiProcessing = false
      }
    },
    toggleUserMenu() {
      this.userMenuOpen = !this.userMenuOpen
    },
    closeUserMenuOutside(e) {
      const root = this.$refs.userMenuRoot
      if (!this.userMenuOpen || !root) return
      if (!root.contains(e.target)) this.userMenuOpen = false
    },
    goProfile() {
      this.userMenuOpen = false
      this.$router.push('/profile')
    },
    logoutFromMenu() {
      this.userMenuOpen = false
      this.handleLogout()
    },
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        if (this.ws) this.ws.close()
        localStorage.clear()
        this.$router.push('/login')
      }
    },
    getContactList() {
      this.contacts = []
      axios.get('/api/chat/getContactList', { params: { userId: this.userId } }).then((res) => {
        if (res.data.code === CODES.SUCCESS) {
          const _data = Array.isArray(res.data.data) ? res.data.data : []
          this.contacts = _data.map((item) => ({
            id: item.id,
            username: item.username,
            nickname: item.nickname,
            lastMessage: item.content || '无消息',
          }))
        }
      })
    },
    handleUserSelected(user) {
      const existingContactIndex = this.contacts.findIndex((c) => c.id == user.id)
      if (existingContactIndex !== -1) {
        const contact = this.contacts.splice(existingContactIndex, 1)[0]
        this.contacts.unshift(contact)
      } else {
        this.contacts.unshift({
          id: user.id,
          username: user.username,
          nickname: user.nickname,
          lastMessage: '无消息',
        })
      }
      this.selectContact({ id: user.id, nickname: user.nickname, username: user.username })
    },
    showTimeDividerForIndex(idx) {
      const list = this.filteredMessages
      if (!list.length) return false
      const msg = list[idx]
      const prev = idx > 0 ? list[idx - 1] : null
      return shouldShowMessageTimeDivider(prev?.timestamp, msg.timestamp, idx === 0)
    },
    formatWeChatDividerTime(timestamp) {
      return formatWeChatTimeDivider(timestamp)
    },
    messageAvatarLetter(msg) {
      if (msg.senderId == this.userId) {
        return (this.nickname && this.nickname.charAt(0)) || '我'
      }
      return (this.currentContactName && this.currentContactName.charAt(0)) || '?'
    },
    formatVoiceDuration(sec) {
      const s = Number(sec)
      if (!Number.isFinite(s) || s < 0) return '0″'
      return `${Math.round(s)}″`
    },
    /** 语音条最小宽度随时长变化；实际宽度不小于内容（避免时长文字溢出） */
    voiceBarStyle(durationSec) {
      const raw = Number(durationSec)
      const sec = Number.isFinite(raw) && raw > 0 ? raw : 1
      const clamped = Math.min(Math.max(sec, 0.5), 120)
      const minPx = 64
      const maxPx = 240
      const t = (clamped - 0.5) / (120 - 0.5)
      const w = Math.round(minPx + t * (maxPx - minPx))
      return {
        width: `${w}px`,
        minWidth: `${w}px`,
        maxWidth: `${maxPx}px`,
        transition: 'width 0.2s ease, min-width 0.2s ease',
      }
    },
    async handleClearHistory() {
      if (!this.selectedContactId) return
      if (!confirm(`确定要清空与 ${this.currentContactName} 的所有聊天记录吗？`)) {
        return
      }
      try {
        const response = await axios.post('/api/chat/removeHistory', {
          userId: this.userId,
          targetId: this.selectedContactId,
        })
        if (response.data.code === CODES.SUCCESS || response.data.code === 200) {
          this.messages = []
          const contact = this.contacts.find((c) => c.id == this.selectedContactId)
          if (contact) {
            contact.lastMessage = '无消息'
          }
          this.showNotification('聊天记录已清空', 'info')
        } else {
          this.showNotification(response.data.msg || '清空历史记录失败', 'error')
        }
      } catch (error) {
        console.error('清空历史记录请求失败', error)
        this.showNotification('清空历史记录失败，网络或服务错误', 'error')
      }
    },
    async handleRecoverHistory() {
      if (!this.selectedContactId) return
      if (!confirm(`确定要恢复与 ${this.currentContactName} 之间已逻辑删除的聊天记录吗？`)) {
        return
      }
      try {
        const payload = {
          userId: this.userId,
          targetId: this.selectedContactId,
        }
        const response = await axios.post('/api/chat/recoverHistory', payload)
        if (response.data.code === CODES.SUCCESS || response.data.code === 200) {
          const currentContact = this.contacts.find((c) => c.id == this.selectedContactId)
          if (currentContact) {
            this.selectContact(currentContact)
          }
          this.showNotification('聊天记录已恢复', 'info')
        } else {
          this.showNotification(response.data.msg || '恢复历史记录失败', 'error')
        }
      } catch (error) {
        console.error('恢复历史记录请求失败', error)
        this.showNotification('恢复历史记录失败，网络或服务错误', 'error')
      }
    },
    async handleAiPolish(style) {
      if (!this.message.trim()) return
      if (this.aiProcessing) {
        this.showNotification('AI 正在处理上一个请求，请稍候', 'warning')
        return
      }
      if (this.aiSuggestion) {
        this.showNotification('请先处理当前的 AI 建议 (Enter/Esc)', 'warning')
        return
      }
      this.aiProcessing = true
      this.aiSuggestion = ''
      this.aiSuggestionType = ''
      try {
        const response = await axios.post('/api/ai/polish', {
          text: this.message,
          style: style,
        })
        if (response.data.code === CODES.SUCCESS && response.data.data) {
          this.aiSuggestion = response.data.data.trim()
          this.aiSuggestionType = 'polish'
          this.showNotification(
            `已完成${style === 'business' ? '商务' : '语气'}润色，请按 Enter 采纳`,
          )
        } else {
          this.showNotification('AI润色服务返回错误或结果为空', 'error')
        }
      } catch (error) {
        this.showNotification('AI服务暂时繁忙', 'error')
      } finally {
        this.aiProcessing = false
      }
    },
    async translateSingleMessage(msg) {
      if (msg.translatedContent || msg.isTranslating) return
      const sourceText =
        msg.type === 'VOICE' ? (msg.textContent || '').trim() : (msg.content || '').trim()
      if (msg.type === 'VOICE' && !sourceText) {
        this.showNotification('请先使用「转文字」后再翻译', 'warning')
        return
      }
      if (!sourceText) return
      msg.isTranslating = true
      try {
        const response = await axios.post('/api/ai/translate', {
          text: sourceText,
          target: this.targetLang,
        })
        if (response.data.code === CODES.SUCCESS) {
          msg.translatedContent = response.data.data.translated
          msg.translatedToLang = this.targetLang
        } else {
          console.warn('翻译接口返回异常', response.data)
          this.showNotification('翻译失败', 'error')
        }
      } catch (error) {
        console.error('翻译失败', error)
        this.showNotification('翻译服务不可用', 'error')
      } finally {
        msg.isTranslating = false
        this.$forceUpdate()
      }
    },
    async sendMessage() {
      if (!this.message.trim() || !this.selectedContactId) return
      const newMessage = {
        id: Date.now(),
        senderId: this.userId,
        senderName: '我',
        targetId: this.selectedContactId,
        targetName: this.currentContactName,
        content: this.message,
        status: 'sending',
        translatedContent: null,
      }
      this.messages.push(newMessage)
      this.scrollToBottom()
      const messageContent = this.message
      this.message = ''
      try {
        const payload = {
          userId: this.userId,
          targetId: this.selectedContactId,
          content: messageContent,
        }
        const response = await fetch('/api/chat/send', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        })
        const data = await response.json()
        if (data.code === CODES.SUCCESS) {
          newMessage.status = 'sent'
          const contactIndex = this.contacts.findIndex((c) => c.id == this.selectedContactId)
          if (contactIndex !== -1) {
            this.contacts[contactIndex].lastMessage = messageContent
            this.contacts.unshift(this.contacts.splice(contactIndex, 1)[0])
          }
        } else {
          newMessage.status = 'offline'
          this.showNotification(data.msg || '对方不在线', 'error')
        }
      } catch (error) {
        newMessage.status = 'error'
        this.showNotification('发送失败', 'error')
      }
    },
    // ---------------- 通用：音频可视化核心逻辑 ----------------
    initVisualizer(stream) {
      // 兼容性处理
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      this.audioContext = new AudioContext();
      const source = this.audioContext.createMediaStreamSource(stream);
      this.analyser = this.audioContext.createAnalyser();
      this.analyser.fftSize = 64;
      source.connect(this.analyser);
      const bufferLength = this.analyser.frequencyBinCount;
      this.dataArray = new Uint8Array(bufferLength);
      this.updateWave();
    },

    updateWave() {
      if (!this.isPcRecording && !this.isMobileRecording) return;
      if (!this.analyser) return;
      this.analyser.getByteFrequencyData(this.dataArray);
      for (let i = 0; i < 20; i++) {
        const val = this.dataArray[i];
        const height = Math.max(6, (val / 255) * 50);
        this.bars[i] = height;
      }
      this.animationId = requestAnimationFrame(() => this.updateWave());
    },

    stopVisualizer() {
      if (this.animationId) cancelAnimationFrame(this.animationId);
      if (this.audioContext) this.audioContext.close();
      this.bars = new Array(20).fill(5);
    },

    /**
     * 非 HTTPS / 非 localhost 时 navigator.mediaDevices 常为 undefined，需提示或走旧版 API。
     * Chrome「将不安全来源视为安全」若仍无效，多为 origin 与地址栏不一致或未彻底重启，见控制台说明。
     */
    requestMicStream() {
      const md = navigator.mediaDevices
      if (md && typeof md.getUserMedia === 'function') {
        return md.getUserMedia({ audio: true })
      }
      const legacy =
        navigator.getUserMedia ||
        navigator.webkitGetUserMedia ||
        navigator.mozGetUserMedia ||
        navigator.msGetUserMedia
      if (typeof legacy === 'function') {
        return new Promise((resolve, reject) => {
          legacy.call(navigator, { audio: true }, resolve, reject)
        })
      }
      const origin = typeof location !== 'undefined' ? location.origin : '(未知)'
      const secure = typeof window !== 'undefined' ? window.isSecureContext : false
      console.warn(
        `[WaiChat 麦克风] 当前页无 navigator.mediaDevices（isSecureContext=${secure}）。\n` +
          `请在 chrome://flags/#unsafely-treat-insecure-origin-as-secure 的输入框中粘贴**下面整行**（须与地址栏完全一致，一项一行多个用英文逗号分隔）：\n` +
          `  ${origin}\n` +
          '常见原因：① 写成了 https 或漏了端口；② 用 127.0.0.1 打开却填了 localhost（或相反）；③ 用 IP 打开却填了主机名；④ 改完未从系统托盘/任务管理器彻底结束 Chrome 再启动；⑤ 企业策略禁用该实验项。更稳妥：Vite 开启 https（自签/mkcert）或正式 HTTPS。',
      )
      return Promise.reject(
        new Error('非安全连接下浏览器未开放麦克风。请按 F12 查看控制台中的 origin 与说明，或改用 HTTPS。'),
      )
    },

    micErrorMessage(err) {
      const name = err && err.name
      const raw = err && err.message != null ? String(err.message) : ''
      const lower = raw.toLowerCase()
      // Chromium：系统未向浏览器授权麦克风时常报 Permission denied by system
      if (
        name === 'NotAllowedError' &&
        (lower.includes('denied by system') || lower.includes('permission denied by system'))
      ) {
        return '系统未向 Chrome 开放麦克风：请到系统「隐私/麦克风」设置中允许 Google Chrome；再在地址栏锁图标 → 网站设置中将本站麦克风设为「允许」。'
      }
      if (name === 'NotAllowedError') {
        return '麦克风权限被拒绝：请点击地址栏左侧「锁形/信息图标」→ 网站设置 → 麦克风 → 允许；若已拒绝过，请改为「询问」后刷新页面再试。'
      }
      if (name === 'NotFoundError') {
        return '未检测到麦克风设备'
      }
      if (raw && !raw.includes('undefined')) {
        return raw
      }
      return '无法访问麦克风：请通过 HTTPS 或 localhost 打开，并检查浏览器与系统权限'
    },

    // ---------------- PC 端录音逻辑 ----------------
    async startPcRecording() {
      if (this.isPcRecording) return;
      try {
        const stream = await this.requestMicStream();
        this.isPcRecording = true;
        this.recordStartTime = Date.now();
        this.recordDuration = 0;
        this.initVisualizer(stream);
        this.mediaRecorder = new MediaRecorder(stream);
        this.audioChunks = [];
        this.mediaRecorder.ondataavailable = (e) => this.audioChunks.push(e.data);
        this.mediaRecorder.start();
        // 确保计时器变量名统一
        if (this.recordTimer) clearInterval(this.recordTimer);
        this.recordTimer = setInterval(() => {
          this.recordDuration = Math.round((Date.now() - this.recordStartTime) / 1000);
        }, 1000);
      } catch (err) {
        console.error('麦克风权限错误:', err)
        this.showNotification(this.micErrorMessage(err), 'error')
      }
    },

    handlePcAction(type) {
      if (!this.mediaRecorder) return;
      this.mediaRecorder.onstop = () => {
        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        const duration = this.recordDuration;
        if (type === 'send') {
          if (duration < 1) {
            this.showNotification('录音时间太短', 'warning')
          } else {
            this.uploadAndSendVoice(audioBlob, duration);
          }
        } else if (type === 'transcribe') {
          this.processAudioSTT(audioBlob);
        }
        this.audioChunks = [];
        this.stopVisualizer(); // 这里会清理 bars 和 animationId
      };

      this.mediaRecorder.stop();
      this.mediaRecorder.stream.getTracks().forEach(track => track.stop());

      // 【修改点】重置状态和计时器
      this.isPcRecording = false;
      if (this.recordTimer) {
        clearInterval(this.recordTimer);
        this.recordTimer = null;
      }
    },
    // --- 录音手势处理 ---
    async handleTouchStart(e) {
      e.preventDefault();
      this.isMobileRecording = true;
      this.recordStatus = 'normal';
      this.recordStartTime = Date.now();
      this.recordDuration = 0;
      // 启动计时器
      this.timerInterval = setInterval(() => {
        this.recordDuration = Math.round((Date.now() - this.recordStartTime) / 1000);
      }, 1000);
      try {
        const stream = await this.requestMicStream()
        // 启动波形可视化
        this.initVisualizer(stream);

        this.mediaRecorder = new MediaRecorder(stream);
        this.audioChunks = [];
        this.mediaRecorder.ondataavailable = (e) => {
          if (e.data.size > 0) this.audioChunks.push(e.data);
        };
        this.mediaRecorder.start();
      } catch (error) {
        console.error('麦克风调用失败:', error)
        this.isMobileRecording = false // 【修正】
        if (this.timerInterval) {
          clearInterval(this.timerInterval)
          this.timerInterval = null
        }
        this.showNotification(this.micErrorMessage(error), 'error')
      }
    },
    handleTouchMove(e) {
      if (!this.isMobileRecording) return;
      const touch = e.touches[0];
      const screenWidth = window.innerWidth;
      const screenHeight = window.innerHeight;
      // 1. 获取手指相对于屏幕的位置
      const clientX = touch.clientX;
      const clientY = touch.clientY;
      // 2. 设定触发阈值 (手指移动到屏幕中上部即视为触发)
      // 当手指离开底部输入区（即 Y 坐标小于屏幕高度的 80% 时）开始判断
      const isUp = clientY < screenHeight * 0.8;

      if (isUp) {
        if (clientX < screenWidth * 0.4) {
          // 手指在左侧 40% 区域 -> 取消
          this.recordStatus = 'cancel';
        } else if (clientX > screenWidth * 0.6) {
          // 手指在右侧 40% 区域 -> 转文字
          this.recordStatus = 'transcribe';
        } else {
          // 手指在中间
          this.recordStatus = 'normal';
        }
      } else {
        // 手指还在底部按钮附近
        this.recordStatus = 'normal';
      }
    },
    async handleTouchEnd() {
      if (!this.isMobileRecording) return;
      // 停止可视化
      this.stopVisualizer();
      this.isMobileRecording = false;
      clearInterval(this.timerInterval);
      if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
        this.mediaRecorder.stop();
        this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
        this.mediaRecorder.onstop = () => {
          const finalDuration = this.recordDuration;
          const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
          if (this.recordStatus === 'cancel') {
            console.log('录音已取消');
          } else if (this.recordStatus === 'transcribe') {
            // 传递录音文件进行转文字
            this.processAudioSTT(audioBlob);
          } else {
            // 发送语音
            if (finalDuration < 1) {
              this.showNotification('录音时间太短', 'warning')
            } else {
              // 【修复点】：确保这里传入的是刚刚获取的 finalDuration
              this.uploadAndSendVoice(audioBlob, finalDuration);
            }
          }
          // 重置状态
          this.audioChunks = [];
          this.recordDuration = 0; // 发送完后再清空
        };
      }
    },
    // --- 语音上传与发送 ---
    async uploadAndSendVoice(blob, duration) {
      const formData = new FormData()
      formData.append('file', blob, 'voice.webm')
      try {
        const res = await axios.post('/api/upload', formData) // 需后端配合
        const audioUrl = res.data.data
        const voiceMsg = {
          userId: this.userId,
          senderId: this.userId,
          senderName: '我',
          targetId: this.selectedContactId,
          targetName: this.currentContactName,
          content: '[语音消息]',
          type: 'VOICE', // 接收来自后端的 type 字段
          audioUrl: audioUrl, // 接收语音地址
          duration: duration, // 接收时长
          translatedContent: null,
          isTranslating: false,
        }
        this.ws.send(JSON.stringify(voiceMsg))
        this.messages.push(voiceMsg)
        this.scrollToBottom()
      } catch (e) {
        this.showNotification('语音发送失败', 'error')
        console.log(e)
      }
    },
    async processAudioSTT(blob) {
      const formData = new FormData()
      formData.append('file', blob, 'stt.wav')
      this.aiProcessing = true
      try {
        const res = await axios.post('/api/ai/audio/stt', formData)
        if (res.data.code === CODES.SUCCESS) {
          this.message = res.data.data
          this.showNotification('已转写为文字')
        }
      } catch (e) {
        this.showNotification('识别失败', 'error')
      } finally {
        this.aiProcessing = false
      }
    },
    playVoice(url) {
      if (this.isPlaying === url) return
      const audio = new Audio(url)
      this.isPlaying = url
      audio.play()
      audio.onended = () => {
        this.isPlaying = null
      }
    },
    showNotification(message, type = 'info') {
      this.notification.message = message
      this.notification.type = type
      this.notification.show = true
      if (this.notification.timer) {
        clearTimeout(this.notification.timer)
      }
      this.notification.timer = setTimeout(() => {
        this.notification.show = false
      }, 3000)
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const messagesEl = this.$el.querySelector('.chat-messages')
        if (messagesEl) messagesEl.scrollTop = messagesEl.scrollHeight
      })
    },
  },
  mounted() {
    this.userId = localStorage.getItem('userId')
    this.username = localStorage.getItem('username')
    this.nickname = localStorage.getItem('nickname')
    if (!this.username) {
      this.$router.push('/login')
      return
    }
    // 初始化检测屏幕大小
    this.checkScreenSize()
    window.addEventListener('resize', this.checkScreenSize)
    this.getContactList()
    this.getLanguages()
    document.addEventListener('keyup', this.handleGlobalKeyup)
    document.addEventListener('click', this.closeUserMenuOutside)
    if (this.userId) {
      this.ws = new WebSocket(`/ws/${this.userId}`)
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          const senderId = data.userId || data.senderId
          // 查找现有联系人
          let contactIndex = this.contacts.findIndex((c) => c.id == senderId)
          let senderName = '未知用户'
          if (contactIndex !== -1) {
            // --- 情况 A: 是熟人 ---
            senderName = this.contacts[contactIndex].nickname
            // 更新左侧列表预览
            this.contacts[contactIndex].lastMessage = data.content
            // 移到顶部
            const existingContact = this.contacts.splice(contactIndex, 1)[0]
            this.contacts.unshift(existingContact)
          } else if (senderId != this.userId) {
            // --- 情况 B: 是陌生人 ---
            senderName = `新朋友` // 暂时显示 ID，等待接口返回昵称
            this.handleStrangerMessage(senderId, data.content)
          }
          const message = buildRealtimeMessage(data, senderName)
          if (this.selectedContactId != senderId) {
            this.unreadCounts[senderId] = (this.unreadCounts[senderId] || 0) + 1
            this.showNotification(`收到来自 "${senderName}" 的新消息`)
          } else {
            this.messages.push(message)
            if (this.autoTranslate) {
              const canAutoVoice =
                message.type === 'VOICE'
                  ? !!(message.textContent && String(message.textContent).trim())
                  : true
              if (canAutoVoice) {
                this.translateSingleMessage(message)
              }
            }
            this.scrollToBottom()
          }
        } catch (e) {
          console.warn('WS error', e)
        }
      }
      this.ws.onclose = (event) => {
        console.log('WebSocket 连接已关闭:', event)
        // this.showNotification('与服务器连接已断开。', 'error')
      }
      this.ws.onerror = (error) => {
        console.error('WebSocket 发生错误:', error)
      }
    }
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkScreenSize)
    if (this.ws) this.ws.close()
    document.removeEventListener('keyup', this.handleGlobalKeyup)
    document.removeEventListener('click', this.closeUserMenuOutside)
  },
}
</script>

<style scoped>
.app-container {
  display: flex;
  gap: 12px;
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
  padding: 12px;
  color: var(--wc-text);
  background: var(--wc-bg-gradient);
  overflow: hidden;
  box-sizing: border-box;
}

.slide-side-enter-active,
.slide-side-leave-active,
.slide-fade-enter-active,
.slide-fade-leave-active,
.slide-up-enter-active,
.slide-up-leave-active,
.modal-fade-enter-active,
.modal-fade-leave-active,
.slide-fade-fast-enter-active,
.slide-fade-fast-leave-active {
  transition: all 0.25s ease;
}

.slide-side-enter-from,
.slide-side-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translate(-50%, -10px);
  opacity: 0;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(12px);
  opacity: 0;
}

.modal-fade-enter-from,
.modal-fade-leave-to,
.slide-fade-fast-enter-from,
.slide-fade-fast-leave-to {
  opacity: 0;
}

.contacts-sidebar {
  width: 280px;
  flex-shrink: 0;
  border: 1px solid var(--wc-glass-border);
  border-radius: var(--wc-radius-lg);
  background: var(--wc-glass-bg);
  box-shadow: var(--wc-shadow-1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid var(--wc-border-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.terminology-sidebar-btn {
  font-size: 12px;
  font-weight: 600;
  color: var(--wc-primary, #6366f1);
  padding: 4px 8px;
  border-radius: 8px;
  flex-shrink: 0;
}

.terminology-sidebar-btn:hover {
  background: color-mix(in srgb, var(--wc-primary, #6366f1) 12%, transparent);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
  flex: 1;
  min-width: 0;
}

.contacts-list {
  list-style: none;
  margin: 0;
  padding: 8px;
  overflow-y: auto;
  flex: 1;
}

.add-chat-item,
.contacts-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 12px;
  cursor: pointer;
  padding: 10px 12px;
  margin-bottom: 8px;
  transition: background 0.18s ease, transform 0.18s ease;
}

.add-chat-item:hover,
.contacts-list li:hover {
  background: rgba(255, 255, 255, 0.22);
}

.contacts-list li.active {
  background: rgba(79, 124, 255, 0.2);
  border: 1px solid rgba(79, 124, 255, 0.25);
}

.add-chat-icon,
.contact-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, var(--wc-primary) 0%, var(--wc-primary-strong) 100%);
}

.add-chat-text {
  font-size: 14px;
  font-weight: 600;
}

.contact-info {
  min-width: 0;
  flex: 1;
}

.contact-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.nickname {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.username,
.last-message {
  font-size: 12px;
  color: var(--wc-text-secondary);
}

.last-message {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff607f, #ff476b);
  color: #fff;
  font-size: 11px;
}

.chat-container {
  position: relative;
  min-width: 0;
  flex: 1;
  border: 1px solid var(--wc-glass-border);
  border-radius: var(--wc-radius-lg);
  background: var(--wc-glass-bg);
  box-shadow: var(--wc-shadow-1);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  min-height: 68px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--wc-border-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--wc-glass-bg-strong);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left {
  min-width: 0;
  flex: 1;
}

.chat-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toggle-sidebar-btn,
.icon-btn,
.summary-btn,
.analysis-btn {
  border: 1px solid var(--wc-border-soft);
  background: rgba(255, 255, 255, 0.22);
  color: var(--wc-text);
  border-radius: 10px;
  padding: 6px 10px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.icon-btn {
  padding: 6px;
  min-width: 36px;
  min-height: 34px;
}

.btn-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  display: block;
}

.toggle-sidebar-btn:hover,
.icon-btn:hover,
.summary-btn:hover,
.analysis-btn:hover {
  background: rgba(255, 255, 255, 0.36);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.translation-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--wc-border-soft);
  background: rgba(255, 255, 255, 0.2);
}

.switch-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--wc-text-secondary);
}

.lang-select {
  border: 1px solid var(--wc-border-soft);
  border-radius: 8px;
  padding: 4px 6px;
  background: rgba(255, 255, 255, 0.5);
  color: var(--wc-text);
  font-size: 12px;
}

.current-user-wrap {
  position: relative;
  flex-shrink: 0;
}

.current-user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0 2px 6px;
}

.user-avatar-trigger {
  padding: 0;
  border-radius: 50%;
  cursor: pointer;
  line-height: 0;
}

.user-avatar-trigger:focus-visible {
  outline: 2px solid var(--wc-primary);
  outline-offset: 2px;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  z-index: 220;
  min-width: 148px;
  padding: 6px;
  border-radius: 12px;
  border: 1px solid var(--wc-border-soft);
  background: var(--wc-glass-bg-strong);
  box-shadow: var(--wc-shadow-2);
  backdrop-filter: blur(10px);
}

.user-dropdown-item {
  display: block;
  width: 100%;
  margin: 0;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  text-align: left;
  color: var(--wc-text);
  cursor: pointer;
  transition: background 0.15s ease;
}

.user-dropdown-item:hover {
  background: rgba(79, 124, 255, 0.12);
}

.user-dropdown-item-warn {
  color: #b42318;
}

.user-dropdown-item-warn:hover {
  background: rgba(180, 35, 24, 0.08);
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.current-user-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.current-user-nickname {
  font-size: 13px;
  font-weight: 600;
  text-align: center;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.2;
  color: var(--wc-text);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
}

.empty-chat-hint {
  color: var(--wc-muted);
  text-align: center;
  padding-top: 24%;
  font-size: 14px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.message-block {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
}

.message-time-divider-wrap {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 8px 0 6px;
}

.message-time-divider {
  font-size: 12px;
  line-height: 1.35;
  color: var(--wc-muted);
  padding: 3px 11px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.06);
  text-align: center;
}

.message-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 10px;
  max-width: min(90%, calc(100% - 12px));
  width: fit-content;
}

.message-row.self-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-row.other-message {
  align-self: flex-start;
}

.message-peer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  background: linear-gradient(135deg, var(--wc-primary) 0%, var(--wc-primary-strong) 100%);
}

.message-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
  max-width: min(72vw, 520px);
}

.message-bubble {
  width: fit-content;
  max-width: 100%;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid var(--wc-border-soft);
  word-break: break-word;
  box-shadow: var(--wc-shadow-2);
}

.self-message .message-bubble {
  background: linear-gradient(135deg, var(--wc-primary), var(--wc-primary-strong));
  color: #fff;
}

.other-message .message-bubble {
  background: #fff;
  color: var(--wc-text);
  border-color: rgba(0, 0, 0, 0.08);
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
}

.translation-content {
  margin-top: 8px;
  font-size: 13px;
}

.translation-line {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 6px;
}

.divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.32);
  margin: 6px 0;
}

.other-message .divider {
  background: rgba(0, 0, 0, 0.12);
}

.trans-icon {
  margin-right: 4px;
}

.manual-trans-btn,
.voice-to-text-btn,
.clear-trans-btn,
.clear-text-btn,
.copy-btn,
.apply-btn,
.cancel-btn {
  border: 1px solid var(--wc-border-soft);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.22);
  color: inherit;
  cursor: pointer;
  font-size: 12px;
  padding: 3px 8px;
}

.message-voice-column {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  max-width: 280px;
}

/* 语音条以下（转写结果、翻译等）略松一点，避免挤在一起 */
.message-voice-column .voice-text-content,
.message-voice-column .voice-translation {
  margin-top: 6px;
}

.message-row.self-message .message-voice-column {
  align-items: flex-end;
}

.voice-player {
  display: flex;
  align-items: center;
  gap: 6px;
  box-sizing: border-box;
  padding: 8px 12px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  box-shadow: var(--wc-shadow-2);
  background: linear-gradient(135deg, var(--wc-primary), var(--wc-primary-strong));
  color: #fff;
}

.message-row.self-message .voice-player {
  flex-direction: row-reverse;
}

.voice-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  color: inherit;
  opacity: 0.95;
}

.message-row.self-message .voice-icon {
  transform: scaleX(-1);
}

.voice-mic-svg {
  width: 16px;
  height: 16px;
  display: block;
}

.voice-duration {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  opacity: 0.98;
  letter-spacing: 0.02em;
}

.voice-translation {
  width: 100%;
  max-width: 100%;
}

.translating-spinner,
.transcribing-spinner {
  margin-top: 6px;
  color: var(--wc-muted);
  font-size: 12px;
}

.chat-input-wrapper {
  border-top: 1px solid var(--wc-border-soft);
  background: var(--wc-glass-bg-strong);
}

.ai-suggestion-box {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--wc-border-soft);
  background: rgba(79, 124, 255, 0.12);
}

.suggestion-text {
  flex: 1;
  font-size: 13px;
}

.suggestion-actions {
  display: flex;
  gap: 6px;
}

.ai-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px 0;
  overflow-x: auto;
}

.ai-tool-btn,
.mic-trigger-btn {
  border: 1px solid var(--wc-border-soft);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  color: var(--wc-text);
  padding: 6px 10px;
  cursor: pointer;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.mic-trigger-btn {
  padding: 6px;
  min-width: 36px;
  min-height: 34px;
  justify-content: center;
}

.chat-input-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
}

.emoji-container {
  position: relative;
}

.emoji-toggle-btn {
  border: 1px solid var(--wc-border-soft);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  color: var(--wc-text);
  font-size: 20px;
  line-height: 1;
  padding: 7px 10px;
  cursor: pointer;
}

.emoji-picker-wrapper {
  position: absolute;
  left: 0;
  bottom: calc(100% + 10px);
  z-index: 30;
}

.message-input {
  flex: 1;
  min-width: 0;
  padding: 11px 12px;
  border-radius: 12px;
  border: 1px solid var(--wc-border-soft);
  background: rgba(255, 255, 255, 0.5);
  color: var(--wc-text);
  outline: none;
}

.message-input:focus {
  border-color: var(--wc-primary);
  box-shadow: 0 0 0 3px rgba(79, 124, 255, 0.2);
}

.send-button {
  border: 0;
  border-radius: 12px;
  padding: 10px 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--wc-primary), var(--wc-primary-strong));
  cursor: pointer;
}

.chat-notification {
  position: absolute;
  left: 50%;
  top: 80px;
  transform: translateX(-50%);
  min-width: 220px;
  border-radius: 10px;
  padding: 10px 14px;
  border: 1px solid var(--wc-border-soft);
  background: var(--wc-glass-bg-strong);
  box-shadow: var(--wc-shadow-2);
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.chat-notification.error {
  border-left: 3px solid var(--wc-danger);
}

.chat-notification.info {
  border-left: 3px solid var(--wc-success);
}

.chat-summary-modal-overlay,
.analysis-modal-overlay {
  position: absolute;
  inset: 0;
  z-index: 2000;
  background: rgba(8, 12, 24, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-summary-modal,
.analysis-modal {
  width: 90%;
  max-width: 880px;
  border-radius: var(--wc-radius-lg);
  border: 1px solid var(--wc-border-soft);
  background: var(--wc-glass-bg-strong);
  box-shadow: var(--wc-shadow-1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 82vh;
}

.modal-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--wc-border-soft);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  border: 1px solid var(--wc-border-soft);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: var(--wc-text);
  font-size: 22px;
  line-height: 1;
  padding: 2px 10px;
  cursor: pointer;
}

.modal-content {
  padding: 16px;
  overflow-y: auto;
}

.modal-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--wc-border-soft);
  text-align: right;
}

.analysis-loading {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--wc-text-secondary);
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(255, 255, 255, 0.35);
  border-top-color: var(--wc-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.dashboard-card {
  min-height: 260px;
  border: 1px solid var(--wc-border-soft);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.22);
  padding: 12px;
}

.summary-card {
  grid-column: 1 / -1;
  min-height: 96px;
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.pc-recording-overlay,
.recording-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(8, 12, 24, 0.5);
  backdrop-filter: blur(4px);
}

.pc-record-modal,
.recording-card {
  border: 1px solid var(--wc-border-soft);
  border-radius: 16px;
  background: var(--wc-glass-bg-strong);
  box-shadow: var(--wc-shadow-1);
  padding: 20px;
}

.pc-record-modal {
  width: min(92vw, 430px);
}

.modal-title {
  text-align: center;
  font-weight: 700;
  margin-bottom: 8px;
}

.record-timer {
  text-align: center;
  margin: 8px 0 12px;
  font-size: 22px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: var(--wc-primary);
}

.pc-record-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  border: 1px solid var(--wc-border-soft);
  background: rgba(255, 255, 255, 0.2);
  color: var(--wc-text);
  border-radius: 10px;
  cursor: pointer;
  padding: 10px 8px;
}

.recording-card {
  width: 190px;
  min-height: 170px;
}

.status-icon {
  font-size: 46px;
  text-align: center;
}

.status-hint {
  margin-top: 6px;
  text-align: center;
  color: var(--wc-text-secondary);
  font-size: 13px;
}

.status-hint.warning {
  color: var(--wc-warning);
}

.record-zones {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 140px;
  display: flex;
  justify-content: space-around;
  pointer-events: none;
}

.zone {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.zone.left.active {
  background: #ea5776;
}

.zone.right.active {
  background: #4b89ff;
}

.pulse {
  animation: pulse 1.4s ease infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
}

.visualizer-container {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 8px;
}

.visualizer-bar {
  width: 5px;
  min-height: 6px;
  background: linear-gradient(180deg, var(--wc-primary), var(--wc-success));
  border-radius: 3px;
  transition: height 0.1s ease;
}

@media (max-width: 960px) {
  .mobile-hide-text .btn-text {
    display: none;
  }

  .translation-controls {
    display: none;
  }

  .contacts-sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .app-container {
    padding: 0;
    gap: 0;
  }

  .contacts-sidebar {
    position: absolute;
    inset: 0;
    width: 100%;
    border-radius: 0;
    z-index: 20;
  }

  .chat-container {
    border-radius: 0;
    border: 0;
  }

  .chat-header {
    min-height: 60px;
    padding: 8px 10px;
  }

  .chat-title {
    font-size: 15px;
    max-width: 125px;
  }

  .chat-messages {
    padding: 10px;
  }

  .chat-input-area {
    padding: 8px 10px;
    gap: 6px;
  }

  .send-button {
    padding: 10px 12px;
  }

  .message-item {
    max-width: 88%;
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .summary-btn,
  .analysis-btn,
  .icon-btn,
  .toggle-sidebar-btn {
    padding: 5px 8px;
    font-size: 12px;
  }
}
</style>
