<script setup lang="ts">
import { computed } from 'vue'

import type { EduClassItem } from '@/api/eduClass'
import type { RoleOptionItem, SysUserFormData } from '@/api/sysUser'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: SysUserFormData
  roleOptions: RoleOptionItem[]
  classOptions: EduClassItem[]
  submitting: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const dialogTitle = computed(() => (props.mode === 'create' ? '新建用户' : '编辑用户'))
const isStudentUser = computed(() => props.formData.roleCodes.includes('STUDENT'))
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
    <el-form label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item :label="isStudentUser ? '学号' : '用户名'" required>
            <el-input
              v-if="isStudentUser"
              v-model="formData.studentNo"
              :maxlength="30"
              show-word-limit
              placeholder="请输入学号，系统将同步作为登录账号"
            />
            <el-input
              v-else
              v-model="formData.username"
              :maxlength="50"
              show-word-limit
              placeholder="请输入登录用户名"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="真实姓名" required>
            <el-input
              v-model="formData.realName"
              maxlength="50"
              show-word-limit
              placeholder="请输入真实姓名"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="isStudentUser" :gutter="16">
        <el-col :span="12">
          <el-form-item label="班级" required>
            <el-select
              v-model="formData.classId"
              filterable
              clearable
              placeholder="请选择班级"
              style="width: 100%"
            >
              <el-option
                v-for="item in classOptions"
                :key="item.id"
                :label="item.className"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="登录账号">
            <el-input :model-value="formData.studentNo || '--'" disabled />
          </el-form-item>
        </el-col>
      </el-row>

      <template v-if="mode === 'create'">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="初始密码" required>
              <el-input
                v-model="formData.password"
                type="password"
                show-password
                maxlength="50"
                placeholder="请输入初始密码"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="确认密码" required>
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                show-password
                maxlength="50"
                placeholder="请再次输入密码"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-form-item label="角色" required>
        <el-select
          v-model="formData.roleCodes"
          multiple
          clearable
          collapse-tags
          collapse-tags-tooltip
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option
            v-for="item in roleOptions"
            :key="item.id"
            :label="item.roleName"
            :value="item.roleCode"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" required>
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>
