<script setup lang="ts">
import { computed } from 'vue'

import type { NoticeFormData } from '@/api/notice'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: NoticeFormData
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建公告' : '编辑公告'))

const handleClose = () => {
  visible.value = false
}

const handleSubmit = () => {
  emit('submit')
}
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="640px" destroy-on-close>
    <el-form label-width="90px" class="notice-form">
      <el-form-item label="公告标题" required>
        <el-input v-model="formData.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="公告内容">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="6"
          placeholder="请输入公告内容"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="目标角色">
        <el-select v-model="formData.targetRole" placeholder="请选择目标角色" style="width: 100%">
          <el-option label="全体可见" value="ALL" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="学生" value="STUDENT" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">已发布</el-radio>
          <el-radio :value="0">草稿/停用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="发布时间">
        <el-date-picker
          v-model="formData.publishTime"
          type="datetime"
          placeholder="可选，不填则由后端自动处理"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
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
.notice-form {
  padding-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>