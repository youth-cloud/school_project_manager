import request from '@/utils/request'

export interface DefenseRecordItem {
  id: string
  scheduleId: string
  teacherId: string
  presentationScore: number | null
  answerScore: number | null
  completionScore: number | null
  totalScore: number | null
  comment: string | null
  createTime: string | null
  updateTime: string | null
}

export interface DefenseRecordPageQuery {
  current: number
  size: number
  scheduleId?: string
  teacherId?: string
}

export interface DefenseRecordFormData {
  id?: string
  scheduleId: string
  presentationScore: number | null
  answerScore: number | null
  completionScore: number | null
  totalScore: number | null
  comment: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getDefenseRecordPageApi = (params: DefenseRecordPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<DefenseRecordItem> }>('/api/defense-records/page', {
    params,
  })
}

export const getDefenseRecordDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: DefenseRecordItem }>(`/api/defense-records/${id}`)
}

export const createDefenseRecordApi = (data: DefenseRecordFormData) => {
  return request.post<any, { code: number; message: string; data: DefenseRecordItem }>('/api/defense-records', data)
}

export const updateDefenseRecordApi = (data: DefenseRecordFormData) => {
  return request.put<any, { code: number; message: string; data: DefenseRecordItem }>('/api/defense-records', data)
}

export const deleteDefenseRecordApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/defense-records/${id}`)
}