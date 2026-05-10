import { createApp } from 'vue';
import './assets/main.css';
import App from './App.vue';
import router from './router';
import axios from './utils/axios';
import 'vue3-emoji-picker/css';
import './assets/chat-reusable.css';
import './assets/theme-tokens.css';
import './assets/ui-kit.css';

const app = createApp(App);
app.use(router);
app.config.globalProperties.$axios = axios; // 全局注册 Axios
app.mount('#app');
