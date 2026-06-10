import request from '@/utils/request'

export interface WeeklyReportItem {
  id: string
  batchId: string
  groupId: string
  studentId: string
  weekIndex: number
  completedWork: string | null
  problemDesc: string | null
  nextPlan: string | null
  teacherComment: string | null
  score: number | null
  submitTime: string | null
  reviewTime: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface WeeklyReportPageQuery {
  current: number
  size: number
  batchId?: string
  groupId?: string
  studentId?: string
  weekIndex?: number
  status?: number
}

export interface WeeklyReportFormData {
  id?: string
  batchId: string
  groupId: string
  weekIndex: number
  completedWork: string
  problemDesc: string
  nextPlan: string
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getWeeklyReportPageApi = (params: WeeklyReportPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<WeeklyReportItem> }>('/api/weekly-reports/page', {
    params,
  })
}

export const getWeeklyReportDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: WeeklyReportItem }>(`/api/weekly-reports/${id}`)
}

export const createWeeklyReportApi = (data: WeeklyReportFormData) => {
  return request.post<any, { code: number; message: string; data: WeeklyReportItem }>('/api/weekly-reports', data)
}

export const updateWeeklyReportApi = (data: WeeklyReportFormData) => {
  return request.put<any, { code: number; message: string; data: WeeklyReportItem }>('/api/weekly-reports', data)
}

export const deleteWeeklyReportApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/weekly-reports/${id}`)
}