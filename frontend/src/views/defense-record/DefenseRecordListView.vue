<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import {
  createDefenseRecordApi,
  deleteDefenseRecordApi,
  getDefenseRecordDetailApi,
  getDefenseRecordPageApi,
  updateDefenseRecordApi,
  type DefenseRecordFormData,
  type DefenseRecordItem,
} from '@/api/defenseRecord'
import { getDefenseSchedulePageApi, type DefenseScheduleItem } from '@/api/defenseSchedule'
import { getProjectGroupListApi, type ProjectGroupOption } from '@/api/projectGroup'
import { getTeacherDisplayName } from '@/utils/userDisplay'
import DefenseRecordFormDialog from '@/views/defense-record/components/DefenseRecordFormDialog.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<DefenseRecordItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<DefenseRecordItem | null>(null)

const deletingId = ref('')
const scheduleOptions = ref<DefenseScheduleItem[]>([])
const groupOptions = ref<ProjectGroupOption[]>([])
const { teacherOptions, teacherOptionMap, loadTeacherOptions } = useUserOptions()

const queryForm = reactive({
  scheduleId: '',
  teacherId: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const defenseRecordForm = reactive<DefenseRecordFormData>({
  scheduleId: '',
  presentationScore: null,
  answerScore: null,
  completionScore: null,
  totalScore: null,
  comment: '',
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看全部答辩记录'
  if (isTeacher.value) return '教师可新增和维护自己登记的答辩记录'
  return '学生可查看自己项目组对应的答辩记录'
})

const canManageRow = (row: DefenseRecordItem) => isTeacher.value && row.teacherId === currentUserId.value

const getScheduleLabel = (id: string) => {
  const item = scheduleOptions.value.find((option) => option.id === id)
  if (!item) return id
  const group = groupOptions.value.find((option) => option.id === item.groupId)
  const groupLabel = group ? (group.projectName ? `${group.groupName} · ${group.projectName}` : group.groupName) : item.groupId
  const date = item.defenseDate || '未设置日期'
  const time = item.defenseTime || '未设置时间'
  const location = item.location || '未设置地点'
  return `${groupLabel} · ${date} ${time} · ${location}`
}

const getTeacherLabel = (id: string) => {
  return getTeacherDisplayName(id, teacherOptionMap.value)
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const fetchScheduleOptions = async () => {
  const res = await getDefenseSchedulePageApi({
    current: 1,
    size: 100,
  })
  scheduleOptions.value = res.data.records
}

const fetchTeacherOptions = async () => {
  await loadTeacherOptions()
}

const fetchGroupOptions = async () => {
  const res = await getProjectGroupListApi()
  groupOptions.value = res.data
}

const fetchDefenseRecordList = async () => {
  loading.value = true
  try {
    const res = await getDefenseRecordPageApi({
      current: pagination.current,
      size: pagination.size,
      scheduleId: queryForm.scheduleId || undefined,
      teacherId: queryForm.teacherId || undefined,
    })

    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchDefenseRecordList()
}

const handleReset = () => {
  queryForm.scheduleId = ''
  queryForm.teacherId = ''
  pagination.current = 1
  fetchDefenseRecordList()
}

const resetDefenseRecordForm = () => {
  defenseRecordForm.id = undefined
  defenseRecordForm.scheduleId = ''
  defenseRecordForm.presentationScore = null
  defenseRecordForm.answerScore = null
  defenseRecordForm.completionScore = null
  defenseRecordForm.totalScore = null
  defenseRecordForm.comment = ''
}

const handleCreate = async () => {
  if (!scheduleOptions.value.length) {
    await fetchScheduleOptions()
  }
  dialogMode.value = 'create'
  resetDefenseRecordForm()
  dialogVisible.value = true
}

const handleEdit = async (row: DefenseRecordItem) => {
  if (!scheduleOptions.value.length) {
    await fetchScheduleOptions()
  }
  dialogMode.value = 'edit'
  defenseRecordForm.id = row.id
  defenseRecordForm.scheduleId = row.scheduleId
  defenseRecordForm.presentationScore = row.presentationScore
  defenseRecordForm.answerScore = row.answerScore
  defenseRecordForm.completionScore = row.completionScore
  defenseRecordForm.totalScore = row.totalScore
  defenseRecordForm.comment = row.comment || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!defenseRecordForm.scheduleId) {
    ElMessage.warning('请先选择答辩安排')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createDefenseRecordApi(defenseRecordForm)
      ElMessage.success('答辩记录创建成功')
    } else {
      await updateDefenseRecordApi(defenseRecordForm)
      ElMessage.success('答辩记录修改成功')
    }
    dialogVisible.value = false
    fetchDefenseRecordList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: DefenseRecordItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getDefenseRecordDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: DefenseRecordItem) => {
  await ElMessageBox.confirm('确认删除这条答辩记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteDefenseRecordApi(row.id)
    ElMessage.success('答辩记录删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchDefenseRecordList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchDefenseRecordList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchDefenseRecordList()
}

onMounted(() => {
  Promise.allSettled([
    fetchScheduleOptions(),
    fetchTeacherOptions(),
    fetchGroupOptions(),
    fetchDefenseRecordList(),
  ])
})
</script>

<template>
  <div class="defense-record-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Defense Record Management</div>
          <h1>答辩记录管理</h1>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="答辩安排">
          <el-select
            v-model="queryForm.scheduleId"
            clearable
            filterable
            placeholder="请选择答辩安排"
            style="width: 320px"
          >
            <el-option
              v-for="item in scheduleOptions"
              :key="item.id"
              :label="getScheduleLabel(item.id)"
              :value="item.id"
            />
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

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="isTeacher" type="success" :icon="Plus" @click="handleCreate">新建答辩记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">答辩记录列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="defense-record-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="答辩安排" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ getScheduleLabel(row.scheduleId) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="教师" width="120">
          <template #default="{ row }">{{ getTeacherLabel(row.teacherId) }}</template>
        </el-table-column>
        <el-table-column label="展示分" width="90">
          <template #default="{ row }">{{ row.presentationScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="问答分" width="90">
          <template #default="{ row }">{{ row.answerScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="完成度分" width="100">
          <template #default="{ row }">{{ row.completionScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="总分" width="90">
          <template #default="{ row }">{{ row.totalScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="评语" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.comment || '暂无内容' }}
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
            <el-button v-if="canManageRow(row)" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="canManageRow(row)"
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

    <DefenseRecordFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="defenseRecordForm"
      :schedule-options="scheduleOptions"
      :group-options="groupOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="答辩记录详情" width="760px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
          <div class="detail-header">
            <div>
              <h2>{{ getScheduleLabel(detailData.scheduleId) }}</h2>
              <p>教师：{{ getTeacherLabel(detailData.teacherId) }}</p>
            </div>
            <div class="detail-tags">
              <el-tag type="primary" effect="light">总分：{{ detailData.totalScore ?? '--' }}</el-tag>
            </div>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="答辩安排">
              {{ getScheduleLabel(detailData.scheduleId) }}
            </el-descriptions-item>
            <el-descriptions-item label="教师">
              {{ getTeacherLabel(detailData.teacherId) }}
            </el-descriptions-item>
            <el-descriptions-item label="展示分">
              {{ detailData.presentationScore ?? '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="问答分">
              {{ detailData.answerScore ?? '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="完成度分">
              {{ detailData.completionScore ?? '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(detailData.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-comment">
            {{ detailData.comment || '暂无评语' }}
          </div>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.defense-record-page {
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

.detail-comment {
  min-height: 120px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
