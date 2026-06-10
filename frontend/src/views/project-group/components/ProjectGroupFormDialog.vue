<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupFormData } from '@/api/projectGroup'
import type { ProjectTopicOption } from '@/api/projectTopic'
import type { TrainingBatchOption } from '@/api/trainingBatch'
import type { UserOptionItem } from '@/api/sysUser'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: ProjectGroupFormData
  batchOptions: TrainingBatchOption[]
  topicOptions: ProjectTopicOption[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建项目组' : '编辑项目组'))

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
  <el-dialog v-model="visible" :title="dialogTitle" width="900px" destroy-on-close>
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

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="项目组名称" required>
            <el-input
              v-model="formData.groupName"
              maxlength="100"
              show-word-limit
              placeholder="请输入项目组名称"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="组长" required>
            <el-select
              v-model="formData.leaderId"
              clearable
              filterable
              placeholder="请选择组长"
              style="width: 100%"
              :disabled="mode === 'edit'"
            >
              <el-option
                v-for="item in studentOptions"
                :key="item.id"
                :label="`${item.realName}（${item.username}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

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
