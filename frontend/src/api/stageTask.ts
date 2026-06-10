import request from '@/utils/request'

export interface StageTaskItem {
  id: string
  batchId: string
  teacherId: string
  taskTitle: string
  taskDescription: string | null
  stageNo: number
  deadline: string | null
  needReport: number
  needSourceCode: number
  needPdf: number
  needScreenshot: number
  needDemoUrl: number
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface StageTaskPageQuery {
  current: number
  size: number
  batchId?: string
  teacherId?: string
  taskTitle?: string
  stageNo?: number
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface StageTaskFormData {
  id?: string
  batchId: string
  taskTitle: string
  taskDescription: string
  stageNo: number
  deadline?: string
  needReport: number
  needSourceCode: number
  needPdf: number
  needScreenshot: number
  needDemoUrl: number
  status: number
}

export const getStageTaskPageApi = (params: StageTaskPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<StageTaskItem> }>('/api/stage-tasks/page', {
    params,
  })
}

export const createStageTaskApi = (data: StageTaskFormData) => {
  return request.post<any, { code: number; message: string; data: StageTaskItem }>('/api/stage-tasks', data)
}

export const updateStageTaskApi = (data: StageTaskFormData) => {
  return request.put<any, { code: number; message: string; data: StageTaskItem }>('/api/stage-tasks', data)
}

export const getStageTaskDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: StageTaskItem }>(`/api/stage-tasks/${id}`)
}

export const deleteStageTaskApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/stage-tasks/${id}`)
}

export const getStageTaskListApi = () => {
  return request.get<any, { code: number; message: string; data: StageTaskItem[] }>('/api/stage-tasks')
}