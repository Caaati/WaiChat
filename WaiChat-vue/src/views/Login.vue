<template>
  <div class="login-container wc-page">
    <div class="login-box wc-glass-card">
      <div class="brand-mark">WaiChat</div>
      <h2>{{ $t('login.title') }}</h2>
      <p class="subtitle">{{ $t('login.subtitle') }}</p>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>{{ $t('login.username') }}</label>
          <input class="wc-input" type="text" v-model="username" required>
        </div>
        <div class="form-group">
          <label>{{ $t('login.password') }}</label>
          <input class="wc-input" type="password" v-model="password" required>
        </div>
        <button type="submit" class="login-btn wc-btn wc-btn-primary">{{ $t('login.submit') }}</button>
        <button type="button" class="register-btn wc-btn wc-btn-ghost" @click="toRegister">{{ $t('login.toRegister') }}</button>
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
      password: ''
    };
  },
  methods: {
    async handleLogin() {
      try {
        const formData = new FormData();
        formData.append('username', this.username);
        formData.append('password', this.password);

        const res = await axios.post('/api/login', formData);

        if (res.data.code === CODES.SUCCESS) {
          if (res.data.data && res.data.data.id) {
            localStorage.setItem('userId', res.data.data.id);
            localStorage.setItem('username', res.data.data.username);
            localStorage.setItem('nickname', res.data.data.nickname);
          }
          this.$router.push('/chat');
        } else {
          alert(res.data.msg || this.$t('login.fail'));
        }
      } catch (err) {
        console.error(err);
        alert(this.$t('login.networkError'));
      }
    },
    toRegister() {
      this.$router.push('/register');
    }
  }
};
</script>

<style scoped>
.login-container {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  overflow: auto;
  box-sizing: border-box;
}

.login-box {
  width: min(92vw, 420px);
  padding: 30px 26px;
  box-sizing: border-box;
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

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.register-btn {
  width: 100%;
  margin-top: 10px;
}
</style>
