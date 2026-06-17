<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import {
  createTrainingBatchApi,
  deleteTrainingBatchApi,
  getTrainingBatchDetailApi,
  getTrainingBatchPageApi,
  updateTrainingBatchApi,
  type TrainingBatchFormData,
  type TrainingBatchOption,
} from '@/api/trainingBatch'
import { getEduCourseListApi, type EduCourseItem } from '@/api/eduCourse'
import { getEduClassListApi, type EduClassItem } from '@/api/eduClass'
import { useUserStore } from '@/stores/user'
import { getTeacherDisplayName } from '@/utils/userDisplay'
import TrainingBatchFormDialog from '@/views/training-batch/components/TrainingBatchFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<TrainingBatchOption[]>([])
const total = ref(0)
const courseOptions = ref<EduCourseItem[]>([])
const classOptions = ref<EduClassItem[]>([])
const { teacherOptions, teacherOptionMap, loadTeacherOptions } = useUserOptions()

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<TrainingBatchOption | null>(null)

const deletingId = ref('')

const queryForm = reactive({
  batchName: '',
  courseId: '',
  teacherId: '',
  classId: '',
  termName: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const batchForm = reactive<TrainingBatchFormData>({
  batchName: '',
  courseId: '',
  teacherId: '',
  classId: '',
  termName: '',
  startTime: '',
  endTime: '',
  defenseTime: '',
  description: '',
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isStudent = computed(() => roles.value.includes('STUDENT'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看并维护全部实训批次'
  if (isTeacher.value) return '教师可维护分配给自己的实训批次'
  return '学生只能查看已启用的实训批次'
})

const canCreate = computed(() => isAdmin.value || isTeacher.value)
const canEditRow = (row: TrainingBatchOption) => isAdmin.value || (isTeacher.value && row.teacherId === currentUserId.value)
const canDeleteRow = (row: TrainingBatchOption) => isAdmin.value || (isTeacher.value && row.teacherId === currentUserId.value)

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const textPreview = (value: string | null, limit = 36) => {
  if (!value) return '暂无说明'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const getCourseName = (courseId: string) => {
  return courseOptions.value.find((item) => item.id === courseId)?.courseName || courseId
}

const getClassName = (classId: string) => {
  return classOptions.value.find((item) => item.id === classId)?.className || classId
}

const getTeacherName = (teacherId: string) => {
  return getTeacherDisplayName(teacherId, teacherOptionMap.value)
}

const loadRelatedOptions = async () => {
  const [courseRes, classRes] = await Promise.all([
    getEduCourseListApi(),
    getEduClassListApi(),
    loadTeacherOptions(),
  ])
  courseOptions.value = courseRes.data
  classOptions.value = classRes.data
}

const fetchTrainingBatchList = async () => {
  loading.value = true
  try {
    const res = await getTrainingBatchPageApi({
      current: pagination.current,
      size: pagination.size,
      batchName: queryForm.batchName || undefined,
      courseId: queryForm.courseId || undefined,
      teacherId: queryForm.teacherId || undefined,
      classId: queryForm.classId || undefined,
      termName: queryForm.termName || undefined,
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
  fetchTrainingBatchList()
}

const handleReset = () => {
  queryForm.batchName = ''
  queryForm.courseId = ''
  queryForm.teacherId = ''
  queryForm.classId = ''
  queryForm.termName = ''
  queryForm.status = undefined
  pagination.current = 1
  fetchTrainingBatchList()
}

const resetBatchForm = () => {
  batchForm.id = undefined
  batchForm.batchName = ''
  batchForm.courseId = ''
  batchForm.teacherId = ''
  batchForm.classId = ''
  batchForm.termName = ''
  batchForm.startTime = ''
  batchForm.endTime = ''
  batchForm.defenseTime = ''
  batchForm.description = ''
  batchForm.status = 1
}

const handleCreate = async () => {
  if (!courseOptions.value.length || !classOptions.value.length || !teacherOptions.value.length) {
    await loadRelatedOptions()
  }
  dialogMode.value = 'create'
  resetBatchForm()
  if (isTeacher.value) {
    batchForm.teacherId = currentUserId.value
  }
  dialogVisible.value = true
}

const handleEdit = async (row: TrainingBatchOption) => {
  if (!courseOptions.value.length || !classOptions.value.length || !teacherOptions.value.length) {
    await loadRelatedOptions()
  }
  dialogMode.value = 'edit'
  batchForm.id = row.id
  batchForm.batchName = row.batchName
  batchForm.courseId = row.courseId
  batchForm.teacherId = row.teacherId
  batchForm.classId = row.classId
  batchForm.termName = row.termName || ''
  batchForm.startTime = row.startTime || ''
  batchForm.endTime = row.endTime || ''
  batchForm.defenseTime = row.defenseTime || ''
  batchForm.description = row.description || ''
  batchForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!batchForm.batchName.trim()) {
    ElMessage.warning('请先填写批次名称')
    return
  }
  if (!batchForm.courseId.trim()) {
    ElMessage.warning('请先选择课程')
    return
  }
  if (!batchForm.teacherId.trim()) {
    ElMessage.warning('请先选择教师')
    return
  }
  if (!batchForm.classId.trim()) {
    ElMessage.warning('请先选择班级')
    return
  }
  if (!batchForm.startTime) {
    ElMessage.warning('请选择开始时间')
    return
  }
  if (!batchForm.endTime) {
    ElMessage.warning('请选择结束时间')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createTrainingBatchApi(batchForm)
      ElMessage.success('实训批次创建成功')
    } else {
      await updateTrainingBatchApi(batchForm)
      ElMessage.success('实训批次修改成功')
    }
    dialogVisible.value = false
    fetchTrainingBatchList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: TrainingBatchOption) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getTrainingBatchDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: TrainingBatchOption) => {
  await ElMessageBox.confirm('确认删除这个实训批次吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteTrainingBatchApi(row.id)
    ElMessage.success('实训批次删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchTrainingBatchList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchTrainingBatchList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchTrainingBatchList()
}

onMounted(() => {
  Promise.allSettled([
    loadRelatedOptions(),
    fetchTrainingBatchList(),
  ])
})
</script>

<template>
  <div class="training-batch-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Training Batch Management</div>
          <h1>实训批次</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前视角</div>
          <div class="hero-side-value">{{ roleHint }}</div>
          <div class="hero-side-meta">当前列表共 {{ total }} 个实训批次</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="批次名称">
          <el-input
            v-model="queryForm.batchName"
            clearable
            placeholder="请输入批次名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="课程">
          <el-select
            v-model="queryForm.courseId"
            clearable
            filterable
            placeholder="请选择课程"
            style="width: 220px"
          >
            <el-option v-for="item in courseOptions" :key="item.id" :label="item.courseName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="教师">
          <el-select
            v-model="queryForm.teacherId"
            clearable
            filterable
            placeholder="请选择教师"
            style="width: 220px"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="班级">
          <el-select
            v-model="queryForm.classId"
            clearable
            filterable
            placeholder="请选择班级"
            style="width: 220px"
          >
            <el-option v-for="item in classOptions" :key="item.id" :label="item.className" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="学期名称">
          <el-input
            v-model="queryForm.termName"
            clearable
            placeholder="请输入学期名称"
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
          <el-button v-if="canCreate" type="success" :icon="Plus" @click="handleCreate">新建批次</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">批次列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="training-batch-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="批次名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ row.batchName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="termName" label="学期" width="150" />
        <el-table-column label="课程" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getCourseName(row.courseId) }}</template>
        </el-table-column>
        <el-table-column label="教师" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getTeacherName(row.teacherId) }}</template>
        </el-table-column>
        <el-table-column label="班级" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getClassName(row.classId) }}</template>
        </el-table-column>
        <el-table-column label="开始时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="批次说明" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ textPreview(row.description) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button v-if="canEditRow(row)" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="canDeleteRow(row)"
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

    <TrainingBatchFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="batchForm"
      :course-options="courseOptions"
      :teacher-options="teacherOptions"
      :teacher-select-disabled="isTeacher"
      :class-options="classOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="实训批次详情" width="860px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.batchName }}</h2>
            <p>{{ detailData.termName || '暂无学期信息' }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程">
            {{ getCourseName(detailData.courseId) }}
          </el-descriptions-item>
          <el-descriptions-item label="教师">
            {{ getTeacherName(detailData.teacherId) }}
          </el-descriptions-item>
          <el-descriptions-item label="班级">
            {{ getClassName(detailData.classId) }}
          </el-descriptions-item>
          <el-descriptions-item label="答辩时间">
            {{ formatDateTime(detailData.defenseTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatDateTime(detailData.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatDateTime(detailData.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailData.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-block-title">批次说明</div>
          <div class="detail-block-content">{{ detailData.description || '暂无批次说明' }}</div>
        </div>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.training-batch-page {
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
  margin: 0 0 10px;
  color: #1f2d3d;
  font-size: 30px;
}

.hero-content p {
  margin: 0;
  max-width: 640px;
  color: #6b7a90;
  line-height: 1.8;
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

.filter-panel-head {
  margin-bottom: 14px;
}

.filter-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}

.filter-subtitle {
  margin-top: 6px;
  color: #7b8ba1;
  font-size: 13px;
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

.detail-loading-panel {
  min-height: 220px;
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

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
