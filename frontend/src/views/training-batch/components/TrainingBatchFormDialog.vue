<script setup lang="ts">
import { computed } from 'vue'

import type { TrainingBatchFormData } from '@/api/trainingBatch'
import type { EduCourseItem } from '@/api/eduCourse'
import type { EduClassItem } from '@/api/eduClass'
import type { UserOptionItem } from '@/api/sysUser'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  formData: TrainingBatchFormData
  courseOptions: EduCourseItem[]
  teacherOptions: UserOptionItem[]
  teacherSelectDisabled: boolean
  classOptions: EduClassItem[]
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

const dialogTitle = computed(() => (props.mode === 'create' ? '新建实训批次' : '编辑实训批次'))
</script>

<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="920px" destroy-on-close>
    <el-form label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="批次名称" required>
            <el-input
              v-model="formData.batchName"
              maxlength="100"
              show-word-limit
              placeholder="请输入批次名称"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="学期名称">
            <el-input
              v-model="formData.termName"
              maxlength="100"
              show-word-limit
              placeholder="例如：2025-2026-1"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="课程" required>
            <el-select v-model="formData.courseId" clearable filterable placeholder="请选择课程" style="width: 100%">
              <el-option v-for="item in courseOptions" :key="item.id" :label="item.courseName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="教师" required>
            <el-select
              v-model="formData.teacherId"
              clearable
              filterable
              :disabled="teacherSelectDisabled"
              placeholder="请选择教师"
              style="width: 100%"
            >
              <el-option
                v-for="item in teacherOptions"
                :key="item.id"
                :label="item.realName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="班级" required>
            <el-select v-model="formData.classId" clearable filterable placeholder="请选择班级" style="width: 100%">
              <el-option v-for="item in classOptions" :key="item.id" :label="item.className" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="开始时间" required>
            <el-date-picker
              v-model="formData.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="结束时间" required>
            <el-date-picker
              v-model="formData.endTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="答辩时间">
            <el-date-picker
              v-model="formData.defenseTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择答辩时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="批次说明">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="请输入实训批次说明"
        />
      </el-form-item>

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
