<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupOption } from '@/api/projectGroup'
import type { DefenseRecordFormData } from '@/api/defenseRecord'
import type { DefenseScheduleItem } from '@/api/defenseSchedule'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: DefenseRecordFormData
  scheduleOptions: DefenseScheduleItem[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建答辩记录' : '编辑答辩记录'))

const getGroupLabel = (groupId: string) => {
  const item = props.groupOptions.find((option) => option.id === groupId)
  if (!item) return groupId
  return item.projectName ? `${item.groupName} · ${item.projectName}` : item.groupName
}

const getScheduleLabel = (item: DefenseScheduleItem) => {
  const groupLabel = getGroupLabel(item.groupId)
  const date = item.defenseDate || '未设置日期'
  const time = item.defenseTime || '未设置时间'
  const location = item.location || '未设置地点'
  return `${groupLabel} · ${date} ${time} · ${location}`
}
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
    <el-form label-width="110px">
      <el-form-item label="答辩安排" required>
        <el-select v-model="formData.scheduleId" filterable placeholder="请选择答辩安排" style="width: 100%">
          <el-option
            v-for="item in scheduleOptions"
            :key="item.id"
            :label="getScheduleLabel(item)"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="展示分">
            <el-input-number
              v-model="formData.presentationScore"
              :min="0"
              :precision="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="问答分">
            <el-input-number
              v-model="formData.answerScore"
              :min="0"
              :precision="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="完成度分">
            <el-input-number
              v-model="formData.completionScore"
              :min="0"
              :precision="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="总分">
            <el-input-number
              v-model="formData.totalScore"
              :min="0"
              :precision="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="评语">
        <el-input
          v-model="formData.comment"
          type="textarea"
          :rows="6"
          maxlength="1000"
          show-word-limit
          placeholder="请输入答辩评语"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>
