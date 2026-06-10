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

export interface ProfileInfo {
  id: string
  username: string
  realName: string
  roles: string[]
  studentNo: string | null
  classId: string | null
  className: string | null
  phone: string | null
  email: string | null
  status: number
}

export interface LoginResponse {
  token: string
  tokenType: string
  userInfo: LoginUserInfo
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface ProfileUpdateRequest {
  phone?: string
  email?: string
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

export const changePasswordApi = (data: ChangePasswordRequest) => {
  return request.put<any, { code: number; message: string; data: boolean }>('/api/auth/password', data)
}

export const getProfileApi = () => {
  return request.get<any, { code: number; message: string; data: ProfileInfo }>('/api/auth/profile')
}

export const updateProfileApi = (data: ProfileUpdateRequest) => {
  return request.put<any, { code: number; message: string; data: ProfileInfo }>('/api/auth/profile', data)
}
