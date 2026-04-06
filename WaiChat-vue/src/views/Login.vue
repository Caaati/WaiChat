<template>
  <div class="login-container">
    <div class="login-box">
      <h2>WaiChat登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="username" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" required>
        </div>
        <button type="submit" class="login-btn">登录</button>
        <button type="button" class="register-btn" @click="toRegister">注册</button>
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
          alert(res.data.msg || '登录失败');
        }
      } catch (err) {
        console.error(err);
        alert('网络错误或服务器异常');
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
  /* 核心：强制占满屏幕，禁止滚动 */
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  /* 防止手机浏览器地址栏导致的抖动 */
  position: fixed;
  top: 0;
  left: 0;
}

.login-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  width: 90%;
  max-width: 400px;
  box-sizing: border-box;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 10px;
  margin-top: 4px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box; /* 确保 padding 不撑大宽度 */
  font-size: 16px; /* 防止 iOS 输入框自动放大 */
  outline: none;
}

input:focus {
  border-color: #42b983;
}

.login-btn, .register-btn {
  width: 100%;
  padding: 10px;
  margin-top: 1rem;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
}

.register-btn {
  background: #2196f3;
  margin-top: 10px;
}

.login-btn:active, .register-btn:active {
  opacity: 0.9;
}
</style>
