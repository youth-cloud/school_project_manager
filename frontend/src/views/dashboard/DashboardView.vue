<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">当前阶段</div>
          </template>
          <div class="card-content">
            后端主业务、权限、认证、文件上传第一轮已经基本闭环。
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">当前任务</div>
          </template>
          <div class="card-content">
            现在进入前端开发，第 1 步先把布局、路由、登录页骨架搭起来。
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">下一步</div>
          </template>
          <div class="card-content">
            下一步我们会接真实登录接口、token 持久化和路由守卫。
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card" shadow="never">
      <template #header>
        <div class="card-header">你现在看到的是什么</div>
      </template>

      <el-steps :active="1" finish-status="success" align-center>
        <el-step title="路由骨架" description="App.vue 只放 router-view" />
        <el-step title="布局页" description="后台菜单 + 顶部栏 + 内容区" />
        <el-step title="登录页" description="先搭结构，下一步再接后端" />
      </el-steps>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.card-content {
  line-height: 1.8;
  color: #606266;
  min-height: 72px;
}

.welcome-card {
  margin-top: 4px;
}
</style>