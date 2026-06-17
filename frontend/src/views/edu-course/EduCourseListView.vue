<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createEduCourseApi,
  deleteEduCourseApi,
  getEduCourseDetailApi,
  getEduCoursePageApi,
  updateEduCourseApi,
  type EduCourseFormData,
  type EduCourseItem,
} from '@/api/eduCourse'
import { useUserStore } from '@/stores/user'
import EduCourseFormDialog from '@/views/edu-course/components/EduCourseFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<EduCourseItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<EduCourseItem | null>(null)

const deletingId = ref('')

const queryForm = reactive({
  courseName: '',
  courseCode: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const courseForm = reactive<EduCourseFormData>({
  courseName: '',
  courseCode: '',
  credit: null,
  remark: '',
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isStudent = computed(() => roles.value.includes('STUDENT'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看并维护全部课程'
  if (isTeacher.value) return '教师可查看全部课程'
  return '学生仅可查看启用中的课程'
})

const canManage = computed(() => isAdmin.value)

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const textPreview = (value: string | null, limit = 36) => {
  if (!value) return '暂无备注'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const fetchEduCourseList = async () => {
  loading.value = true
  try {
    const res = await getEduCoursePageApi({
      current: pagination.current,
      size: pagination.size,
      courseName: queryForm.courseName || undefined,
      courseCode: queryForm.courseCode || undefined,
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
  fetchEduCourseList()
}

const handleReset = () => {
  queryForm.courseName = ''
  queryForm.courseCode = ''
  queryForm.status = undefined
  pagination.current = 1
  fetchEduCourseList()
}

const resetCourseForm = () => {
  courseForm.id = undefined
  courseForm.courseName = ''
  courseForm.courseCode = ''
  courseForm.credit = null
  courseForm.remark = ''
  courseForm.status = 1
}

const handleCreate = () => {
  dialogMode.value = 'create'
  resetCourseForm()
  dialogVisible.value = true
}

const handleEdit = (row: EduCourseItem) => {
  dialogMode.value = 'edit'
  courseForm.id = row.id
  courseForm.courseName = row.courseName
  courseForm.courseCode = row.courseCode || ''
  courseForm.credit = row.credit
  courseForm.remark = row.remark || ''
  courseForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!courseForm.courseName.trim()) {
    ElMessage.warning('请先填写课程名称')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createEduCourseApi(courseForm)
      ElMessage.success('课程创建成功')
    } else {
      await updateEduCourseApi(courseForm)
      ElMessage.success('课程修改成功')
    }
    dialogVisible.value = false
    fetchEduCourseList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: EduCourseItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getEduCourseDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: EduCourseItem) => {
  await ElMessageBox.confirm('确认删除这条课程记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteEduCourseApi(row.id)
    ElMessage.success('课程删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchEduCourseList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchEduCourseList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchEduCourseList()
}

onMounted(() => {
  fetchEduCourseList()
})
</script>

<template>
  <div class="edu-course-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Course Management</div>
          <h1>课程管理</h1>
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
        <el-form-item label="课程名称">
          <el-input
            v-model="queryForm.courseName"
            clearable
            placeholder="请输入课程名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="课程编码">
          <el-input
            v-model="queryForm.courseCode"
            clearable
            placeholder="请输入课程编码"
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            clearable
            placeholder="请选择状态"
            style="width: 140px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canManage" type="success" :icon="Plus" @click="handleCreate">新建课程</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">课程列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="edu-course-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="课程名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ row.courseName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="courseCode" label="课程编码" width="140" />
        <el-table-column prop="credit" label="学分" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="课程备注" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ textPreview(row.remark) }}</template>
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
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <EduCourseFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="courseForm"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="课程详情" width="760px" destroy-on-close>
      <div v-if="detailData" v-loading="detailLoading" class="detail-panel">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.courseName }}</h2>
            <p>{{ detailData.courseCode || '暂无课程编码' }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程编码">
            {{ detailData.courseCode || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">
            {{ detailData.credit ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailData.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-block-title">课程备注</div>
          <div class="detail-block-content">{{ detailData.remark || '暂无备注' }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.edu-course-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card,
.filter-card,
.table-card {
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

.detail-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-block-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.detail-block-content {
  min-height: 96px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
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
