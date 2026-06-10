<script setup lang="ts">
import { computed } from 'vue'

import type { TrainingBatchOption } from '@/api/trainingBatch'
import type { StageTaskFormData } from '@/api/stageTask'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: StageTaskFormData
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建阶段任务' : '编辑阶段任务'))

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
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="760px" destroy-on-close>
    <el-form label-width="110px" class="stage-task-form">
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

      <el-form-item label="任务标题" required>
        <el-input v-model="formData.taskTitle" placeholder="请输入任务标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="任务说明">
        <el-input
          v-model="formData.taskDescription"
          type="textarea"
          :rows="5"
          placeholder="请输入任务说明"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="阶段序号" required>
            <el-input-number v-model="formData.stageNo" :min="1" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="截止时间">
            <el-date-picker
              v-model="formData.deadline"
              type="datetime"
              placeholder="可选，不填则表示不限"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="提交要求">
        <div class="requirement-grid">
          <div class="requirement-item">
            <span>周报</span>
            <el-switch v-model="formData.needReport" :active-value="1" :inactive-value="0" />
          </div>
          <div class="requirement-item">
            <span>源码</span>
            <el-switch v-model="formData.needSourceCode" :active-value="1" :inactive-value="0" />
          </div>
          <div class="requirement-item">
            <span>PDF</span>
            <el-switch v-model="formData.needPdf" :active-value="1" :inactive-value="0" />
          </div>
          <div class="requirement-item">
            <span>截图</span>
            <el-switch v-model="formData.needScreenshot" :active-value="1" :inactive-value="0" />
          </div>
          <div class="requirement-item">
            <span>演示链接</span>
            <el-switch v-model="formData.needDemoUrl" :active-value="1" :inactive-value="0" />
          </div>
        </div>
      </el-form-item>

      <el-form-item label="状态" required>
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
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
.stage-task-form {
  padding-top: 8px;
}

.requirement-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
  width: 100%;
}

.requirement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #edf5ff 100%);
  color: #2f3d4f;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>