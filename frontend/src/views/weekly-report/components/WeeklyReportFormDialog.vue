<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupOption } from '@/api/projectGroup'
import type { TrainingBatchOption } from '@/api/trainingBatch'
import type { WeeklyReportFormData } from '@/api/weeklyReport'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: WeeklyReportFormData
  batchOptions: TrainingBatchOption[]
  groupOptions: ProjectGroupOption[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建周报' : '编辑周报'))

const filteredGroupOptions = computed(() => {
  if (!props.formData.batchId) return props.groupOptions
  return props.groupOptions.filter((item) => item.batchId === props.formData.batchId)
})

const getBatchLabel = (item: TrainingBatchOption) => {
  if (!item.termName) return item.batchName
  return `${item.batchName} · ${item.termName}`
}

const getGroupLabel = (item: ProjectGroupOption) => {
  if (!item.projectName) return item.groupName
  return `${item.groupName} · ${item.projectName}`
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
          <el-form-item label="项目组" required>
            <el-select v-model="formData.groupId" placeholder="请选择项目组" style="width: 100%">
              <el-option
                v-for="item in filteredGroupOptions"
                :key="item.id"
                :label="getGroupLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="周次" required>
        <el-input-number
          v-model="formData.weekIndex"
          :min="1"
          controls-position="right"
          style="width: 220px"
        />
      </el-form-item>

      <el-form-item label="本周完成">
        <el-input
          v-model="formData.completedWork"
          type="textarea"
          :rows="5"
          maxlength="3000"
          show-word-limit
          placeholder="请输入本周已完成工作"
        />
      </el-form-item>

      <el-form-item label="问题困难">
        <el-input
          v-model="formData.problemDesc"
          type="textarea"
          :rows="4"
          maxlength="3000"
          show-word-limit
          placeholder="请输入本周遇到的问题或困难"
        />
      </el-form-item>

      <el-form-item label="下周计划">
        <el-input
          v-model="formData.nextPlan"
          type="textarea"
          :rows="4"
          maxlength="3000"
          show-word-limit
          placeholder="请输入下周工作计划"
        />
      </el-form-item>

      <el-form-item label="状态" required>
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">已提交</el-radio>
          <el-radio :value="0">草稿/停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>