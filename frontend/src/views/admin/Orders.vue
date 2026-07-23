<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="header"><h2>兑换订单管理</h2></div></template>
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
        <el-table-column v-if="false" prop="id" label="记录ID" width="80" />

        <el-table-column prop="orderNumber" label="订单编号" width="180">
          <template #default="{ row }">
            {{ row.orderNumber || row.id }}
          </template>
        </el-table-column>
        <el-table-column prop="trackingNumber" label="快递单号" width="200">
          <template #default="{ row }">
            {{ row.trackingNumber || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="volunteerId" label="志愿者ID" width="100" />
        <el-table-column prop="volunteerName" label="志愿者" width="120" />
        <el-table-column v-if="false" prop="goodsId" label="商品ID" width="100" />

        <el-table-column prop="goodsName" label="商品名称" width="160" />
        <el-table-column prop="goodsDescription" label="商品描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="goodsImage" label="商品图片" width="120">
          <template #default="{ row }">
            <img
              v-if="row.goodsImage"
              :src="row.goodsImage"
              alt="商品图片"
              style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #eee;"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPoints" label="消耗积分" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="收货地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status || '待发货' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundStatus" label="退款状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.refundStatus" type="warning">{{ row.refundStatus }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundReason" label="退款原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="退款凭证" width="120">
          <template #default="{ row }">
            <el-button v-if="row.refundEvidence" type="text" size="small" @click="showRefundEvidence(row)">
              查看材料
            </el-button>
            <span v-else>无</span>
          </template>
        </el-table-column>

        <el-table-column prop="refundApplyTime" label="退款申请时间" width="180">
          <template #default="{ row }">
            {{ row.refundApplyTime ? formatTime(row.refundApplyTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="refundAuditTime" label="退款审核时间" width="180">
          <template #default="{ row }">
            {{ row.refundAuditTime ? formatTime(row.refundAuditTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column v-if="false" prop="refundAdminId" label="审核管理员ID" width="140" />
        <el-table-column prop="createdAt" label="兑换时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待发货'" type="success" size="small" @click="handleShip(row)">发货</el-button>
            <el-button v-if="row.status === '已签收'" type="primary" size="small" @click="handleComplete(row)">完成</el-button>
            <el-button
              v-if="row.refundStatus === '待审核'"
              type="warning"
              size="small"
              @click="handleApproveRefund(row)"
            >同意退款</el-button>
            <el-button
              v-if="row.refundStatus === '待审核'"
              size="small"
              @click="handleRejectRefund(row)"
            >拒绝退款</el-button>
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

    <!-- 退款凭证查看弹窗 -->
    <el-dialog v-model="showEvidenceDialog" title="退款凭证" width="800px">
      <div v-if="currentEvidence && currentEvidence.length > 0" class="evidence-container">
        <div v-for="(file, index) in currentEvidence" :key="index" class="evidence-item">
          <div class="file-info">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">({{ formatFileSize(file.size) }})</span>
          </div>
          <div v-if="file.data && file.type && file.type.startsWith('image/')" class="image-preview">
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
        <el-empty description="暂无退款凭证" />
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
import { getOrderList, updateOrder, deleteOrder, batchDeleteOrder, reviewExchangeRefund } from '@/api/order'

const dataList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = 5

// 退款凭证查看
const showEvidenceDialog = ref(false)
const currentEvidence = ref([])

const filteredList = computed(() => searchText.value ? dataList.value.filter(item => item.volunteerName?.includes(searchText.value)) : dataList.value)
const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize) || 1)
const paginatedList = computed(() => filteredList.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize))

const handleSearch = () => { currentPage.value = 1 }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
const handleSelectionChange = (selection) => { selectedIds.value = selection.map(item => item.id) }

const getStatusType = (status) => {
  if (status === '待发货') return 'warning'
  if (status === '已发货') return 'primary'
  if (status === '已签收') return 'success'
  if (status === '已完成') return 'success'
  if (status === '已取消') return 'info'
  if (status === '已退款') return 'danger'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  try {
    return new Date(time).toLocaleString()
  } catch (e) {
    return String(time)
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const showRefundEvidence = (row) => {
  try {
    if (row.refundEvidence) {
      const files = JSON.parse(row.refundEvidence)
      currentEvidence.value = Array.isArray(files) ? files : []
    } else {
      currentEvidence.value = []
    }
    showEvidenceDialog.value = true
  } catch (error) {
    console.error('解析退款凭证失败:', error)
    ElMessage.error('退款凭证格式错误')
    currentEvidence.value = []
    showEvidenceDialog.value = true
  }
}

const generateTrackingNumber = () => {
  const timestamp = Date.now().toString()
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  return `EX${timestamp}${random}`
}

const handleApproveRefund = async (row) => {
  ElMessageBox.confirm('确定要同意该订单的退款申请吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await reviewExchangeRefund(row.id, {
        refundStatus: '已通过',
        refundReason: row.refundReason,
        refundEvidence: row.refundEvidence
      })
      ElMessage.success('退款已通过，积分已退回')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    }
  })
}

const handleShip = async (row) => {
  try {
    const trackingNumber = row.trackingNumber || generateTrackingNumber()
    await updateOrder({ ...row, status: '已发货', trackingNumber })
    ElMessage.success('已发货')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleComplete = async (row) => {
  try {
    await updateOrder({ ...row, status: '已完成' })
    ElMessage.success('订单已完成')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteOrder(row.id)
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
      await batchDeleteOrder(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const loadData = async () => {
  try {
    const res = await getOrderList()
    dataList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
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
</style>