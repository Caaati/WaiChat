import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import Chat from '@/views/Chat.vue';
import Terminology from '@/views/Terminology.vue';
import Profile from '@/views/Profile.vue';
import i18n from '@/i18n';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/chat', component: Chat },
  { path: '/terminology', component: Terminology },
  { path: '/profile', component: Profile }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

const TITLE_KEYS = {
  '/login': 'docTitle.login',
  '/register': 'docTitle.register',
  '/chat': 'docTitle.chat',
  '/terminology': 'docTitle.terminology',
  '/profile': 'docTitle.profile',
};

router.afterEach((to) => {
  const key = TITLE_KEYS[to.path];
  const g = i18n.global;
  const suffix = g.t('docTitle.suffix');
  if (key) {
    document.title = `${g.t(key)} · ${suffix}`;
  } else {
    document.title = suffix;
  }
});

export default router;
