<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupApplicationCreateFormData } from '@/api/projectGroupApplication'
import type { ProjectTopicOption } from '@/api/projectTopic'
import type { TrainingBatchOption } from '@/api/trainingBatch'

const props = defineProps<{
  modelValue: boolean
  formData: ProjectGroupApplicationCreateFormData
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
</script>

<template>
  <el-dialog v-model="visible" title="发起建组申请" width="920px" destroy-on-close>
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
                :label="item.topicName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="项目组名称" required>
        <el-input
          v-model="formData.groupName"
          maxlength="100"
          show-word-limit
          placeholder="请输入项目组名称"
        />
      </el-form-item>

      <el-alert
        title="当前申请人会默认作为组长发起建组，项目组正式建立后，其他成员可再通过项目组成员模块加入。"
        type="info"
        :closable="false"
        show-icon
      />

      <el-form-item label="项目名称">
        <el-input
          v-model="formData.projectName"
          maxlength="200"
          show-word-limit
          placeholder="请输入项目名称"
        />
      </el-form-item>

      <el-form-item label="项目简介">
        <el-input
          v-model="formData.projectDescription"
          type="textarea"
          :rows="5"
          maxlength="2000"
          show-word-limit
          placeholder="请输入项目简介"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="仓库地址">
            <el-input
              v-model="formData.repoUrl"
              maxlength="255"
              show-word-limit
              placeholder="例如：https://github.com/xxx/xxx"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="部署地址">
            <el-input
              v-model="formData.deployUrl"
              maxlength="255"
              show-word-limit
              placeholder="例如：http://demo.xxx.com"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="建组理由">
        <el-input
          v-model="formData.applyReason"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="请输入建组申请理由"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">提交申请</el-button>
    </template>
  </el-dialog>
</template>
