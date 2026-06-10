import request from '@/utils/request'

export interface EduClassItem {
  id: string
  className: string
  majorName: string | null
  grade: number | null
  counselorName: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface EduClassPageQuery {
  current: number
  size: number
  className?: string
  majorName?: string
}

export interface EduClassFormData {
  id?: string
  className: string
  majorName: string
  grade: number | null
  counselorName: string
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getEduClassListApi = () => {
  return request.get<any, { code: number; message: string; data: EduClassItem[] }>('/api/classes')
}

export const getEduClassPageApi = (params: EduClassPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<EduClassItem> }>('/api/classes/page', {
    params,
  })
}

export const getEduClassDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: EduClassItem }>(`/api/classes/${id}`)
}

export const createEduClassApi = (data: EduClassFormData) => {
  return request.post<any, { code: number; message: string; data: EduClassItem }>('/api/classes', data)
}

export const updateEduClassApi = (data: EduClassFormData) => {
  return request.put<any, { code: number; message: string; data: EduClassItem }>('/api/classes', data)
}

export const deleteEduClassApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/classes/${id}`)
}