import request from '@/utils/request'

export interface ProjectTopicItem {
  id: string
  batchId: string
  teacherId: string
  topicName: string
  topicDescription: string | null
  difficultyLevel: number | null
  techRequirements: string | null
  maxMembers: number | null
  selectedCount: number | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface ProjectTopicPageQuery {
  current: number
  size: number
  topicName?: string
  batchId?: string
  teacherId?: string
  difficultyLevel?: number
  status?: number
}

export interface ProjectTopicFormData {
  id?: string
  batchId: string
  topicName: string
  topicDescription: string
  difficultyLevel: number | null
  techRequirements: string
  maxMembers: number | null
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export type ProjectTopicOption = ProjectTopicItem

export const getProjectTopicListApi = () => {
  return request.get<any, { code: number; message: string; data: ProjectTopicOption[] }>('/api/project-topics')
}

export const getProjectTopicPageApi = (params: ProjectTopicPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<ProjectTopicItem> }>('/api/project-topics/page', {
    params,
  })
}

export const getProjectTopicDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: ProjectTopicItem }>(`/api/project-topics/${id}`)
}

export const createProjectTopicApi = (data: ProjectTopicFormData) => {
  return request.post<any, { code: number; message: string; data: ProjectTopicItem }>('/api/project-topics', data)
}

export const updateProjectTopicApi = (data: ProjectTopicFormData) => {
  return request.put<any, { code: number; message: string; data: ProjectTopicItem }>('/api/project-topics', data)
}

export const deleteProjectTopicApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/project-topics/${id}`)
}