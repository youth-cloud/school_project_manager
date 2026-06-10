<script setup lang="ts">
import { computed } from 'vue'

import type { EduCourseFormData } from '@/api/eduCourse'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: EduCourseFormData
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建课程' : '编辑课程'))
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="课程名称" required>
        <el-input
          v-model="formData.courseName"
          maxlength="100"
          show-word-limit
          placeholder="请输入课程名称"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="课程编码">
            <el-input
              v-model="formData.courseCode"
              maxlength="50"
              show-word-limit
              placeholder="例如：SE2026"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="学分">
            <el-input-number
              v-model="formData.credit"
              :min="0"
              :precision="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="课程备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="请输入课程备注"
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