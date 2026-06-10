<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Folder, House, Key, Paperclip, SwitchButton, User } from '@element-plus/icons-vue'

import { changePasswordApi } from '@/api/auth'
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '未登录用户')
const roleText = computed(() => (userStore.userInfo?.roles || []).join(' / ') || '暂无角色')
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('ADMIN'))
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const openChangePassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword.trim()) {
    ElMessage.warning('请先填写原密码')
    return
  }
  if (!passwordForm.newPassword.trim()) {
    ElMessage.warning('请先填写新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSubmitting.value = true
  try {
    await changePasswordApi(passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    userStore.clearLoginState()
    router.push('/login')
  } finally {
    passwordSubmitting.value = false
  }
}

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
          <div class="logo-mark">
            <img src="/064b08b0-66ae-46d6-85c4-bf0d3b1fb191.png" alt="平台标识" class="logo-image" />
          </div>
          <div>
            <div class="logo-title">学校实训管理平台</div>
          </div>
        </div>

        <el-menu router default-active="/" class="menu" background-color="transparent" text-color="#dbeafe" active-text-color="#ffffff">
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/notices">
            <el-icon><Document /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/stage-tasks">
            <el-icon><Folder /></el-icon>
            <span>阶段任务</span>
          </el-menu-item>
          <el-menu-item index="/stage-submissions">
            <el-icon><Document /></el-icon>
            <span>阶段提交</span>
          </el-menu-item>
          <el-menu-item index="/submission-files">
            <el-icon><Paperclip /></el-icon>
            <span>提交附件</span>
          </el-menu-item>
          <el-menu-item index="/review-records">
            <el-icon><Document /></el-icon>
            <span>审核记录</span>
          </el-menu-item>
          <el-menu-item index="/score-records">
            <el-icon><Document /></el-icon>
            <span>成绩记录</span>
          </el-menu-item>
          <el-menu-item index="/weekly-reports">
            <el-icon><Document /></el-icon>
            <span>周报管理</span>
          </el-menu-item>
          <el-menu-item index="/defense-schedules">
            <el-icon><Document /></el-icon>
            <span>答辩安排</span>
          </el-menu-item>
          <el-menu-item index="/defense-records">
            <el-icon><Document /></el-icon>
            <span>答辩记录</span>
          </el-menu-item>
          <el-menu-item index="/topic-applications">
            <el-icon><Document /></el-icon>
            <span>选题申请</span>
          </el-menu-item>
          <el-menu-item index="/project-topics">
            <el-icon><Document /></el-icon>
            <span>课题管理</span>
          </el-menu-item>
          <el-menu-item index="/project-groups">
            <el-icon><Document /></el-icon>
            <span>项目组管理</span>
          </el-menu-item>
          <el-menu-item index="/project-group-applications">
            <el-icon><Document /></el-icon>
            <span>建组申请</span>
          </el-menu-item>
          <el-menu-item index="/project-group-members">
            <el-icon><Document /></el-icon>
            <span>项目组成员</span>
          </el-menu-item>
          <el-menu-item index="/training-batches">
            <el-icon><Document /></el-icon>
            <span>实训批次</span>
          </el-menu-item>
          <el-menu-item index="/edu-classes">
            <el-icon><Document /></el-icon>
            <span>班级管理</span>
          </el-menu-item>
          <el-menu-item index="/edu-courses">
            <el-icon><Document /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/operation-logs">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/sys-users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="layout-header">
          <div class="header-left">
            <h2>后台管理端</h2>
          </div>

          <div class="header-right">
            <div class="user-panel">
              <div class="user-name">{{ displayName }}</div>
              <div class="user-role">{{ roleText }}</div>
            </div>
            <el-button plain @click="openChangePassword">
              <el-icon><Key /></el-icon>
              修改密码
            </el-button>
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

    <ChangePasswordDialog
      v-model="passwordDialogVisible"
      :form-data="passwordForm"
      :submitting="passwordSubmitting"
      @submit="handleChangePassword"
    />
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
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgb(255 255 255 / 16%);
  overflow: hidden;
  flex-shrink: 0;
}
.logo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.logo-title {
  font-size: 16px;
  font-weight: 700;
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
