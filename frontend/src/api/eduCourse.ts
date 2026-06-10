import request from '@/utils/request'

export interface EduCourseItem {
  id: string
  courseName: string
  courseCode: string | null
  credit: number | null
  remark: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface EduCoursePageQuery {
  current: number
  size: number
  courseName?: string
  courseCode?: string
  status?: number
}

export interface EduCourseFormData {
  id?: string
  courseName: string
  courseCode: string
  credit: number | null
  remark: string
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getEduCourseListApi = () => {
  return request.get<any, { code: number; message: string; data: EduCourseItem[] }>('/api/courses')
}

export const getEduCoursePageApi = (params: EduCoursePageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<EduCourseItem> }>('/api/courses/page', {
    params,
  })
}

export const getEduCourseDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: EduCourseItem }>(`/api/courses/${id}`)
}

export const createEduCourseApi = (data: EduCourseFormData) => {
  return request.post<any, { code: number; message: string; data: EduCourseItem }>('/api/courses', data)
}

export const updateEduCourseApi = (data: EduCourseFormData) => {
  return request.put<any, { code: number; message: string; data: EduCourseItem }>('/api/courses', data)
}

export const deleteEduCourseApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/courses/${id}`)
}