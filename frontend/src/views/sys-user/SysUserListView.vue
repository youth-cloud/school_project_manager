<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Edit, Key, Plus, Refresh, Search, Switch, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getEduClassListApi, type EduClassItem } from '@/api/eduClass'
import {
  adminResetPasswordApi,
  createSysUserApi,
  getRoleOptionsApi,
  getSysUserDetailApi,
  getSysUserPageApi,
  updateSysUserApi,
  updateSysUserStatusApi,
  type RoleOptionItem,
  type SysUserFormData,
  type SysUserItem,
} from '@/api/sysUser'
import { useUserStore } from '@/stores/user'
import SysUserFormDialog from '@/views/sys-user/components/SysUserFormDialog.vue'
import SysUserResetPasswordDialog from '@/views/sys-user/components/SysUserResetPasswordDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const statusSubmittingId = ref('')
const resetSubmitting = ref(false)

const tableData = ref<SysUserItem[]>([])
const total = ref(0)
const roleOptions = ref<RoleOptionItem[]>([])
const classOptions = ref<EduClassItem[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<SysUserItem | null>(null)

const resetDialogVisible = ref(false)
const resetTarget = ref<{ id: string; username: string } | null>(null)

const queryForm = reactive({
  username: '',
  realName: '',
  roleCode: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const userForm = reactive<SysUserFormData>({
  username: '',
  realName: '',
  studentNo: '',
  classId: '',
  password: '',
  confirmPassword: '',
  status: 1,
  roleCodes: [],
})

const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const roles = computed(() => userStore.userInfo?.roles || [])
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isStudentFormUser = computed(() => userForm.roleCodes.includes('STUDENT'))

const roleHint = computed(() => '仅管理员可创建、编辑、启停用户账号并分配角色')

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')
const formatDateTime = (value: string | null) => (value ? value.replace('T', ' ') : '--')
const roleText = (row: SysUserItem) => row.roleNames?.length ? row.roleNames.join(' / ') : row.roleCodes.join(' / ') || '--'
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))

const fetchRoleOptions = async () => {
  if (!isAdmin.value) return
  const res = await getRoleOptionsApi()
  roleOptions.value = res.data
}

const fetchClassOptions = async () => {
  if (!isAdmin.value) return
  const res = await getEduClassListApi()
  classOptions.value = res.data.filter((item) => item.status === 1)
}

const fetchUserList = async () => {
  if (!isAdmin.value) {
    tableData.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const res = await getSysUserPageApi({
      current: pagination.current,
      size: pagination.size,
      username: queryForm.username || undefined,
      realName: queryForm.realName || undefined,
      roleCode: queryForm.roleCode || undefined,
      status: queryForm.status,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

const handleReset = () => {
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.roleCode = ''
  queryForm.status = undefined
  pagination.current = 1
  fetchUserList()
}

const resetUserForm = () => {
  userForm.id = undefined
  userForm.username = ''
  userForm.realName = ''
  userForm.studentNo = ''
  userForm.classId = ''
  userForm.password = ''
  userForm.confirmPassword = ''
  userForm.status = 1
  userForm.roleCodes = []
}

const handleCreate = () => {
  dialogMode.value = 'create'
  resetUserForm()
  dialogVisible.value = true
}

const handleEdit = (row: SysUserItem) => {
  dialogMode.value = 'edit'
  userForm.id = row.id
  userForm.username = row.username
  userForm.realName = row.realName
  userForm.studentNo = row.studentNo || ''
  userForm.classId = row.classId || ''
  userForm.password = ''
  userForm.confirmPassword = ''
  userForm.status = row.status
  userForm.roleCodes = [...row.roleCodes]
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!userForm.realName.trim()) {
    ElMessage.warning('请先填写真实姓名')
    return
  }
  if (!userForm.roleCodes.length) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  if (isStudentFormUser.value) {
    if (!userForm.studentNo.trim()) {
      ElMessage.warning('请先填写学号')
      return
    }
    if (!userForm.classId) {
      ElMessage.warning('请先选择班级')
      return
    }
    userForm.username = userForm.studentNo.trim()
  } else if (!userForm.username.trim()) {
    ElMessage.warning('请先填写用户名')
    return
  }
  if (dialogMode.value === 'create') {
    if (!userForm.password.trim()) {
      ElMessage.warning('请先填写初始密码')
      return
    }
    if (userForm.password !== userForm.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createSysUserApi(userForm)
      ElMessage.success('用户创建成功')
    } else {
      await updateSysUserApi(userForm)
      ElMessage.success('用户修改成功')
    }
    dialogVisible.value = false
    fetchUserList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: SysUserItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getSysUserDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleToggleStatus = async (row: SysUserItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '停用'

  await ElMessageBox.confirm(`确认${actionText}用户 ${row.username} 吗？`, `${actionText}确认`, {
    type: 'warning',
    confirmButtonText: `确认${actionText}`,
    cancelButtonText: '取消',
  })

  statusSubmittingId.value = row.id
  try {
    await updateSysUserStatusApi(row.id, { status: nextStatus })
    ElMessage.success(`用户${actionText}成功`)
    fetchUserList()
  } finally {
    statusSubmittingId.value = ''
  }
}

const openResetPassword = (row: SysUserItem) => {
  resetTarget.value = { id: row.id, username: row.username }
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetDialogVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  if (!resetTarget.value) return
  if (!resetPasswordForm.newPassword.trim()) {
    ElMessage.warning('请先填写新密码')
    return
  }
  if (resetPasswordForm.newPassword !== resetPasswordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  resetSubmitting.value = true
  try {
    await adminResetPasswordApi({
      userId: resetTarget.value.id,
      newPassword: resetPasswordForm.newPassword,
      confirmPassword: resetPasswordForm.confirmPassword,
    })
    ElMessage.success('密码重置成功')
    resetDialogVisible.value = false
  } finally {
    resetSubmitting.value = false
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchUserList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchUserList()
}

onMounted(() => {
  if (!isAdmin.value) return
  Promise.allSettled([
    fetchRoleOptions(),
    fetchClassOptions(),
    fetchUserList(),
  ])
})
</script>

<template>
  <div class="sys-user-page">
    <template v-if="isAdmin">
      <el-card class="hero-card" shadow="never">
        <div class="hero-content">
          <div class="hero-main">
            <div class="hero-badge">User Management</div>
            <h1>用户管理</h1>
          </div>
          <div class="hero-side">
            <div class="hero-side-label">当前视角</div>
            <div class="hero-side-value">{{ roleHint }}</div>
            <div class="hero-side-meta">共 {{ total }} 条记录</div>
          </div>
        </div>
      </el-card>

      <el-card class="filter-card" shadow="never">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="用户名">
            <el-input
              v-model="queryForm.username"
              clearable
              placeholder="请输入用户名"
              style="width: 180px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-form-item label="真实姓名">
            <el-input
              v-model="queryForm.realName"
              clearable
              placeholder="请输入真实姓名"
              style="width: 180px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-form-item label="角色">
            <el-select
              v-model="queryForm.roleCode"
              clearable
              filterable
              placeholder="请选择角色"
              style="width: 180px"
            >
              <el-option
                v-for="item in roleOptions"
                :key="item.id"
                :label="item.roleName"
                :value="item.roleCode"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="queryForm.status"
              clearable
              placeholder="请选择状态"
              style="width: 160px"
            >
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button type="success" :icon="Plus" @click="handleCreate">新建用户</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="table-card" shadow="never">
        <template #header>
          <div class="table-header">
            <div class="table-title">用户列表</div>
            <div class="header-tags">
              <el-tag type="info">共 {{ total }} 条</el-tag>
              <el-tag type="primary">{{ roleHint }}</el-tag>
            </div>
          </div>
        </template>

        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="username" label="用户名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="realName" label="真实姓名" min-width="160" show-overflow-tooltip />
          <el-table-column label="班级" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.className || '--' }}</template>
          </el-table-column>
          <el-table-column label="角色" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ roleText(row) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="light">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="更新时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
              <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button
                type="warning"
                link
                :icon="Switch"
                :loading="statusSubmittingId === row.id"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button type="danger" link :icon="Key" @click="openResetPassword(row)">重置密码</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 30, 50]"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </el-card>
    </template>

    <el-card v-else shadow="never" class="no-access-card">
      <el-result
        icon="warning"
        title="无权访问"
        sub-title="当前账号不是管理员，不能进入用户管理模块。"
      />
    </el-card>

    <SysUserFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="userForm"
      :role-options="roleOptions"
      :class-options="classOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <SysUserResetPasswordDialog
      v-model="resetDialogVisible"
      :username="resetTarget?.username || ''"
      :form-data="resetPasswordForm"
      :submitting="resetSubmitting"
      @submit="handleResetPasswordSubmit"
    />

    <el-dialog v-model="detailVisible" title="用户详情" width="760px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
          <div class="detail-header">
            <div>
              <h2>{{ detailData.realName }}</h2>
              <p>{{ detailData.username }}</p>
            </div>
            <div class="detail-tags">
              <el-tag :type="getStatusTagType(detailData.status)" effect="light">
                {{ getStatusLabel(detailData.status) }}
              </el-tag>
              <el-tag
                v-if="detailData.id === currentUserId"
                type="primary"
                effect="light"
              >
                当前登录账号
              </el-tag>
            </div>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ detailData.username }}
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              {{ detailData.realName }}
            </el-descriptions-item>
            <el-descriptions-item label="学号">
              {{ detailData.studentNo || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="班级">
              {{ detailData.className || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              {{ getStatusLabel(detailData.status) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(detailData.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(detailData.updateTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="角色" :span="2">
              {{ roleText(detailData) }}
            </el-descriptions-item>
          </el-descriptions>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.sys-user-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card,
.filter-card,
.table-card,
.no-access-card {
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

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 0;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.table-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

.header-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-panel {
  min-height: 180px;
}

.detail-loading-panel {
  min-height: 220px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.detail-header h2 {
  margin: 0 0 8px;
  color: #1f2d3d;
  font-size: 24px;
}

.detail-header p {
  margin: 0;
  color: #7b8ba1;
}

.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
