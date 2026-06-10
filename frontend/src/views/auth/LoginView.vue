<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const handleLogin = async () => {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    await userStore.loginAction({ username: form.username.trim(), password: form.password.trim() })
    ElMessage.success('登录成功，欢迎回来')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="bg-circle bg-circle-one"></div>
    <div class="bg-circle bg-circle-two"></div>

    <div class="login-card">
      <div class="login-title">
        <span class="title-badge">School Project Manager</span>
        <h1>学校实训管理平台</h1>
      </div>

      <el-form label-position="top" class="login-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable size="large" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button type="primary" class="login-button" size="large" :loading="loading" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 24px;
  background: linear-gradient(135deg, #eef6ff 0%, #f7fbff 45%, #ffffff 100%);
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(8px);
  opacity: 0.55;
}

.bg-circle-one {
  width: 320px;
  height: 320px;
  top: -60px;
  left: -80px;
  background: radial-gradient(circle, #6bb7ff 0%, rgb(107 183 255 / 10%) 70%);
}

.bg-circle-two {
  width: 260px;
  height: 260px;
  right: -40px;
  bottom: -40px;
  background: radial-gradient(circle, #9cc9ff 0%, rgb(156 201 255 / 10%) 70%);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 440px;
  padding: 36px 34px 28px;
  border: 1px solid rgb(255 255 255 / 70%);
  border-radius: 24px;
  background: rgb(255 255 255 / 88%);
  box-shadow: 0 24px 60px rgb(33 89 166 / 15%);
  backdrop-filter: blur(18px);
}

.login-title {
  text-align: center;
  margin-bottom: 24px;
}

.title-badge {
  display: inline-block;
  margin-bottom: 14px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #e9f3ff;
  color: #2f6fdb;
  font-size: 12px;
  font-weight: 600;
}

.login-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2d3d;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  margin-top: 10px;
  border-radius: 12px;
  box-shadow: 0 12px 24px rgb(64 158 255 / 20%);
}
</style>
