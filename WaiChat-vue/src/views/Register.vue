<template>
  <div class="register-container wc-page">
    <div class="register-box wc-glass-card">
      <div class="brand-mark">WaiChat</div>
      <h2>创建账号</h2>
      <p class="subtitle">填写信息后即可开始聊天</p>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input class="wc-input" type="text" v-model="username" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input class="wc-input" type="password" v-model="password" required>
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input class="wc-input" type="password" v-model="confirmPassword" required>
        </div>
        <button type="submit" class="register-btn wc-btn wc-btn-primary">注册</button>
        <p class="login-link">已有账号？<a href="/login">去登录</a></p>
      </form>
    </div>
  </div>
</template>

<script>
// ... (Script 保持不变)
import axios from 'axios';
import { useRouter } from 'vue-router';
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
        alert('两次输入的密码不一致！');
        return;
      }

      try {
        const res = await axios.post('/api/register', {
          username: this.username,
          password: this.password
        });
        if (res.data.code === CODES.SUCCESS) {
          alert('注册成功！请登录');
          this.$router.push('/login')
        } else {
          alert('注册失败：' + res.data.msg);
        }
      } catch (err) {
        console.error('注册请求失败：', err);
        alert('注册失败，请稍后重试');
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
  margin-top: 10px;
}

.login-link {
  margin-top: 14px;
  text-align: center;
  font-size: 13px;
  color: var(--wc-text-secondary);
}

.login-link a {
  color: var(--wc-primary);
  text-decoration: none;
  font-weight: 700;
}

@media (max-width: 480px) {
  .register-box {
    padding: 24px 18px;
  }

  h2 {
    font-size: 24px;
  }
}
</style>
