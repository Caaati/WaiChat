import { createApp } from 'vue';
import './assets/main.css';
import App from './App.vue';
import router from './router';
import axios from './utils/axios';
import i18n from './i18n';
import 'vue3-emoji-picker/css';
import './assets/chat-reusable.css';
import './assets/theme-tokens.css';
import './assets/ui-kit.css';

const app = createApp(App);
app.use(i18n);
app.use(router);
app.config.globalProperties.$axios = axios; // 全局注册 Axios
app.mount('#app');
