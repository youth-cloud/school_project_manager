import request from '@/utils/request'

export interface UserOptionItem {
  id: string
  username: string
  realName: string
}

export interface RoleOptionItem {
  id: string
  roleCode: string
  roleName: string
}

export interface SysUserItem {
  id: string
  username: string
  realName: string
  studentNo?: string | null
  classId?: string | null
  className?: string | null
  status: number
  createTime: string | null
  updateTime: string | null
  roleCodes: string[]
  roleNames: string[]
}

export interface SysUserPageQuery {
  current: number
  size: number
  username?: string
  realName?: string
  roleCode?: string
  status?: number
}

export interface SysUserFormData {
  id?: string
  username: string
  realName: string
  studentNo: string
  classId: string
  password: string
  confirmPassword: string
  status: number
  roleCodes: string[]
}

export interface SysUserStatusFormData {
  status: number
}

export interface AdminResetPasswordFormData {
  userId: string
  newPassword: string
  confirmPassword: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getTeacherOptionsApi = () => {
  return request.get<any, { code: number; message: string; data: UserOptionItem[] }>('/api/sys-users/teacher-options')
}

export const getStudentOptionsApi = () => {
  return request.get<any, { code: number; message: string; data: UserOptionItem[] }>('/api/sys-users/student-options')
}

export const getUserOptionsApi = () => {
  return request.get<any, { code: number; message: string; data: UserOptionItem[] }>('/api/sys-users/user-options')
}

export const getRoleOptionsApi = () => {
  return request.get<any, { code: number; message: string; data: RoleOptionItem[] }>('/api/sys-users/role-options')
}

export const getSysUserPageApi = (params: SysUserPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<SysUserItem> }>('/api/sys-users/page', {
    params,
  })
}

export const getSysUserDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: SysUserItem }>(`/api/sys-users/${id}`)
}

export const createSysUserApi = (data: SysUserFormData) => {
  return request.post<any, { code: number; message: string; data: SysUserItem }>('/api/sys-users', {
    username: data.username,
    realName: data.realName,
    studentNo: data.studentNo || undefined,
    classId: data.classId || undefined,
    password: data.password,
    status: data.status,
    roleCodes: data.roleCodes,
  })
}

export const updateSysUserApi = (data: SysUserFormData) => {
  return request.put<any, { code: number; message: string; data: SysUserItem }>('/api/sys-users', {
    id: data.id,
    username: data.username,
    realName: data.realName,
    studentNo: data.studentNo || undefined,
    classId: data.classId || undefined,
    status: data.status,
    roleCodes: data.roleCodes,
  })
}

export const updateSysUserStatusApi = (id: string, data: SysUserStatusFormData) => {
  return request.put<any, { code: number; message: string; data: SysUserItem }>(`/api/sys-users/${id}/status`, data)
}

export const adminResetPasswordApi = (data: AdminResetPasswordFormData) => {
  return request.put<any, { code: number; message: string; data: boolean }>('/api/auth/admin/reset-password', data)
}
