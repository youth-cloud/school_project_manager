import type { UserOptionItem } from '@/api/sysUser'

export type UserOptionMap = Record<string, UserOptionItem>

const normalizeFallback = (id: string | null | undefined) => {
  if (id == null) return '--'
  const text = String(id).trim()
  return text || '--'
}

export const buildUserOptionMap = (options: UserOptionItem[]): UserOptionMap => {
  return options.reduce<UserOptionMap>((acc, item) => {
    acc[item.id] = item
    return acc
  }, {})
}

export const getStudentDisplayName = (id: string | null | undefined, optionMap: UserOptionMap) => {
  const fallback = normalizeFallback(id)
  if (!id) return fallback
  const item = optionMap[id]
  return item ? `${item.realName}（${item.username}）` : fallback
}

export const getTeacherDisplayName = (id: string | null | undefined, optionMap: UserOptionMap) => {
  const fallback = normalizeFallback(id)
  if (!id) return fallback
  return optionMap[id]?.realName || fallback
}

export const getUserRealName = (id: string | null | undefined, optionMap: UserOptionMap) => {
  return getTeacherDisplayName(id, optionMap)
}

export const getUserDisplayName = (
  id: string | null | undefined,
  userOptionMap: UserOptionMap,
  studentOptionMap?: UserOptionMap,
) => {
  if (studentOptionMap && id && studentOptionMap[id]) {
    return getStudentDisplayName(id, studentOptionMap)
  }
  return getUserRealName(id, userOptionMap)
}
