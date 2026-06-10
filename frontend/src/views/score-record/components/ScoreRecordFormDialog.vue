<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupOption } from '@/api/projectGroup'
import type { ScoreRecordFormData } from '@/api/scoreRecord'
import type { UserOptionItem } from '@/api/sysUser'
import type { TrainingBatchOption } from '@/api/trainingBatch'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: ScoreRecordFormData
  batchOptions: TrainingBatchOption[]
  groupOptions: ProjectGroupOption[]
  studentOptions: UserOptionItem[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建成绩记录' : '编辑成绩记录'))

const filteredGroupOptions = computed(() => {
  if (!props.formData.batchId) return props.groupOptions
  return props.groupOptions.filter((item) => item.batchId === props.formData.batchId)
})

const gradeOptions = ['A', 'B', 'C', 'D', 'E', '优秀', '良好', '中等', '及格', '不及格']

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
  <el-dialog v-model="visible" :title="dialogTitle" width="820px" destroy-on-close>
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
            <el-select
              v-model="formData.groupId"
              placeholder="请选择项目组"
              style="width: 100%"
            >
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

      <el-form-item label="学生" required>
        <el-select
          v-model="formData.studentId"
          filterable
          clearable
          :disabled="!formData.groupId"
          :placeholder="formData.groupId ? '请选择项目组内学生' : '请先选择项目组'"
          style="width: 100%"
        >
          <el-option
            v-for="item in studentOptions"
            :key="item.id"
            :label="`${item.realName}（${item.username}）`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="过程分">
            <el-input-number v-model="formData.processScore" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报告分">
            <el-input-number v-model="formData.reportScore" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="材料分">
            <el-input-number v-model="formData.submissionScore" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="答辩分">
            <el-input-number v-model="formData.defenseScore" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="最终总分">
            <el-input-number v-model="formData.finalScore" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="等级">
            <el-select
              v-model="formData.gradeLevel"
              filterable
              allow-create
              clearable
              default-first-option
              placeholder="请选择或输入等级"
              style="width: 100%"
            >
              <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请输入成绩备注"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>
