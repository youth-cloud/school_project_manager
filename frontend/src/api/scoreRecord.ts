import request from '@/utils/request'

export interface ScoreRecordItem {
  id: string
  batchId: string
  groupId: string | null
  studentId: string
  processScore: number | null
  reportScore: number | null
  submissionScore: number | null
  defenseScore: number | null
  finalScore: number | null
  gradeLevel: string | null
  remark: string | null
  createTime: string | null
  updateTime: string | null
}

export interface ScoreRecordPageQuery {
  current: number
  size: number
  batchId?: string
  groupId?: string
  studentId?: string
  gradeLevel?: string
}

export interface ScoreRecordFormData {
  id?: string
  batchId: string
  groupId: string
  studentId: string
  processScore: number | null
  reportScore: number | null
  submissionScore: number | null
  defenseScore: number | null
  finalScore: number | null
  gradeLevel: string
  remark: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getScoreRecordPageApi = (params: ScoreRecordPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<ScoreRecordItem> }>('/api/score-records/page', {
    params,
  })
}

export const getScoreRecordDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: ScoreRecordItem }>(`/api/score-records/${id}`)
}

export const createScoreRecordApi = (data: ScoreRecordFormData) => {
  return request.post<any, { code: number; message: string; data: ScoreRecordItem }>('/api/score-records', data)
}

export const updateScoreRecordApi = (data: ScoreRecordFormData) => {
  return request.put<any, { code: number; message: string; data: ScoreRecordItem }>('/api/score-records', data)
}

export const deleteScoreRecordApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/score-records/${id}`)
}
