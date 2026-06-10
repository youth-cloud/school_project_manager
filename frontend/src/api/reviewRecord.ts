import request from '@/utils/request'

export interface ReviewRecordItem {
  id: string
  submissionId: string
  reviewerId: string
  reviewResult: string
  score: number | null
  comment: string | null
  reviewTime: string | null
  createTime: string | null
}

export interface ReviewRecordPageQuery {
  current: number
  size: number
  submissionId?: string
  reviewerId?: string
  reviewResult?: string
}

export interface ReviewRecordFormData {
  id?: string
  submissionId: string
  reviewResult: string
  score: number | null
  comment: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getReviewRecordPageApi = (params: ReviewRecordPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<ReviewRecordItem> }>('/api/review-records/page', {
    params,
  })
}

export const getReviewRecordDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: ReviewRecordItem }>(`/api/review-records/${id}`)
}

export const createReviewRecordApi = (data: ReviewRecordFormData) => {
  return request.post<any, { code: number; message: string; data: ReviewRecordItem }>('/api/review-records', data)
}

export const updateReviewRecordApi = (data: ReviewRecordFormData) => {
  return request.put<any, { code: number; message: string; data: ReviewRecordItem }>('/api/review-records', data)
}

export const deleteReviewRecordApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/review-records/${id}`)
}