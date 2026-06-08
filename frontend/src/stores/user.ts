import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { getCurrentUserApi, loginApi, logoutApi, type LoginRequest, type LoginUserInfo } from '@/api/auth'
import { getToken, removeToken, setToken } from '@/utils/token'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<LoginUserInfo | null>(null)

  const isLogin = computed(() => !!token.value)

  const loginAction = async (payload: LoginRequest) => {
    const res = await loginApi(payload)

    token.value = res.data.token
    setToken(res.data.token)
    userInfo.value = res.data.userInfo
  }

  const fetchCurrentUser = async () => {
    const res = await getCurrentUserApi()
    userInfo.value = res.data
    return res.data
  }

  const logoutAction = async () => {
    try {
      await logoutApi()
    } finally {
      clearLoginState()
    }
  }

  const clearLoginState = () => {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    isLogin,
    loginAction,
    fetchCurrentUser,
    logoutAction,
    clearLoginState,
  }
})