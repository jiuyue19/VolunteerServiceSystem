<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="header"><h2>补录申请管理</h2></div></template>
      <div class="toolbar">
        <div class="search-box">
          <el-input v-model="searchText" placeholder="搜索志愿者姓名" clearable style="width: 300px" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量删除</el-button>
        </div>
      </div>
      <el-table :data="paginatedList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="volunteerName" label="志愿者" width="120" />
        <el-table-column prop="activityName" label="活动名称" width="200" />
        <el-table-column label="补录时长" width="100">
          <template #default="{ row }">
            {{ row.hours }}小时
          </template>
        </el-table-column>
        <el-table-column label="补录积分" width="100">
          <template #default="{ row }">
            {{ row.earnedPoints || 0 }}分
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="补录原因" show-overflow-tooltip />
        <el-table-column label="佐证材料" width="120">
          <template #default="{ row }">
            <el-button v-if="row.evidenceFiles" type="text" size="small" @click="showEvidence(row)">
              查看材料
            </el-button>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待审核' || row.status === 0" type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === '待审核' || row.status === 0" type="warning" size="small" @click="handleReject(row)">拒绝</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-button :disabled="currentPage === 1" @click="prevPage"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-button :disabled="currentPage === totalPages" @click="nextPage"><el-icon><ArrowRight /></el-icon></el-button>
      </div>
    </el-card>

    <!-- 佐证材料查看弹窗 -->
    <el-dialog v-model="showEvidenceDialog" title="佐证材料" width="800px">
      <div v-if="currentEvidence && currentEvidence.length > 0" class="evidence-container">
        <div v-for="(file, index) in currentEvidence" :key="index" class="evidence-item">
          <div class="file-info">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">
              ({{ formatFileSize(file.size) }}
              <span v-if="file.compressedSize && file.originalSize !== file.compressedSize">
                → {{ formatFileSize(file.compressedSize) }}
              </span>)
            </span>
          </div>
          <div v-if="file.data && file.type.startsWith('image/')" class="image-preview">
            <el-image 
              :src="file.data" 
              :preview-src-list="[file.data]"
              fit="contain"
              style="max-width: 200px; max-height: 200px;"
            />
          </div>
          <div v-else-if="file.type === 'application/pdf'" class="pdf-info">
            <el-icon><Document /></el-icon>
            <span>PDF文件 - {{ file.name }}</span>
          </div>
        </div>
      </div>
      <div v-else class="no-evidence">
        <el-empty description="暂无佐证材料" />
      </div>
      
      <template #footer>
        <el-button @click="showEvidenceDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReplenishList, reviewReplenish, deleteReplenish, batchDeleteReplenish } from '@/api/replenish'

const dataList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = 5

// 佐证材料相关
const showEvidenceDialog = ref(false)
const currentEvidence = ref([])

const filteredList = computed(() => searchText.value ? dataList.value.filter(item => item.volunteerName?.includes(searchText.value)) : dataList.value)
const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize) || 1)
const paginatedList = computed(() => filteredList.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize))

const handleSearch = () => { currentPage.value = 1 }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
const handleSelectionChange = (selection) => { selectedIds.value = selection.map(item => item.id) }

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 获取状态类型
const getStatusType = (status) => {
  if (status === '已通过' || status === 1) return 'success'
  if (status === '已拒绝' || status === 2) return 'danger'
  return 'warning'
}

// 获取状态文本
const getStatusText = (status) => {
  if (status === '已通过' || status === 1) return '已通过'
  if (status === '已拒绝' || status === 2) return '已拒绝'
  return '待审核'
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 显示佐证材料
const showEvidence = (row) => {
  try {
    if (row.evidenceFiles) {
      const files = JSON.parse(row.evidenceFiles)
      currentEvidence.value = Array.isArray(files) ? files : []
    } else {
      currentEvidence.value = []
    }
    showEvidenceDialog.value = true
  } catch (error) {
    console.error('解析佐证材料失败:', error)
    ElMessage.error('佐证材料格式错误')
    currentEvidence.value = []
    showEvidenceDialog.value = true
  }
}

const handleApprove = async (row) => {
  try {
    await reviewReplenish({
      id: row.id,
      status: '已通过',
      rejectReason: null
    })
    ElMessage.success('审核通过')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleReject = async (row) => {
  try {
    const { value: rejectReason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '拒绝原因不能为空'
    })
    
    await reviewReplenish({
      id: row.id,
      status: '已拒绝',
      rejectReason
    })
    ElMessage.success('已拒绝')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteReplenish(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await batchDeleteReplenish(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const loadData = async () => {
  try {
    const res = await getReplenishList()
    dataList.value = res.data || []
    console.log('管理员端加载补录数据:', dataList.value)
    // 检查佐证材料数据
    dataList.value.forEach(item => {
      if (item.evidenceFiles) {
        console.log(`ID ${item.id} 的佐证材料:`, item.evidenceFiles)
      }
    })
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.header h2 { margin: 0; color: #ce4c4c; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.search-box { display: flex; gap: 10px; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 20px; margin-top: 20px; }
.page-info { font-size: 14px; color: #606266; }
:deep(.el-button--primary) { background-color: #ce4c4c; border-color: #ce4c4c; }
:deep(.el-button--primary:hover) { background-color: #d97373; border-color: #d97373; }
:deep(.el-table__header th) { background-color: #f5f5f5; color: #333; }

/* 佐证材料样式 */
.evidence-container {
  max-height: 500px;
  overflow-y: auto;
}

.evidence-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fafafa;
}

.file-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.file-name {
  font-weight: 500;
  margin-left: 8px;
  margin-right: 8px;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.image-preview {
  text-align: center;
  margin-top: 10px;
}

.pdf-info {
  display: flex;
  align-items: center;
  color: #606266;
  margin-top: 10px;
}

.no-evidence {
  text-align: center;
  padding: 40px 0;
}
</style>