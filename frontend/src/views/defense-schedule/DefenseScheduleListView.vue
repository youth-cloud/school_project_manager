<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createDefenseScheduleApi,
  deleteDefenseScheduleApi,
  getDefenseScheduleDetailApi,
  getDefenseSchedulePageApi,
  updateDefenseScheduleApi,
  type DefenseScheduleFormData,
  type DefenseScheduleItem,
} from '@/api/defenseSchedule'
import { getProjectGroupListApi, type ProjectGroupOption } from '@/api/projectGroup'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { useUserStore } from '@/stores/user'
import DefenseScheduleFormDialog from '@/views/defense-schedule/components/DefenseScheduleFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<DefenseScheduleItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<DefenseScheduleItem | null>(null)

const deletingId = ref('')
const batchOptions = ref<TrainingBatchOption[]>([])
const groupOptions = ref<ProjectGroupOption[]>([])

const queryForm = reactive({
  batchId: '',
  groupId: '',
  orderNo: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const scheduleForm = reactive<DefenseScheduleFormData>({
  batchId: '',
  groupId: '',
  defenseDate: '',
  defenseTime: '',
  location: '',
  orderNo: null,
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const canManage = computed(() => roles.value.includes('ADMIN') || roles.value.includes('TEACHER'))
const roleHint = computed(() => {
  if (roles.value.includes('ADMIN')) return '管理员可查看并维护全部答辩安排'
  if (roles.value.includes('TEACHER')) return '教师可维护自己业务链下的答辩安排'
  return '学生只查看自己项目组的答辩安排'
})

const filteredGroupOptions = computed(() => {
  if (!queryForm.batchId) return groupOptions.value
  return groupOptions.value.filter((item) => item.batchId === queryForm.batchId)
})

const getBatchLabel = (id: string) => {
  const item = batchOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.termName ? `${item.batchName} · ${item.termName}` : item.batchName
}

const getGroupLabel = (id: string) => {
  const item = groupOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.projectName ? `${item.groupName} · ${item.projectName}` : item.groupName
}

const formatDate = (value: string | null) => value || '--'
const formatTime = (value: string | null) => value || '--'
const formatDateTime = (date: string | null, time: string | null) => {
  if (!date && !time) return '--'
  if (!date) return time || '--'
  if (!time) return date
  return `${date} ${time}`
}

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const fetchOptions = async () => {
  const [batchRes, groupRes] = await Promise.all([getTrainingBatchListApi(), getProjectGroupListApi()])
  batchOptions.value = batchRes.data
  groupOptions.value = groupRes.data
}

const fetchDefenseScheduleList = async () => {
  loading.value = true
  try {
    const res = await getDefenseSchedulePageApi({
      current: pagination.current,
      size: pagination.size,
      batchId: queryForm.batchId || undefined,
      groupId: queryForm.groupId || undefined,
      orderNo: queryForm.orderNo,
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
  fetchDefenseScheduleList()
}

const handleReset = () => {
  queryForm.batchId = ''
  queryForm.groupId = ''
  queryForm.orderNo = undefined
  queryForm.status = undefined
  pagination.current = 1
  fetchDefenseScheduleList()
}

const resetScheduleForm = () => {
  scheduleForm.id = undefined
  scheduleForm.batchId = ''
  scheduleForm.groupId = ''
  scheduleForm.defenseDate = ''
  scheduleForm.defenseTime = ''
  scheduleForm.location = ''
  scheduleForm.orderNo = null
  scheduleForm.status = 1
}

const handleCreate = async () => {
  if (!batchOptions.value.length || !groupOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'create'
  resetScheduleForm()
  dialogVisible.value = true
}

const handleEdit = async (row: DefenseScheduleItem) => {
  if (!batchOptions.value.length || !groupOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'edit'
  scheduleForm.id = row.id
  scheduleForm.batchId = row.batchId
  scheduleForm.groupId = row.groupId
  scheduleForm.defenseDate = row.defenseDate || ''
  scheduleForm.defenseTime = row.defenseTime || ''
  scheduleForm.location = row.location || ''
  scheduleForm.orderNo = row.orderNo
  scheduleForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!scheduleForm.batchId) {
    ElMessage.warning('请先选择所属批次')
    return
  }

  if (!scheduleForm.groupId) {
    ElMessage.warning('请先选择项目组')
    return
  }

  if (!scheduleForm.defenseDate) {
    ElMessage.warning('请选择答辩日期')
    return
  }

  if (!scheduleForm.defenseTime) {
    ElMessage.warning('请选择答辩时间')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createDefenseScheduleApi(scheduleForm)
      ElMessage.success('答辩安排创建成功')
    } else {
      await updateDefenseScheduleApi(scheduleForm)
      ElMessage.success('答辩安排修改成功')
    }
    dialogVisible.value = false
    fetchDefenseScheduleList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: DefenseScheduleItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getDefenseScheduleDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: DefenseScheduleItem) => {
  await ElMessageBox.confirm('确认删除这条答辩安排吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteDefenseScheduleApi(row.id)
    ElMessage.success('答辩安排删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchDefenseScheduleList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchDefenseScheduleList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchDefenseScheduleList()
}

onMounted(async () => {
  await fetchOptions()
  await fetchDefenseScheduleList()
})
</script>

<template>
  <div class="defense-schedule-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Defense Schedule Management</div>
          <h1>答辩安排管理</h1>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="所属批次">
          <el-select
            v-model="queryForm.batchId"
            clearable
            filterable
            placeholder="请选择实训批次"
            style="width: 250px"
          >
            <el-option
              v-for="item in batchOptions"
              :key="item.id"
              :label="getBatchLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="项目组">
          <el-select
            v-model="queryForm.groupId"
            clearable
            filterable
            placeholder="请选择项目组"
            style="width: 250px"
          >
            <el-option
              v-for="item in filteredGroupOptions"
              :key="item.id"
              :label="getGroupLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="顺序号">
          <el-input-number
            v-model="queryForm.orderNo"
            :min="1"
            controls-position="right"
            placeholder="顺序号"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="请选择状态" style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canManage" type="success" :icon="Plus" @click="handleCreate">新建答辩安排</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">答辩安排列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="defense-schedule-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="所属批次" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ getBatchLabel(row.batchId) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="项目组" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getGroupLabel(row.groupId) }}
          </template>
        </el-table-column>
        <el-table-column label="答辩日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.defenseDate) }}
          </template>
        </el-table-column>
        <el-table-column label="答辩时间" width="110">
          <template #default="{ row }">
            {{ formatTime(row.defenseTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="答辩地点" min-width="180" show-overflow-tooltip />
        <el-table-column prop="orderNo" label="顺序号" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
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

    <DefenseScheduleFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="scheduleForm"
      :batch-options="batchOptions"
      :group-options="groupOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="答辩安排详情" width="760px" destroy-on-close>
      <div v-if="detailData" v-loading="detailLoading" class="detail-panel">
        <div class="detail-header">
          <div>
            <h2>{{ getBatchLabel(detailData.batchId) }}</h2>
            <p>{{ getGroupLabel(detailData.groupId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
            <el-tag type="primary" effect="light">
              顺序号 {{ detailData.orderNo ?? '--' }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="所属批次">
            {{ getBatchLabel(detailData.batchId) }}
          </el-descriptions-item>
          <el-descriptions-item label="项目组">
            {{ getGroupLabel(detailData.groupId) }}
          </el-descriptions-item>
          <el-descriptions-item label="答辩日期">
            {{ formatDate(detailData.defenseDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="答辩时间">
            {{ formatTime(detailData.defenseTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="答辩地点">
            {{ detailData.location || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailData.updateTime?.slice(0, 10) || null, detailData.updateTime?.slice(11, 19) || null) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.defense-schedule-page {
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
