<template>
  <div class="profile-page wc-page">
    <div class="profile-card wc-glass-card">
      <header class="profile-head">
        <button type="button" class="wc-btn wc-btn-ghost back-btn" @click="goChat">{{ $t('profile.backChat') }}</button>
        <h1>{{ $t('profile.title') }}</h1>
        <p class="sub">{{ $t('profile.sub') }}</p>
        <p class="profile-id-inline mono">{{ $t('profile.idLabel') }}{{ userId || '—' }}</p>
      </header>

      <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
      <p v-if="formOk" class="form-ok" role="status">{{ formOk }}</p>

      <form class="profile-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="pf-nick">{{ $t('profile.nickname') }}</label>
          <input id="pf-nick" v-model.trim="formNickname" class="wc-input" type="text" autocomplete="nickname" required />
        </div>
        <div class="form-group">
          <label for="pf-user">{{ $t('profile.username') }}</label>
          <input id="pf-user" v-model.trim="formUsername" class="wc-input" type="text" autocomplete="username" required />
        </div>

        <div class="pwd-actions">
          <button
            v-if="!showPasswordFields"
            type="button"
            class="wc-btn wc-btn-ghost pwd-toggle-btn"
            @click="showPasswordFields = true"
          >
            {{ $t('profile.changePassword') }}
          </button>
          <template v-else>
            <div class="form-group">
              <label for="pf-new">{{ $t('profile.newPassword') }}</label>
              <input
                id="pf-new"
                v-model="newPassword"
                class="wc-input"
                type="password"
                autocomplete="new-password"
                :placeholder="$t('profile.placeholderNewPwd')"
              />
            </div>
            <div class="form-group">
              <label for="pf-new2">{{ $t('profile.confirmNewPassword') }}</label>
              <input
                id="pf-new2"
                v-model="confirmPassword"
                class="wc-input"
                type="password"
                autocomplete="new-password"
                :placeholder="$t('profile.placeholderConfirmPwd')"
              />
            </div>
            <button type="button" class="wc-btn wc-btn-ghost pwd-collapse-btn" @click="cancelPasswordChange">
              {{ $t('profile.collapse') }}
            </button>
          </template>
        </div>

        <button type="submit" class="wc-btn wc-btn-primary save-btn" :disabled="saving">
          {{ saving ? $t('profile.saving') : $t('profile.save') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { CODES } from '@/constants/codes.js'

const jsonCreds = {
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
}

function apiMessage(vm, res) {
  const d = res && res.data
  if (!d) return vm.$t('profile.requestFail')
  return d.message || d.msg || vm.$t('profile.requestFail')
}

export default {
  name: 'ProfileView',
  data() {
    return {
      userId: '',
      originalUsername: '',
      formNickname: '',
      formUsername: '',
      showPasswordFields: false,
      newPassword: '',
      confirmPassword: '',
      formError: '',
      formOk: '',
      saving: false,
    }
  },
  mounted() {
    this.userId = localStorage.getItem('userId') || ''
    const u = localStorage.getItem('username') || ''
    if (!u) {
      this.$router.replace('/login')
      return
    }
    this.originalUsername = u
    this.formUsername = u
    this.formNickname = localStorage.getItem('nickname') || u
  },
  methods: {
    goChat() {
      this.$router.push('/chat')
    },
    cancelPasswordChange() {
      this.showPasswordFields = false
      this.newPassword = ''
      this.confirmPassword = ''
    },
    validateClient() {
      if (!this.formNickname) return this.$t('profile.errNickname')
      if (!this.formUsername) return this.$t('profile.errUsername')
      if (!this.showPasswordFields) return ''
      const np = this.newPassword || ''
      const cp = this.confirmPassword || ''
      const hasAny = np.length > 0 || cp.length > 0
      if (!hasAny) return ''
      if (!np || !cp) return this.$t('profile.errPwdBoth')
      if (np !== cp) return this.$t('profile.errPwdMismatch')
      if (np.length < 6) return this.$t('profile.errPwdLen')
      return ''
    },
    clearMessages() {
      this.formError = ''
      this.formOk = ''
    },
    async handleSubmit() {
      this.clearMessages()
      const err = this.validateClient()
      if (err) {
        this.formError = err
        return
      }
      const np = this.newPassword || ''
      const cp = this.confirmPassword || ''
      const passwordChanging = this.showPasswordFields && np.length > 0
      this.saving = true
      try {
        const body = {
          nickname: this.formNickname,
          username: this.formUsername,
        }
        if (passwordChanging) {
          body.newPassword = np
          body.confirmPassword = cp
        }
        const res = await axios.put('/api/user/profile', body, jsonCreds)
        if (res.data.code !== CODES.SUCCESS) {
          this.formError = apiMessage(this, res)
          return
        }
        const needRelogin = !!(res.data.data && res.data.data.needRelogin)
        if (needRelogin) {
          localStorage.clear()
          alert(this.$t('profile.reloginAlert'))
          this.$router.replace('/login')
          return
        }
        localStorage.setItem('nickname', this.formNickname)
        localStorage.setItem('username', this.formUsername)
        this.originalUsername = this.formUsername
        this.cancelPasswordChange()
        this.formOk = this.$t('profile.saved')
        setTimeout(() => {
          this.formOk = ''
        }, 2500)
      } catch (e) {
        const msg = e.response ? apiMessage(this, e.response) : this.$t('profile.networkErr')
        this.formError = msg
      } finally {
        this.saving = false
      }
    },
  },
}
</script>

<style scoped>
.profile-page {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 24px 16px 48px;
  overflow: auto;
  box-sizing: border-box;
}

.profile-card {
  width: min(92vw, 440px);
  padding: 22px 22px 28px;
}

.profile-head {
  margin-bottom: 18px;
}

.profile-head h1 {
  margin: 12px 0 6px;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--wc-text);
}

.sub {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.45;
  color: var(--wc-muted);
}

.profile-id-inline {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--wc-text-secondary);
  letter-spacing: 0.02em;
}

.back-btn {
  margin-bottom: 4px;
}

.profile-form {
  margin-top: 8px;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--wc-text-secondary);
}

.mono {
  font-family: ui-monospace, monospace;
}

.pwd-actions {
  margin: 18px 0 4px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

.pwd-toggle-btn,
.pwd-collapse-btn {
  align-self: flex-start;
}

.save-btn {
  width: 100%;
  margin-top: 16px;
}

.form-error {
  margin: 0 0 12px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: #b42318;
  background: rgba(180, 35, 24, 0.08);
  border: 1px solid rgba(180, 35, 24, 0.2);
}

.form-ok {
  margin: 0 0 12px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: var(--wc-text);
  background: rgba(79, 124, 255, 0.12);
  border: 1px solid rgba(79, 124, 255, 0.22);
}
</style>
