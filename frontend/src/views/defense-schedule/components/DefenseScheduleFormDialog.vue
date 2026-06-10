<script setup lang="ts">
import { computed } from 'vue'

import type { DefenseScheduleFormData } from '@/api/defenseSchedule'
import type { ProjectGroupOption } from '@/api/projectGroup'
import type { TrainingBatchOption } from '@/api/trainingBatch'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: DefenseScheduleFormData
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建答辩安排' : '编辑答辩安排'))

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
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
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

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="答辩日期" required>
            <el-date-picker
              v-model="formData.defenseDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择答辩日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="答辩时间" required>
            <el-time-picker
              v-model="formData.defenseTime"
              value-format="HH:mm:ss"
              placeholder="请选择答辩时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="答辩地点">
            <el-input
              v-model="formData.location"
              maxlength="100"
              show-word-limit
              placeholder="请输入答辩地点"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="答辩顺序">
            <el-input-number
              v-model="formData.orderNo"
              :min="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

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