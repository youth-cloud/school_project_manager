import request from '@/utils/request'

export interface ProjectGroupMemberItem {
  id: string
  groupId: string
  userId: string
  isLeader: number
  joinTime: string | null
  status: number
}

export interface ProjectGroupMemberPageQuery {
  current: number
  size: number
  groupId?: string
  userId?: string
  isLeader?: number
  status?: number
}

export interface ProjectGroupMemberCreateFormData {
  groupId: string
  isLeader: number
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getProjectGroupMemberPageApi = (params: ProjectGroupMemberPageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<ProjectGroupMemberItem> }>(
    '/api/project-group-members/page',
    {
      params,
    },
  )
}

export const getProjectGroupMemberDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: ProjectGroupMemberItem }>(
    `/api/project-group-members/${id}`,
  )
}

export const createProjectGroupMemberApi = (data: ProjectGroupMemberCreateFormData) => {
  return request.post<any, { code: number; message: string; data: ProjectGroupMemberItem }>(
    '/api/project-group-members',
    data,
  )
}

export const deleteProjectGroupMemberApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(
    `/api/project-group-members/${id}`,
  )
}