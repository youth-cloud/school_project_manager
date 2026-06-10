<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectTopicFormData } from '@/api/projectTopic'
import type { TrainingBatchOption } from '@/api/trainingBatch'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: ProjectTopicFormData
  batchOptions: TrainingBatchOption[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建课题' : '编辑课题'))

const difficultyOptions = [
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '较难', value: 3 },
  { label: '困难', value: 4 },
]

const getBatchLabel = (item: TrainingBatchOption) => {
  if (!item.termName) return item.batchName
  return `${item.batchName} · ${item.termName}`
}
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="860px" destroy-on-close>
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
          <el-form-item label="难度等级">
            <el-select v-model="formData.difficultyLevel" clearable placeholder="请选择难度" style="width: 100%">
              <el-option
                v-for="item in difficultyOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="课题名称" required>
        <el-input
          v-model="formData.topicName"
          maxlength="100"
          show-word-limit
          placeholder="请输入课题名称"
        />
      </el-form-item>

      <el-form-item label="课题简介">
        <el-input
          v-model="formData.topicDescription"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="请输入课题简介"
        />
      </el-form-item>

      <el-form-item label="技术要求">
        <el-input
          v-model="formData.techRequirements"
          type="textarea"
          :rows="4"
          maxlength="1000"
          show-word-limit
          placeholder="请输入技术要求"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="最大人数">
            <el-input-number
              v-model="formData.maxMembers"
              :min="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="状态" required>
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>