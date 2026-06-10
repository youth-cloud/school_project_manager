import request from '@/utils/request'

export interface DefenseScheduleItem {
  id: string
  batchId: string
  groupId: string
  defenseDate: string | null
  defenseTime: string | null
  location: string | null
  orderNo: number | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface DefenseSchedulePageQuery {
  current: number
  size: number
  batchId?: string
  groupId?: string
  orderNo?: number
  status?: number
}

export interface DefenseScheduleFormData {
  id?: string
  batchId: string
  groupId: string
  defenseDate: string
  defenseTime: string
  location: string
  orderNo: number | null
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getDefenseSchedulePageApi = (params: DefenseSchedulePageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<DefenseScheduleItem> }>('/api/defense-schedules/page', {
    params,
  })
}

export const getDefenseScheduleDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: DefenseScheduleItem }>(`/api/defense-schedules/${id}`)
}

export const createDefenseScheduleApi = (data: DefenseScheduleFormData) => {
  return request.post<any, { code: number; message: string; data: DefenseScheduleItem }>('/api/defense-schedules', data)
}

export const updateDefenseScheduleApi = (data: DefenseScheduleFormData) => {
  return request.put<any, { code: number; message: string; data: DefenseScheduleItem }>('/api/defense-schedules', data)
}

export const deleteDefenseScheduleApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/defense-schedules/${id}`)
}