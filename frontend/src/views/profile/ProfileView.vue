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
const heroIdentity = computed(() => profile.value?.realName || profile.value?.username || '当前账号')

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
        <div class="hero-main">
          <div class="hero-badge">Profile</div>
          <h1>个人信息</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前账号</div>
          <div class="hero-side-value">{{ heroIdentity }}</div>
          <div class="hero-side-meta">{{ roleText }}</div>
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
  gap: 22px;
}

.hero-card,
.info-card {
  border-radius: 24px;
  border: 1px solid rgba(120, 148, 196, 0.14);
  box-shadow: 0 18px 38px rgb(57 118 201 / 8%);
}

.hero-card {
  background:
    radial-gradient(circle at top right, rgba(116, 166, 255, 0.18), transparent 24%),
    linear-gradient(135deg, #eef7ff 0%, #f8fbff 58%, #ffffff 100%);
}

.hero-content {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
}

.hero-main {
  max-width: 760px;
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
  margin: 0;
  color: #1f2d3d;
  font-size: 30px;
}

.hero-side {
  min-width: 240px;
  padding: 20px 22px;
  border-radius: 20px;
  border: 1px solid rgba(120, 148, 196, 0.12);
  background: rgba(255, 255, 255, 0.72);
}

.hero-side-label {
  color: #7b8ba1;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-side-value {
  margin-top: 10px;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.5;
}

.hero-side-meta {
  margin-top: 10px;
  color: #7b8ba1;
  line-height: 1.7;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}
</style>
