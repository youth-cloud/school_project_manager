<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  CollectionTag,
  DataAnalysis,
  Document,
  Files,
  Folder,
  House,
  Key,
  Medal,
  Memo,
  Notebook,
  Paperclip,
  Reading,
  School,
  SetUp,
  SwitchButton,
  Tickets,
  User,
  UserFilled,
} from '@element-plus/icons-vue'

import { changePasswordApi } from '@/api/auth'
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const logoUrl = `${import.meta.env.BASE_URL}064b08b0-66ae-46d6-85c4-bf0d3b1fb191.png`
const beianText = '吉ICP备2026003158号-1'
const beianUrl = 'https://beian.miit.gov.cn/'

const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '未登录用户')
const roleLabelMap: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
}
const roleText = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.map((item) => roleLabelMap[item] || item).join(' / ') || '暂无角色'
})
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('ADMIN'))
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const navSections = [
  {
    title: '工作台',
    items: [
      { index: '/', label: '首页', icon: House },
      { index: '/profile', label: '个人信息', icon: User },
    ],
  },
  {
    title: '项目流程',
    items: [
      { index: '/notices', label: '公告管理', icon: Bell },
      { index: '/project-topics', label: '课题管理', icon: CollectionTag },
      { index: '/topic-applications', label: '选题申请', icon: Tickets },
      { index: '/project-group-applications', label: '建组申请', icon: Memo },
      { index: '/project-groups', label: '项目组管理', icon: School },
      { index: '/project-group-members', label: '项目组成员', icon: UserFilled },
    ],
  },
  {
    title: '过程管理',
    items: [
      { index: '/stage-tasks', label: '阶段任务', icon: Folder },
      { index: '/stage-submissions', label: '阶段提交', icon: Document },
      { index: '/submission-files', label: '提交附件', icon: Paperclip },
      { index: '/review-records', label: '审核记录', icon: Notebook },
      { index: '/weekly-reports', label: '周报管理', icon: Reading },
    ],
  },
  {
    title: '考核结果',
    items: [
      { index: '/defense-schedules', label: '答辩安排', icon: Files },
      { index: '/defense-records', label: '答辩记录', icon: Medal },
      { index: '/score-records', label: '成绩记录', icon: DataAnalysis },
    ],
  },
  {
    title: '基础数据',
    items: [
      { index: '/training-batches', label: '实训批次', icon: SetUp },
      { index: '/edu-classes', label: '班级管理', icon: School },
      { index: '/edu-courses', label: '课程管理', icon: CollectionTag },
      { index: '/operation-logs', label: '操作日志', icon: Document },
      { index: '/sys-users', label: '用户管理', icon: User, adminOnly: true },
    ],
  },
]

const visibleNavSections = computed(() =>
  navSections
    .map((section) => ({
      ...section,
      items: section.items.filter((item) => !item.adminOnly || isAdmin.value),
    }))
    .filter((section) => section.items.length > 0),
)

const currentPageTitle = computed(() => {
  for (const section of visibleNavSections.value) {
    const matched = section.items.find((item) => item.index === route.path)
    if (matched) {
      return matched.label
    }
  }
  return '首页'
})

const pageGroupTitle = computed(() => {
  const matchedSection = visibleNavSections.value.find((section) =>
    section.items.some((item) => item.index === route.path),
  )
  return matchedSection?.title || '工作台'
})

const currentDateText = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }).format(new Date()),
)

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const userInitial = computed(() => (displayName.value || 'U').trim().charAt(0).toUpperCase())

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
            <img :src="logoUrl" alt="平台标识" class="logo-image" />
          </div>
          <div>
            <div class="logo-title">学校实训管理平台</div>
            <div class="logo-subtitle">Project Training Workspace</div>
          </div>
        </div>

        <el-menu
          router
          :default-active="route.path"
          class="menu"
          background-color="transparent"
          text-color="#dbeafe"
          active-text-color="#ffffff"
        >
          <template v-for="section in visibleNavSections" :key="section.title">
            <div class="menu-section-title">{{ section.title }}</div>
            <el-menu-item v-for="item in section.items" :key="item.index" :index="item.index">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="layout-header">
          <div class="header-left">
            <div class="header-title-wrap">
              <div class="header-kicker">{{ pageGroupTitle }}</div>
              <h2>{{ currentPageTitle }}</h2>
            </div>
            <div class="header-date">{{ currentDateText }}</div>
          </div>

          <div class="header-right">
            <div class="user-panel">
              <div class="user-avatar">{{ userInitial }}</div>
              <div class="user-meta">
                <div class="user-name">{{ greetingText }}，{{ displayName }}</div>
                <div class="user-role">{{ roleText }}</div>
              </div>
            </div>
            <el-button class="header-action" plain @click="openChangePassword">
              <el-icon><Key /></el-icon>
              修改密码
            </el-button>
            <el-button class="header-action primary-outline" type="primary" plain @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-header>

        <el-main class="layout-main">
          <div class="layout-main-content">
            <router-view />
          </div>
          <div class="layout-footer">
            <a :href="beianUrl" target="_blank" rel="noopener noreferrer">{{ beianText }}</a>
          </div>
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
}

.layout-container {
  min-height: 100vh;
}

.layout-aside {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgb(115 176 255 / 22%), transparent 24%),
    linear-gradient(180deg, #0f3f91 0%, #184aa0 48%, #0f3278 100%);
  color: #fff;
  box-shadow: 8px 0 30px rgb(21 77 165 / 16%);
}

.layout-aside::after {
  content: '';
  position: absolute;
  inset: auto -60px -80px auto;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgb(255 255 255 / 16%) 0%, rgb(255 255 255 / 0%) 68%);
  pointer-events: none;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 78px;
  padding: 0 20px;
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

.logo-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: rgb(219 234 254 / 76%);
}

.menu {
  border-right: none;
  padding: 14px 10px 20px;
  height: calc(100vh - 78px);
  overflow-y: auto;
}

:deep(.el-menu-item) {
  height: 46px;
  margin: 4px 0;
  border-radius: 14px;
}

:deep(.el-menu-item .el-icon) {
  font-size: 16px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgb(255 255 255 / 20%) 0%, rgb(255 255 255 / 12%) 100%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 12%);
}

.menu-section-title {
  padding: 14px 12px 6px;
  color: rgb(219 234 254 / 60%);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 24px;
  background: rgb(255 255 255 / 72%);
  border-bottom: 1px solid rgba(120, 148, 196, 0.12);
  backdrop-filter: blur(18px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.header-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-kicker {
  color: #6d7f9d;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  color: #1d2b45;
}

.header-date {
  padding: 8px 12px;
  border: 1px solid rgba(120, 148, 196, 0.14);
  border-radius: 999px;
  background: rgb(255 255 255 / 68%);
  color: #6d7f9d;
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px 8px 8px;
  border: 1px solid rgba(120, 148, 196, 0.12);
  border-radius: 999px;
  background: rgb(255 255 255 / 72%);
}

.user-avatar {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #2f6bff 0%, #74a3ff 100%);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 10px 20px rgb(47 107 255 / 24%);
}

.user-meta {
  text-align: left;
}

.user-name {
  font-weight: 600;
  color: #1d2b45;
}

.user-role {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8ba1;
}

.header-action {
  height: 40px;
  padding: 0 16px;
  border-color: rgba(120, 148, 196, 0.18);
  background: rgb(255 255 255 / 72%);
}

.primary-outline {
  border-color: rgba(47, 107, 255, 0.18);
}

.layout-main {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 72px);
  padding: 28px 28px 18px;
}

.layout-main-content {
  flex: 1;
}

.layout-footer {
  display: flex;
  justify-content: center;
  padding-top: 18px;
}

.layout-footer a {
  color: #7b8ba1;
  font-size: 13px;
}

.layout-footer a:hover {
  color: #2f6bff;
}
</style>
