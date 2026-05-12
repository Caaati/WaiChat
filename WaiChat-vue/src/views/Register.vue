<template>
  <div class="register-container wc-page">
    <div class="register-box wc-glass-card">
      <div class="brand-mark">WaiChat</div>
      <h2>{{ $t('register.title') }}</h2>
      <p class="subtitle">{{ $t('register.subtitle') }}</p>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>{{ $t('register.username') }}</label>
          <input class="wc-input" type="text" v-model="username" required>
        </div>
        <div class="form-group">
          <label>{{ $t('register.password') }}</label>
          <input class="wc-input" type="password" v-model="password" required>
        </div>
        <div class="form-group">
          <label>{{ $t('register.confirmPassword') }}</label>
          <input class="wc-input" type="password" v-model="confirmPassword" required>
        </div>
        <button type="submit" class="register-btn wc-btn wc-btn-primary">{{ $t('register.submit') }}</button>
        <p class="login-link">{{ $t('register.hasAccount') }}<router-link to="/login">{{ $t('register.toLogin') }}</router-link></p>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import {CODES} from "@/constants/codes.js";

export default {
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: ''
    };
  },
  methods: {
    async handleRegister() {
      if (this.password !== this.confirmPassword) {
        alert(this.$t('register.passwordMismatch'));
        return;
      }

      try {
        const res = await axios.post('/api/register', {
          username: this.username,
          password: this.password
        });
        if (res.data.code === CODES.SUCCESS) {
          alert(this.$t('register.success'));
          this.$router.push('/login')
        } else {
          alert(this.$t('register.failPrefix') + res.data.msg);
        }
      } catch (err) {
        console.error('注册请求失败：', err);
        alert(this.$t('register.failRetry'));
      }
    }
  }
};
</script>

<style scoped>
.register-container {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  overflow: auto;
  box-sizing: border-box;
}

.register-box {
  width: min(92vw, 420px);
  padding: 30px 26px;
  box-sizing: border-box;
  max-height: 90vh;
  overflow-y: auto;
}

.brand-mark {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #fff;
  background: linear-gradient(135deg, var(--wc-primary), var(--wc-primary-strong));
  margin-bottom: 14px;
}

h2 {
  margin-bottom: 6px;
  font-size: 28px;
  font-weight: 700;
}

.subtitle {
  margin-bottom: 22px;
  font-size: 14px;
  color: var(--wc-text-secondary);
}

.form-group {
  margin-bottom: 14px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--wc-text-secondary);
  font-weight: 600;
}

.register-btn {
  width: 100%;
  margin-top: 8px;
}

.login-link {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: var(--wc-text-secondary);
}

.login-link a {
  color: var(--wc-primary);
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
