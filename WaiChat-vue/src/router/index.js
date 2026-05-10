import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import Chat from '@/views/Chat.vue';
import Terminology from '@/views/Terminology.vue';
import Profile from '@/views/Profile.vue';

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

export default router;
