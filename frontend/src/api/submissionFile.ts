import axios from 'axios'

import request from '@/utils/request'
import { getToken } from '@/utils/token'

const FILE_API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export interface SubmissionFileItem {
  id: string
  submissionId: string
  fileName: string
  originalName: string
  fileType: string
  fileSize: number
  filePath: string
  fileUrl: string | null
  bizType: string | null
  uploadUserId: string
  createTime: string | null
}

export interface SubmissionFilePageQuery {
  current: number
  size: number
  submissionId?: string
  fileType?: string
  bizType?: string
  uploadUserId?: string
}

export interface SubmissionFileUploadFormData {
  submissionId: string
  bizType: string
  file: File | null
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getSubmissionFilePageApi = (params: SubmissionFilePageQuery) => {
  return request.get<any, { code: number; message: string; data: PageResult<SubmissionFileItem> }>('/api/submission-files/page', {
    params,
  })
}

export const getSubmissionFileDetailApi = (id: string) => {
  return request.get<any, { code: number; message: string; data: SubmissionFileItem }>(`/api/submission-files/${id}`)
}

export const uploadSubmissionFileApi = (formData: FormData) => {
  return request.post<any, { code: number; message: string; data: SubmissionFileItem }>('/api/submission-files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const deleteSubmissionFileApi = (id: string) => {
  return request.delete<any, { code: number; message: string; data: boolean }>(`/api/submission-files/${id}`)
}

export const fetchSubmissionFilePreviewApi = (id: string) => {
  const token = getToken()

  return axios.get(`${FILE_API_BASE_URL}/api/submission-files/${id}/preview`, {
    responseType: 'blob',
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : undefined,
  })
}

export const fetchSubmissionFileDownloadApi = (id: string) => {
  const token = getToken()

  return axios.get(`${FILE_API_BASE_URL}/api/submission-files/${id}/download`, {
    responseType: 'blob',
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : undefined,
  })
}