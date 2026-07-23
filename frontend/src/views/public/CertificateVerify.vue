<template>
  <div class="verify-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" size="large">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
      </div>
      <div class="header-center">
        <h2><el-icon><CircleCheck /></el-icon> 证书验伪系统</h2>
        <p class="subtitle">基于区块链的志愿服务证书真伪验证</p>
      </div>
      <div class="header-right">
        <!-- 占位保持居中 -->
      </div>
    </div>

    <!-- 验证表单 -->
    <el-card class="verify-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Search /></el-icon> 证书验证</span>
        </div>
      </template>

      <el-alert
        title="如何验证证书？"
        type="info"
        :closable="false"
        show-icon
        class="tips-alert">
        <p>请输入以下任意一种信息进行验证：</p>
        <ul>
          <li><strong>方式1（推荐）：</strong>证书编号 + 证书哈希（最安全的验证方式）</li>
          <li><strong>方式2：</strong>仅证书编号（快速验证）</li>
        </ul>
      </el-alert>

      <el-tabs v-model="activeTab" class="verify-tabs">
        <!-- 方式1：增强验证 -->
        <el-tab-pane label="增强验证（推荐）" name="enhanced">
          <el-form :model="enhancedForm" label-width="120px" style="max-width: 600px; margin: 20px auto;">
            <el-form-item label="志愿者姓名" required>
              <el-input 
                v-model="enhancedForm.volunteerName" 
                placeholder="请输入证书上的志愿者姓名" 
                clearable />
            </el-form-item>
            
            <el-form-item label="证书编号" required>
              <el-input 
                v-model="enhancedForm.certificateNo" 
                placeholder="例如：CERT-1BE3-6352" 
                clearable />
            </el-form-item>
            
            <el-form-item label="证书哈希" required>
              <el-input 
                v-model="enhancedForm.certificateHash" 
                type="textarea"
                :rows="3"
                placeholder="请输入完整的证书哈希值（64位十六进制）" 
                clearable />
              <span class="form-tip">证书哈希通常以 0x 开头，长度为 66 个字符</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="verifyEnhanced" :loading="verifying" size="large">
                <el-icon><CircleCheck /></el-icon> 开始验证
              </el-button>
              <el-button @click="resetEnhancedForm" size="large">
                <el-icon><RefreshRight /></el-icon> 重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 方式2：快速验证 -->
        <el-tab-pane label="快速验证" name="quick">
          <el-form :model="quickForm" label-width="120px" style="max-width: 600px; margin: 20px auto;">
            <el-form-item label="证书编号" required>
              <el-input 
                v-model="quickForm.certificateNo" 
                placeholder="例如：CERT-1BE3-6352" 
                clearable 
                size="large" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="verifyQuick" :loading="verifying" size="large">
                <el-icon><Search /></el-icon> 快速验证
              </el-button>
              <el-button @click="resetQuickForm" size="large">
                <el-icon><RefreshRight /></el-icon> 重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 验证结果 -->
    <el-card v-if="verifyResult" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Tickets /></el-icon> 验证结果</span>
        </div>
      </template>

      <!-- 成功结果 -->
      <div v-if="verifyResult.valid" class="result-success">
        <div class="success-icon">
          <el-icon :size="80" color="#67C23A"><CircleCheck /></el-icon>
        </div>
        <h2 class="success-title">✓ 证书验证通过</h2>
        <p class="success-message">{{ verifyResult.message }}</p>

        <el-descriptions :column="1" border class="cert-details">
          <el-descriptions-item label="证书编号">
            <el-tag type="primary" size="large">{{ verifyResult.certificate.certificateNo }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="志愿者姓名">
            <span class="highlight-text">{{ verifyResult.certificate.volunteerName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="服务时长">
            <el-tag type="success" size="large">{{ verifyResult.certificate.chainTotalHours }} 小时</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="补录时长">
            <el-tag type="warning">{{ verifyResult.certificate.chainReplenishHours }} 小时</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="钱包地址">
            <el-tag type="info">{{ verifyResult.certificate.walletAddress }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="证书状态">
            <el-tag 
              :type="verifyResult.certificate.status === 'issued' ? 'success' : 'warning'"
              effect="dark"
              size="large">
              {{ verifyResult.certificate.status === 'issued' ? '已发放' : '待发放' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发放时间" v-if="verifyResult.certificate.issueDate">
            {{ formatDate(verifyResult.certificate.issueDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(verifyResult.certificate.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="证书哈希">
            <el-input 
              :model-value="verifyResult.certificate.certificateHash" 
              readonly
              size="small">
              <template #append>
                <el-button @click="copyHash(verifyResult.certificate.certificateHash)">
                  <el-icon><DocumentCopy /></el-icon> 复制
                </el-button>
              </template>
            </el-input>
          </el-descriptions-item>
        </el-descriptions>

        <div class="blockchain-badge-section">
          <el-tag type="success" effect="dark" size="large">
            <el-icon><Link /></el-icon> 区块链认证
          </el-tag>
          <p class="badge-text">此证书数据已上链存储，真实可信</p>
        </div>
      </div>

      <!-- 失败结果 -->
      <div v-else class="result-failure">
        <div class="failure-icon">
          <el-icon :size="80" color="#F56C6C"><CircleClose /></el-icon>
        </div>
        <h2 class="failure-title">✗ 证书验证失败</h2>
        <p class="failure-message">{{ verifyResult.message }}</p>

        <el-alert
          title="可能的原因"
          type="error"
          :closable="false"
          show-icon
          class="failure-tips">
          <ul>
            <li>证书编号不存在或输入错误</li>
            <li>证书哈希值不匹配（证书可能被篡改）</li>
            <li>志愿者姓名与证书不符</li>
            <li>证书已被撤销</li>
          </ul>
        </el-alert>

        <div class="failure-actions">
          <el-button type="primary" @click="resetAll">
            <el-icon><RefreshRight /></el-icon> 重新验证
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 验证说明 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><QuestionFilled /></el-icon> 验证说明</span>
        </div>
      </template>

      <el-collapse v-model="activeNames">
        <el-collapse-item title="什么是证书验伪？" name="1">
          <p>证书验伪是通过区块链技术验证志愿服务证书真实性的过程。我们的系统采用多重验证机制：</p>
          <ul>
            <li><strong>证书编号验证：</strong>检查证书是否存在于系统中</li>
            <li><strong>哈希值验证：</strong>通过 SHA-256 算法验证证书数据完整性</li>
            <li><strong>链上数据校验：</strong>对比链上数据确保证书未被篡改</li>
          </ul>
        </el-collapse-item>

        <el-collapse-item title="如何获取证书信息？" name="2">
          <p>证书信息可以从以下位置获取：</p>
          <ul>
            <li><strong>证书编号：</strong>位于证书顶部的"证书编号"字段，格式为 CERT-XXXX-XXXX</li>
            <li><strong>证书哈希：</strong>位于证书底部的"区块链认证"区域</li>
            <li><strong>志愿者姓名：</strong>证书中间的大字显示</li>
          </ul>
        </el-collapse-item>

        <el-collapse-item title="验证方式的区别？" name="3">
          <table class="compare-table">
            <thead>
              <tr>
                <th>验证方式</th>
                <th>所需信息</th>
                <th>安全性</th>
                <th>推荐程度</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>增强验证</td>
                <td>姓名 + 编号 + 哈希</td>
                <td><el-tag type="success">极高</el-tag></td>
                <td><el-rate :model-value="5" disabled /></td>
              </tr>
              <tr>
                <td>快速验证</td>
                <td>仅证书编号</td>
                <td><el-tag type="warning">中等</el-tag></td>
                <td><el-rate :model-value="3" disabled /></td>
              </tr>
            </tbody>
          </table>
        </el-collapse-item>

        <el-collapse-item title="验证失败怎么办？" name="4">
          <p>如果验证失败，请按以下步骤操作：</p>
          <ol>
            <li>仔细检查输入的证书编号、哈希值是否正确</li>
            <li>确认证书是否已被管理员发放（待发放状态的证书可能验证失败）</li>
            <li>检查证书是否已被撤销</li>
            <li>联系志愿服务管理中心核实</li>
          </ol>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  CircleCheck, CircleClose, Search, RefreshRight, Tickets, 
  QuestionFilled, Link, DocumentCopy, ArrowLeft 
} from '@element-plus/icons-vue'
import { verifyCertificate, verifyCertificateEnhanced } from '@/api/certificate'

const router = useRouter()

const activeTab = ref('enhanced')
const verifying = ref(false)
const verifyResult = ref(null)
const activeNames = ref(['1'])

const enhancedForm = ref({
  volunteerName: '',
  certificateNo: '',
  certificateHash: ''
})

const quickForm = ref({
  certificateNo: ''
})

// 返回上一页（需要判断登录状态）
const goBack = () => {
  const token = localStorage.getItem('token')
  if (token) {
    // 已登录，返回到证书页面
    router.push('/volunteer/certificate')
  } else {
    // 未登录，提示用户登录
    ElMessage.warning('请先登录系统')
    router.push('/login')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 增强验证
const verifyEnhanced = async () => {
  if (!enhancedForm.value.volunteerName || !enhancedForm.value.certificateNo || !enhancedForm.value.certificateHash) {
    ElMessage.warning('请填写完整的验证信息：姓名、证书编号、证书哈希')
    return
  }

  verifying.value = true
  verifyResult.value = null

  try {
    const res = await verifyCertificateEnhanced(enhancedForm.value)
    
    if (res.code === 200) {
      verifyResult.value = res.data
      
      if (res.data.valid) {
        ElMessage.success({
          message: '证书验证成功！',
          duration: 3000
        })
      } else {
        ElMessage.error({
          message: '证书验证失败：' + res.data.message,
          duration: 5000
        })
      }
    } else {
      ElMessage.error(res.message || '验证请求失败')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证失败，请检查网络连接')
  } finally {
    verifying.value = false
  }
}

// 快速验证
const verifyQuick = async () => {
  if (!quickForm.value.certificateNo) {
    ElMessage.warning('请输入证书编号')
    return
  }

  verifying.value = true
  verifyResult.value = null

  try {
    const res = await verifyCertificate(quickForm.value.certificateNo)
    
    if (res.code === 200) {
      verifyResult.value = res.data
      
      if (res.data.valid) {
        ElMessage.success('证书验证成功！')
      } else {
        ElMessage.error('证书验证失败：' + res.data.message)
      }
    } else {
      ElMessage.error(res.message || '验证请求失败')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证失败，请检查网络连接')
  } finally {
    verifying.value = false
  }
}

// 重置增强表单
const resetEnhancedForm = () => {
  enhancedForm.value = {
    volunteerName: '',
    certificateNo: '',
    certificateHash: ''
  }
  verifyResult.value = null
}

// 重置快速表单
const resetQuickForm = () => {
  quickForm.value = {
    certificateNo: ''
  }
  verifyResult.value = null
}

// 重置所有
const resetAll = () => {
  resetEnhancedForm()
  resetQuickForm()
}

// 复制哈希
const copyHash = (hash) => {
  navigator.clipboard.writeText(hash).then(() => {
    ElMessage.success('证书哈希已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}
</script>

<style scoped>
.verify-page {
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
  color: #67C23A;
}

.subtitle {
  color: #909399;
  margin: 8px 0 0 0;
  font-size: 14px;
}

.verify-card,
.result-card,
.info-card {
  margin-bottom: 24px;
  border-radius: 12px;
  max-width: 1200px;
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

.tips-alert {
  margin-bottom: 24px;
}

.tips-alert ul {
  margin: 10px 0 0 20px;
  padding: 0;
}

.tips-alert li {
  margin: 5px 0;
}

.verify-tabs {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-top: 5px;
}

/* 成功结果样式 */
.result-success {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  margin-bottom: 20px;
  animation: successBounce 0.6s ease-out;
}

@keyframes successBounce {
  0% { transform: scale(0); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.success-title {
  font-size: 28px;
  color: #67C23A;
  margin: 20px 0 10px 0;
}

.success-message {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.cert-details {
  margin: 30px auto;
  max-width: 800px;
  text-align: left;
}

.highlight-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.blockchain-badge-section {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 12px;
}

.badge-text {
  margin: 10px 0 0 0;
  color: #606266;
  font-size: 14px;
}

/* 失败结果样式 */
.result-failure {
  text-align: center;
  padding: 40px 20px;
}

.failure-icon {
  margin-bottom: 20px;
  animation: failureShake 0.5s ease-out;
}

@keyframes failureShake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

.failure-title {
  font-size: 28px;
  color: #F56C6C;
  margin: 20px 0 10px 0;
}

.failure-message {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.failure-tips {
  max-width: 600px;
  margin: 20px auto;
  text-align: left;
}

.failure-tips ul {
  margin: 10px 0 0 20px;
  padding: 0;
}

.failure-tips li {
  margin: 8px 0;
}

.failure-actions {
  margin-top: 30px;
}

/* 对比表格 */
.compare-table {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
}

.compare-table th,
.compare-table td {
  padding: 12px;
  border: 1px solid #EBEEF5;
  text-align: center;
}

.compare-table thead {
  background: #F5F7FA;
  font-weight: 600;
}

.compare-table tbody tr:hover {
  background: #F5F7FA;
}
</style>
