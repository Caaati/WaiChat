<template>
  <div class="register-container">
    <div class="register-box">
      <h2>WaiChat 注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="username" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" required>
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input type="password" v-model="confirmPassword" required>
        </div>
        <button type="submit" class="register-btn">注册</button>
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
  /* 核心：强制占满屏幕，禁止滚动 */
  height: 100vh;
  width: 100vw;
  overflow: hidden;

  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;

  /* 固定定位确保稳固 */
  position: fixed;
  top: 0;
  left: 0;
}

.register-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);

  /* 响应式宽度适配 */
  width: 90%;
  max-width: 360px;
  box-sizing: border-box;

  /* 如果手机横屏高度不足，允许盒子内部轻微滚动，但不影响整体页面 */
  max-height: 90vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.3rem;
  color: #333;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 16px;
  outline: none;
}

input:focus {
  border-color: #42b983;
}

.register-btn {
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

.register-btn:active {
  background-color: #36a47e;
}

.login-link {
  margin-top: 1rem;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #2196f3;
  text-decoration: none;
  font-weight: 500;
}
</style>
