<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>报名审核</h2>
        </div>
      </template>
      
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchText"
            placeholder="搜索活动名称或志愿者名称"
            clearable
            style="width: 300px"
          />
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="待审核" />
            <el-option label="待参与" value="待参与" />
            <el-option label="已拒绝" value="已拒绝" />
            <el-option label="已通过" value="已通过" />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量删除</el-button>
        </div>
      </div>

      <el-table :data="paginatedList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="activityName" label="活动名称" width="180" />
        <el-table-column prop="volunteerName" label="志愿者名称" width="120" />
        <el-table-column prop="activityAddress" label="活动地址" width="150" show-overflow-tooltip />
        <el-table-column label="活动时间" width="170">
          <template #default="{ row }">
            <div style="font-size: 12px; line-height: 1.5;">
              <div>{{ formatDateTime(row.activityStartTime) }}</div>
              <div style="color: #909399;">至</div>
              <div>{{ formatDateTime(row.activityEndTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.rejectReason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待审核'"
              size="small"
              type="success"
              @click="handleReview(row, '已通过')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === '待审核'"
              size="small"
              type="danger"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <span v-if="row.status !== '待审核'" style="color: #909399; font-size: 12px;">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-button :disabled="currentPage === 1" @click="prevPage">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-button :disabled="currentPage === totalPages" @click="nextPage">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </el-card>
    
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { reviewApplication, getApplications, deleteApplication, batchDeleteApplications } from '@/api/activity'

const applications = ref([])
const filteredApplications = ref([])
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRow = ref(null)
const searchText = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const selectedIds = ref([])

const loadApplications = async () => {
  try {
    const res = await getApplications()
    console.log('Fetched applications:', res.data)
    applications.value = res.data || []
    filteredApplications.value = applications.value
  } catch (error) {
    console.error('加载申请列表失败', error)
    ElMessage.error('加载申请列表失败')
  }
}

const handleSearch = () => {
  let result = applications.value
  
  // 文本搜索
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(item => 
      (item.activityName && item.activityName.toLowerCase().includes(keyword)) ||
      (item.volunteerName && item.volunteerName.toLowerCase().includes(keyword))
    )
  }
  
  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value)
  }
  
  filteredApplications.value = result
  currentPage.value = 1
}

const totalPages = computed(() => {
  return Math.ceil(filteredApplications.value.length / pageSize.value) || 1
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredApplications.value.slice(start, end)
})

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const d = new Date(datetime)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getStatusType = (status) => {
  const typeMap = {
    '待审核': 'warning',
    '待参与': 'info',
    '已通过': 'success',
    '已拒绝': 'danger'
  }
  return typeMap[status] || 'info'
}

const handleReview = async (row, status) => {
  try {
    await reviewApplication({
      id: row.id,
      status,
      rejectReason: null
    })
    ElMessage.success('审核成功')
    await loadApplications()
    handleSearch()
  } catch (error) {
    console.error(error)
  }
}

const handleReject = (row) => {
  currentRow.value = row
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    await reviewApplication({
      id: currentRow.value.id,
      status: '已拒绝',
      rejectReason: rejectReason.value
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    rejectReason.value = ''
    await loadApplications()
    handleSearch()
  } catch (error) {
    console.error(error)
  }
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条申请记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await batchDeleteApplications(selectedIds.value)
      ElMessage.success('批量删除成功')
      await loadApplications()
      handleSearch()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  })
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  color: #ce4c4c;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.page-info {
  font-size: 14px;
  color: #606266;
}

:deep(.el-button--primary) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-button--primary:hover) {
  background-color: #d97373;
  border-color: #d97373;
}

:deep(.el-table__header th) {
  background-color: #f5f5f5;
  color: #333;
}
</style>