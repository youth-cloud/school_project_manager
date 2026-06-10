<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getProfileApi, updateProfileApi, type ProfileInfo } from '@/api/auth'

const loading = ref(false)
const submitting = ref(false)
const profile = ref<ProfileInfo | null>(null)

const formData = reactive({
  phone: '',
  email: '',
})

const roleText = computed(() => (profile.value?.roles || []).join(' / ') || '暂无角色')
const statusText = computed(() => (profile.value?.status === 1 ? '启用' : '停用'))
const isStudent = computed(() => (profile.value?.roles || []).includes('STUDENT'))

const fillForm = (data: ProfileInfo) => {
  formData.phone = data.phone || ''
  formData.email = data.email || ''
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getProfileApi()
    profile.value = res.data
    fillForm(res.data)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await updateProfileApi({
      phone: formData.phone.trim() || undefined,
      email: formData.email.trim() || undefined,
    })
    profile.value = res.data
    fillForm(res.data)
    ElMessage.success('个人信息更新成功')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="profile-page" v-loading="loading">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Profile</div>
          <h1>个人信息</h1>
          <p>这里维护当前登录账号的基础资料与联系方式，密码修改可直接使用顶部入口。</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title">基础信息</div>
          </template>

          <template v-if="profile">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
              <el-descriptions-item label="真实姓名">{{ profile.realName }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
              <el-descriptions-item label="账号状态">{{ statusText }}</el-descriptions-item>
              <el-descriptions-item v-if="isStudent" label="学号">
                {{ profile.studentNo || '--' }}
              </el-descriptions-item>
              <el-descriptions-item v-if="isStudent" label="班级">
                {{ profile.className || '--' }}
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title">联系方式</div>
          </template>

          <el-form label-width="88px">
            <el-form-item label="手机号">
              <el-input
                v-model="formData.phone"
                maxlength="20"
                placeholder="请输入手机号"
              />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input
                v-model="formData.email"
                maxlength="100"
                placeholder="请输入邮箱"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.info-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 14px 32px rgb(57 118 201 / 8%);
}

.hero-card {
  background: linear-gradient(135deg, #eef7ff 0%, #f8fbff 58%, #ffffff 100%);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.hero-badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #dcebff;
  color: #2f6fdb;
  font-size: 12px;
  font-weight: 600;
}

.hero-content h1 {
  margin: 0 0 10px;
  color: #1f2d3d;
  font-size: 28px;
}

.hero-content p {
  margin: 0;
  color: #6b7a90;
  line-height: 1.8;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}
</style>
