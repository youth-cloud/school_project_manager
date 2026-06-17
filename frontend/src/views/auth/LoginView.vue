<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const beianText = '吉ICP备2026003158号-1'
const beianUrl = 'https://beian.miit.gov.cn/'

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
    <div class="bg-grid"></div>

    <div class="login-shell">
      <div class="login-brand-panel">
        <span class="title-badge">School Project Manager</span>
        <h1>学校实训管理平台</h1>
      </div>

      <div class="login-card">
        <div class="login-title">
          <div class="login-title-top">欢迎登录</div>
          <h2>进入平台工作台</h2>
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

    <a class="beian-link" :href="beianUrl" target="_blank" rel="noopener noreferrer">
      {{ beianText }}
    </a>
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

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgb(118 153 211 / 8%) 1px, transparent 1px),
    linear-gradient(90deg, rgb(118 153 211 / 8%) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(180deg, rgb(0 0 0 / 22%) 0%, rgb(0 0 0 / 0%) 70%);
  pointer-events: none;
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

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.12fr 0.88fr;
  gap: 24px;
  align-items: stretch;
}

.login-brand-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 44px 40px;
  border: 1px solid rgb(255 255 255 / 68%);
  border-radius: 28px;
  background: linear-gradient(180deg, rgb(255 255 255 / 58%) 0%, rgb(255 255 255 / 28%) 100%);
  box-shadow: 0 24px 60px rgb(33 89 166 / 10%);
  backdrop-filter: blur(18px);
}

.login-brand-panel h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.2;
  color: #1f2d3d;
}

.login-card {
  position: relative;
  z-index: 1;
  padding: 36px 34px 28px;
  border: 1px solid rgb(255 255 255 / 70%);
  border-radius: 24px;
  background: rgb(255 255 255 / 88%);
  box-shadow: 0 24px 60px rgb(33 89 166 / 15%);
  backdrop-filter: blur(18px);
}

.login-title {
  margin-bottom: 24px;
}

.login-title-top {
  color: #7b8ba4;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-title h2 {
  margin: 10px 0 0;
  font-size: 30px;
  color: #1d2b45;
}

.login-title {
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

.login-form {
  margin-bottom: 22px;
}

.login-button {
  width: 100%;
  margin-top: 10px;
  border-radius: 12px;
  box-shadow: 0 12px 24px rgb(64 158 255 / 20%);
}

.beian-link {
  position: absolute;
  left: 50%;
  bottom: 18px;
  transform: translateX(-50%);
  color: #6d7f9d;
  font-size: 13px;
  line-height: 1;
}

.beian-link:hover {
  color: #2f6bff;
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-brand-panel {
    padding: 34px 28px;
  }

  .login-brand-panel h1 {
    font-size: 34px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 16px 16px 56px;
  }

  .login-brand-panel {
    padding: 28px 22px;
  }

  .login-card {
    padding: 28px 22px 22px;
  }

  .login-brand-panel h1,
  .login-title h2 {
    font-size: 26px;
  }
}
</style>
