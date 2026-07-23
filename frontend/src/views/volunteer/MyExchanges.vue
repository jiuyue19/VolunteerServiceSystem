<template>
  <div class="my-exchanges">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>我的兑换</h2>
          <div class="header-stats">
            <span class="stat-item">
              <span class="stat-label">总兑换次数：</span>
              <span class="stat-value">{{ totalExchanges }}</span>
            </span>
            <span class="stat-item">
              <span class="stat-label">累计消费积分：</span>
              <span class="stat-value points">{{ totalPoints }}</span>
            </span>
          </div>
        </div>
      </template>
      
      <div class="page-content">
        <!-- 搜索和筛选栏 -->
        <div class="toolbar">
          <div class="search-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索商品名称..."
              clearable
              style="width: 300px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="filter-bar">
            <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px" @change="handleFilter">
              <el-option label="全部" value="" />
              <el-option label="待发货" value="pending" />
              <el-option label="已发货" value="shipped" />
              <el-option label="已签收" value="received" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
              <el-option label="成功退款" value="refunded" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
              @change="handleFilter"
            />
          </div>
        </div>
        
        <!-- 兑换记录列表 -->
        <div class="exchanges-list" v-loading="loading">
          <el-card 
            v-for="exchange in paginatedExchanges" 
            :key="exchange.id" 
            class="exchange-card"
            shadow="hover"
          >
            <div class="exchange-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ exchange.orderNumber }}</span>
                <el-tag :type="getStatusType(exchange.status)" size="small">
                  {{ getStatusText(exchange.status) }}
                </el-tag>
              </div>
              <div class="exchange-time">
                {{ formatTime(exchange.createTime) }}
              </div>
            </div>
            
            <div class="exchange-content">
              <div class="goods-info">
                <img :src="exchange.goodsImage" :alt="exchange.goodsName" class="goods-image" />
                <div class="goods-details">
                  <h4 class="goods-name">{{ exchange.goodsName }}</h4>
                  <p class="goods-desc">{{ exchange.goodsDescription }}</p>
                  <div class="exchange-details">
                    <span class="quantity">数量：{{ exchange.quantity }}</span>
                    <span class="points">积分：{{ exchange.totalPoints }}</span>
                  </div>
                </div>
              </div>
              
              <div class="delivery-info" v-if="exchange.deliveryInfo">
                <h5>配送信息</h5>
                <p class="address">{{ exchange.deliveryInfo.address }}</p>
                <p class="contact">联系人：{{ exchange.deliveryInfo.contact }} {{ exchange.deliveryInfo.phone }}</p>
                <p class="tracking" v-if="exchange.deliveryInfo.trackingNumber">
                  快递单号：{{ exchange.deliveryInfo.trackingNumber }}
                </p>
              </div>
            </div>
            <div class="refund-info" v-if="exchange.status === 'refunded'">
              <el-tag type="success" size="small" effect="plain">成功退款</el-tag>
              <span class="refund-text">积分已退回到您的账户</span>
              <span class="refund-time" v-if="exchange.refundAuditTime">
                退款时间：{{ formatTime(exchange.refundAuditTime) }}
              </span>
            </div>
            
            <div class="exchange-actions">
              <el-button size="small" @click="viewDetails(exchange)">查看详情</el-button>
              <el-button 
                v-if="exchange.status === 'shipped'" 
                size="small" 
                type="success" 
                @click="confirmReceived(exchange)"
              >
                确认收货
              </el-button>
              <el-button 
                v-if="exchange.status === 'pending'" 
                size="small" 
                type="danger" 
                @click="cancelExchange(exchange)"
              >
                取消兑换
              </el-button>
              <el-button 
                v-if="exchange.status === 'completed'" 
                size="small" 
                @click="exchangeAgain(exchange)"
              >
                再次兑换
              </el-button>
            </div>
          </el-card>
          
          <!-- 空状态 -->
          <div v-if="!loading && filteredExchanges.length === 0" class="empty-state">
            <el-empty description="还没有兑换记录">
              <el-button type="primary" @click="goToExchange">去积分商城看看</el-button>
            </el-empty>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination" v-if="totalPages > 1">
          <el-button :disabled="currentPage === 1" @click="prevPage">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
          <el-button :disabled="currentPage === totalPages" @click="nextPage">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="兑换详情" width="600px">
      <div v-if="selectedExchange" class="exchange-detail">
        <div class="detail-section">
          <h4>商品信息</h4>
          <div class="goods-detail">
            <img :src="selectedExchange.goodsImage" :alt="selectedExchange.goodsName" class="detail-image" />
            <div class="detail-info">
              <h5>{{ selectedExchange.goodsName }}</h5>
              <p>{{ selectedExchange.goodsDescription }}</p>
              <p>兑换数量：{{ selectedExchange.quantity }}</p>
              <p>消费积分：{{ selectedExchange.totalPoints }}</p>
            </div>
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedExchange.deliveryInfo">
          <h4>配送信息</h4>
          <p>收货地址：{{ selectedExchange.deliveryInfo.address }}</p>
          <p>联系人：{{ selectedExchange.deliveryInfo.contact }}</p>
          <p>联系电话：{{ selectedExchange.deliveryInfo.phone }}</p>
          <p v-if="selectedExchange.deliveryInfo.trackingNumber">
            快递单号：{{ selectedExchange.deliveryInfo.trackingNumber }}
          </p>
        </div>
        
        <div class="detail-section">
          <h4>订单状态</h4>
          <el-steps :active="getStepActive(selectedExchange)" align-center>
            <el-step title="提交订单" />
            <el-step title="商品发货" />
            <el-step title="确认收货" />
            <el-step title="兑换完成" />
            <el-step v-if="isRefunded(selectedExchange)" title="退款成功" />
          </el-steps>
        </div>

        <div class="detail-section" v-if="isRefunded(selectedExchange)">
          <h4>退款信息</h4>
          <p>退款状态：{{ selectedExchange.refundStatus || '成功退款' }}</p>
          <p v-if="selectedExchange.refundReason">退款原因：{{ selectedExchange.refundReason }}</p>
          <p v-if="selectedExchange.refundApplyTime">申请时间：{{ formatTime(selectedExchange.refundApplyTime) }}</p>
          <p v-if="selectedExchange.refundAuditTime">退款时间：{{ formatTime(selectedExchange.refundAuditTime) }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getMyExchanges, confirmExchange as confirmExchangeApi, cancelExchange as cancelExchangeApi } from '@/api/order'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = 10
const exchanges = ref([])
const detailDialogVisible = ref(false)
const selectedExchange = ref(null)

// 计算属性
const filteredExchanges = computed(() => {
  let result = [...exchanges.value]
  
  // 搜索筛选
  if (searchKeyword.value) {
    result = result.filter(exchange => 
      exchange.goodsName.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(exchange => exchange.status === statusFilter.value)
  }
  
  // 日期筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    result = result.filter(exchange => {
      const exchangeDate = new Date(exchange.createTime)
      return exchangeDate >= startDate && exchangeDate <= endDate
    })
  }
  
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredExchanges.value.length / pageSize) || 1
})

const paginatedExchanges = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredExchanges.value.slice(start, end)
})

const totalExchanges = computed(() => {
  return exchanges.value.length
})

const totalPoints = computed(() => {
  return exchanges.value.reduce((sum, exchange) => sum + exchange.totalPoints, 0)
})

// 方法
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    shipped: 'primary',
    received: 'success',
    completed: 'success',
    cancelled: 'danger',
    refunded: 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待发货',
    shipped: '已发货',
    received: '已签收',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '成功退款'
  }
  return texts[status] || '未知'
}

const isRefunded = (exchange) => {
  if (!exchange) return false
  return exchange.status === 'refunded'
}

const getStepActive = (exchange) => {
  if (!exchange) return 0
  if (isRefunded(exchange)) {
    // 退款成功，走到最后一步
    return 4
  }
  const steps = {
    pending: 0,
    shipped: 1,
    received: 2,
    completed: 3,
    cancelled: -1
  }
  return steps[exchange.status] || 0
}

const viewDetails = (exchange) => {
  selectedExchange.value = exchange
  detailDialogVisible.value = true
}

const confirmReceived = async (exchange) => {
  try {
    await ElMessageBox.confirm(
      '确认已收到商品吗？',
      '确认收货',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await confirmExchangeApi(exchange.id)
    ElMessage.success('确认收货成功')
    loadExchanges()
  } catch {
    // 用户取消
  }
}

const cancelExchange = async (exchange) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个兑换订单吗？取消后积分将退回到您的账户。',
      '取消兑换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cancelExchangeApi(exchange.id)
    ElMessage.success('兑换已取消，积分已退回')
    loadExchanges()
  } catch {
    // 用户取消
  }
}

const exchangeAgain = (exchange) => {
  // 跳转到兑换页面，并传递商品ID作为查询参数
  router.push({
    path: '/volunteer/exchange',
    query: { goodsId: exchange.goodsId }
  })
}

const goToExchange = () => {
  router.push('/volunteer/exchange')
}

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

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const loadExchanges = async () => {
  try {
    loading.value = true
    const res = await getMyExchanges()
    const list = Array.isArray(res.data) ? res.data : []
    const mapStatus = (status) => {
      if (status === '待发货') return 'pending'
      if (status === '已发货') return 'shipped'
      if (status === '已签收') return 'received'
      if (status === '已完成') return 'completed'
      if (status === '已取消') return 'cancelled'
      if (status === '已退款') return 'refunded'
      return 'pending'
    }
    exchanges.value = list.map(item => ({
      id: item.id,
      orderNumber: item.orderNumber,
      goodsId: item.goodsId,
      goodsName: item.goodsName,
      goodsDescription: item.goodsDescription,
      goodsImage: item.goodsImage,
      quantity: item.quantity,
      totalPoints: item.totalPoints,
      status: mapStatus(item.status),
      createTime: item.createdAt,
      refundStatus: item.refundStatus,
      refundApplyTime: item.refundApplyTime,
      refundAuditTime: item.refundAuditTime,
      deliveryInfo: {
        address: item.address,
        contact: item.contactName,
        phone: item.phone,
        trackingNumber: item.trackingNumber
      }
    }))
  } catch (error) {
    ElMessage.error('加载兑换记录失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadExchanges()
})
</script>

<style scoped>
.my-exchanges {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.main-card {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 15px 0;
  color: #ce4c4c;
  font-size: 24px;
  font-weight: 600;
}

.header-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-label {
  color: #666;
}

.stat-value {
  font-weight: 600;
  color: #333;
}

.stat-value.points {
  color: #ce4c4c;
}

.page-content {
  padding: 20px 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.exchanges-list {
  min-height: 400px;
}

.exchange-card {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.exchange-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.exchange-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-number {
  font-weight: 500;
  color: #333;
}

.exchange-time {
  font-size: 14px;
  color: #999;
}

.exchange-content {
  margin-bottom: 15px;
}

.goods-info {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.goods-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.goods-details {
  flex: 1;
}

.goods-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.goods-desc {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.exchange-details {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.delivery-info {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 6px;
  font-size: 14px;
}

.delivery-info h5 {
  margin: 0 0 8px 0;
  color: #333;
  font-weight: 500;
}

.delivery-info p {
  margin: 4px 0;
  color: #666;
}

.exchange-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page-info {
  font-size: 14px;
  color: #606266;
  margin: 0 10px;
}

/* 详情对话框样式 */
.exchange-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-weight: 600;
  border-bottom: 1px solid #eee;
  padding-bottom: 5px;
}

.goods-detail {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.detail-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.detail-info h5 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.detail-info p {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

/* 全局样式 */
:deep(.el-button--primary) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-button--primary:hover) {
  background-color: #d97373;
  border-color: #d97373;
}

:deep(.el-steps .el-step__title.is-process) {
  color: #ce4c4c;
}

:deep(.el-steps .el-step__head.is-process) {
  border-color: #ce4c4c;
  color: #ce4c4c;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar,
  .filter-bar {
    justify-content: center;
  }
  
  .exchange-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .goods-info {
    flex-direction: column;
  }
  
  .exchange-actions {
    justify-content: center;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
