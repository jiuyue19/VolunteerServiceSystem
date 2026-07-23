<template>
  <div class="certificate-library-page">
    <div class="page-header">
      <h2><el-icon><Medal /></el-icon> 证书库管理</h2>
      <p class="subtitle">管理志愿服务证书的生成、发放与撤销</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">证书总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待发放</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon><Select /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.issued }}</div>
            <div class="stat-label">已发放</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon><CloseBold /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.revoked }}</div>
            <div class="stat-label">已撤销</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="18">
          <el-form :inline="true" :model="queryForm">
            <el-form-item label="证书状态">
              <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 150px">
                <el-option label="全部" value=""></el-option>
                <el-option label="待发放" value="pending"></el-option>
                <el-option label="已发放" value="issued"></el-option>
                <el-option label="已撤销" value="revoked"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCertificates" :loading="loading">
                <el-icon><Search /></el-icon> 查询
              </el-button>
              <el-button @click="resetQuery">
                <el-icon><RefreshRight /></el-icon> 重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button 
            type="success" 
            @click="batchIssue" 
            :disabled="selectedCertificates.length === 0">
            <el-icon><Select /></el-icon> 批量发放 ({{ selectedCertificates.length }})
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 证书列表 -->
    <el-card class="table-card" shadow="hover">
      <el-table 
        :data="certificates" 
        stripe 
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        empty-text="暂无证书记录">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        
        <el-table-column prop="certificateNo" label="证书编号" width="180" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.certificateNo }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="certificateHash" label="证书哈希" width="120" align="center">
          <template #default="{ row }">
            <el-tooltip :content="row.certificateHash" placement="top">
              <el-tag type="info" size="small">{{ row.certificateHash?.slice(0, 8) }}...</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="volunteerName" label="志愿者" width="120" align="center" />
        
        <el-table-column prop="walletAddress" label="钱包地址" width="160" align="center">
          <template #default="{ row }">
            <el-tooltip :content="row.walletAddress" placement="top">
              <el-tag type="success" size="small">{{ formatAddress(row.walletAddress) }}</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="chainTotalHours" label="链上时长" width="120" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff;">{{ row.chainTotalHours }} 小时</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'issued' ? 'success' : row.status === 'pending' ? 'warning' : 'danger'"
              effect="dark">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="issueDate" label="发放时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.issueDate ? formatDate(row.issueDate) : '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="viewCertificate(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            
            <el-button 
              v-if="row.status === 'pending'"
              type="success" 
              size="small" 
              link
              @click="issueSingle(row)">
              <el-icon><Select /></el-icon> 发放
            </el-button>
            
            <el-button 
              v-if="row.status === 'issued'"
              type="warning" 
              size="small" 
              link
              @click="revokeConfirm(row)">
              <el-icon><CloseBold /></el-icon> 撤销
            </el-button>
            
            <el-button 
              type="danger" 
              size="small" 
              link
              @click="deleteConfirm(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 证书详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="证书详情" width="700px">
      <el-descriptions :column="1" border v-if="selectedCert">
        <el-descriptions-item label="证书编号">
          <el-tag type="primary" size="large">{{ selectedCert.certificateNo }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="证书哈希">
          <el-tag type="info">{{ selectedCert.certificateHash }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="志愿者姓名">
          <strong style="font-size: 16px">{{ selectedCert.volunteerName }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="钱包地址">
          <el-tag type="success">{{ selectedCert.walletAddress }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="链上总时长">
          <el-tag type="primary" size="large">{{ selectedCert.chainTotalHours }} 小时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="链上补录时长">
          <el-tag type="warning" size="large">{{ selectedCert.chainReplenishHours }} 小时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="参与活动数">
          {{ selectedCert.activityCount }}
        </el-descriptions-item>
        <el-descriptions-item label="证书状态">
          <el-tag 
            :type="selectedCert.status === 'issued' ? 'success' : selectedCert.status === 'pending' ? 'warning' : 'danger'"
            size="large">
            {{ getStatusText(selectedCert.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(selectedCert.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="发放时间" v-if="selectedCert.issueDate">
          {{ formatDate(selectedCert.issueDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" v-if="selectedCert.remark">
          {{ selectedCert.remark }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Medal, Document, Clock, Select, CloseBold, Search, RefreshRight, 
  View, Delete 
} from '@element-plus/icons-vue'
import { 
  getAllCertificates, 
  getCertificateStats, 
  issueCertificate, 
  batchIssueCertificates,
  revokeCertificate,
  deleteCertificate
} from '@/api/certificate'

const loading = ref(false)
const certificates = ref([])
const selectedCertificates = ref([])
const showDetailDialog = ref(false)
const selectedCert = ref(null)

const queryForm = ref({
  status: ''
})

const stats = ref({
  total: 0,
  pending: 0,
  issued: 0,
  revoked: 0
})

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '未知'
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待发放',
    'issued': '已发放',
    'revoked': '已撤销'
  }
  return statusMap[status] || status
}

// 加载证书列表
const loadCertificates = async () => {
  loading.value = true
  try {
    const res = await getAllCertificates(queryForm.value.status)
    if (res.code === 200) {
      certificates.value = res.data || []
    }
  } catch (error) {
    console.error('加载证书列表失败:', error)
    ElMessage.error('加载证书列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getCertificateStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.value.status = ''
  loadCertificates()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedCertificates.value = selection.filter(cert => cert.status === 'pending')
}

// 查看证书详情
const viewCertificate = (cert) => {
  selectedCert.value = cert
  showDetailDialog.value = true
}

// 单个发放
const issueSingle = async (cert) => {
  try {
    const res = await issueCertificate({
      certificateId: cert.id,
      issuerId: 1 // TODO: 从用户信息获取
    })
    
    if (res.code === 200) {
      ElMessage.success('证书发放成功')
      await loadCertificates()
      await loadStats()
    } else {
      ElMessage.error(res.message || '发放失败')
    }
  } catch (error) {
    console.error('发放失败:', error)
    ElMessage.error('发放失败')
  }
}

// 批量发放
const batchIssue = async () => {
  if (selectedCertificates.value.length === 0) {
    ElMessage.warning('请选择待发放的证书')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认发放 ${selectedCertificates.value.length} 张证书？发放后不可撤回。`,
      '批量发放确认',
      {
        confirmButtonText: '确认发放',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedCertificates.value.map(cert => cert.id)
    const res = await batchIssueCertificates({
      certificateIds: ids,
      issuerId: 1 // TODO: 从用户信息获取
    })
    
    if (res.code === 200) {
      ElMessage.success(res.data || '批量发放成功')
      await loadCertificates()
      await loadStats()
    } else {
      ElMessage.error(res.message || '批量发放失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发放失败:', error)
      ElMessage.error('批量发放失败')
    }
  }
}

// 撤销确认
const revokeConfirm = async (cert) => {
  try {
    await ElMessageBox.confirm(
      `确认撤销证书 "${cert.certificateNo}"？`,
      '撤销确认',
      {
        confirmButtonText: '确认撤销',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await revokeCertificate({
      certificateId: cert.id,
      issuerId: 1
    })
    
    if (res.code === 200) {
      ElMessage.success('证书已撤销')
      await loadCertificates()
      await loadStats()
    } else {
      ElMessage.error(res.message || '撤销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销失败:', error)
      ElMessage.error('撤销失败')
    }
  }
}

// 删除确认
const deleteConfirm = async (cert) => {
  try {
    await ElMessageBox.confirm(
      `确认删除证书 "${cert.certificateNo}"？删除后无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const res = await deleteCertificate(cert.id)
    
    if (res.code === 200) {
      ElMessage.success('证书已删除')
      await loadCertificates()
      await loadStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadCertificates()
  loadStats()
})
</script>

<style scoped>
.certificate-library-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

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

.operation-card,
.table-card {
  margin-bottom: 24px;
  border-radius: 12px;
}
</style>
