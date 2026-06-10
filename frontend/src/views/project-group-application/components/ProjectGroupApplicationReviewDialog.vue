<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupApplicationReviewFormData } from '@/api/projectGroupApplication'

const props = defineProps<{
  modelValue: boolean
  formData: ProjectGroupApplicationReviewFormData
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
</script>

<template>
  <el-dialog v-model="visible" title="审核建组申请" width="680px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="审核结果" required>
        <el-radio-group v-model="formData.status">
          <el-radio value="APPROVED">通过</el-radio>
          <el-radio value="REJECTED">驳回</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审核意见">
        <el-input
          v-model="formData.reviewComment"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请输入审核意见"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">提交审核</el-button>
    </template>
  </el-dialog>
</template>