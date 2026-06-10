import request from '@/utils/request'

export interface StageSubmissionItem {
  id: string
  taskId: string
  batchId: string
  groupId: string
  submitterId: string
  versionNo: number
  summary: string | null
  reportText: string | null
  repoUrl: string | null
  deployUrl: string | null
  status: number
  submitTime: string | null
  createTime: string | null
  updateTime: string | null
}

export interface StageSubmissionPageQuery {
  current: number
  size: number
  versionNo?: number
  summary?: string
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface StageSubmissionFormData {
  id?: string
  taskId: string
  batchId: string
  groupId: string
  versionNo: number
  summary: string
  reportText: string
  repoUrl: string
  deployUrl: string
  status: number
}

export const getStageSubmissionPageApi = (params: StageSubmissionPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<StageSubmissionItem> }>('/api/stage-submissions/page', {
    params,
  })
}

export const createStageSubmissionApi = (data: StageSubmissionFormData) => {
  return request.post<any, { code: number; message: string; data: StageSubmissionItem }>('/api/stage-submissions', data)
}

export const updateStageSubmissionApi = (data: StageSubmissionFormData) => {
  return request.put<any, { code: number; message: string; data: StageSubmissionItem }>('/api/stage-submissions', data)
}

export const getStageSubmissionDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: StageSubmissionItem }>(`/api/stage-submissions/${id}`)
}

export const deleteStageSubmissionApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/stage-submissions/${id}`)
}

export const getStageSubmissionListApi = () => {
  return request.get<any, { code: number; message: string; data: StageSubmissionItem[] }>('/api/stage-submissions')
}