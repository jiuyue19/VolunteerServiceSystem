<template>
  <div class="blockchain-page">
    <div class="page-header">
      <div class="header-top">
        <div>
          <h2><el-icon><Link /></el-icon> 区块链证书管理</h2>
          <p class="subtitle">本地私链 + MetaMask 上链记录、补录时长与证书管理</p>
        </div>
        <div class="header-actions">
          <el-button type="success" @click="handleConnectMetaMask" :disabled="walletConnecting">
            <el-icon><Link /></el-icon> {{ walletConnecting ? '连接中...' : '连接 MetaMask' }}
          </el-button>
          <el-button type="info" @click="viewContractInfo">
            <el-icon><Lock /></el-icon> 查看合约信息
          </el-button>
        </div>
      </div>
    </div>

    <el-card class="wallet-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Link /></el-icon> MetaMask 连接状态</span>
          <el-tag :type="walletInstalled ? 'success' : 'danger'">{{ walletInstalled ? '已检测到 MetaMask' : '未安装 MetaMask' }}</el-tag>
        </div>
      </template>

      <el-alert
        v-if="!walletInstalled"
        title="当前浏览器未检测到 MetaMask，请先安装浏览器插件后再进行部署和写链操作"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="钱包连接状态">
          <el-tag :type="walletAddress ? 'success' : 'info'">{{ walletAddress ? '已连接' : '未连接' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前网络状态">
          <el-tag :type="isExpectedNetwork ? 'success' : 'warning'">{{ walletChainId || '未知网络' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前钱包地址" :span="2">
          <span class="break-all">{{ walletAddress || '尚未连接 MetaMask 钱包' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="目标私链 Chain ID">
          <span>{{ CURRENT_NETWORK.chainId }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="网络匹配结果">
          <el-tag :type="isExpectedNetwork ? 'success' : 'danger'">{{ isExpectedNetwork ? '已连接到本地私链' : '未切换到目标私链' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="data-actions">
        <el-button type="success" @click="handleConnectMetaMask" :disabled="walletConnecting || !walletInstalled">
          <el-icon><Link /></el-icon> {{ walletAddress ? '重新连接钱包' : '连接钱包' }}
        </el-button>
        <el-button @click="refreshWalletState" :disabled="!walletInstalled">
          <el-icon><Search /></el-icon> 刷新钱包状态
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon icon-blue"><el-icon><DataLine /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ blockchainStats.totalRecords }}</div>
            <div class="stat-label">上链记录总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon icon-pink"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ blockchainStats.totalVolunteers }}</div>
            <div class="stat-label">已上链志愿者</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon icon-cyan"><el-icon><Document /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ blockchainStats.totalCertificates }}</div>
            <div class="stat-label">已颁发证书</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon icon-green"><el-icon><Clock /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ blockchainStats.totalHours }}</div>
            <div class="stat-label">总服务时长（小时）</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="deploy-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Lock /></el-icon> 合约接入说明</span>
          <el-tag type="warning">使用 Remix 部署</el-tag>
        </div>
      </template>

      <el-alert
        title="请先在 Remix 中通过 MetaMask 将 VolunteerHours 合约部署到本地私链，再回到本系统进行查询和写链操作"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="当前合约地址">
          <span class="address-text">{{ activeContractAddress || '未部署' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="网络">
          <el-tag>{{ contractInfo?.chainId || '2025' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="RPC URL">
          <span class="break-all">{{ contractInfo?.rpcUrl || 'http://127.0.0.1:8545' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="交易签名方式">
          <el-tag type="warning">MetaMask</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form label-width="120px" class="contract-address-form">
        <el-form-item label="Remix 合约地址">
          <el-input v-model="contractAddressInput" placeholder="请输入在 Remix 部署到本地私链后的合约地址" clearable />
        </el-form-item>
      </el-form>

      <div class="data-actions">
        <el-button type="primary" @click="handleSaveContractAddress">
          <el-icon><Lock /></el-icon> 保存合约地址
        </el-button>
        <el-button @click="handleClearContractAddress">
          <el-icon><Document /></el-icon> 清空已保存地址
        </el-button>
        <el-button type="info" @click="viewContractInfo">
          <el-icon><Lock /></el-icon> 查看当前合约配置
        </el-button>
      </div>
    </el-card>

    <el-card class="query-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Search /></el-icon> 查询链上数据</span>
        </div>
      </template>
      <el-form :inline="true" label-width="100px">
        <el-form-item label="选择志愿者">
          <el-select v-model="queryVolunteer" filterable placeholder="请选择志愿者姓名" style="width: 250px" @focus="loadVolunteers">
            <el-option
              v-for="vol in volunteerList"
              :key="vol.id"
              :label="`${vol.realName} (ID: ${vol.id})`"
              :value="vol.id"
            >
              <span style="float: left">{{ vol.realName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ vol.walletAddress ? '已绑定' : '未绑定钱包' }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="querying" :disabled="!queryVolunteer">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="chainData" class="chain-data-result">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="钱包地址"><el-tag type="success">{{ formatAddress(chainData.walletAddress) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="链上总时长"><el-tag type="primary" size="large">{{ chainData.chainTotalHours }} 小时</el-tag></el-descriptions-item>
          <el-descriptions-item label="链上补录时长"><el-tag type="warning" size="large">{{ chainData.chainReplenishHours }} 小时</el-tag></el-descriptions-item>
          <el-descriptions-item label="数据库正常时长"><el-tag type="success" size="large">{{ chainData.dbNormalHours ?? 0 }} 小时</el-tag></el-descriptions-item>
          <el-descriptions-item label="数据库补录时长"><el-tag type="warning" size="large">{{ chainData.dbReplenishHours ?? 0 }} 小时</el-tag></el-descriptions-item>
          <el-descriptions-item label="数据库总时长"><el-tag type="info" size="large">{{ chainData.dbTotalHours }} 小时</el-tag></el-descriptions-item>
          <el-descriptions-item label="待同步正常时长"><el-tag type="success" size="large">{{ syncableHours }} 小时</el-tag></el-descriptions-item>
          <el-descriptions-item label="数据一致性" :span="2">
            <el-tag :type="isHoursConsistent ? 'success' : 'danger'">
              {{ isHoursConsistent ? '一致' : '不一致' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="data-actions">
          <el-button type="primary" :loading="writingActivity" @click="handleSync" :disabled="!chainData.walletAddress">
            <el-icon><Upload /></el-icon> 同步时长上链
          </el-button>
          <el-button type="warning" :loading="writingReplenish" @click="handleReplenish" :disabled="!chainData.walletAddress">
            <el-icon><Clock /></el-icon> 补录时长上链
          </el-button>
          <el-button type="success" @click="openCertificateDialog" :disabled="!chainData.walletAddress">
            <el-icon><Medal /></el-icon> 为该志愿者生成证书
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card v-if="lastActionReceipt" class="receipt-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Document /></el-icon> 最近一次写链回执</span>
          <el-tag type="primary">{{ lastActionType }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交易哈希"><span class="break-all">{{ lastActionReceipt.transactionHash }}</span></el-descriptions-item>
        <el-descriptions-item label="区块号">{{ lastActionReceipt.blockNumber }}</el-descriptions-item>
        <el-descriptions-item label="时间戳">{{ formatTimestamp(lastActionReceipt.timestamp) }}</el-descriptions-item>
        <el-descriptions-item label="Gas 使用量">{{ lastActionReceipt.gasUsed }}</el-descriptions-item>
        <el-descriptions-item label="调用账户"><span class="break-all">{{ lastActionReceipt.from }}</span></el-descriptions-item>
        <el-descriptions-item label="合约地址"><span class="break-all">{{ lastActionReceipt.to }}</span></el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="template-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Medal /></el-icon> 证书模板管理</span>
          <el-button type="primary" @click="showTemplatePreview = !showTemplatePreview">
            <el-icon><View /></el-icon> {{ showTemplatePreview ? '隐藏' : '预览' }}模板
          </el-button>
        </div>
      </template>

      <div v-show="showTemplatePreview" class="template-preview" ref="templateRef">
        <div class="certificate-container">
          <div class="certificate-border-rainbow">
            <div class="certificate-content">
              <div class="certificate-header">
                <h1 class="certificate-title">志愿服务证书</h1>
                <p class="certificate-subtitle">VOLUNTEER SERVICE CERTIFICATE</p>
              </div>
              <div class="certificate-body">
                <div class="recipient-section">
                  <p class="recipient-label">兹证明</p>
                  <p class="recipient-name">{{ chainData?.volunteerName || '志愿者姓名' }}</p>
                </div>
                <div class="content-section">
                  <p class="content-text">在志愿服务活动中表现优异，累计服务时长达到</p>
                  <p class="hours-highlight">
                    <svg class="hours-svg" :width="getHoursWidth()" height="80" xmlns="http://www.w3.org/2000/svg">
                      <defs>
                        <linearGradient id="hoursGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                          <stop offset="50%" style="stop-color:#f093fb;stop-opacity:1" />
                          <stop offset="100%" style="stop-color:#feca57;stop-opacity:1" />
                        </linearGradient>
                      </defs>
                      <text x="0" y="60" font-size="72" font-weight="700" fill="url(#hoursGradient)" font-family="system-ui, -apple-system, sans-serif">{{ chainData?.chainTotalHours || 0 }}</text>
                    </svg>
                    <span class="hours-unit">小时</span>
                  </p>
                  <p class="content-text">其中补录时长 <strong>{{ chainData?.chainReplenishHours || 0 }}</strong> 小时。</p>
                  <p class="content-text" style="margin-top: 20px;">特此证明，以资鼓励。</p>
                </div>
                <div class="blockchain-info">
                  <p><strong>钱包地址：</strong>{{ formatAddress(chainData?.walletAddress || '') }}</p>
                  <p><strong>证书编号：</strong>{{ certificateId }}</p>
                  <p><strong>证书哈希：</strong>{{ certificateHash }}</p>
                  <p><strong>生成时间：</strong>{{ currentDate }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="template-actions">
        <el-button type="success" @click="downloadTemplate" :disabled="!chainData"><el-icon><Download /></el-icon> 下载证书模板</el-button>
        <el-button @click="exportTemplateImage" :disabled="!chainData"><el-icon><Picture /></el-icon> 导出为图片</el-button>
      </div>
    </el-card>

    <el-dialog v-model="showCertificateDialog" title="生成志愿服务证书" width="600px">
      <el-alert title="证书数据来源于区块链" type="info" :closable="false" show-icon style="margin-bottom: 20px">
        <p>以下数据自动从区块链读取，不可手动修改</p>
      </el-alert>
      <el-form :model="certificateForm" label-width="120px">
        <el-form-item label="志愿者姓名"><el-input v-model="certificateForm.name" disabled /></el-form-item>
        <el-form-item label="服务时长"><el-input-number v-model="certificateForm.hours" :min="0" :precision="2" disabled /> 小时</el-form-item>
        <el-form-item label="补录时长"><el-input-number v-model="certificateForm.replenishHours" :min="0" :precision="2" disabled /> 小时</el-form-item>
        <el-form-item label="证书备注"><el-input v-model="certificateForm.remark" type="textarea" :rows="3" placeholder="可选填写" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCertificateDialog = false">取消</el-button>
        <el-button type="primary" @click="generateCertificate">生成证书</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReplenishDialog" title="补录时长上链" width="520px">
      <el-form :model="replenishForm" label-width="120px">
        <el-form-item label="活动名称"><el-input v-model="replenishForm.activityName" placeholder="请输入补录活动名称" /></el-form-item>
        <el-form-item label="补录时长"><el-input-number v-model="replenishForm.hours" :min="0.01" :precision="2" /> 小时</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReplenishDialog = false">取消</el-button>
        <el-button type="primary" :loading="writingReplenish" @click="confirmReplenish">确认上链</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showContractDialog" title="区块链合约配置信息" width="700px">
      <el-descriptions :column="1" border v-if="contractInfo">
        <el-descriptions-item label="合约地址">{{ activeContractAddress || contractInfo.contractAddress || '未部署' }}</el-descriptions-item>
        <el-descriptions-item label="RPC URL">{{ contractInfo?.rpcUrl || '未配置' }}</el-descriptions-item>
        <el-descriptions-item label="Chain ID">{{ contractInfo?.chainId || '未配置' }}</el-descriptions-item>
        <el-descriptions-item label="管理员地址">由 MetaMask 当前签名账户决定</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Link, DataLine, User, Document, Clock, Upload, Search, Medal, View, Download, Picture, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getTotalHours, getBlockchainStats, getContractInfo } from '@/api/blockchain'
import { generateCertificate as generateCert } from '@/api/certificate'
import {
  CURRENT_NETWORK,
  checkMetaMaskInstalled,
  clearStoredContractAddress,
  connectMetaMask,
  getCurrentChainId,
  saveContractAddress,
  sendAddActivityTransaction,
  sendAddReplenishActivityTransaction,
  getStoredContractAddress,
  getMetaMaskAddress
} from '@/config/blockchain'

import html2canvas from 'html2canvas'

const volunteerList = ref([])
const queryVolunteer = ref(null)
const querying = ref(false)
const writingActivity = ref(false)
const writingReplenish = ref(false)
const walletConnecting = ref(false)
const chainData = ref(null)
const blockchainStats = ref({ totalRecords: 0, totalVolunteers: 0, totalCertificates: 0, totalHours: 0 })
const contractInfo = ref(null)
const activeContractAddress = ref(getStoredContractAddress())
const contractAddressInput = ref(getStoredContractAddress())
const walletInstalled = ref(false)
const walletAddress = ref('')
const walletChainId = ref('')
const lastActionReceipt = ref(null)
const lastActionType = ref('')

const showTemplatePreview = ref(false)
const showCertificateDialog = ref(false)
const showReplenishDialog = ref(false)
const showContractDialog = ref(false)
const templateRef = ref(null)
const generatedCertificate = ref(null)
const certificateForm = ref({ name: '', hours: 0, replenishHours: 0, remark: '' })
const replenishForm = ref({ activityName: '补录服务时长', hours: 0.5 })

const certificateId = computed(() => generatedCertificate.value?.certificateNo || 'CERT-XXXX-XXXX')
const certificateHash = computed(() => generatedCertificate.value?.certificateHash || '********************************')
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})
const isExpectedNetwork = computed(() => {
  if (!walletChainId.value) return false
  return walletChainId.value.toLowerCase() === CURRENT_NETWORK.chainId.toLowerCase()
})
const syncableHours = computed(() => {
  if (!chainData.value) return 0
  return Math.max(Number((Number(chainData.value.dbNormalHours || 0)).toFixed(2)), 0)
})
const isHoursConsistent = computed(() => {
  if (!chainData.value) return false
  const chainTotal = Number(chainData.value.chainTotalHours || 0)
  const dbTotal = Number(chainData.value.dbTotalHours || 0)
  return Math.abs(chainTotal - dbTotal) < 0.000001
})

const getHoursWidth = () => {
  if (!chainData.value) return 150
  return Math.max(String(chainData.value.chainTotalHours || 0).length * 50, 80)
}

const formatAddress = (address) => {
  if (!address) return '未连接'
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

const formatTimestamp = (timestamp) => {
  if (!timestamp) return '未知'
  return new Date(timestamp * 1000).toLocaleString('zh-CN')
}

const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制到剪贴板')).catch(() => ElMessage.error('复制失败'))
}

const isValidAddress = (value) => /^0x[a-fA-F0-9]{40}$/.test((value || '').trim())

// 刷新当前钱包环境：检查是否安装 MetaMask，并读取当前账户和当前链 ID。
// 这一步主要用于页面初始化和手动刷新，为后续写链做准备。
const refreshWalletState = async () => {
  walletInstalled.value = checkMetaMaskInstalled()
  if (!walletInstalled.value) {
    walletAddress.value = ''
    walletChainId.value = ''
    return
  }

  try {
    walletChainId.value = await getCurrentChainId()
  } catch (error) {
    walletChainId.value = ''
  }

  try {
    walletAddress.value = await getMetaMaskAddress()
  } catch (error) {
    walletAddress.value = ''
  }
}

// 管理员点击“连接 MetaMask”后的入口。
// 连接成功后，前端才能获得签名账户，后续 addActivity / addReplenishActivity
// 这类合约写入方法都需要该账户在钱包中确认签名。
const handleConnectMetaMask = async () => {
  walletConnecting.value = true
  try {
    walletInstalled.value = checkMetaMaskInstalled()
    if (!walletInstalled.value) {
      throw new Error('请先安装 MetaMask 浏览器插件')
    }

    walletAddress.value = await connectMetaMask()
    walletChainId.value = await getCurrentChainId()
    ElMessage.success('MetaMask 钱包连接成功')
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '连接 MetaMask 失败')
  } finally {
    walletConnecting.value = false
  }
}

const loadVolunteers = async () => {
  if (volunteerList.value.length > 0) return
  const res = await request({ url: '/volunteer/list', method: 'get' })
  if (res.code === 200) {
    volunteerList.value = res.data || []
  }
}

const loadBlockchainStats = async () => {
  try {
    const res = await getBlockchainStats()
    if (res.code === 200) blockchainStats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 读取当前系统配置的链信息与合约地址。
// 答辩时可以说明：页面发交易前，必须先知道要连接哪条链、哪个合约。
const viewContractInfo = async () => {
  try {
    const res = await getContractInfo()
    if (res.code === 200) {
      contractInfo.value = res.data
      activeContractAddress.value = getStoredContractAddress() || res.data?.contractAddress || ''
      contractAddressInput.value = activeContractAddress.value
      showContractDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取合约信息失败')
  }
}

// 根据下拉框中选择的志愿者，获取其数据库信息。
// 链上身份不是用 volunteerId，而是使用该志愿者绑定的钱包地址。
const getCurrentVolunteer = () => {
  const volunteer = volunteerList.value.find(v => v.id === queryVolunteer.value)
  if (!volunteer) throw new Error('未找到志愿者信息')
  if (!volunteer.walletAddress) throw new Error('该志愿者未绑定钱包地址，无法上链')
  return volunteer
}

// 发交易前的统一校验：
// 1. 必须已经配置好合约地址
// 2. 必须能够从 MetaMask 拿到当前签名账户
const ensureWalletReady = async () => {
  if (!activeContractAddress.value) {
    throw new Error('请先部署合约或配置合约地址')
  }
  return getMetaMaskAddress()
}

// 查询功能本身不写链，而是先对比数据库中的业务数据和链上数据是否一致。
// 管理员通常先查询，再决定是否执行“同步时长上链”或“补录时长上链”。
const handleQuery = async () => {
  if (!queryVolunteer.value) {
    ElMessage.warning('请选择志愿者')
    return
  }
  querying.value = true
  generatedCertificate.value = null
  try {
    const res = await getTotalHours(queryVolunteer.value)
    chainData.value = res.data
  } catch (error) {
    ElMessage.error('查询失败，请重试')
  } finally {
    querying.value = false
  }
}

// 正常服务时长上链的核心入口。
// 这里会调用 sendAddActivityTransaction，底层实际执行的是合约 addActivity 方法，
// 并通过 MetaMask 弹窗完成交易签名与广播。
const handleSync = async () => {
  writingActivity.value = true
  try {
    const volunteer = getCurrentVolunteer()
    await ensureWalletReady()
    if (syncableHours.value <= 0) {
      throw new Error('当前没有需要同步的正常服务时长')
    }

    // 将“数据库中待同步的正常服务时长”写入到目标合约。
    const receipt = await sendAddActivityTransaction({
      activityName: '志愿服务时长同步',
      volunteerAddress: volunteer.walletAddress,
      hours: syncableHours.value
    })
    lastActionReceipt.value = receipt
    lastActionType.value = '时长同步上链'
    ElMessage.success('MetaMask 交易已确认并写入区块链')
    await handleQuery()
    await loadBlockchainStats()
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '上链失败')
  } finally {
    writingActivity.value = false
  }
}

// 打开补录上链对话框。
// 补录记录与正常记录分开，是为了在链上保留不同业务类型的标记。
const handleReplenish = () => {
  if (!chainData.value?.walletAddress) {
    ElMessage.warning('请先查询链上数据')
    return
  }
  replenishForm.value = { activityName: '补录服务时长', hours: 0.5 }
  showReplenishDialog.value = true
}

// 补录时长上链的核心入口。
// 这里调用的是 sendAddReplenishActivityTransaction，底层对应合约 addReplenishActivity 方法。
const confirmReplenish = async () => {
  writingReplenish.value = true
  try {
    const volunteer = getCurrentVolunteer()
    await ensureWalletReady()
    const receipt = await sendAddReplenishActivityTransaction({
      activityName: replenishForm.value.activityName,
      volunteerAddress: volunteer.walletAddress,
      hours: replenishForm.value.hours
    })
    lastActionReceipt.value = receipt
    lastActionType.value = '补录时长上链'
    showReplenishDialog.value = false
    ElMessage.success('补录时长已通过 MetaMask 上链')
    await handleQuery()
    await loadBlockchainStats()
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '补录上链失败')
  } finally {
    writingReplenish.value = false
  }
}

// 打开证书生成对话框时，证书中的服务时长直接取当前查询到的链上结果，
// 这样可以说明证书是“以链上可信数据为依据”生成的。
const openCertificateDialog = () => {
  if (!chainData.value?.walletAddress) {
    ElMessage.warning('请先查询链上数据')
    return
  }
  const volunteer = volunteerList.value.find(v => v.id === queryVolunteer.value)
  certificateForm.value = {
    name: volunteer?.realName || chainData.value.volunteerName || '',
    hours: chainData.value.chainTotalHours || 0,
    replenishHours: chainData.value.chainReplenishHours || 0,
    remark: ''
  }
  chainData.value.volunteerName = certificateForm.value.name
  showCertificateDialog.value = true
}

// 生成证书这一步不是写链，而是调用后端证书接口。
// 后端会基于链上时长生成证书编号、证书哈希并保存到证书库，用于后续验真。
const generateCertificate = async () => {
  try {
    const res = await generateCert({ volunteerId: queryVolunteer.value, activityCount: 0, remark: certificateForm.value.remark })
    if (res.code === 200) {
      generatedCertificate.value = res.data
      ElMessage.success('证书已生成并保存到证书库')
      showCertificateDialog.value = false
      await downloadTemplate()
    }
  } catch (error) {
    ElMessage.error('生成证书失败')
  }
}

const downloadTemplate = async () => {
  if (!chainData.value) return
  const wasHidden = !showTemplatePreview.value
  if (wasHidden) {
    showTemplatePreview.value = true
    await new Promise(resolve => setTimeout(resolve, 100))
  }
  const canvas = await html2canvas(templateRef.value, { scale: 2, backgroundColor: '#ffffff', logging: false, useCORS: true })
  const link = document.createElement('a')
  link.download = `志愿服务证书_${chainData.value.volunteerName || '志愿者'}_${Date.now()}.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
  if (wasHidden) showTemplatePreview.value = false
}

const exportTemplateImage = async () => {
  await downloadTemplate()
}

// 保存 Remix 部署后的合约地址到本地缓存。
// 页面后续发交易时，会使用这个地址去实例化目标合约。
const handleSaveContractAddress = () => {
  const address = (contractAddressInput.value || '').trim()
  if (!isValidAddress(address)) {
    ElMessage.error('请输入有效的合约地址')
    return
  }
  saveContractAddress(address)
  activeContractAddress.value = address
  contractAddressInput.value = address
  ElMessage.success('Remix 部署的合约地址已保存')
}

const handleClearContractAddress = () => {
  clearStoredContractAddress()
  activeContractAddress.value = ''
  contractAddressInput.value = ''
  ElMessage.success('已清空本地保存的合约地址')
}

// 页面初始化：
// 1. 检查钱包环境
// 2. 加载志愿者列表与区块链统计
// 3. 读取当前合约配置
onMounted(async () => {
  await refreshWalletState()
  await loadVolunteers()
  await loadBlockchainStats()
  try {
    const res = await getContractInfo()
    if (res.code === 200) {
      contractInfo.value = res.data
      activeContractAddress.value = getStoredContractAddress() || res.data?.contractAddress || ''
      contractAddressInput.value = activeContractAddress.value
    }
  } catch (error) {
    console.error('初始化合约信息失败:', error)
  }
})
</script>

<style scoped>
.blockchain-page { padding: 24px; background: #f5f7fa; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 24px; }
.header-top { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.header-actions { display: flex; gap: 12px; }
.page-header h2 { font-size: 28px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 12px; margin: 0; }
.subtitle { color: #909399; margin: 8px 0 0 44px; font-size: 14px; }
.wallet-card { margin-bottom: 24px; border-radius: 12px; overflow: hidden; }
.stats-row { margin-bottom: 24px; }
.stat-card { background: white; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); height: 100%; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; font-size: 28px; }
.icon-blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.icon-pink { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.icon-cyan { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.icon-green { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.stat-content { flex: 1; }
.stat-value { font-size: 28px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #909399; }
.deploy-card, .query-card, .template-card, .receipt-card { margin-bottom: 24px; border-radius: 12px; overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; }
.card-header span { display: flex; align-items: center; gap: 8px; }
.chain-data-result { margin-top: 20px; }
.data-actions, .template-actions { margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap; }
.break-all, .address-text { word-break: break-all; }
.template-preview { margin: 24px 0; display: flex; justify-content: center; }
.certificate-container { width: 100%; max-width: 900px; }
.certificate-border-rainbow { background: linear-gradient(90deg, #ff6b6b 0%, #feca57 16.67%, #48dbfb 33.33%, #1dd1a1 50%, #5f27cd 66.67%, #ee5a6f 83.33%, #ff6b6b 100%); padding: 6px; border-radius: 16px; }
.certificate-content { background: linear-gradient(to bottom, #ffffff 0%, #f8f9fa 100%); border-radius: 8px; padding: 60px 80px; min-height: 500px; }
.certificate-header { text-align: center; margin-bottom: 50px; }
.certificate-title { font-size: 48px; font-weight: 700; color: #303133; margin: 0 0 12px 0; letter-spacing: 8px; }
.certificate-subtitle { font-size: 16px; color: #909399; letter-spacing: 4px; margin: 0; }
.recipient-section { text-align: center; margin-bottom: 40px; }
.recipient-label { font-size: 18px; color: #606266; margin: 0 0 12px 0; }
.recipient-name { font-size: 36px; font-weight: 700; color: #303133; margin: 0; }
.content-section { text-align: center; margin-bottom: 40px; }
.content-text { font-size: 20px; line-height: 1.8; color: #606266; margin: 12px 0; }
.hours-highlight { display: flex; align-items: baseline; justify-content: center; gap: 12px; margin: 24px 0; }
.hours-unit { font-size: 32px; font-weight: 600; color: #606266; }
.blockchain-info { margin-top: 30px; padding: 20px; background: rgba(64, 158, 255, 0.05); border-radius: 12px; }
</style>