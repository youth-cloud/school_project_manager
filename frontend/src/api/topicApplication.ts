import request from '@/utils/request'

export interface TopicApplicationItem {
  id: string
  batchId: string
  topicId: string
  studentId: string
  applyReason: string | null
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELED' | string
  reviewTime: string | null
  reviewerId: string | null
  reviewComment: string | null
  createTime: string | null
  updateTime: string | null
}

export interface TopicApplicationPageQuery {
  current: number
  size: number
  batchId?: string
  topicId?: string
  studentId?: string
  status?: string
}

export interface TopicApplicationCreateFormData {
  batchId: string
  topicId: string
  applyReason: string
}

export interface TopicApplicationReviewFormData {
  id?: string
  status: 'APPROVED' | 'REJECTED'
  reviewComment: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getTopicApplicationPageApi = (params: TopicApplicationPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<TopicApplicationItem> }>('/api/topic-applications/page', {
    params,
  })
}

export const getTopicApplicationDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: TopicApplicationItem }>(`/api/topic-applications/${id}`)
}

export const createTopicApplicationApi = (data: TopicApplicationCreateFormData) => {
  return request.post<any, { code: number; message: string; data: TopicApplicationItem }>('/api/topic-applications', data)
}

export const reviewTopicApplicationApi = (data: TopicApplicationReviewFormData) => {
  return request.put<any, { code: number; message: string; data: TopicApplicationItem }>('/api/topic-applications/review', data)
}