<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, CollectionTag, DataAnalysis, FolderOpened, Notebook, User } from '@element-plus/icons-vue'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '未登录用户')
const username = computed(() => userStore.userInfo?.username || '--')
const roleLabelMap: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
}

const roles = computed(() => {
  const items = userStore.userInfo?.roles || []
  return items.map((item) => roleLabelMap[item] || item).join(' / ') || '暂无角色'
})

const primaryRole = computed(() => roles.value.split(' / ')[0] || '平台成员')

const quickEntries = computed(() => {
  const roleSet = new Set(userStore.userInfo?.roles || [])
  if (roleSet.has('ADMIN')) {
    return [
      { title: '用户管理', desc: '维护教师、学生与管理员账号', path: '/sys-users', icon: User },
      { title: '实训批次', desc: '管理课程、班级与教师批次关系', path: '/training-batches', icon: FolderOpened },
      { title: '课题管理', desc: '发布与调整实训课题信息', path: '/project-topics', icon: CollectionTag },
      { title: '操作日志', desc: '查看关键操作记录', path: '/operation-logs', icon: DataAnalysis },
    ]
  }
  if (roleSet.has('TEACHER')) {
    return [
      { title: '阶段任务', desc: '发布任务并跟踪完成情况', path: '/stage-tasks', icon: Notebook },
      { title: '建组申请', desc: '审批学生建组申请', path: '/project-group-applications', icon: User },
      { title: '答辩安排', desc: '安排项目组答辩与顺序', path: '/defense-schedules', icon: Bell },
      { title: '成绩记录', desc: '完成项目组与学生成绩登记', path: '/score-records', icon: DataAnalysis },
    ]
  }
  return [
    { title: '公告管理', desc: '查看课程与实训相关通知', path: '/notices', icon: Bell },
    { title: '选题申请', desc: '查看自己的选题与审核状态', path: '/topic-applications', icon: CollectionTag },
    { title: '项目组成员', desc: '查看当前项目组成员与组号', path: '/project-group-members', icon: User },
    { title: '阶段提交', desc: '提交阶段成果与附件材料', path: '/stage-submissions', icon: Notebook },
  ]
})

const statusCards = computed(() => [
  { label: '当前身份', value: primaryRole.value },
  { label: '登录账号', value: username.value },
  { label: '显示名称', value: displayName.value },
])
</script>

<template>
  <div class="dashboard-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Workspace</div>
          <h1>欢迎回来，{{ displayName }}</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前角色</div>
          <div class="hero-side-value">{{ roles }}</div>
        </div>
      </div>
    </el-card>

    <div class="overview-grid">
      <el-card v-for="card in statusCards" :key="card.label" class="overview-card" shadow="never">
        <div class="overview-label">{{ card.label }}</div>
        <div class="overview-value">{{ card.value }}</div>
      </el-card>
    </div>

    <el-row :gutter="20" class="content-grid">
      <el-col :xl="16" :lg="15" :md="24">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <div class="card-header">
              <span>常用入口</span>
            </div>
          </template>
          <div class="quick-grid">
            <button
              v-for="item in quickEntries"
              :key="item.path"
              type="button"
              class="quick-entry"
              @click="router.push(item.path)"
            >
              <div class="quick-entry-icon">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="quick-entry-title">{{ item.title }}</div>
              <div class="quick-entry-desc">{{ item.desc }}</div>
            </button>
          </div>
        </el-card>
      </el-col>

      <el-col :xl="8" :lg="9" :md="24">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-header">
              <span>当前用户</span>
            </div>
          </template>
          <div class="profile-head">
            <div class="profile-avatar">{{ displayName.slice(0, 1) }}</div>
            <div>
              <div class="profile-name">{{ displayName }}</div>
              <div class="profile-role">{{ roles }}</div>
            </div>
          </div>
          <div class="profile-list">
            <div class="profile-line"><span>登录名</span><strong>{{ username }}</strong></div>
            <div class="profile-line"><span>显示名</span><strong>{{ displayName }}</strong></div>
            <div class="profile-line"><span>当前身份</span><strong>{{ primaryRole }}</strong></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card {
  border: none;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(116, 166, 255, 0.24), transparent 26%),
    linear-gradient(135deg, #eef5ff 0%, #f7fbff 55%, #ffffff 100%);
  box-shadow: 0 18px 40px rgb(58 120 201 / 10%);
}

.hero-content {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
}

.hero-main {
  max-width: 720px;
}

.hero-badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #dcecff;
  color: #2f6fdb;
  font-size: 12px;
  font-weight: 600;
}

.hero-content h1 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #1f2d3d;
}

.hero-side {
  min-width: 250px;
  padding: 20px 22px;
  border: 1px solid rgba(120, 148, 196, 0.12);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.7);
}

.hero-side-label {
  color: #7c8ca6;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-side-value {
  margin-top: 10px;
  color: #1d2b45;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.overview-card {
  padding: 2px;
}

.overview-label {
  color: #7a8aa4;
  font-size: 13px;
}

.overview-value {
  margin-top: 10px;
  color: #1d2b45;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.4;
}

.content-grid {
  margin: 0;
}

.quick-card,
.profile-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 700;
  color: #303133;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.quick-entry {
  padding: 18px;
  border: 1px solid rgba(120, 148, 196, 0.14);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(246, 250, 255, 0.95) 0%, rgba(255, 255, 255, 0.98) 100%);
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.quick-entry:hover {
  transform: translateY(-2px);
  border-color: rgba(73, 126, 237, 0.26);
  box-shadow: 0 16px 28px rgba(53, 97, 178, 0.10);
}

.quick-entry-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #e8f1ff 0%, #f4f8ff 100%);
  color: #2f6bff;
  font-size: 18px;
}

.quick-entry-title {
  margin-top: 16px;
  color: #1d2b45;
  font-size: 17px;
  font-weight: 700;
}

.quick-entry-desc {
  margin-top: 8px;
  color: #7686a1;
  line-height: 1.7;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 14px;
}

.profile-avatar {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: linear-gradient(135deg, #2f6bff 0%, #7da6ff 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 14px 24px rgba(47, 107, 255, 0.22);
}

.profile-name {
  color: #1d2b45;
  font-size: 20px;
  font-weight: 700;
}

.profile-role {
  margin-top: 6px;
  color: #7686a1;
}

.profile-list {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(246, 250, 255, 0.9);
  color: #73839e;
}

.profile-line strong {
  color: #1d2b45;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .hero-content {
    flex-direction: column;
  }

  .overview-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 28px;
  }
}
</style>
