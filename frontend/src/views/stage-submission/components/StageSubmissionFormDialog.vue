<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupOption } from '@/api/projectGroup'
import type { StageSubmissionFormData } from '@/api/stageSubmission'
import type { StageTaskItem } from '@/api/stageTask'
import type { TrainingBatchOption } from '@/api/trainingBatch'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: StageSubmissionFormData
  batchOptions: TrainingBatchOption[]
  groupOptions: ProjectGroupOption[]
  taskOptions: StageTaskItem[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建阶段提交' : '编辑阶段提交'))

const filteredGroupOptions = computed(() => {
  if (!props.formData.batchId) return props.groupOptions
  return props.groupOptions.filter((item) => item.batchId === props.formData.batchId)
})

const filteredTaskOptions = computed(() => {
  if (!props.formData.batchId) return props.taskOptions
  return props.taskOptions.filter((item) => item.batchId === props.formData.batchId)
})

const handleClose = () => {
  visible.value = false
}

const handleSubmit = () => {
  emit('submit')
}

const getBatchLabel = (item: TrainingBatchOption) => {
  if (!item.termName) return item.batchName
  return `${item.batchName} · ${item.termName}`
}

const getGroupLabel = (item: ProjectGroupOption) => {
  if (!item.projectName) return item.groupName
  return `${item.groupName} · ${item.projectName}`
}

const getTaskLabel = (item: StageTaskItem) => {
  return `第${item.stageNo}阶段 · ${item.taskTitle}`
}
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="820px" destroy-on-close>
    <el-form label-width="100px" class="submission-form">
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
          <el-form-item label="阶段任务" required>
            <el-select v-model="formData.taskId" placeholder="请选择阶段任务" style="width: 100%">
              <el-option
                v-for="item in filteredTaskOptions"
                :key="item.id"
                :label="getTaskLabel(item)"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
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

        <el-col :span="12">
          <el-form-item label="版本号" required>
            <el-input-number v-model="formData.versionNo" :min="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="提交摘要">
        <el-input
          v-model="formData.summary"
          placeholder="请输入本次提交摘要"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="提交说明">
        <el-input
          v-model="formData.reportText"
          type="textarea"
          :rows="6"
          placeholder="请输入本次提交说明"
          maxlength="5000"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="仓库地址">
            <el-input v-model="formData.repoUrl" placeholder="例如：https://github.com/xxx/xxx" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="部署地址">
            <el-input v-model="formData.deployUrl" placeholder="例如：http://demo.xxx.com" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="状态" required>
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">已提交</el-radio>
          <el-radio :value="0">草稿/停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.submission-form {
  padding-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>