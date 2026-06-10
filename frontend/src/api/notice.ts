import request from '@/utils/request'

export interface NoticeItem {
  id: string
  title: string
  content: string
  publisherId: string
  targetRole: string | null
  status: number
  publishTime: string | null
  createTime: string | null
  updateTime: string | null
}

export interface NoticePageQuery {
  current: number
  size: number
  title?: string
  publisherId?: string
  targetRole?: string
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface NoticeFormData {
  id?: string
  title: string
  content: string
  targetRole: string
  status: number
  publishTime?: string
}

export const getNoticePageApi = (params: NoticePageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<NoticeItem> }>('/api/notices/page', {
    params,
  })
}

export const createNoticeApi = (data: NoticeFormData) => {
  return request.post<any, { code: number; message: string; data: NoticeItem }>('/api/notices', data)
}

export const updateNoticeApi = (data: NoticeFormData) => {
  return request.put<any, { code: number; message: string; data: NoticeItem }>('/api/notices', data)
}

export const getNoticeDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: NoticeItem }>(`/api/notices/${id}`)
}

export const deleteNoticeApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/notices/${id}`)
}