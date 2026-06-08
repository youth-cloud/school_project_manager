import request from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginUserInfo {
  id: number
  username: string
  realName: string
  roles: string[]
}

export interface LoginResponse {
  token: string
  tokenType: string
  userInfo: LoginUserInfo
}

export const loginApi = (data: LoginRequest) => {
  return request.post<any, { code: number; message: string; data: LoginResponse }>('/api/auth/login', data)
}

export const getCurrentUserApi = () => {
  return request.get<any, { code: number; message: string; data: LoginUserInfo }>('/api/auth/me')
}

export const logoutApi = () => {
  return request.post<any, { code: number; message: string; data: boolean }>('/api/auth/logout')
}