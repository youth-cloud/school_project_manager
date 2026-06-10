<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createOperationLogApi,
  deleteOperationLogApi,
  getOperationLogDetailApi,
  getOperationLogPageApi,
  updateOperationLogApi,
  type OperationLogFormData,
  type OperationLogItem,
} from '@/api/operationLog'
import { useUserOptions } from '@/composables/useUserOptions'
import { useUserStore } from '@/stores/user'
import { getUserRealName } from '@/utils/userDisplay'
import OperationLogFormDialog from '@/views/operation-log/components/OperationLogFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<OperationLogItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<OperationLogItem | null>(null)

const deletingId = ref('')
const { userOptions, userOptionMap, loadUserOptions } = useUserOptions()

const queryForm = reactive({
  moduleName: '',
  operationType: '',
  operatorId: '',
  result: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const operationLogForm = reactive<OperationLogFormData>({
  moduleName: '',
  operationType: '',
  requestMethod: '',
  requestUri: '',
  ip: '',
  operationDesc: '',
  result: 'SUCCESS',
})

const roles = computed(() => userStore.userInfo?.roles || [])
const isAdmin = computed(() => roles.value.includes('ADMIN'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看全部日志，也可按操作人筛选'
  return '当前角色只能查看自己的日志记录'
})

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const emptyText = (value: string | null) => {
  return value && value.trim() ? value : '--'
}

const textPreview = (value: string | null, limit = 40) => {
  if (!value) return '--'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const getUserLabel = (id: string | null) => {
  return getUserRealName(id, userOptionMap.value)
}

const getResultTagType = (value: string | null) => {
  const result = (value || '').toUpperCase()
  if (result.includes('SUCCESS') || result.includes('PASS')) return 'success'
  if (result.includes('FAIL') || result.includes('ERROR')) return 'danger'
  if (result.includes('PENDING')) return 'warning'
  return 'info'
}

const resetOperationLogForm = () => {
  operationLogForm.id = undefined
  operationLogForm.moduleName = ''
  operationLogForm.operationType = ''
  operationLogForm.requestMethod = ''
  operationLogForm.requestUri = ''
  operationLogForm.ip = ''
  operationLogForm.operationDesc = ''
  operationLogForm.result = 'SUCCESS'
}

const fetchUserOptions = async () => {
  await loadUserOptions()
}

const fetchOperationLogList = async () => {
  loading.value = true
  try {
    const res = await getOperationLogPageApi({
      current: pagination.current,
      size: pagination.size,
      moduleName: queryForm.moduleName || undefined,
      operationType: queryForm.operationType || undefined,
      operatorId: isAdmin.value && queryForm.operatorId.trim() ? queryForm.operatorId.trim() : undefined,
      result: queryForm.result || undefined,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchOperationLogList()
}

const handleReset = () => {
  queryForm.moduleName = ''
  queryForm.operationType = ''
  queryForm.operatorId = ''
  queryForm.result = ''
  pagination.current = 1
  fetchOperationLogList()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  resetOperationLogForm()
  dialogVisible.value = true
}

const handleEdit = (row: OperationLogItem) => {
  dialogMode.value = 'edit'
  operationLogForm.id = row.id
  operationLogForm.moduleName = row.moduleName
  operationLogForm.operationType = row.operationType
  operationLogForm.requestMethod = row.requestMethod || ''
  operationLogForm.requestUri = row.requestUri || ''
  operationLogForm.ip = row.ip || ''
  operationLogForm.operationDesc = row.operationDesc || ''
  operationLogForm.result = row.result || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!operationLogForm.moduleName.trim()) {
    ElMessage.warning('请先填写模块名称')
    return
  }
  if (!operationLogForm.operationType.trim()) {
    ElMessage.warning('请先填写操作类型')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createOperationLogApi(operationLogForm)
      ElMessage.success('操作日志创建成功')
    } else {
      await updateOperationLogApi(operationLogForm)
      ElMessage.success('操作日志修改成功')
    }
    dialogVisible.value = false
    fetchOperationLogList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: OperationLogItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getOperationLogDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: OperationLogItem) => {
  await ElMessageBox.confirm('确认删除这条操作日志吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteOperationLogApi(row.id)
    ElMessage.success('操作日志删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchOperationLogList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchOperationLogList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchOperationLogList()
}

onMounted(() => {
  Promise.allSettled([
    fetchUserOptions(),
    fetchOperationLogList(),
  ])
})
</script>

<template>
  <div class="operation-log-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Operation Log</div>
          <h1>操作日志</h1>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="模块名称">
          <el-input
            v-model="queryForm.moduleName"
            clearable
            placeholder="请输入模块名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="操作类型">
          <el-input
            v-model="queryForm.operationType"
            clearable
            placeholder="请输入操作类型"
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item v-if="isAdmin" label="操作人">
          <el-select
            v-model="queryForm.operatorId"
            clearable
            filterable
            placeholder="请选择操作人"
            style="width: 220px"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="结果">
          <el-input
            v-model="queryForm.result"
            clearable
            placeholder="请输入结果"
            style="width: 160px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Plus" @click="handleCreate">新建日志</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">日志列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="operation-log-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="模块名称" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ row.moduleName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="140" show-overflow-tooltip />
        <el-table-column v-if="isAdmin" label="操作人" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getUserLabel(row.operatorId) }}
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="请求方式" width="110" />
        <el-table-column label="请求地址" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ emptyText(row.requestUri) }}</template>
        </el-table-column>
        <el-table-column prop="ip" label="IP 地址" width="140" show-overflow-tooltip />
        <el-table-column label="结果" width="120">
          <template #default="{ row }">
            <el-tag :type="getResultTagType(row.result)" effect="light">
              {{ emptyText(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ textPreview(row.operationDesc) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
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

    <OperationLogFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="operationLogForm"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="操作日志详情" width="860px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
          <div class="detail-header">
            <div>
              <h2>{{ detailData.moduleName }}</h2>
              <p>{{ detailData.operationType }}</p>
            </div>
            <div class="detail-tags">
              <el-tag :type="getResultTagType(detailData.result)" effect="light">
                {{ emptyText(detailData.result) }}
              </el-tag>
            </div>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="操作人">
              {{ getUserLabel(detailData.operatorId) }}
            </el-descriptions-item>
            <el-descriptions-item label="请求方式">
              {{ emptyText(detailData.requestMethod) }}
            </el-descriptions-item>
            <el-descriptions-item label="IP 地址">
              {{ emptyText(detailData.ip) }}
            </el-descriptions-item>
            <el-descriptions-item label="请求地址" :span="2">
              {{ emptyText(detailData.requestUri) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(detailData.createTime) }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-block">
            <div class="detail-block-title">操作描述</div>
            <div class="detail-block-content">{{ detailData.operationDesc || '暂无操作描述' }}</div>
          </div>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.operation-log-page {
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
</style>
