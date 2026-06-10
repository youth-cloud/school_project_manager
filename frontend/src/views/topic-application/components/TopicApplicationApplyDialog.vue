<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectTopicOption } from '@/api/projectTopic'
import type { TopicApplicationCreateFormData } from '@/api/topicApplication'
import type { TrainingBatchOption } from '@/api/trainingBatch'

const props = defineProps<{
  modelValue: boolean
  formData: TopicApplicationCreateFormData
  batchOptions: TrainingBatchOption[]
  topicOptions: ProjectTopicOption[]
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

const filteredTopicOptions = computed(() => {
  if (!props.formData.batchId) return props.topicOptions
  return props.topicOptions.filter((item) => item.batchId === props.formData.batchId)
})

const getBatchLabel = (item: TrainingBatchOption) => {
  if (!item.termName) return item.batchName
  return `${item.batchName} · ${item.termName}`
}

const getTopicLabel = (item: ProjectTopicOption) => {
  return item.topicName
}
</script>

<template>
  <el-dialog v-model="visible" title="新建选题申请" width="760px" destroy-on-close>
    <el-form label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属批次" required>
            <el-select v-model="formData.batchId" placeholder="请选择实训批次" style="width: 100%">
              <el-option
                v-for="item in batchOptions"
                :key="item.id"
                :label="getBatchLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="目标课题" required>
            <el-select v-model="formData.topicId" filterable placeholder="请选择课题" style="width: 100%">
              <el-option
                v-for="item in filteredTopicOptions"
                :key="item.id"
                :label="getTopicLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="申请理由">
        <el-input
          v-model="formData.applyReason"
          type="textarea"
          :rows="6"
          maxlength="500"
          show-word-limit
          placeholder="请输入申请理由"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">提交申请</el-button>
    </template>
  </el-dialog>
</template>