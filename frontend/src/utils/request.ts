import axios from 'axios'
import { ElMessage } from 'element-plus'

import { getToken, removeToken } from '@/utils/token'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = getToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === 200) {
      return res
    }

    ElMessage.error(res.message || '请求失败')

    if (res.code === 401) {
      removeToken()
    }

    return Promise.reject(res)
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  },
)

export default request