import request from '@/utils/request'

export interface ProjectGroupApplicationItem {
  id: string
  batchId: string
  topicId: string
  leaderId: string
  groupName: string
  projectName: string | null
  projectDescription: string | null
  repoUrl: string | null
  deployUrl: string | null
  applyReason: string | null
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELED' | string
  reviewerId: string | null
  reviewComment: string | null
  reviewTime: string | null
  generatedGroupId: string | null
  createTime: string | null
  updateTime: string | null
}

export interface ProjectGroupApplicationPageQuery {
  current: number
  size: number
  groupName?: string
  batchId?: string
  topicId?: string
  leaderId?: string
  status?: string
}

export interface ProjectGroupApplicationCreateFormData {
  batchId: string
  topicId: string
  groupName: string
  projectName: string
  projectDescription: string
  repoUrl: string
  deployUrl: string
  applyReason: string
  memberIds: string[]
}

export interface ProjectGroupApplicationReviewFormData {
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

export const getProjectGroupApplicationPageApi = (params: ProjectGroupApplicationPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<ProjectGroupApplicationItem> }>(
    '/api/project-group-applications/page',
    {
      params,
    },
  )
}

export const getProjectGroupApplicationDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: ProjectGroupApplicationItem }>(
    `/api/project-group-applications/${id}`,
  )
}

export const createProjectGroupApplicationApi = (data: ProjectGroupApplicationCreateFormData) => {
  return request.post<any, { code: number; message: string; data: ProjectGroupApplicationItem }>(
    '/api/project-group-applications',
    data,
  )
}

export const reviewProjectGroupApplicationApi = (data: ProjectGroupApplicationReviewFormData) => {
  return request.put<any, { code: number; message: string; data: ProjectGroupApplicationItem }>(
    '/api/project-group-applications/review',
    data,
  )
}

export const cancelProjectGroupApplicationApi = (id: string) => {
  return request.put<any, { code: number; message: string; data: ProjectGroupApplicationItem }>(
    `/api/project-group-applications/${id}/cancel`,
  )
}