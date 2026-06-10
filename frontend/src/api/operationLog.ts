import request from '@/utils/request'

export interface OperationLogItem {
  id: string
  moduleName: string
  operationType: string
  operatorId: string
  requestMethod: string | null
  requestUri: string | null
  ip: string | null
  operationDesc: string | null
  result: string | null
  createTime: string | null
}

export interface OperationLogPageQuery {
  current: number
  size: number
  moduleName?: string
  operationType?: string
  operatorId?: string
  result?: string
}

export interface OperationLogFormData {
  id?: string
  moduleName: string
  operationType: string
  requestMethod: string
  requestUri: string
  ip: string
  operationDesc: string
  result: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getOperationLogListApi = () => {
  return request.get<any, { code: number; message: string; data: OperationLogItem[] }>('/api/operation-logs')
}

export const getOperationLogPageApi = (params: OperationLogPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<OperationLogItem> }>(
    '/api/operation-logs/page',
    { params },
  )
}

export const getOperationLogDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: OperationLogItem }>(`/api/operation-logs/${id}`)
}

export const createOperationLogApi = (data: OperationLogFormData) => {
  return request.post<any, { code: number; message: string; data: OperationLogItem }>('/api/operation-logs', data)
}

export const updateOperationLogApi = (data: OperationLogFormData) => {
  return request.put<any, { code: number; message: string; data: OperationLogItem }>('/api/operation-logs', data)
}

export const deleteOperationLogApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/operation-logs/${id}`)
}