<script setup lang="ts">
import { computed } from 'vue'

import type { ReviewRecordFormData } from '@/api/reviewRecord'
import type { StageSubmissionItem } from '@/api/stageSubmission'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: ReviewRecordFormData
  submissionOptions: StageSubmissionItem[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建审核记录' : '编辑审核记录'))

const reviewResultOptions = [
  { label: '通过', value: 'APPROVED' },
  { label: '驳回', value: 'REJECTED' },
  { label: '需修改', value: 'NEED_FIX' },
]

const getSubmissionLabel = (item: StageSubmissionItem) => {
  const summary = item.summary?.trim() || '未填写摘要'
  return `V${item.versionNo} · ${summary}`
}
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="阶段提交" required>
        <el-select v-model="formData.submissionId" filterable placeholder="请选择阶段提交" style="width: 100%">
          <el-option
            v-for="item in submissionOptions"
            :key="item.id"
            :label="getSubmissionLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="审核结果" required>
        <el-select
          v-model="formData.reviewResult"
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入审核结果"
          style="width: 100%"
        >
          <el-option
            v-for="item in reviewResultOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="评分">
        <el-input-number
          v-model="formData.score"
          :min="0"
          :max="100"
          :precision="1"
          controls-position="right"
          style="width: 100%"
          placeholder="可选"
        />
      </el-form-item>

      <el-form-item label="审核评语">
        <el-input
          v-model="formData.comment"
          type="textarea"
          :rows="6"
          maxlength="1000"
          show-word-limit
          placeholder="请输入审核评语"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>