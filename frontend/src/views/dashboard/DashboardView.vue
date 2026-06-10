<script setup lang="ts">
import { computed } from 'vue'

import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '未登录用户')
const username = computed(() => userStore.userInfo?.username || '--')
const roles = computed(() => (userStore.userInfo?.roles || []).join(' / ') || '暂无角色')
</script>

<template>
  <div class="dashboard-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Dashboard</div>
          <h1>欢迎回来，{{ displayName }}</h1>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">当前用户</div>
          </template>
          <div class="card-content">
            <div class="info-line"><span>登录名：</span><strong>{{ username }}</strong></div>
            <div class="info-line"><span>显示名：</span><strong>{{ displayName }}</strong></div>
            <div class="info-line"><span>角色：</span><strong>{{ roles }}</strong></div>
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
  gap: 20px;
}
.hero-card {
  border: none;
  border-radius: 24px;
  background: linear-gradient(135deg, #eaf4ff 0%, #f8fbff 55%, #ffffff 100%);
  box-shadow: 0 18px 40px rgb(58 120 201 / 10%);
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
  background: #dcecff;
  color: #2f6fdb;
  font-size: 12px;
  font-weight: 600;
}
.hero-content h1 {
  margin: 0 0 10px;
  color: #1f2d3d;
}
.hero-content p {
  margin: 0;
  color: #6b7a90;
  line-height: 1.8;
}
.info-card {
  min-height: 160px;
  border-radius: 20px;
}
.card-header {
  font-weight: 600;
  color: #303133;
}
.card-content {
  line-height: 1.9;
  color: #606266;
  min-height: 92px;
}
.info-line span {
  color: #7b8ba1;
}
</style>
