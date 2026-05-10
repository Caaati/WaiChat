<template>
  <div v-if="isVisible" class="modal-overlay">
    <div class="modal wc-glass-card">
      <div class="modal-header">
        <h3>查找用户</h3>
        <button class="close-btn" @click="closeModal" aria-label="关闭">&times;</button>
      </div>
      <div class="modal-body">
        <input
            type="text"
            v-model="searchQuery"
            @keyup.enter="fetchSearchResults(searchQuery)"
            placeholder="输入用户名、昵称或ID，按Enter搜索"
            class="search-input wc-input"
            ref="searchInput"
        />
        <div class="search-results">
          <div v-if="searching">搜索中...</div>
          <div v-else-if="searchResults.length === 0">
            未找到匹配的用户
          </div>
          <div
              v-for="user in searchResults"
              :key="user.id"
              class="search-result-item"
              @click="selectUser(user)"
          >
            <div class="contact-avatar">
              <span>{{ user.nickname.charAt(0) }}</span>
            </div>
            <div class="contact-info">
              <div class="contact-name">
                <span class="nickname">{{ user.nickname }}</span>
                <span class="username">@{{ user.username }}</span>
              </div>
              <div class="user-id">ID: {{ user.id }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import {CODES} from "@/constants/codes.js";

export default {
  name: "AddContactModal",
  props: {
    isVisible: {
      type: Boolean,
      required: true,
      default: false
    },
    currentUserId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      searchQuery: '',
      searchResults: [],
      searching: false
    };
  },
  watch: {
    isVisible(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.$refs.searchInput?.focus();
        });
      } else {
        this.searchQuery = '';
        this.searchResults = [];
      }
    }
  },
  methods: {
    async fetchSearchResults(query) {
      if (!query.trim()) {
        this.searchResults = [];
        return;
      }

      this.searching = true;
      try {
        const response = await axios.get('/api/user/search', {
          params: { key: query }
        });

        if (response.data.code === CODES.SUCCESS) {
          this.searchResults = response.data.data.filter(
              user => user.id !== this.currentUserId
          );
        } else {
          this.searchResults = [];
          // this.$emit('search-error', response.data.msg || '搜索失败');
        }
      } catch (error) {
        console.error('搜索用户时出错:', error);
        this.searchResults = [];
        // this.$emit('search-error', '网络错误，搜索失败');
      } finally {
        this.searching = false;
      }
    },

    selectUser(user) {
      this.$emit('user-selected', user);
      this.closeModal();
    },

    closeModal() {
      this.$emit('close');
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(8, 12, 24, 0.46);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
}

.modal {
  width: 90%;
  max-width: 450px;
  border-radius: var(--wc-radius-lg);
  border: 1px solid var(--wc-border-soft);
  overflow: hidden;
}

.modal-header {
  padding: 16px 18px;
  border-bottom: 1px solid var(--wc-border-soft);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--wc-text);
}

.close-btn {
  border: 1px solid var(--wc-border-soft);
  border-radius: 8px;
  padding: 2px 10px;
  font-size: 22px;
  cursor: pointer;
  color: var(--wc-text-secondary);
  background: rgba(255, 255, 255, 0.2);
  transition: background 0.18s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.34);
}

.modal-body {
  padding: 16px 18px 18px;
}

.search-input {
  font-size: 14px;
  margin-bottom: 16px;
}

.search-results {
  max-height: 320px;
  overflow-y: auto;
}

.search-result-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: background-color 0.18s ease;
  border-radius: 12px;
  border: 1px solid transparent;
  margin-bottom: 8px;
}

.search-result-item:hover {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: var(--wc-border-soft);
}

.contact-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--wc-primary), var(--wc-primary-strong));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.contact-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  min-width: 0;
}

.contact-name {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--wc-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.username, .user-id {
  font-size: 12px;
  color: var(--wc-text-secondary);
  white-space: nowrap;
}
</style>
