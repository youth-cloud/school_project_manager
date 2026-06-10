import request from '@/utils/request'

export interface ProjectGroupItem {
  id: string
  batchId: string
  topicId: string
  groupName: string
  leaderId: string
  teacherId: string
  projectName: string | null
  projectDescription: string | null
  repoUrl: string | null
  deployUrl: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}

export interface ProjectGroupPageQuery {
  current: number
  size: number
  groupName?: string
  batchId?: string
  topicId?: string
  leaderId?: string
  teacherId?: string
  status?: number
}

export interface ProjectGroupFormData {
  id?: string
  batchId: string
  topicId: string
  groupName: string
  leaderId: string
  projectName: string
  projectDescription: string
  repoUrl: string
  deployUrl: string
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export type ProjectGroupOption = ProjectGroupItem

export const getProjectGroupListApi = () => {
  return request.get<any, { code: number; message: string; data: ProjectGroupOption[] }>('/api/project-groups')
}

export const getProjectGroupPageApi = (params: ProjectGroupPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<ProjectGroupItem> }>('/api/project-groups/page', {
    params,
  })
}

export const getProjectGroupDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: ProjectGroupItem }>(`/api/project-groups/${id}`)
}

export const createProjectGroupApi = (data: ProjectGroupFormData) => {
  return request.post<any, { code: number; message: string; data: ProjectGroupItem }>('/api/project-groups', data)
}

export const updateProjectGroupApi = (data: ProjectGroupFormData) => {
  return request.put<any, { code: number; message: string; data: ProjectGroupItem }>('/api/project-groups', data)
}

export const deleteProjectGroupApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/project-groups/${id}`)
}