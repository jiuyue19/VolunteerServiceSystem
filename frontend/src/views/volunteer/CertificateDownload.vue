<template>
  <div class="certificate-download-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" size="large">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
      </div>
      <div class="header-center">
        <h2><el-icon><Download /></el-icon> 证书下载</h2>
        <p class="subtitle">输入姓名和钱包地址下载您的志愿服务证书</p>
      </div>
      <div class="header-right">
        <!-- 占位保持居中 -->
      </div>
    </div>

    <!-- 证书查询 -->
    <el-card class="query-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Search /></el-icon> 查询我的证书</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" label-width="120px" class="query-form">
        <el-form-item label="志愿者姓名" required>
          <el-input 
            v-model="queryForm.volunteerName" 
            placeholder="请输入您的姓名" 
            clearable 
            style="width: 220px" />
        </el-form-item>
        
        <el-form-item label="钱包地址" required>
          <el-input 
            v-model="queryForm.walletAddress" 
            placeholder="请输入您的钱包地址" 
            clearable 
            style="width: 400px" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="queryCertificates" :loading="queryLoading" size="large">
            <el-icon><Search /></el-icon> 查询证书
          </el-button>
          <el-button @click="resetQuery" size="large">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="提示"
        type="info"
        :closable="false"
        show-icon
        class="query-tips">
        <p>• 请输入您在系统中注册的姓名</p>
        <p>• 钱包地址需与您绑定的 MetaMask 地址一致</p>
        <p>• 查询结果将显示所有已发放的证书</p>
      </el-alert>
    </el-card>

    <!-- 查询结果列表 -->
    <el-card v-if="myCertificates.length > 0" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Tickets /></el-icon> 查询到 {{ myCertificates.length }} 张证书</span>
        </div>
      </template>

      <el-table :data="myCertificates" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        
        <el-table-column prop="certificateNo" label="证书编号" width="180" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.certificateNo }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="chainTotalHours" label="服务时长" width="120" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff;">{{ row.chainTotalHours }} 小时</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="activityCount" label="活动次数" width="100" align="center" />
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'issued' ? 'success' : 'warning'" 
              effect="dark">
              {{ row.status === 'issued' ? '已发放' : '待发放' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="issueDate" label="发放时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.issueDate ? formatDate(row.issueDate) : '未发放' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="previewCertificate(row)"
              :disabled="row.status !== 'issued'">
              <el-icon><View /></el-icon> 预览
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="downloadCertificate(row)"
              :disabled="row.status !== 'issued'">
              <el-icon><Download /></el-icon> 下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 未查询或无结果 -->
    <el-card v-else-if="queriedOnce" class="empty-card" shadow="hover">
      <el-empty description="未查询到证书，请检查输入信息是否正确" />
    </el-card>

    <!-- 证书预览对话框 -->
    <el-dialog 
      v-model="showPreviewDialog" 
      title="证书预览" 
      width="1000px"
      :close-on-click-modal="false">
      
      <div class="certificate-preview" ref="certificateRef" v-if="currentCertificate">
        <div class="certificate-container">
          <div class="certificate-border-rainbow">
            <div class="certificate-content">
              <!-- 顶部装饰 -->
              <div class="certificate-header">
                <div class="certificate-logo">
                  <div class="lotus-badge">
                    <svg class="lotus-icon" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                      <defs>
                        <linearGradient id="lotusGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" style="stop-color:#ff6b6b;stop-opacity:1" />
                          <stop offset="50%" style="stop-color:#ee5a6f;stop-opacity:1" />
                          <stop offset="100%" style="stop-color:#f093fb;stop-opacity:1" />
                        </linearGradient>
                        <radialGradient id="lotusCenter" cx="50%" cy="50%" r="50%">
                          <stop offset="0%" style="stop-color:#feca57;stop-opacity:1" />
                          <stop offset="100%" style="stop-color:#ff6b6b;stop-opacity:1" />
                        </radialGradient>
                      </defs>
                      <circle cx="50" cy="50" r="8" fill="url(#lotusCenter)"/>
                      <path d="M50 30 Q45 35 50 45 Q55 35 50 30" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M65 38 Q60 43 55 48 Q63 50 65 38" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M70 52 Q65 52 60 55 Q68 60 70 52" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M65 66 Q63 60 55 58 Q60 68 65 66" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M50 72 Q50 67 50 57 Q50 67 50 72" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M35 66 Q37 60 45 58 Q40 68 35 66" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M30 52 Q35 52 40 55 Q32 60 30 52" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M35 38 Q40 43 45 48 Q37 50 35 38" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M50 42 Q48 38 44 38 Q40 38 40 42 Q40 46 50 52 Q60 46 60 42 Q60 38 56 38 Q52 38 50 42 Z" 
                            fill="url(#lotusGradient)" opacity="0.85"/>
                    </svg>
                  </div>
                </div>
                <h1 class="certificate-title">志愿服务证书</h1>
                <p class="certificate-subtitle">VOLUNTEER SERVICE CERTIFICATE</p>
              </div>

              <!-- 证书主体 -->
              <div class="certificate-body">
                <div class="recipient-section">
                  <p class="recipient-label">兹证明</p>
                  <p class="recipient-name">{{ currentCertificate.volunteerName }}</p>
                </div>

                <div class="content-section">
                  <p class="content-text">
                    在志愿服务活动中表现优异，累计服务时长达到
                  </p>
                  <p class="hours-highlight">
                    <svg class="hours-svg" :width="getHoursWidth()" height="80" xmlns="http://www.w3.org/2000/svg">
                      <defs>
                        <linearGradient id="hoursGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                          <stop offset="50%" style="stop-color:#f093fb;stop-opacity:1" />
                          <stop offset="100%" style="stop-color:#feca57;stop-opacity:1" />
                        </linearGradient>
                      </defs>
                      <text x="0" y="60" font-size="72" font-weight="700" fill="url(#hoursGradient)" font-family="system-ui, -apple-system, sans-serif">
                        {{ currentCertificate.chainTotalHours }}
                      </text>
                    </svg>
                    <span class="hours-unit">小时</span>
                  </p>
                  <p class="content-text">
                    参与志愿活动 <strong>{{ currentCertificate.activityCount || 0 }}</strong> 次，
                    其中补录时长 <strong>{{ currentCertificate.chainReplenishHours || 0 }}</strong> 小时。
                  </p>
                  <p class="content-text" style="margin-top: 20px;">
                    特此证明，以资鼓励。
                  </p>
                </div>

                <!-- 区块链验证信息 -->
                <div class="blockchain-section">
                  <div class="blockchain-badge">
                    <svg class="blockchain-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <defs>
                        <linearGradient id="blockchainGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" style="stop-color:#00f2fe;stop-opacity:1" />
                          <stop offset="50%" style="stop-color:#4facfe;stop-opacity:1" />
                          <stop offset="100%" style="stop-color:#00f2fe;stop-opacity:1" />
                        </linearGradient>
                        <linearGradient id="blockGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" style="stop-color:#43e97b;stop-opacity:1" />
                          <stop offset="100%" style="stop-color:#38f9d7;stop-opacity:1" />
                        </linearGradient>
                      </defs>
                      <rect x="2" y="8" width="6" height="6" rx="1" fill="url(#blockGradient)" opacity="0.9"/>
                      <rect x="3" y="9" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="3" y="11" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="9" y="6" width="6" height="6" rx="1" fill="url(#blockGradient)" opacity="0.9"/>
                      <rect x="10" y="7" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="10" y="9" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="16" y="10" width="6" height="6" rx="1" fill="url(#blockGradient)" opacity="0.9"/>
                      <rect x="17" y="11" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="17" y="13" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <line x1="8" y1="11" x2="9" y2="9" stroke="url(#blockchainGradient)" stroke-width="1.5" opacity="0.8"/>
                      <line x1="15" y1="9" x2="16" y2="13" stroke="url(#blockchainGradient)" stroke-width="1.5" opacity="0.8"/>
                      <circle cx="8" cy="11" r="1.5" fill="url(#blockchainGradient)"/>
                      <circle cx="9" cy="9" r="1.5" fill="url(#blockchainGradient)"/>
                      <circle cx="15" cy="9" r="1.5" fill="url(#blockchainGradient)"/>
                      <circle cx="16" cy="13" r="1.5" fill="url(#blockchainGradient)"/>
                    </svg>
                    <span>区块链认证</span>
                  </div>
                  <div class="blockchain-info">
                    <p><strong>钱包地址：</strong>{{ formatAddress(currentCertificate.walletAddress) }}</p>
                    <p><strong>证书编号：</strong>{{ currentCertificate.certificateNo }}</p>
                    <p><strong>证书哈希：</strong><span class="hash-text">{{ currentCertificate.certificateHash }}</span></p>
                    <p><strong>生成时间：</strong>{{ formatDate(currentCertificate.issueDate) }}</p>
                  </div>
                </div>
              </div>

              <!-- 底部签章 -->
              <div class="certificate-footer">
                <div class="seal-section">
                  <div class="seal">
                    <div class="seal-inner">志愿服务<br/>管理中心</div>
                  </div>
                  <p class="seal-date">{{ formatDate(currentCertificate.issueDate) }}</p>
                </div>
              </div>

              <!-- 装饰元素 -->
              <div class="corner-decoration top-left"></div>
              <div class="corner-decoration top-right"></div>
              <div class="corner-decoration bottom-left"></div>
              <div class="corner-decoration bottom-right"></div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showPreviewDialog = false">关闭</el-button>
        <el-button type="primary" @click="downloadCurrentCertificate">
          <el-icon><Download /></el-icon> 下载证书
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Download, Search, RefreshRight, Tickets, View, ArrowLeft 
} from '@element-plus/icons-vue'
import { queryCertificates as queryCertsAPI } from '@/api/certificate'
import html2canvas from 'html2canvas'

const router = useRouter()

const queryLoading = ref(false)
const queriedOnce = ref(false)
const myCertificates = ref([])
const showPreviewDialog = ref(false)
const currentCertificate = ref(null)
const certificateRef = ref(null)

const queryForm = ref({
  volunteerName: '',
  walletAddress: ''
})

// 返回上一页
const goBack = () => {
  router.push('/volunteer/certificate')
}

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '未知'
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算SVG宽度
const getHoursWidth = () => {
  if (!currentCertificate.value) return 150
  const hours = String(currentCertificate.value.chainTotalHours || 0)
  const width = hours.split('').reduce((sum, char) => {
    return sum + (char === '.' ? 25 : 50)
  }, 0)
  return Math.max(width, 80)
}

// 查询证书
const queryCertificates = async () => {
  if (!queryForm.value.volunteerName || !queryForm.value.walletAddress) {
    ElMessage.warning('请输入完整的查询信息')
    return
  }

  queryLoading.value = true
  queriedOnce.value = true

  try {
    const res = await queryCertsAPI({
      volunteerName: queryForm.value.volunteerName,
      walletAddress: queryForm.value.walletAddress
    })

    if (res.code === 200) {
      // 只显示已发放的证书
      myCertificates.value = res.data.filter(cert => cert.status === 'issued')
      
      if (myCertificates.value.length > 0) {
        ElMessage.success(`查询到 ${myCertificates.value.length} 张已发放的证书`)
      } else {
        ElMessage.warning('未查询到已发放的证书，请确认证书是否已被管理员发放')
      }
    } else {
      ElMessage.error(res.message || '查询失败')
      myCertificates.value = []
    }
  } catch (error) {
    console.error('查询证书失败:', error)
    ElMessage.error('查询证书失败，请检查网络连接')
    myCertificates.value = []
  } finally {
    queryLoading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.value = {
    volunteerName: '',
    walletAddress: ''
  }
  myCertificates.value = []
  queriedOnce.value = false
}

// 预览证书
const previewCertificate = (cert) => {
  currentCertificate.value = cert
  showPreviewDialog.value = true
}

// 下载证书
const downloadCertificate = async (cert) => {
  currentCertificate.value = cert
  showPreviewDialog.value = true
  
  // 等待对话框打开
  await new Promise(resolve => setTimeout(resolve, 300))
  await downloadCurrentCertificate()
}

// 下载当前预览的证书
const downloadCurrentCertificate = async () => {
  if (!currentCertificate.value) {
    ElMessage.warning('请先选择要下载的证书')
    return
  }

  try {
    ElMessage.info('正在生成证书图片，请稍候...')

    await new Promise(resolve => setTimeout(resolve, 100))

    const element = certificateRef.value
    if (!element) {
      throw new Error('证书元素未找到')
    }

    const canvas = await html2canvas(element, {
      scale: 2,
      backgroundColor: '#ffffff',
      logging: false,
      useCORS: true
    })

    const link = document.createElement('a')
    link.download = `志愿服务证书_${currentCertificate.value.volunteerName}_${currentCertificate.value.certificateNo}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()

    ElMessage.success('证书下载成功！')
  } catch (error) {
    console.error('下载证书失败:', error)
    ElMessage.error('证书下载失败，请重试')
  }
}
</script>

<style scoped>
.certificate-download-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.header-left {
  flex: 0 0 120px;
  display: flex;
  justify-content: flex-start;
}

.header-center {
  flex: 1;
  text-align: center;
}

.header-right {
  flex: 0 0 120px;
  display: flex;
  justify-content: flex-end;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  display: inline-flex;
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
  margin: 8px 0 0 0;
  font-size: 14px;
}

.query-card,
.result-card,
.empty-card {
  margin-bottom: 24px;
  border-radius: 12px;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.query-form {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

.query-tips {
  margin-top: 20px;
}

.query-tips p {
  margin: 5px 0;
}

/* 证书样式 - 复用管理员端的样式 */
.certificate-preview {
  margin: 24px 0;
  display: flex;
  justify-content: center;
}

.certificate-container {
  width: 100%;
  max-width: 900px;
}

.certificate-border-rainbow {
  background: linear-gradient(90deg, #ff6b6b 0%, #feca57 16.67%, #48dbfb 33.33%, #1dd1a1 50%, #5f27cd 66.67%, #ee5a6f 83.33%, #ff6b6b 100%);
  padding: 6px;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  animation: rainbow-rotate 8s linear infinite;
  background-size: 200% 100%;
}

@keyframes rainbow-rotate {
  0% { background-position: 0% 50%; }
  100% { background-position: 200% 50%; }
}

.certificate-content {
  background: linear-gradient(to bottom, #ffffff 0%, #f8f9fa 100%);
  border-radius: 8px;
  padding: 60px 80px;
  position: relative;
  min-height: 700px;
}

.certificate-header {
  text-align: center;
  margin-bottom: 50px;
}

.certificate-logo {
  margin-bottom: 20px;
}

.lotus-badge {
  display: inline-block;
  width: 100px;
  height: 100px;
  animation: lotus-float 3s ease-in-out infinite;
}

.lotus-icon {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(255, 107, 107, 0.4));
}

@keyframes lotus-float {
  0%, 100% { transform: translateY(0) scale(1); filter: drop-shadow(0 4px 12px rgba(255, 107, 107, 0.4)); }
  50% { transform: translateY(-8px) scale(1.05); filter: drop-shadow(0 6px 16px rgba(255, 107, 107, 0.6)); }
}

.certificate-title {
  font-size: 48px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 12px 0;
  letter-spacing: 8px;
}

.certificate-subtitle {
  font-size: 16px;
  color: #909399;
  letter-spacing: 4px;
  margin: 0;
}

.certificate-body {
  margin-bottom: 50px;
}

.recipient-section {
  text-align: center;
  margin-bottom: 40px;
}

.recipient-label {
  font-size: 18px;
  color: #606266;
  margin: 0 0 12px 0;
}

.recipient-name {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  padding: 0 20px 10px;
  border-bottom: 3px solid transparent;
  border-image: linear-gradient(90deg, #ff6b6b, #feca57, #48dbfb, #1dd1a1, #5f27cd);
  border-image-slice: 1;
  display: inline-block;
}

.content-section {
  text-align: center;
  line-height: 2;
}

.content-text {
  font-size: 18px;
  color: #606266;
  margin: 12px 0;
}

.hours-highlight {
  margin: 30px 0;
  padding: 0;
  background: transparent;
  text-align: center;
}

.hours-svg {
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

.hours-unit {
  font-size: 28px;
  color: #606266;
  vertical-align: middle;
}

.blockchain-section {
  margin-top: 40px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #dcdfe6;
}

.blockchain-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 50%, #43e97b 100%);
  color: white;
  padding: 10px 20px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
  box-shadow: 0 4px 15px rgba(79, 172, 254, 0.4);
  animation: blockchain-pulse 2s ease-in-out infinite;
}

.blockchain-icon {
  width: 24px;
  height: 24px;
  filter: drop-shadow(0 2px 4px rgba(255, 255, 255, 0.5));
}

@keyframes blockchain-pulse {
  0%, 100% { 
    box-shadow: 0 4px 15px rgba(79, 172, 254, 0.4);
    transform: scale(1);
  }
  50% { 
    box-shadow: 0 6px 24px rgba(0, 242, 254, 0.6);
    transform: scale(1.02);
  }
}

.blockchain-info {
  font-size: 14px;
  color: #606266;
  line-height: 2;
}

.blockchain-info p {
  margin: 4px 0;
}

.hash-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  word-break: break-all;
}

.certificate-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 50px;
}

.seal-section {
  text-align: center;
}

.seal {
  width: 120px;
  height: 120px;
  border: 4px solid #e74c3c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  position: relative;
  background: rgba(231, 76, 60, 0.05);
}

.seal-inner {
  color: #e74c3c;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  text-align: center;
}

.seal-date {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.corner-decoration {
  position: absolute;
  width: 60px;
  height: 60px;
  border: 3px solid;
}

.corner-decoration.top-left {
  top: 20px;
  left: 20px;
  border-right: none;
  border-bottom: none;
  border-radius: 8px 0 0 0;
  border-color: #ff6b6b #feca57;
}

.corner-decoration.top-right {
  top: 20px;
  right: 20px;
  border-left: none;
  border-bottom: none;
  border-radius: 0 8px 0 0;
  border-color: #48dbfb #1dd1a1;
}

.corner-decoration.bottom-left {
  bottom: 20px;
  left: 20px;
  border-right: none;
  border-top: none;
  border-radius: 0 0 0 8px;
  border-color: #5f27cd #ee5a6f;
}

.corner-decoration.bottom-right {
  bottom: 20px;
  right: 20px;
  border-left: none;
  border-top: none;
  border-radius: 0 0 8px 0;
  border-color: #feca57 #ff6b6b;
}
</style>
