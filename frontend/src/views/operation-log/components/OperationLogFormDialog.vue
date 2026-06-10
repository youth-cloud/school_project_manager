<script setup lang="ts">
import { computed } from 'vue'

import type { OperationLogFormData } from '@/api/operationLog'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: OperationLogFormData
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建操作日志' : '编辑操作日志'))

const methodOptions = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
const resultOptions = ['SUCCESS', 'FAIL', 'PENDING', 'UNKNOWN']
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="820px" destroy-on-close>
    <el-form label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="模块名称" required>
            <el-input
              v-model="formData.moduleName"
              maxlength="100"
              show-word-limit
              placeholder="例如：Notice / StageSubmission / Auth"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="操作类型" required>
            <el-input
              v-model="formData.operationType"
              maxlength="50"
              show-word-limit
              placeholder="例如：CREATE / UPDATE / DELETE / LOGIN"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="请求方式">
            <el-select v-model="formData.requestMethod" clearable filterable allow-create default-first-option style="width: 100%">
              <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="操作结果">
            <el-select v-model="formData.result" clearable filterable allow-create default-first-option style="width: 100%">
              <el-option v-for="item in resultOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="请求地址">
        <el-input
          v-model="formData.requestUri"
          maxlength="255"
          show-word-limit
          placeholder="例如：/api/notices/page"
        />
      </el-form-item>

      <el-form-item label="IP 地址">
        <el-input
          v-model="formData.ip"
          maxlength="50"
          show-word-limit
          placeholder="例如：127.0.0.1"
        />
      </el-form-item>

      <el-form-item label="操作描述">
        <el-input
          v-model="formData.operationDesc"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="请输入操作说明"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">保存</el-button>
    </template>
  </el-dialog>
</template>
