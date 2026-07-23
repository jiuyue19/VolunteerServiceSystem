<template>
  <div class="my-orders">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
          <div class="header-stats">
            <span class="stat-item">
              <span class="stat-label">总订单数：</span>
              <span class="stat-value">{{ totalOrders }}</span>
            </span>
            <span class="stat-item">
              <span class="stat-label">待处理：</span>
              <span class="stat-value pending">{{ pendingOrders }}</span>
            </span>
            <span class="stat-item">
              <span class="stat-label">已完成：</span>
              <span class="stat-value completed">{{ completedOrders }}</span>
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
              placeholder="搜索订单号或商品名称..."
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
              <el-option label="待付款" value="unpaid" />
              <el-option label="待发货" value="pending" />
              <el-option label="已发货" value="shipped" />
              <el-option label="已签收" value="received" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
              <el-option label="已退款" value="refunded" />
            </el-select>
            <el-select v-model="typeFilter" placeholder="类型筛选" style="width: 120px" @change="handleFilter">
              <el-option label="全部" value="" />
              <el-option label="积分兑换" value="exchange" />
              <el-option label="活动报名" value="activity" />
              <el-option label="捐赠订单" value="donation" />
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
        
        <!-- 订单列表 -->
        <div class="orders-list" v-loading="loading">
          <el-card 
            v-for="order in paginatedOrders" 
            :key="order.id" 
            class="order-card"
            shadow="hover"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ order.orderNumber }}</span>
                <el-tag :type="getStatusType(order.status)" size="small">
                  {{ getStatusText(order.status) }}
                </el-tag>
                <el-tag :type="getTypeColor(order.type)" size="small">
                  {{ getTypeText(order.type) }}
                </el-tag>
              </div>
              <div class="order-time">
                {{ formatTime(order.createTime) }}
              </div>
            </div>
            
            <div class="order-content">
              <!-- 商品信息 -->
              <div class="items-list">
                <div 
                  v-for="item in order.items" 
                  :key="item.id" 
                  class="order-item"
                >
                  <img :src="item.image" :alt="item.name" class="item-image" />
                  <div class="item-details">
                    <h4 class="item-name">{{ item.name }}</h4>
                    <p class="item-desc">{{ item.description }}</p>
                    <div class="item-meta">
                      <span class="quantity">数量：{{ item.quantity }}</span>
                      <span class="price" v-if="item.price">
                        价格：¥{{ item.price }}
                      </span>
                      <span class="points" v-if="item.points">
                        积分：{{ item.points }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 订单总计 -->
              <div class="order-summary">
                <div class="summary-item" v-if="order.totalAmount">
                  <span class="label">订单金额：</span>
                  <span class="value amount">¥{{ order.totalAmount }}</span>
                </div>
                <div class="summary-item" v-if="order.totalPoints">
                  <span class="label">消费积分：</span>
                  <span class="value points">{{ order.totalPoints }}</span>
                </div>
              </div>
              
              <!-- 配送信息 -->
              <div class="delivery-info" v-if="order.deliveryInfo">
                <h5>配送信息</h5>
                <p class="address">{{ order.deliveryInfo.address }}</p>
                <p class="contact">联系人：{{ order.deliveryInfo.contact }} {{ order.deliveryInfo.phone }}</p>
                <p class="tracking" v-if="order.deliveryInfo.trackingNumber">
                  快递单号：
                  <el-link type="primary" @click="trackPackage(order.deliveryInfo.trackingNumber)">
                    {{ order.deliveryInfo.trackingNumber }}
                  </el-link>
                </p>
              </div>
            </div>
            <div class="refund-info" v-if="order.status === 'refunded'">
              <el-tag type="success" size="small" effect="plain">成功退款</el-tag>
              <span class="refund-text">积分已退回到您的账户</span>
              <span class="refund-time" v-if="order.refundAuditTime">
                退款时间：{{ formatTime(order.refundAuditTime) }}
              </span>
            </div>
            
            <div class="order-actions">
              <el-button size="small" @click="viewDetails(order)">查看详情</el-button>
              <el-button 
                v-if="order.status === 'unpaid'" 
                size="small" 
                type="primary" 
                @click="payOrder(order)"
              >
                立即付款
              </el-button>
              <el-button 
                v-if="order.status === 'shipped'" 
                size="small" 
                type="success" 
                @click="confirmReceived(order)"
              >
                确认收货
              </el-button>
              <el-button 
                v-if="['unpaid', 'pending'].includes(order.status)" 
                size="small" 
                type="danger" 
                @click="cancelOrder(order)"
              >
                取消订单
              </el-button>
              <el-button 
                v-if="order.status === 'completed'" 
                size="small" 
                @click="orderAgain(order)"
              >
                再次下单
              </el-button>
              <el-button 
                v-if="order.status === 'completed'" 
                size="small" 
                @click="applyRefund(order)"
              >
                申请退款
              </el-button>
            </div>
          </el-card>
          
          <!-- 空状态 -->
          <div v-if="!loading && filteredOrders.length === 0" class="empty-state">
            <el-empty description="还没有订单记录">
              <el-button type="primary" @click="goToShop">去商城看看</el-button>
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
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <div v-if="selectedOrder" class="order-detail">
        <div class="detail-section">
          <h4>订单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单号：</span>
              <span class="value">{{ selectedOrder.orderNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(selectedOrder.status)">
                {{ getStatusText(selectedOrder.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">订单类型：</span>
              <el-tag :type="getTypeColor(selectedOrder.type)">
                {{ getTypeText(selectedOrder.type) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatTime(selectedOrder.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>商品清单</h4>
          <el-table :data="selectedOrder.items" border>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">
                <span v-if="row.price">¥{{ row.price }}</span>
                <span v-else-if="row.points">{{ row.points }}积分</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                <span v-if="row.price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
                <span v-else-if="row.points">{{ row.points * row.quantity }}积分</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="detail-section" v-if="selectedOrder.deliveryInfo">
          <h4>配送信息</h4>
          <p>收货地址：{{ selectedOrder.deliveryInfo.address }}</p>
          <p>联系人：{{ selectedOrder.deliveryInfo.contact }}</p>
          <p>联系电话：{{ selectedOrder.deliveryInfo.phone }}</p>
          <p v-if="selectedOrder.deliveryInfo.trackingNumber">
            快递单号：{{ selectedOrder.deliveryInfo.trackingNumber }}
          </p>
        </div>

        <div class="detail-section" v-if="selectedOrder && selectedOrder.status === 'refunded'">
          <h4>退款信息</h4>
          <p>退款状态：{{ selectedOrder.refundStatus || '成功退款' }}</p>
          <p v-if="selectedOrder.refundReason">退款原因：{{ selectedOrder.refundReason }}</p>
          <p v-if="selectedOrder.refundApplyTime">申请时间：{{ formatTime(selectedOrder.refundApplyTime) }}</p>
          <p v-if="selectedOrder.refundAuditTime">退款时间：{{ formatTime(selectedOrder.refundAuditTime) }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 申请退款弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="600px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="请填写退款原因" />
        </el-form-item>
        <el-form-item label="佐证材料">
          <el-upload
            ref="refundUploadRef"
            :file-list="refundFileList"
            :on-change="handleRefundFileChange"
            :on-remove="handleRefundFileRemove"
            :before-upload="beforeRefundUpload"
            :auto-upload="false"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持图片、PDF等格式，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingRefund" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 再次兑换选择地址弹窗 -->
    <el-dialog
      v-model="addressDialogVisible"
      title="再次兑换确认"
      width="600px"
    >
      <div v-if="reExchangeGoods" class="address-dialog-body">
        <p>商品：{{ reExchangeGoods.name }}（{{ reExchangeGoods.points }} 积分）</p>
        <p>当前积分：{{ userPoints }}</p>
        <div v-if="addressLoading" style="margin: 20px 0;">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else>
          <div v-if="addressList.length">
            <el-radio-group v-model="selectedAddressId" class="address-radio-group">
              <el-radio
                v-for="addr in addressList"
                :key="addr.id"
                :label="addr.id"
              >
                {{ addr.contactName }} {{ addr.contactPhone }} -
                {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
                <el-tag v-if="addr.isDefault" size="small" type="success" style="margin-left: 8px;">默认</el-tag>
              </el-radio>
            </el-radio-group>
          </div>
          <div v-else style="margin-top: 10px;">
            <el-empty description="暂无收货地址，请先在“收货地址管理”中添加" />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addressDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!selectedAddressId || !reExchangeGoods || !addressList.length || userPoints < (reExchangeGoods?.points || 0)"
            :loading="confirmingReExchange"
            @click="submitReExchange"
          >
            确认兑换
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowLeft, ArrowRight, UploadFilled } from '@element-plus/icons-vue'
import { getMyExchanges, confirmExchange as confirmExchangeApi, cancelExchange as cancelExchangeApi, applyExchangeRefund, exchangeGoods as exchangeGoodsApi } from '@/api/order'
import { getVolunteerInfo } from '@/api/volunteer'
import { getAddressList } from '@/api/address'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = 10
const orders = ref([])
const detailDialogVisible = ref(false)
const selectedOrder = ref(null)
const refundDialogVisible = ref(false)
const refundForm = ref({ reason: '' })
const refundUploadRef = ref(null)
const refundFileList = ref([])
const submittingRefund = ref(false)
const refundOrderId = ref(null)
const addressDialogVisible = ref(false)
const addressLoading = ref(false)
const addressList = ref([])
const selectedAddressId = ref(null)
const reExchangeGoods = ref(null)
const confirmingReExchange = ref(false)
const userPoints = ref(0)

// 计算属性
const filteredOrders = computed(() => {
  let result = [...orders.value]
  
  // 搜索筛选
  if (searchKeyword.value) {
    result = result.filter(order => 
      order.orderNumber.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      order.items.some(item => item.name.toLowerCase().includes(searchKeyword.value.toLowerCase()))
    )
  }
  
  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(order => order.status === statusFilter.value)
  }
  
  // 类型筛选
  if (typeFilter.value) {
    result = result.filter(order => order.type === typeFilter.value)
  }
  
  // 日期筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    result = result.filter(order => {
      const orderDate = new Date(order.createTime)
      return orderDate >= startDate && orderDate <= endDate
    })
  }
  
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize) || 1
})

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredOrders.value.slice(start, end)
})

const totalOrders = computed(() => {
  return orders.value.length
})

const pendingOrders = computed(() => {
  return orders.value.filter(order => ['unpaid', 'pending'].includes(order.status)).length
})

const completedOrders = computed(() => {
  return orders.value.filter(order => order.status === 'completed').length
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
    unpaid: 'warning',
    pending: 'info',
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
    unpaid: '待付款',
    pending: '待发货',
    shipped: '已发货',
    received: '已签收',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '成功退款'
  }
  return texts[status] || '未知'
}

const getTypeColor = (type) => {
  const colors = {
    exchange: 'success',
    activity: 'info',
    donation: 'warning'
  }
  return colors[type] || 'info'
}

const getTypeText = (type) => {
  const texts = {
    exchange: '积分兑换',
    activity: '活动报名',
    donation: '捐赠订单'
  }
  return texts[type] || '其他'
}

const viewDetails = (order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

const payOrder = (order) => {
  ElMessage.info(`跳转到支付页面，订单号：${order.orderNumber}`)
  // 实际应该跳转到支付页面
}

const confirmReceived = async (order) => {
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
    
    if (order.type === 'exchange') {
      await confirmExchangeApi(order.id)
      ElMessage.success('确认收货成功')
      loadOrders()
    } else {
      ElMessage.info('当前仅支持积分兑换订单的收货确认')
    }
  } catch {
    // 用户取消
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (order.type === 'exchange') {
      await cancelExchangeApi(order.id)
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.info('当前仅支持积分兑换订单的取消')
    }
  } catch {
    // 用户取消
  }
}

const orderAgain = async (order) => {
  if (order.type !== 'exchange') {
    ElMessage.info('当前仅支持积分兑换订单的再次下单')
    return
  }
  const item = (order.items && order.items.length > 0) ? order.items[0] : null
  if (!item) {
    ElMessage.error('订单商品信息缺失')
    return
  }
  reExchangeGoods.value = {
    id: item.id,
    name: item.name,
    points: item.points || order.totalPoints
  }
  await loadUserPoints()
  if (userPoints.value < (reExchangeGoods.value.points || 0)) {
    ElMessage.warning('积分不足')
    return
  }
  await loadAddressList()
  if (!addressList.value.length) {
    ElMessage.warning('暂无收货地址，请先在“收货地址管理”中添加')
    return
  }
  const defaultAddr = addressList.value.find(addr => addr.isDefault)
  selectedAddressId.value = defaultAddr ? defaultAddr.id : addressList.value[0].id
  addressDialogVisible.value = true
}

const applyRefund = (order) => {
  if (order.type !== 'exchange') {
    ElMessage.info('当前仅支持积分兑换订单的退款')
    return
  }
  if (order.status !== 'completed') {
    ElMessage.warning('只有已完成的订单可以申请退款')
    return
  }
  if (order.refundStatus === '待审核') {
    ElMessage.info('该订单已提交退款申请，请等待审核')
    return
  }
  if (order.refundStatus === '已通过') {
    ElMessage.info('该订单已完成退款')
    return
  }
  refundOrderId.value = order.id
  refundForm.value = { reason: '' }
  refundFileList.value = []
  refundDialogVisible.value = true
}

const trackPackage = (trackingNumber) => {
  ElMessage.info(`查询快递：${trackingNumber}`)
  // 实际应该跳转到快递查询页面或打开快递公司官网
}

const goToShop = () => {
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

const loadUserPoints = async () => {
  try {
    const userRes = await getVolunteerInfo()
    if (!userRes || !userRes.data) {
      ElMessage.error('获取用户信息失败')
      return
    }
    userPoints.value = userRes.data.points || 0
  } catch (error) {
    ElMessage.error('获取用户信息失败: ' + (error.message || '未知错误'))
  }
}

const loadAddressList = async () => {
  try {
    addressLoading.value = true
    const res = await getAddressList()
    const list = Array.isArray(res.data) ? res.data : []
    addressList.value = list.map(item => ({
      ...item,
      isDefault: !!item.isDefault
    }))
  } catch (error) {
    ElMessage.error('加载收货地址失败: ' + (error.message || '未知错误'))
  } finally {
    addressLoading.value = false
  }
}

const submitReExchange = async () => {
  if (!reExchangeGoods.value || !selectedAddressId.value) return
  if (userPoints.value < (reExchangeGoods.value.points || 0)) {
    ElMessage.warning('积分不足')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认使用 ${reExchangeGoods.value.points} 积分再次兑换 ${reExchangeGoods.value.name}？`,
      '确认兑换',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  try {
    confirmingReExchange.value = true
    const payload = {
      goodsId: reExchangeGoods.value.id,
      quantity: 1,
      addressId: selectedAddressId.value
    }
    await exchangeGoodsApi(payload)
    ElMessage.success('再次兑换成功')
    addressDialogVisible.value = false
    await loadUserPoints()
    await loadOrders()
  } catch (error) {
    ElMessage.error('兑换失败: ' + (error.message || '未知错误'))
  } finally {
    confirmingReExchange.value = false
  }
}

const handleRefundFileChange = (file, uploadFileList) => {
  refundFileList.value = uploadFileList
}

const handleRefundFileRemove = (file, uploadFileList) => {
  refundFileList.value = uploadFileList
}

const beforeRefundUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 'image/jpg'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isValidType) {
    ElMessage.error('只能上传图片或PDF文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return false
}

const refundFileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    if (file.type.startsWith('image/')) {
      compressRefundImage(file).then(resolve).catch(reject)
    } else {
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = () => resolve(reader.result)
      reader.onerror = error => reject(error)
    }
  })
}

const compressRefundImage = (file, maxWidth = 800, quality = 0.8) => {
  return new Promise((resolve, reject) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()
    img.onload = () => {
      let { width, height } = img
      if (width > maxWidth) {
        height = (height * maxWidth) / width
        width = maxWidth
      }
      canvas.width = width
      canvas.height = height
      ctx.drawImage(img, 0, 0, width, height)
      const compressedBase64 = canvas.toDataURL(file.type, quality)
      resolve(compressedBase64)
    }
    img.onerror = reject
    const reader = new FileReader()
    reader.onload = (e) => {
      img.src = e.target.result
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

const submitRefund = async () => {
  if (!refundOrderId.value) return
  if (!refundForm.value.reason) {
    ElMessage.warning('请填写退款原因')
    return
  }
  try {
    submittingRefund.value = true
    let evidenceFiles = ''
    if (refundFileList.value && refundFileList.value.length > 0) {
      const fileInfos = []
      for (const fileItem of refundFileList.value) {
        const file = fileItem.raw || fileItem
        if (file) {
          try {
            if (file.type && file.type.startsWith('image/')) {
              const base64 = await refundFileToBase64(file)
              const base64Size = base64.length * 0.75
              if (base64Size > 5 * 1024 * 1024) {
                ElMessage.warning(`图片 ${file.name} 压缩后仍然过大，将跳过`)
                continue
              }
              fileInfos.push({
                name: file.name,
                type: file.type,
                size: file.size,
                data: base64
              })
            } else {
              fileInfos.push({
                name: file.name,
                type: file.type,
                size: file.size,
                data: null
              })
            }
          } catch (error) {
            ElMessage.warning(`文件 ${file.name} 处理失败，将跳过`)
          }
        }
      }
      evidenceFiles = JSON.stringify(fileInfos)
      const jsonSize = new Blob([evidenceFiles]).size
      if (jsonSize > 10 * 1024 * 1024) {
        ElMessage.error('佐证材料总大小超过限制，请减少文件数量或使用更小的图片')
        return
      }
    }
    await applyExchangeRefund(refundOrderId.value, {
      refundReason: refundForm.value.reason,
      refundEvidence: evidenceFiles
    })
    ElMessage.success('退款申请已提交，请等待审核')
    refundDialogVisible.value = false
    refundOrderId.value = null
    refundForm.value = { reason: '' }
    refundFileList.value = []
    await loadOrders()
  } catch (error) {
    ElMessage.error('提交退款申请失败: ' + (error.message || '未知错误'))
  } finally {
    submittingRefund.value = false
  }
}

const loadOrders = async () => {
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
    orders.value = list.map(item => ({
      id: item.id,
      orderNumber: item.orderNumber,
      type: 'exchange',
      status: mapStatus(item.status),
      totalAmount: null,
      totalPoints: item.totalPoints,
      createTime: item.createdAt,
      refundStatus: item.refundStatus,
      refundApplyTime: item.refundApplyTime,
      refundAuditTime: item.refundAuditTime,
      refundReason: item.refundReason,
      items: [
        {
          id: item.goodsId,
          name: item.goodsName,
          description: item.goodsDescription,
          image: item.goodsImage,
          quantity: item.quantity,
          points: item.quantity ? Math.round((item.totalPoints / item.quantity) * 100) / 100 : item.totalPoints
        }
      ],
      deliveryInfo: {
        address: item.address,
        contact: item.contactName,
        phone: item.phone,
        trackingNumber: item.trackingNumber
      }
    }))
  } catch (error) {
    ElMessage.error('加载订单失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-orders {
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

.stat-value.pending {
  color: #e6a23c;
}

.stat-value.completed {
  color: #67c23a;
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

.orders-list {
  min-height: 400px;
}

.order-card {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.order-number {
  font-weight: 500;
  color: #333;
}

.order-time {
  font-size: 14px;
  color: #999;
}

.order-content {
  margin-bottom: 15px;
}

.items-list {
  margin-bottom: 15px;
}

.order-item {
  display: flex;
  gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.item-details {
  flex: 1;
}

.item-name {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.item-desc {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 12px;
}

.item-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
}

.order-summary {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item .label {
  color: #666;
  font-size: 14px;
}

.summary-item .value {
  font-weight: 600;
  font-size: 14px;
}

.summary-item .value.amount {
  color: #f56c6c;
}

.summary-item .value.points {
  color: #ce4c4c;
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

.order-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  flex-wrap: wrap;
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
.order-detail {
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

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  color: #666;
  font-size: 14px;
  min-width: 80px;
}

.info-item .value {
  color: #333;
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

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar,
  .filter-bar {
    justify-content: center;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .order-item {
    flex-direction: column;
  }
  
  .order-actions {
    justify-content: center;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 10px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
