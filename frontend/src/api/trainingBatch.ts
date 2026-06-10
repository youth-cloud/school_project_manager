import request from '@/utils/request'

export interface TrainingBatchOption {
  id: string
  batchName: string
  courseId: string
  teacherId: string
  classId: string
  termName: string | null
  startTime: string | null
  endTime: string | null
  defenseTime: string | null
  description: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface TrainingBatchPageQuery {
  current: number
  size: number
  batchName?: string
  courseId?: string
  teacherId?: string
  classId?: string
  termName?: string
  status?: number
}

export interface TrainingBatchFormData {
  id?: string
  batchName: string
  courseId: string
  teacherId: string
  classId: string
  termName: string
  startTime: string
  endTime: string
  defenseTime: string
  description: string
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getTrainingBatchListApi = () => {
  return request.get<any, { code: number; message: string; data: TrainingBatchOption[] }>('/api/training-batches')
}

export const getTrainingBatchPageApi = (params: TrainingBatchPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<TrainingBatchOption> }>('/api/training-batches/page', {
    params,
  })
}

export const getTrainingBatchDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: TrainingBatchOption }>(`/api/training-batches/${id}`)
}

export const createTrainingBatchApi = (data: TrainingBatchFormData) => {
  return request.post<any, { code: number; message: string; data: TrainingBatchOption }>('/api/training-batches', data)
}

export const updateTrainingBatchApi = (data: TrainingBatchFormData) => {
  return request.put<any, { code: number; message: string; data: TrainingBatchOption }>('/api/training-batches', data)
}

export const deleteTrainingBatchApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/training-batches/${id}`)
}