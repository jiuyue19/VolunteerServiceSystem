<template>
  <div class="blockchain-query-page">
    <div class="page-header">
      <div>
        <h2><el-icon><Search /></el-icon> 区块链服务查询</h2>
        <p class="subtitle">查询志愿者活动、时长、补录数据的上链记录</p>
      </div>
    </div>

    <!-- 查询条件卡片 -->
    <el-card class="query-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Filter /></el-icon> 查询条件</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" label-width="100px">
        <el-form-item label="志愿者">
          <el-select 
            v-model="queryForm.volunteerId" 
            filterable 
            clearable
            placeholder="选择志愿者（留空查询全部）" 
            style="width: 280px"
            @focus="loadVolunteers">
            <el-option
              v-for="vol in volunteerList"
              :key="vol.id"
              :label="`${vol.realName} (ID: ${vol.id})`"
              :value="vol.id">
              <span style="float: left">{{ vol.realName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ vol.walletAddress ? '✓ 已绑定' : '✗ 未绑定' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="记录类型">
          <el-select v-model="queryForm.recordType" clearable placeholder="全部类型" style="width: 150px">
            <el-option label="全部" value=""></el-option>
            <el-option label="活动记录" value="activity"></el-option>
            <el-option label="补录记录" value="replenish"></el-option>
            <el-option label="总时长同步" value="sync"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 上链记录统计 -->
    <el-row :gutter="20" class="stats-row" v-if="recordsData.length > 0">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ recordsData.length }}</div>
            <div class="stat-label">查询到的记录数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ activityRecordsCount }}</div>
            <div class="stat-label">活动上链记录</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ replenishRecordsCount }}</div>
            <div class="stat-label">补录上链记录</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalHoursOnChain }}</div>
            <div class="stat-label">累计上链时长（小时）</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 上链记录列表 -->
    <el-card class="records-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><List /></el-icon> 上链记录列表</span>
          <el-button type="success" size="small" @click="exportRecords" :disabled="recordsData.length === 0">
            <el-icon><Download /></el-icon> 导出记录
          </el-button>
        </div>
      </template>

      <el-table 
        :data="recordsData" 
        stripe 
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无上链记录">
        <el-table-column type="index" label="序号" width="60" align="center" />
        
        <el-table-column prop="volunteerName" label="志愿者" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.volunteerName }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="walletAddress" label="钱包地址" width="160" align="center">
          <template #default="{ row }">
            <el-tooltip :content="row.walletAddress" placement="top">
              <el-tag type="success" size="small">{{ formatAddress(row.walletAddress) }}</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="recordType" label="记录类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.isReplenish ? 'warning' : 'primary'" 
              effect="plain">
              {{ row.isReplenish ? '补录记录' : '活动记录' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="duration" label="时长（小时）" width="130" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff;">{{ row.duration }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="activityName" label="活动名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.activityName" placement="top">
              <span style="font-weight: 500; color: #303133;">
                {{ row.activityName || '未知活动' }}
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="timestamp" label="上链时间" width="180" align="center">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; justify-content: center;">
              <el-icon style="margin-right: 4px;"><Timer /></el-icon>
              {{ formatTimestamp(row.timestamp) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="区块链验证" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link 
              @click="viewOnBlockchain(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
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

    <!-- 记录详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="上链记录详情" width="700px">
      <el-descriptions :column="1" border v-if="selectedRecord">
        <el-descriptions-item label="志愿者姓名">
          <strong style="font-size: 16px">{{ selectedRecord.volunteerName }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="钱包地址">
          <el-tag type="success">{{ selectedRecord.walletAddress }}</el-tag>
          <el-button 
            link 
            type="primary" 
            size="small" 
            @click="copyToClipboard(selectedRecord.walletAddress)"
            style="margin-left: 10px">
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="记录类型">
          <el-tag :type="selectedRecord.isReplenish ? 'warning' : 'primary'" size="large">
            {{ selectedRecord.isReplenish ? '补录记录' : '活动记录' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动名称">
          <el-tag type="primary" size="large">{{ selectedRecord.activityName || '未知活动' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务时长">
          <el-tag type="info" size="large">{{ selectedRecord.duration }} 小时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上链时间">
          <el-tag>{{ formatTimestamp(selectedRecord.timestamp) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间戳">
          {{ selectedRecord.timestamp }}
        </el-descriptions-item>
      </el-descriptions>
      
      <el-alert
        title="区块链验证说明"
        type="info"
        :closable="false"
        style="margin-top: 20px">
        <p>上链记录已永久存储在区块链上，不可篡改。</p>
        <p>您可以通过区块链浏览器验证记录的真实性。</p>
      </el-alert>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="viewOnBlockchain(selectedRecord)">
          在区块链浏览器中查看
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, Filter, Document, TrendCharts, EditPen, Clock, 
  List, Download, View, Timer, RefreshRight, ArrowLeft, ArrowRight 
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getChainRecords, getAllChainRecords } from '@/api/blockchain'

const queryForm = ref({
  volunteerId: null,
  volunteerName: '',
  recordType: ''
})

const volunteerList = ref([])
const recordsData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const showDetailDialog = ref(false)
const selectedRecord = ref(null)

// 计算统计数据
const activityRecordsCount = computed(() => {
  return recordsData.value.filter(r => !r.isReplenish).length
})

const replenishRecordsCount = computed(() => {
  return recordsData.value.filter(r => r.isReplenish).length
})

const totalHoursOnChain = computed(() => {
  return recordsData.value.reduce((sum, r) => sum + parseFloat(r.duration || 0), 0).toFixed(2)
})

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '未知'
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

// 格式化时间戳
const formatTimestamp = (timestamp) => {
  if (!timestamp) return '未知'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 加载志愿者列表
const loadVolunteers = async () => {
  if (volunteerList.value.length > 0) return
  
  try {
    const res = await request({
      url: '/volunteer/list',
      method: 'get'
    })
    if (res.code === 200) {
      // 只显示已绑定钱包的志愿者
      volunteerList.value = (res.data || []).filter(v => v.walletAddress)
    } else {
      console.error('加载志愿者列表失败:', res.message)
    }
  } catch (error) {
    console.error('加载志愿者列表失败:', error)
  }
}

// 查询上链记录
const handleQuery = async () => {
  loading.value = true
  try {
    let res
    if (queryForm.value.volunteerId) {
      // 查询特定志愿者的记录
      res = await getChainRecords(queryForm.value.volunteerId)
    } else {
      // 查询所有记录
      res = await getAllChainRecords()
    }
    
    if (res.code === 200) {
      recordsData.value = res.data || []
      
      // 根据记录类型筛选
      if (queryForm.value.recordType) {
        if (queryForm.value.recordType === 'activity') {
          recordsData.value = recordsData.value.filter(r => !r.isReplenish)
        } else if (queryForm.value.recordType === 'replenish') {
          recordsData.value = recordsData.value.filter(r => r.isReplenish)
        }
      }
      
      // 按志愿者真实姓名模糊筛选
      if (queryForm.value.volunteerName) {
        const keyword = queryForm.value.volunteerName.trim()
        if (keyword) {
          recordsData.value = recordsData.value.filter(r => (r.volunteerName || '').includes(keyword))
        }
      }
      
      ElMessage.success(`查询到 ${recordsData.value.length} 条上链记录`)
    } else {
      ElMessage.error(res.message || '查询失败')
      recordsData.value = []
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败，请检查网络连接')
    recordsData.value = []
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.value = {
    volunteerId: null,
    volunteerName: '',
    recordType: ''
  }
  recordsData.value = []
  currentPage.value = 1
}

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(recordsData.value.length / pageSize.value) || 1
})

// 分页处理
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

// 在区块链浏览器中查看
const viewOnBlockchain = (record) => {
  if (!record) return
  
  selectedRecord.value = record
  showDetailDialog.value = true
  
  // 可以在这里添加跳转到区块链浏览器的逻辑
  // 例如：window.open(`https://etherscan.io/address/${record.walletAddress}`)
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 导出记录
const exportRecords = () => {
  if (recordsData.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  ElMessage.info({
    message: '导出功能需要安装 xlsx 库。请在 frontend 目录运行：npm install xlsx',
    duration: 5000
  })
  
  // 如果已安装 xlsx 库，取消下面的注释即可使用
  /*
  try {
    const XLSX = await import('xlsx')
    
    // 准备导出数据
    const exportData = recordsData.value.map((record, index) => ({
      '序号': index + 1,
      '志愿者': record.volunteerName,
      '钱包地址': record.walletAddress,
      '记录类型': record.isReplenish ? '补录记录' : '活动记录',
      '时长（小时）': record.duration,
      '记录哈希': record.recordHash,
      '上链时间': formatTimestamp(record.timestamp),
      '时间戳': record.timestamp
    }))
    
    // 创建工作簿
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '上链记录')
    
    // 导出文件
    const fileName = `区块链上链记录_${new Date().getTime()}.xlsx`
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('导出成功！')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请先安装 xlsx 库')
  }
  */
}

// 组件挂载时自动加载志愿者列表
onMounted(() => {
  loadVolunteers()
})
</script>

<style scoped>
.blockchain-query-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.page-header h2 .el-icon {
  font-size: 32px;
  color: #409eff;
}

.subtitle {
  color: #909399;
  margin: 8px 0 0 44px;
  font-size: 14px;
}

/* 查询卡片 */
.query-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

/* 记录卡片 */
.records-card {
  margin-bottom: 24px;
}

/* 分页 */
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

/* 响应式 */
@media (max-width: 768px) {
  .page-header h2 {
    font-size: 24px;
  }
  
  .stat-card {
    margin-bottom: 12px;
  }
}
</style>
