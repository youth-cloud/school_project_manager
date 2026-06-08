<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Folder, House, SwitchButton, User } from '@element-plus/icons-vue'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '未登录用户')
const roleText = computed(() => (userStore.userInfo?.roles || []).join(' / ') || '暂无角色')

const handleLogout = async () => {
  await userStore.logoutAction()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  if (!userStore.userInfo && userStore.token) {
    userStore.fetchCurrentUser().catch(() => {
      userStore.clearLoginState()
      router.push('/login')
    })
  }
})
</script>

<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <el-aside width="236px" class="layout-aside">
        <div class="logo">
          <div class="logo-mark">SP</div>
          <div>
            <div class="logo-title">学校实训管理平台</div>
            <div class="logo-subtitle">Blue & White Admin</div>
          </div>
        </div>

        <el-menu router default-active="/" class="menu" background-color="transparent" text-color="#dbeafe" active-text-color="#ffffff">
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/login">
            <el-icon><User /></el-icon>
            <span>登录页</span>
          </el-menu-item>
          <el-menu-item index="/">
            <el-icon><Document /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/">
            <el-icon><Folder /></el-icon>
            <span>项目管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="layout-header">
          <div class="header-left">
            <h2>后台管理端</h2>
            <span>蓝白风格 · 登录态接入中</span>
          </div>

          <div class="header-right">
            <div class="user-panel">
              <div class="user-name">{{ displayName }}</div>
              <div class="user-role">{{ roleText }}</div>
            </div>
            <el-button type="primary" plain @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-header>

        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  background: linear-gradient(180deg, #f3f8ff 0%, #f7fbff 100%);
}
.layout-container {
  min-height: 100vh;
}
.layout-aside {
  background: linear-gradient(180deg, #0f3f91 0%, #174ea6 100%);
  color: #fff;
  box-shadow: 6px 0 24px rgb(21 77 165 / 16%);
}
.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 72px;
  padding: 0 18px;
  border-bottom: 1px solid rgb(255 255 255 / 12%);
}
.logo-mark {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgb(255 255 255 / 16%);
  font-weight: 700;
}
.logo-title {
  font-size: 16px;
  font-weight: 700;
}
.logo-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: rgb(219 234 254 / 90%);
}
.menu {
  border-right: none;
  padding: 12px 8px;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 24px;
  background: rgb(255 255 255 / 88%);
  border-bottom: 1px solid #e6eef8;
  backdrop-filter: blur(12px);
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.header-left h2 {
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
}
.header-left span {
  color: #6b7a90;
  font-size: 14px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.user-panel {
  text-align: right;
}
.user-name {
  font-weight: 600;
  color: #1f2d3d;
}
.user-role {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8ba1;
}
.layout-main {
  padding: 24px;
}
</style>