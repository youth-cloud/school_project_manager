<script setup lang="ts">
import { computed } from 'vue'

import type { ProjectGroupMemberCreateFormData } from '@/api/projectGroupMember'

const props = defineProps<{
  modelValue: boolean
  formData: ProjectGroupMemberCreateFormData
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
</script>

<template>
  <el-dialog v-model="visible" title="加入项目组" width="720px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="项目组 ID" required>
        <el-input
          v-model="formData.groupId"
          placeholder="请输入教师或组长提供的项目组 ID"
          clearable
        />
      </el-form-item>

      <el-alert
        title="只有拿到正确的项目组 ID 才能直接加入，当前加入后默认作为普通成员。"
        type="info"
        :closable="false"
        show-icon
      />

      <el-form-item label="状态" required>
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">确认加入</el-button>
    </template>
  </el-dialog>
</template>
