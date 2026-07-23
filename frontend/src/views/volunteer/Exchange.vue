<template>
  <div class="exchange">
    <!-- 头部：左右布局 -->
    <div class="header">
      <h2>积分兑换</h2>
      <div class="points-info">
        <span class="points-text">我的积分：</span>
        <span class="points-value">{{ userPoints || 0 }}</span>
      </div>
    </div>

    <!-- 搜索和标签栏：左右布局 -->
    <div class="search-tags-bar">
      <!-- 左侧：搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入商品名称"
          clearable
          style="width: 300px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <!-- 右侧：标签栏 -->
      <div class="tags-section">
        <div 
          v-for="tag in tags" 
          :key="tag.key"
          :class="{ active: activeTag === tag.key && tag.key !== 'filter' }"
          class="tag-item"
          @click="handleTagClick(tag.key)"
        >
          {{ tag.label }}
          <!-- 为兑换量、积分、上新添加上下三角形图标 -->
          <div v-if="['exchange', 'points', 'new'].includes(tag.key)" class="triangle-icons">
            <div class="triangle-up"></div>
            <div class="triangle-down"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品展示：两行六列网格布局 -->
    <div class="goods-container">
      <div class="goods-grid" v-loading="loading" element-loading-text="加载商品数据中...">
        <el-card 
          v-for="item in paginatedGoods" 
          :key="item.id"
          shadow="hover" 
          class="goods-card"
          @click="showGoodsDetail(item)"
        >
          <img :src="item.image" class="goods-image" />
          <div class="goods-content">
            <h3>{{ item.name }}</h3>
            <p class="goods-desc">{{ item.description }}</p>
            <div class="goods-footer">
              <span class="points-cost">
                <el-icon><TrophyBase /></el-icon>
                {{ item.points }}积分
              </span>
              <el-button 
                :type="canExchange(item) ? 'success' : 'primary'" 
                size="small" 
                @click.stop="startExchange(item)"
                :disabled="!item.stock || item.stock <= 0"
              >
                {{ !item.stock || item.stock <= 0 ? '已售罄' : (canExchange(item) ? '可兑换' : '兑换') }}
              </el-button>
            </div>
          </div>
        </el-card>
        
        <!-- 无数据提示 -->
        <div v-if="!loading && originalGoods.length === 0" class="no-data">
          <el-empty description="暂无商品数据" />
        </div>
      </div>
      
      <!-- 分页控制 -->
      <div class="pagination" v-if="totalPages > 1">
        <el-button 
          :disabled="currentPage === 1" 
          @click="prevPage"
          class="page-btn"
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-button 
          :disabled="currentPage === totalPages" 
          @click="nextPage"
          class="page-btn"
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 筛选抽屉 -->
    <el-drawer
      v-model="filterDrawerVisible"
      title="商品筛选"
      direction="rtl"
      size="400px"
    >
      <div class="filter-content">
        <!-- 积分区间 -->
        <div class="filter-section">
          <h4>积分区间</h4>
          <el-slider
            v-model="filterOptions.pointsRange"
            range
            :min="0"
            :max="500"
            :step="10"
            show-stops
          />
          <div class="range-display">
            {{ filterOptions.pointsRange[0] }} - {{ filterOptions.pointsRange[1] }} 积分
          </div>
        </div>
        
        <!-- 材质 -->
        <div class="filter-section">
          <h4>材质</h4>
          <el-checkbox-group v-model="filterOptions.materials">
            <el-checkbox label="纯棉">纯棉</el-checkbox>
            <el-checkbox label="混纺">混纺</el-checkbox>
            <el-checkbox label="塑料">塑料</el-checkbox>
            <el-checkbox label="金属">金属</el-checkbox>
            <el-checkbox label="皮革">皮革</el-checkbox>
          </el-checkbox-group>
        </div>
        
        <!-- 颜色 -->
        <div class="filter-section">
          <h4>颜色</h4>
          <el-checkbox-group v-model="filterOptions.colors">
            <el-checkbox label="红色">红色</el-checkbox>
            <el-checkbox label="蓝色">蓝色</el-checkbox>
            <el-checkbox label="绿色">绿色</el-checkbox>
            <el-checkbox label="黄色">黄色</el-checkbox>
            <el-checkbox label="白色">白色</el-checkbox>
            <el-checkbox label="黑色">黑色</el-checkbox>
          </el-checkbox-group>
        </div>
        
        <!-- 适用人群 -->
        <div class="filter-section">
          <h4>适用人群</h4>
          <el-checkbox-group v-model="filterOptions.targetGroups">
            <el-checkbox label="成人">成人</el-checkbox>
            <el-checkbox label="儿童">儿童</el-checkbox>
            <el-checkbox label="老人">老人</el-checkbox>
            <el-checkbox label="学生">学生</el-checkbox>
          </el-checkbox-group>
        </div>
        
        <!-- 适用季节 -->
        <div class="filter-section">
          <h4>适用季节</h4>
          <el-checkbox-group v-model="filterOptions.seasons">
            <el-checkbox label="春季">春季</el-checkbox>
            <el-checkbox label="夏季">夏季</el-checkbox>
            <el-checkbox label="秋季">秋季</el-checkbox>
            <el-checkbox label="冬季">冬季</el-checkbox>
            <el-checkbox label="四季">四季</el-checkbox>
          </el-checkbox-group>
        </div>
        
        <!-- 其他 -->
        <div class="filter-section">
          <h4>其他</h4>
          <el-checkbox-group v-model="filterOptions.others">
            <el-checkbox label="防水">防水</el-checkbox>
            <el-checkbox label="透气">透气</el-checkbox>
            <el-checkbox label="保温">保温</el-checkbox>
            <el-checkbox label="耐磨">耐磨</el-checkbox>
            <el-checkbox label="易清洗">易清洗</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      
      <template #footer>
        <div class="filter-footer">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="applyFilters">应用筛选</el-button>
        </div>
      </template>
    </el-drawer>
    
    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="goodsDetailVisible"
      :title="selectedGoods?.name || '商品详情'"
      width="600px"
      @close="closeGoodsDetail"
    >
      <div v-if="selectedGoods" class="goods-detail">
        <div class="detail-image">
          <img :src="selectedGoods.image" :alt="selectedGoods.name" />
        </div>
        <div class="detail-info">
          <h3>{{ selectedGoods.name }}</h3>
          <p class="detail-desc">{{ selectedGoods.description }}</p>
          <div class="detail-stats">
            <div class="stat-item">
              <span class="stat-label">所需积分：</span>
              <span class="stat-value points">{{ selectedGoods.points }} 积分</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">可兑换数：</span>
              <span class="stat-value">{{ selectedGoods.stock || 0 }} 次</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">商品状态：</span>
              <span class="stat-value" :class="{ 'out-of-stock': !selectedGoods.stock || selectedGoods.stock <= 0 }">
                {{ !selectedGoods.stock || selectedGoods.stock <= 0 ? '已售罄' : '在售' }}
              </span>
            </div>
          </div>
          <div class="detail-features">
            <h4>商品特色</h4>
            <ul>
              <li>高品质材料制作</li>
              <li>志愿者专属设计</li>
              <li>实用性与纪念性并重</li>
              <li>环保可持续理念</li>
            </ul>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeGoodsDetail">取消</el-button>
          <el-button 
            type="primary" 
            @click="startExchange(selectedGoods)"
            :disabled="userPoints < (selectedGoods?.points || 0)"
          >
            {{ userPoints < (selectedGoods?.points || 0) ? '积分不足' : '立即兑换' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 选择收货地址并确认兑换 -->
    <el-dialog
      v-model="addressDialogVisible"
      title="确认兑换"
      width="600px"
    >
      <div v-if="exchangeGoods" class="address-dialog-body">
        <p>商品：{{ exchangeGoods.name }}（{{ exchangeGoods.points }} 积分）</p>
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
            :disabled="!selectedAddressId || !exchangeGoods || !addressList.length || userPoints < (exchangeGoods?.points || 0)"
            :loading="confirmingExchange"
            @click="submitExchange"
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, TrophyBase, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getVolunteerInfo } from '@/api/volunteer'
import { getGoodsList } from '@/api/goods'
import { getAddressList } from '@/api/address'
import { exchangeGoods as exchangeGoodsApi } from '@/api/order'
import { calculatePoints } from '@/api/points'

const route = useRoute()

const userPoints = ref(0)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 12 // 每页显示12个商品（两行六列）
const filterDrawerVisible = ref(false)
const goodsDetailVisible = ref(false)
const selectedGoods = ref(null)

// 地址选择与兑换
const addressDialogVisible = ref(false)
const addressLoading = ref(false)
const addressList = ref([])
const selectedAddressId = ref(null)
const exchangeGoods = ref(null)
const confirmingExchange = ref(false)

// 筛选选项
const filterOptions = ref({
  pointsRange: [0, 500],
  materials: [],
  colors: [],
  targetGroups: [],
  seasons: [],
  others: []
})

const tags = ref([
  { key: 'comprehensive', label: '综合' },
  { key: 'exchange', label: '兑换量' },
  { key: 'points', label: '积分' },
  { key: 'new', label: '上新' },
  { key: 'filter', label: '筛选' }
])

const activeTag = ref('comprehensive')

// 商品数据（从后端API获取）
const originalGoods = ref([])
const loading = ref(false)

// 根据搜索关键词和标签筛选商品
const allGoods = computed(() => {
  let filteredGoods = [...originalGoods.value]
  
  // 搜索筛选
  if (searchKeyword.value) {
    filteredGoods = filteredGoods.filter(item => 
      item.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  // 标签筛选和排序
  switch (activeTag.value) {
    case 'exchange':
      // 按库存排序（库存多的排前面）
      filteredGoods.sort((a, b) => (b.stock || 0) - (a.stock || 0))
      break
    case 'points':
      // 按积分排序
      filteredGoods.sort((a, b) => a.points - b.points)
      break
    case 'new':
      // 显示有库存的商品
      filteredGoods = filteredGoods.filter(item => item.stock && item.stock > 0)
      break
    case 'filter':
      // 可以添加更多筛选逻辑
      break
    default:
      // 综合排序（默认）
      break
  }
  
  // 筛选抽屉：根据积分区间过滤商品
  if (filterOptions.value && Array.isArray(filterOptions.value.pointsRange)) {
    const [minPoints, maxPoints] = filterOptions.value.pointsRange
    filteredGoods = filteredGoods.filter(item => {
      const points = item.points || 0
      return points >= minPoints && points <= maxPoints
    })
  }
  
  // 材质筛选
  if (filterOptions.value.materials && filterOptions.value.materials.length > 0) {
    filteredGoods = filteredGoods.filter(item => 
      item.material && filterOptions.value.materials.includes(item.material)
    )
  }
  
  // 颜色筛选
  if (filterOptions.value.colors && filterOptions.value.colors.length > 0) {
    filteredGoods = filteredGoods.filter(item => 
      item.color && filterOptions.value.colors.includes(item.color)
    )
  }
  
  // 适用人群筛选
  if (filterOptions.value.targetGroups && filterOptions.value.targetGroups.length > 0) {
    console.log('筛选适用人群:', filterOptions.value.targetGroups)
    const beforeCount = filteredGoods.length
    filteredGoods = filteredGoods.filter(item => {
      const match = item.targetGroup && filterOptions.value.targetGroups.includes(item.targetGroup)
      if (!match && item.targetGroup) {
        console.log('不匹配的商品:', item.name, '适用人群:', item.targetGroup, '筛选条件:', filterOptions.value.targetGroups)
      }
      return match
    })
    console.log('适用人群筛选后: 从', beforeCount, '减少到', filteredGoods.length)
  }
  
  // 适用季节筛选
  if (filterOptions.value.seasons && filterOptions.value.seasons.length > 0) {
    console.log('筛选适用季节:', filterOptions.value.seasons)
    const beforeCount = filteredGoods.length
    filteredGoods = filteredGoods.filter(item => {
      const match = item.season && filterOptions.value.seasons.includes(item.season)
      if (!match && item.season) {
        console.log('不匹配的商品:', item.name, '适用季节:', item.season, '筛选条件:', filterOptions.value.seasons)
      }
      return match
    })
    console.log('适用季节筛选后: 从', beforeCount, '减少到', filteredGoods.length)
  }
  
  // 其他特性筛选
  if (filterOptions.value.others && filterOptions.value.others.length > 0) {
    filteredGoods = filteredGoods.filter(item => {
      if (!item.features) return false
      const itemFeatures = item.features.split(',')
      return filterOptions.value.others.some(feature => itemFeatures.includes(feature))
    })
  }
  
  // 将可兑换的商品排在最前面
  filteredGoods.sort((a, b) => {
    const aCanExchange = a.points <= userPoints.value
    const bCanExchange = b.points <= userPoints.value
    if (aCanExchange && !bCanExchange) return -1
    if (!aCanExchange && bCanExchange) return 1
    return 0
  })
  
  return filteredGoods
})

const handleTagClick = (key) => {
  if (key === 'filter') {
    filterDrawerVisible.value = true
  } else {
    // 如果点击的是当前激活的标签，则取消选中，恢复默认
    if (activeTag.value === key) {
      activeTag.value = 'comprehensive'
    } else {
      activeTag.value = key
    }
    currentPage.value = 1 // 切换标签时重置到第一页
  }
}

const handleSearch = () => {
  // 搜索时重置为综合标签和第一页
  if (searchKeyword.value && activeTag.value !== 'comprehensive') {
    activeTag.value = 'comprehensive'
  }
  currentPage.value = 1
}

// 分页相关计算属性
const totalPages = computed(() => {
  return Math.ceil(allGoods.value.length / pageSize) || 1
})

const paginatedGoods = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return allGoods.value.slice(start, end)
})

// 分页控制方法
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

// 筛选相关方法
const resetFilters = () => {
  filterOptions.value = {
    pointsRange: [0, 500],
    materials: [],
    colors: [],
    targetGroups: [],
    seasons: [],
    others: []
  }
  currentPage.value = 1
}

const applyFilters = () => {
  // 应用筛选逻辑（这里可以根据实际需求实现）
  filterDrawerVisible.value = false
  currentPage.value = 1
  ElMessage.success('筛选条件已应用')
}

// 加载用户积分数据（调用后端统一积分计算接口）
const loadUserPoints = async () => {
  try {
    const userRes = await getVolunteerInfo()
    if (!userRes || !userRes.data) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const volunteerId = userRes.data.id
    if (!volunteerId) {
      ElMessage.error('无法获取用户ID')
      return
    }
    
    // 调用后端统一积分计算接口
    const pointsRes = await calculatePoints(volunteerId)
    if (pointsRes && pointsRes.data) {
      userPoints.value = pointsRes.data.totalPoints || 0
      console.log('积分兑换页面 - 积分计算结果:', pointsRes.data)
      console.log('积分计算公式:', pointsRes.data.formula)
    } else {
      userPoints.value = 0
    }
  } catch (error) {
    console.error('获取用户积分失败:', error)
    ElMessage.error('获取用户积分失败: ' + (error.message || '未知错误'))
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

const startExchange = async (item) => {
  if (!item) return
  if (!item.stock || item.stock <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  if (userPoints.value < item.points) {
    ElMessage.warning('积分不足')
    return
  }

  exchangeGoods.value = item
  await loadAddressList()

  if (!addressList.value.length) {
    ElMessage.warning('暂无收货地址，请先在“收货地址管理”中添加')
    return
  }

  const defaultAddr = addressList.value.find(addr => addr.isDefault)
  selectedAddressId.value = defaultAddr ? defaultAddr.id : addressList.value[0].id
  addressDialogVisible.value = true
}

const submitExchange = async () => {
  if (!exchangeGoods.value || !selectedAddressId.value) return
  if (userPoints.value < (exchangeGoods.value.points || 0)) {
    ElMessage.warning('积分不足')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认使用 ${exchangeGoods.value.points} 积分兑换 ${exchangeGoods.value.name}？`,
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
    confirmingExchange.value = true
    const payload = {
      goodsId: exchangeGoods.value.id,
      quantity: 1,
      addressId: selectedAddressId.value
    }
    await exchangeGoodsApi(payload)
    ElMessage.success('兑换成功')
    addressDialogVisible.value = false
    if (goodsDetailVisible.value) {
      closeGoodsDetail()
    }
    await loadUserPoints()
  } catch (error) {
    ElMessage.error('兑换失败: ' + (error.message || '未知错误'))
  } finally {
    confirmingExchange.value = false
  }
}

// 显示商品详情
const showGoodsDetail = (goods) => {
  selectedGoods.value = goods
  goodsDetailVisible.value = true
}

// 关闭商品详情
const closeGoodsDetail = () => {
  goodsDetailVisible.value = false
  selectedGoods.value = null
}

// 判断是否可以兑换
const canExchange = (item) => {
  return item && item.points <= userPoints.value
}

// 加载商品数据
const loadGoodsData = async () => {
  try {
    loading.value = true
    const res = await getGoodsList()
    
    if (res && res.data) {
      // 直接使用后端数据，不添加模拟字段
      originalGoods.value = res.data.filter(item => item.status === 1) // 只显示上架商品
      
      // 调试：打印商品数据，检查targetGroup和season字段
      console.log('商品数据加载完成，共', originalGoods.value.length, '个商品')
      if (originalGoods.value.length > 0) {
        console.log('第一个商品示例:', originalGoods.value[0])
        console.log('所有商品的targetGroup值:', originalGoods.value.map(item => item.targetGroup))
        console.log('所有商品的season值:', originalGoods.value.map(item => item.season))
      }
    } else {
      ElMessage.error('获取商品数据失败')
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    ElMessage.error('加载商品数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  loadUserPoints()
  await loadGoodsData()
  
  // 处理查询参数，如果有goodsId，自动打开兑换对话框
  const goodsId = route.query.goodsId
  if (goodsId) {
    const goods = originalGoods.value.find(item => String(item.id) === String(goodsId))
    if (goods) {
      // 延迟一点打开，确保数据加载完成
      setTimeout(() => {
        startExchange(goods)
      }, 300)
    } else {
      ElMessage.warning('未找到该商品')
    }
  }
})
</script>

<style scoped>
.exchange {
  padding: 20px 30px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.points-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.points-text {
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.points-value {
  font-size: 24px;
  font-weight: 600;
  color: #ce4c4c;
}

.search-tags-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0;
}

.search-section {
  flex-shrink: 0;
}

.tags-section {
  display: flex;
  gap: 30px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  cursor: pointer;
  transition: color 0.3s;
  color: #666;
  font-size: 16px;
  font-weight: 400;
  background: none;
  border: none;
}

.tag-item:hover {
  color: #ce4c4c;
}

.tag-item.active {
  color: #ce4c4c;
  font-weight: 600;
}

/* 上下三角形图标 */
.triangle-icons {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.triangle-up,
.triangle-down {
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
}

.triangle-up {
  border-bottom: 6px solid #666;
}

.triangle-down {
  border-top: 6px solid #666;
}

.tag-item:hover .triangle-up {
  border-bottom-color: #ce4c4c;
}

.tag-item:hover .triangle-down {
  border-top-color: #ce4c4c;
}

.tag-item.active .triangle-up {
  border-bottom-color: #ce4c4c;
}

.tag-item.active .triangle-down {
  border-top-color: #ce4c4c;
}

.goods-container {
  margin-top: 20px;
  width: 100%;
  overflow-x: hidden;
}

.goods-grid {
  display: grid;

  /* 固定6列 */
  grid-template-columns: repeat(6, minmax(0, 1fr));

  /* 固定2行 */
  grid-template-rows: repeat(2, auto);

  gap: 20px;

  width: 100%;
}

.goods-card {
  width: 100%;
  min-width: 0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.goods-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.goods-card :deep(.el-card__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.goods-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.goods-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

h3 {
  font-size: 16px;
  margin: 0 0 10px 0;
  font-weight: 600;
  color: #333;
  line-height: 1.3;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-desc {
  color: #666;
  font-size: 14px;
  margin: 0 0 15px 0;
  line-height: 1.4;

  overflow: hidden;
  text-overflow: ellipsis;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;

  flex: 1;
}

.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.points-cost {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ce4c4c;
  font-weight: bold;
  font-size: 14px;
}

.goods-footer .el-button {
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 20px;
  flex-shrink: 0;
}

/* 去掉所有商品卡片滚动条 */
.goods-card,
.goods-card * {
  scrollbar-width: none;
}

.goods-card::-webkit-scrollbar,
.goods-card *::-webkit-scrollbar {
  display: none;
}

/* 无数据 */
.no-data {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  padding: 20px 0;
}

.page-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  background: #fff;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.page-btn:disabled {
  color: #c0c4cc;
  border-color: #e4e7ed;
  background: #fff;
  cursor: not-allowed;
}

.page-info {
  margin: 0 16px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 筛选抽屉样式 */
.filter-content {
  padding: 20px 0;
}

.filter-section {
  margin-bottom: 30px;
}

.filter-section h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.filter-section .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-section .el-checkbox {
  margin-right: 0;
}

.range-display {
  text-align: center;
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}

.filter-footer {
  display: flex;
  justify-content: space-between;
  padding: 20px 0;
}

.filter-footer .el-button {
  flex: 1;
  margin: 0 5px;
}

/* 商品详情对话框样式 */
.goods-detail {
  display: flex;
  gap: 20px;
}

.detail-image {
  flex-shrink: 0;
  width: 200px;
}

.detail-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.detail-info {
  flex: 1;
}

.detail-info h3 {
  margin: 0 0 15px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.detail-desc {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.detail-stats {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-weight: 500;
  color: #666;
}

.stat-value {
  font-weight: 600;
  color: #333;
}

.stat-value.points {
  color: #ce4c4c;
}

.new-badge {
  color: #67c23a !important;
}

.out-of-stock {
  color: #f56c6c !important;
}

.detail-features {
  margin-top: 20px;
}

.detail-features h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.detail-features ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.no-data {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.detail-features li {
  margin-bottom: 5px;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
