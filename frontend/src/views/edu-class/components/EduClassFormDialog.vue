<script setup lang="ts">
import { computed } from 'vue'

import type { EduClassFormData } from '@/api/eduClass'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: EduClassFormData
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建班级' : '编辑班级'))
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="班级名称" required>
        <el-input
          v-model="formData.className"
          maxlength="100"
          show-word-limit
          placeholder="请输入班级名称"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="专业名称">
            <el-input
              v-model="formData.majorName"
              maxlength="100"
              show-word-limit
              placeholder="请输入专业名称"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="年级">
            <el-input-number
              v-model="formData.grade"
              :min="2000"
              :max="2100"
              controls-position="right"
              style="width: 100%"
              placeholder="例如 2023"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="辅导员">
        <el-input
          v-model="formData.counselorName"
          maxlength="100"
          show-word-limit
          placeholder="请输入辅导员姓名"
        />
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