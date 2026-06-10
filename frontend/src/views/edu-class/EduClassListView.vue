<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createEduClassApi,
  deleteEduClassApi,
  getEduClassDetailApi,
  getEduClassPageApi,
  updateEduClassApi,
  type EduClassFormData,
  type EduClassItem,
} from '@/api/eduClass'
import { useUserStore } from '@/stores/user'
import EduClassFormDialog from '@/views/edu-class/components/EduClassFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<EduClassItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<EduClassItem | null>(null)

const deletingId = ref('')

const queryForm = reactive({
  className: '',
  majorName: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const classForm = reactive<EduClassFormData>({
  className: '',
  majorName: '',
  grade: null,
  counselorName: '',
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isStudent = computed(() => roles.value.includes('STUDENT'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看并维护全部班级'
  if (isTeacher.value) return '教师可查看全部班级'
  return '学生只查看启用中的班级'
})

const canManage = computed(() => isAdmin.value)

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const fetchEduClassList = async () => {
  loading.value = true
  try {
    const res = await getEduClassPageApi({
      current: pagination.current,
      size: pagination.size,
      className: queryForm.className || undefined,
      majorName: queryForm.majorName || undefined,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchEduClassList()
}

const handleReset = () => {
  queryForm.className = ''
  queryForm.majorName = ''
  pagination.current = 1
  fetchEduClassList()
}

const resetClassForm = () => {
  classForm.id = undefined
  classForm.className = ''
  classForm.majorName = ''
  classForm.grade = null
  classForm.counselorName = ''
  classForm.status = 1
}

const handleCreate = () => {
  dialogMode.value = 'create'
  resetClassForm()
  dialogVisible.value = true
}

const handleEdit = (row: EduClassItem) => {
  dialogMode.value = 'edit'
  classForm.id = row.id
  classForm.className = row.className
  classForm.majorName = row.majorName || ''
  classForm.grade = row.grade
  classForm.counselorName = row.counselorName || ''
  classForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!classForm.className.trim()) {
    ElMessage.warning('请先填写班级名称')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createEduClassApi(classForm)
      ElMessage.success('班级创建成功')
    } else {
      await updateEduClassApi(classForm)
      ElMessage.success('班级修改成功')
    }
    dialogVisible.value = false
    fetchEduClassList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: EduClassItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getEduClassDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: EduClassItem) => {
  await ElMessageBox.confirm('确认删除这条班级记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteEduClassApi(row.id)
    ElMessage.success('班级删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchEduClassList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchEduClassList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchEduClassList()
}

onMounted(() => {
  fetchEduClassList()
})
</script>

<template>
  <div class="edu-class-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Class Management</div>
          <h1>班级管理</h1>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="班级名称">
          <el-input
            v-model="queryForm.className"
            clearable
            placeholder="请输入班级名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="专业名称">
          <el-input
            v-model="queryForm.majorName"
            clearable
            placeholder="请输入专业名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canManage" type="success" :icon="Plus" @click="handleCreate">新建班级</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">班级列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="edu-class-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="班级名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ row.className }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="majorName" label="专业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="counselorName" label="辅导员" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button v-if="canManage" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="canManage"
              type="danger"
              link
              :icon="Delete"
              :loading="deletingId === row.id"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <EduClassFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="classForm"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="班级详情" width="760px" destroy-on-close>
      <div v-if="detailData" v-loading="detailLoading" class="detail-panel">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.className }}</h2>
            <p>{{ detailData.majorName || '暂无专业信息' }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="专业名称">
            {{ detailData.majorName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="年级">
            {{ detailData.grade ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="辅导员">
            {{ detailData.counselorName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDateTime(detailData.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.edu-class-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.filter-card,
.table-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 14px 32px rgb(57 118 201 / 8%);
}

.hero-card {
  background: linear-gradient(135deg, #eef7ff 0%, #f8fbff 58%, #ffffff 100%);
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
  background: #dcebff;
  color: #2f6fdb;
  font-size: 12px;
  font-weight: 600;
}

.hero-content h1 {
  margin: 0 0 10px;
  color: #1f2d3d;
  font-size: 28px;
}

.hero-content p {
  margin: 0;
  color: #6b7a90;
  line-height: 1.8;
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

.table-subtitle {
  margin-top: 6px;
  color: #7b8ba1;
  font-size: 13px;
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
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
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
</style>
