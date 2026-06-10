<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  username: string
  formData: {
    newPassword: string
    confirmPassword: string
  }
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
  <el-dialog v-model="visible" title="重置密码" width="560px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="目标用户">
        <el-input :model-value="username || '--'" disabled />
      </el-form-item>

      <el-form-item label="新密码" required>
        <el-input
          v-model="formData.newPassword"
          type="password"
          show-password
          maxlength="50"
          placeholder="请输入新密码"
        />
      </el-form-item>

      <el-form-item label="确认密码" required>
        <el-input
          v-model="formData.confirmPassword"
          type="password"
          show-password
          maxlength="50"
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="emit('submit')">确认重置</el-button>
    </template>
  </el-dialog>
</template>
