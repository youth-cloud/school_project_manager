import { computed, ref } from 'vue'

import {
  getStudentOptionsApi,
  getTeacherOptionsApi,
  getUserOptionsApi,
  type UserOptionItem,
} from '@/api/sysUser'
import { buildUserOptionMap } from '@/utils/userDisplay'

export type UserOptionKind = 'student' | 'teacher' | 'user'

export const useUserOptions = () => {
  const studentOptions = ref<UserOptionItem[]>([])
  const teacherOptions = ref<UserOptionItem[]>([])
  const userOptions = ref<UserOptionItem[]>([])

  const studentOptionMap = computed(() => buildUserOptionMap(studentOptions.value))
  const teacherOptionMap = computed(() => buildUserOptionMap(teacherOptions.value))
  const userOptionMap = computed(() => buildUserOptionMap(userOptions.value))

  const loadStudentOptions = async () => {
    const res = await getStudentOptionsApi()
    studentOptions.value = res.data
    return res.data
  }

  const loadTeacherOptions = async () => {
    const res = await getTeacherOptionsApi()
    teacherOptions.value = res.data
    return res.data
  }

  const loadUserOptions = async () => {
    const res = await getUserOptionsApi()
    userOptions.value = res.data
    return res.data
  }

  const loadRequestedOptions = async (kinds: UserOptionKind[]) => {
    const uniqueKinds = Array.from(new Set(kinds))
    await Promise.all(
      uniqueKinds.map((kind) => {
        if (kind === 'student') return loadStudentOptions()
        if (kind === 'teacher') return loadTeacherOptions()
        return loadUserOptions()
      }),
    )
  }

  return {
    studentOptions,
    teacherOptions,
    userOptions,
    studentOptionMap,
    teacherOptionMap,
    userOptionMap,
    loadStudentOptions,
    loadTeacherOptions,
    loadUserOptions,
    loadRequestedOptions,
  }
}
