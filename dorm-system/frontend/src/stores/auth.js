import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin:   (s) => s.user?.role === 'ADMIN',
    isManager: (s) => ['ADMIN', 'MANAGER'].includes(s.user?.role),
    isStudent: (s) => s.user?.role === 'STUDENT',
  },
  actions: {
    async login(username, password) {
      const res = await authApi.login({ username, password })
      this.token = res.data.token
      this.user  = res.data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = null
      this.user  = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
  },
})
