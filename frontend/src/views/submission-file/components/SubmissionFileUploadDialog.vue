<script setup lang="ts">
import { computed } from 'vue'
import type { UploadFile } from 'element-plus'
import type { StageSubmissionItem } from '@/api/stageSubmission'
import type { SubmissionFileUploadFormData } from '@/api/submissionFile'
const p=defineProps<{modelValue:boolean;formData:SubmissionFileUploadFormData;submissionOptions:StageSubmissionItem[];submitting:boolean}>()
const e=defineEmits<{ 'update:modelValue':[boolean]; submit:[] }>()
const v=computed({get:()=>p.modelValue,set:(x:boolean)=>e('update:modelValue',x)})
const opts=[['REPORT','周报'],['SOURCE_CODE','源码'],['PDF','PDF 文档'],['SCREENSHOT','截图'],['DEMO','演示说明'],['OTHER','其他']]
const label=(i:StageSubmissionItem)=>`V${i.versionNo} · ${i.summary?.trim()||'未填写摘要'}`
const ch=(f:UploadFile)=>{p.formData.file=f.raw||null}
</script>
<template>
  <el-dialog v-model="v" title="上传提交附件" width="680px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="目标提交" required>
        <el-select v-model="formData.submissionId" filterable placeholder="请选择阶段提交" style="width:100%">
          <el-option v-for="i in submissionOptions" :key="i.id" :label="label(i)" :value="i.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="业务类型">
        <el-select v-model="formData.bizType" filterable allow-create clearable default-first-option placeholder="请选择业务类型" style="width:100%">
          <el-option v-for="i in opts" :key="i[0]" :label="i[1]" :value="i[0]" />
        </el-select>
      </el-form-item>
      <el-form-item label="选择文件" required>
        <div style="width:100%">
          <el-upload :auto-upload="false" :show-file-list="false" :limit="1" @change="ch">
            <el-button type="primary">选择本地文件</el-button>
          </el-upload>
          <div v-if="formData.file" style="margin-top:12px">{{ formData.file.name }}（{{ (formData.file.size/1024/1024).toFixed(2) }} MB）<el-button type="danger" link @click="formData.file=null">移除</el-button></div>
          <div v-else style="margin-top:12px;color:#909399">支持真实文件上传，大小与扩展名限制以后端配置为准</div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="v=false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="e('submit')">开始上传</el-button>
    </template>
  </el-dialog>
</template>