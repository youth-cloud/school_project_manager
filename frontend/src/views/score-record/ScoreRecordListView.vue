<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import { getProjectGroupListApi, type ProjectGroupOption } from '@/api/projectGroup'
import { getProjectGroupMemberPageApi } from '@/api/projectGroupMember'
import { getStudentDisplayName } from '@/utils/userDisplay'
import {
  createScoreRecordApi,
  deleteScoreRecordApi,
  getScoreRecordDetailApi,
  getScoreRecordPageApi,
  updateScoreRecordApi,
  type ScoreRecordFormData,
  type ScoreRecordItem,
} from '@/api/scoreRecord'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { useUserStore } from '@/stores/user'
import ScoreRecordFormDialog from '@/views/score-record/components/ScoreRecordFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ScoreRecordItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ScoreRecordItem | null>(null)

const deletingId = ref('')
const batchOptions = ref<TrainingBatchOption[]>([])
const groupOptions = ref<ProjectGroupOption[]>([])
const { studentOptions, userOptionMap, loadRequestedOptions } = useUserOptions()
const scoreFormStudentOptions = ref<typeof studentOptions.value>([])

const queryForm = reactive({
  batchId: '',
  groupId: '',
  studentId: '',
  gradeLevel: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const scoreForm = reactive<ScoreRecordFormData>({
  batchId: '',
  groupId: '',
  studentId: '',
  processScore: null,
  reportScore: null,
  submissionScore: null,
  defenseScore: null,
  finalScore: null,
  gradeLevel: '',
  remark: '',
})

const roles = computed(() => userStore.userInfo?.roles || [])
const canManage = computed(() => roles.value.includes('ADMIN') || roles.value.includes('TEACHER'))
const roleHint = computed(() => {
  if (roles.value.includes('ADMIN')) return '管理员可查看并维护全部成绩记录'
  if (roles.value.includes('TEACHER')) return '教师可维护自己业务链上的成绩记录'
  return '学生只查看自己的成绩记录'
})

const filteredGroupOptions = computed(() => {
  if (!queryForm.batchId) return groupOptions.value
  return groupOptions.value.filter((item) => item.batchId === queryForm.batchId)
})

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const getBatchLabel = (id: string) => {
  const item = batchOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.termName ? `${item.batchName} · ${item.termName}` : item.batchName
}

const getGroupLabel = (id: string | null) => {
  if (!id) return '--'
  const item = groupOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.projectName ? `${item.groupName} · ${item.projectName}` : item.groupName
}

const getStudentLabel = (id: string) => {
  return getStudentDisplayName(id, userOptionMap.value)
}

const syncScoreFormStudentOptions = (userIds: string[]) => {
  scoreFormStudentOptions.value = studentOptions.value.filter((option) => userIds.includes(option.id))
}

const gradeTagType = (value: string | null) => {
  if (!value) return 'info'
  if (['A', '优秀'].includes(value)) return 'success'
  if (['B', '良好'].includes(value)) return 'primary'
  if (['C', '中等'].includes(value)) return 'warning'
  if (['D', '及格'].includes(value)) return 'info'
  if (['E', '不及格'].includes(value)) return 'danger'
  return 'info'
}

const fetchOptions = async () => {
  const [batchRes, groupRes] = await Promise.all([
    getTrainingBatchListApi(),
    getProjectGroupListApi(),
    loadRequestedOptions(['student', 'user']),
  ])
  batchOptions.value = batchRes.data
  groupOptions.value = groupRes.data
}

const loadScoreFormStudentOptions = async (groupId: string) => {
  if (!groupId) {
    scoreFormStudentOptions.value = []
    return
  }

  const res = await getProjectGroupMemberPageApi({
    current: 1,
    size: 100,
    groupId,
    status: 1,
  })
  const memberUserIds = Array.from(new Set(res.data.records.map((item) => item.userId)))
  syncScoreFormStudentOptions(memberUserIds)
}

const fetchScoreRecordList = async () => {
  loading.value = true
  try {
    const res = await getScoreRecordPageApi({
      current: pagination.current,
      size: pagination.size,
      batchId: queryForm.batchId || undefined,
      groupId: queryForm.groupId || undefined,
      studentId: queryForm.studentId || undefined,
      gradeLevel: queryForm.gradeLevel || undefined,
    })

    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchScoreRecordList()
}

const handleReset = () => {
  queryForm.batchId = ''
  queryForm.groupId = ''
  queryForm.studentId = ''
  queryForm.gradeLevel = ''
  pagination.current = 1
  fetchScoreRecordList()
}

const resetScoreForm = () => {
  scoreForm.id = undefined
  scoreForm.batchId = ''
  scoreForm.groupId = ''
  scoreForm.studentId = ''
  scoreForm.processScore = null
  scoreForm.reportScore = null
  scoreForm.submissionScore = null
  scoreForm.defenseScore = null
  scoreForm.finalScore = null
  scoreForm.gradeLevel = ''
  scoreForm.remark = ''
}

const handleCreate = async () => {
  if (!batchOptions.value.length || !groupOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'create'
  resetScoreForm()
  scoreFormStudentOptions.value = []
  dialogVisible.value = true
}

const handleEdit = async (row: ScoreRecordItem) => {
  if (!batchOptions.value.length || !groupOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'edit'
  scoreForm.id = row.id
  scoreForm.batchId = row.batchId
  scoreForm.groupId = row.groupId || ''
  scoreForm.studentId = row.studentId
  scoreForm.processScore = row.processScore
  scoreForm.reportScore = row.reportScore
  scoreForm.submissionScore = row.submissionScore
  scoreForm.defenseScore = row.defenseScore
  scoreForm.finalScore = row.finalScore
  scoreForm.gradeLevel = row.gradeLevel || ''
  scoreForm.remark = row.remark || ''
  await loadScoreFormStudentOptions(scoreForm.groupId)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!scoreForm.batchId) {
    ElMessage.warning('请先选择所属批次')
    return
  }
  if (!scoreForm.groupId) {
    ElMessage.warning('请先选择项目组')
    return
  }

  if (!scoreForm.studentId.trim()) {
    ElMessage.warning('请先选择学生')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createScoreRecordApi(scoreForm)
      ElMessage.success('成绩记录创建成功')
    } else {
      await updateScoreRecordApi(scoreForm)
      ElMessage.success('成绩记录修改成功')
    }
    dialogVisible.value = false
    fetchScoreRecordList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: ScoreRecordItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getScoreRecordDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: ScoreRecordItem) => {
  await ElMessageBox.confirm('确认删除这条成绩记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteScoreRecordApi(row.id)
    ElMessage.success('成绩记录删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchScoreRecordList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchScoreRecordList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchScoreRecordList()
}

onMounted(async () => {
  await fetchOptions()
  await fetchScoreRecordList()
})

watch(
  () => scoreForm.groupId,
  async (groupId, previousGroupId) => {
    if (!dialogVisible.value) return
    if (!groupId) {
      scoreFormStudentOptions.value = []
      scoreForm.studentId = ''
      return
    }

    await loadScoreFormStudentOptions(groupId)
    if (groupId !== previousGroupId && !scoreFormStudentOptions.value.some((item) => item.id === scoreForm.studentId)) {
      scoreForm.studentId = ''
    }
  },
)
</script>

<template>
  <div class="score-record-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Score Record Management</div>
          <h1>成绩记录管理</h1>
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
            style="width: 260px"
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
            style="width: 260px"
          >
            <el-option
              v-for="item in filteredGroupOptions"
              :key="item.id"
              :label="getGroupLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学生">
          <el-select
            v-model="queryForm.studentId"
            clearable
            filterable
            placeholder="请选择学生"
            style="width: 240px"
          >
            <el-option
              v-for="item in studentOptions"
              :key="item.id"
              :label="`${item.realName}（${item.username}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="等级">
          <el-input
            v-model="queryForm.gradeLevel"
            clearable
            placeholder="例如 A / 优秀"
            style="width: 160px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canManage" type="success" :icon="Plus" @click="handleCreate">新建成绩记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">成绩记录列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="score-record-table">
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
        <el-table-column label="学生" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getStudentLabel(row.studentId) }}
          </template>
        </el-table-column>
        <el-table-column label="过程分" width="90">
          <template #default="{ row }">{{ row.processScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="报告分" width="90">
          <template #default="{ row }">{{ row.reportScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="提交分" width="90">
          <template #default="{ row }">{{ row.submissionScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="答辩分" width="90">
          <template #default="{ row }">{{ row.defenseScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="最终总分" width="100">
          <template #default="{ row }">{{ row.finalScore ?? '--' }}</template>
        </el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="gradeTagType(row.gradeLevel)" effect="light">
              {{ row.gradeLevel || '--' }}
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
            <el-button
              v-if="canManage"
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
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

    <ScoreRecordFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="scoreForm"
      :batch-options="batchOptions"
      :group-options="groupOptions"
      :student-options="scoreFormStudentOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="成绩记录详情" width="780px" destroy-on-close>
      <div v-if="detailData" v-loading="detailLoading" class="detail-panel">
        <div class="detail-header">
          <div>
            <h2>{{ getBatchLabel(detailData.batchId) }}</h2>
            <p>学生：{{ getStudentLabel(detailData.studentId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="gradeTagType(detailData.gradeLevel)" effect="light">
              等级：{{ detailData.gradeLevel || '--' }}
            </el-tag>
            <el-tag type="primary" effect="light">
              总分：{{ detailData.finalScore ?? '--' }}
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
          <el-descriptions-item label="学生">
            {{ getStudentLabel(detailData.studentId) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailData.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="过程分">
            {{ detailData.processScore ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="报告分">
            {{ detailData.reportScore ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="提交分">
            {{ detailData.submissionScore ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="答辩分">
            {{ detailData.defenseScore ?? '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-remark">
          {{ detailData.remark || '暂无备注' }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.score-record-page {
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

.detail-remark {
  min-height: 120px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
