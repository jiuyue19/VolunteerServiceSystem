<template>
  <div class="certificate-page">
    <div class="page-header">
      <div class="header-left">
        <h2><el-icon><Document /></el-icon> 服务证书</h2>
        <p class="subtitle">基于区块链的志愿服务时长认证</p>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleWalletCommand">
          <el-button :type="walletConnected ? 'success' : 'primary'" class="wallet-button">
            <el-icon><Wallet /></el-icon>
            <span v-if="walletConnected">{{ formatAddress(walletAddress) }}</span>
            <span v-else>钱包管理</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="!walletConnected" command="connect">
                <el-icon><Link /></el-icon> 连接我的钱包
              </el-dropdown-item>
              <template v-else>
                <el-dropdown-item disabled>
                  <div class="wallet-info-item">
                    <span class="label">钱包地址:</span>
                    <el-tag size="small" type="success">{{ formatAddress(walletAddress) }}</el-tag>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item disabled>
                  <div class="wallet-info-item">
                    <span class="label">网络:</span>
                    <span>{{ networkName }}</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="viewData">
                  <el-icon><View /></el-icon> 查看链上数据
                </el-dropdown-item>
                <el-dropdown-item command="disconnect">
                  <el-icon><SwitchButton /></el-icon> 断开连接
                </el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ volunteerStats.totalHours }}</div>
            <div class="stat-label">累计服务时长（小时）</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><TrophyBase /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ volunteerStats.totalPoints }}</div>
            <div class="stat-label">累计积分</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ volunteerStats.activityCount }}</div>
            <div class="stat-label">参与活动数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ volunteerStats.certificateCount || 0 }}</div>
            <div class="stat-label">已获证书</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 功能入口卡片 -->
    <el-row :gutter="20" class="action-cards-row">
      <el-col :xs="24" :sm="12" :md="8">
        <div class="action-card action-card-preview" @click="viewPreview">
          <div class="action-card-icon-wrapper">
            <el-icon class="action-card-icon"><View /></el-icon>
          </div>
          <div class="action-card-content">
            <h3 class="action-card-title">预览证书</h3>
            <p class="action-card-desc">连接您绑定的钱包查看链上数据并预览证书</p>
          </div>
          <div class="action-card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8">
        <div class="action-card action-card-download" @click="gotoDownload">
          <div class="action-card-icon-wrapper">
            <el-icon class="action-card-icon"><Download /></el-icon>
          </div>
          <div class="action-card-content">
            <h3 class="action-card-title">证书下载</h3>
            <p class="action-card-desc">查询并下载您的志愿服务证书</p>
          </div>
          <div class="action-card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8">
        <div class="action-card action-card-verify" @click="gotoVerify">
          <div class="action-card-icon-wrapper">
            <el-icon class="action-card-icon"><CircleCheck /></el-icon>
          </div>
          <div class="action-card-content">
            <h3 class="action-card-title">证书验伪</h3>
            <p class="action-card-desc">验证志愿服务证书的真实性</p>
          </div>
          <div class="action-card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷提示（未连接钱包时显示） -->
    <el-alert
      v-if="!walletConnected"
      title="请连接钱包"
      type="warning"
      show-icon
      :closable="false"
      class="wallet-alert">
      <template #default>
        <p>请点击右上角的"钱包管理"按钮连接您绑定的钱包地址以查看链上数据和生成证书</p>
        <p style="margin-top: 8px; font-size: 13px; color: #E6A23C;">
          注意：只能连接您在系统中绑定的钱包地址。如果您还未绑定钱包地址，请先在注册页面或个人信息中进行绑定。
        </p>
      </template>
    </el-alert>

    <!-- 链上数据 -->
    <el-card v-if="chainData" class="chain-data-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><DataLine /></el-icon> 区块链数据</span>
          <el-tag type="success" effect="dark">已上链</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="链上总时长">
          <el-tag type="primary" size="large">{{ chainData.chainTotalHours }} 小时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="链上补录时长">
          <el-tag type="warning" size="large">{{ chainData.chainReplenishHours }} 小时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据库总时长">
          <el-tag type="info" size="large">{{ chainData.dbTotalHours }} 小时</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据一致性">
          <el-tag :type="chainData.chainTotalHours === chainData.dbTotalHours ? 'success' : 'danger'">
            {{ chainData.chainTotalHours === chainData.dbTotalHours ? '一致' : '不一致' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 证书预览与生成 -->
    <el-card v-if="showCertificate" class="certificate-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Medal /></el-icon> 服务证书预览</span>
        </div>
      </template>
      
      <div class="certificate-preview" ref="certificateRef">
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
                      <!-- 中心圆 -->
                      <circle cx="50" cy="50" r="8" fill="url(#lotusCenter)"/>
                      <!-- 莲花花瓣 -->
                      <path d="M50 30 Q45 35 50 45 Q55 35 50 30" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M65 38 Q60 43 55 48 Q63 50 65 38" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M70 52 Q65 52 60 55 Q68 60 70 52" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M65 66 Q63 60 55 58 Q60 68 65 66" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M50 72 Q50 67 50 57 Q50 67 50 72" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M35 66 Q37 60 45 58 Q40 68 35 66" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M30 52 Q35 52 40 55 Q32 60 30 52" fill="url(#lotusGradient)" opacity="0.9"/>
                      <path d="M35 38 Q40 43 45 48 Q37 50 35 38" fill="url(#lotusGradient)" opacity="0.9"/>
                      <!-- 莲花心形顶部 -->
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
                  <p class="recipient-name">{{ volunteerStats.name || '志愿者' }}</p>
                </div>

                <div class="content-section">
                  <p class="content-text">
                    在志愿服务活动中表现优异，累计服务时长达到
                  </p>
                  <p class="hours-highlight">
                    <span class="hours-number">{{ chainData?.chainTotalHours || 0 }}</span>
                    <span class="hours-unit">小时</span>
                  </p>
                  <p class="content-text">
                    参与志愿活动 <strong>{{ volunteerStats.activityCount }}</strong> 次，
                    其中补录时长 <strong>{{ chainData?.chainReplenishHours || 0 }}</strong> 小时。
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
                      <!-- 左侧区块 -->
                      <rect x="2" y="8" width="6" height="6" rx="1" fill="url(#blockGradient)" opacity="0.9"/>
                      <rect x="3" y="9" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="3" y="11" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <!-- 中间区块 -->
                      <rect x="9" y="6" width="6" height="6" rx="1" fill="url(#blockGradient)" opacity="0.9"/>
                      <rect x="10" y="7" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="10" y="9" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <!-- 右侧区块 -->
                      <rect x="16" y="10" width="6" height="6" rx="1" fill="url(#blockGradient)" opacity="0.9"/>
                      <rect x="17" y="11" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <rect x="17" y="13" width="4" height="1" fill="#fff" opacity="0.3"/>
                      <!-- 连接线 -->
                      <line x1="8" y1="11" x2="9" y2="9" stroke="url(#blockchainGradient)" stroke-width="1.5" opacity="0.8"/>
                      <line x1="15" y1="9" x2="16" y2="13" stroke="url(#blockchainGradient)" stroke-width="1.5" opacity="0.8"/>
                      <!-- 节点 -->
                      <circle cx="8" cy="11" r="1.5" fill="url(#blockchainGradient)"/>
                      <circle cx="9" cy="9" r="1.5" fill="url(#blockchainGradient)"/>
                      <circle cx="15" cy="9" r="1.5" fill="url(#blockchainGradient)"/>
                      <circle cx="16" cy="13" r="1.5" fill="url(#blockchainGradient)"/>
                    </svg>
                    <span>区块链认证</span>
                  </div>
                  <div class="blockchain-info">
                    <p><strong>钱包地址：</strong>{{ formatAddress(chainData?.walletAddress || walletAddress) }}</p>
                    <p><strong>证书编号：</strong>{{ certificateId }}</p>
                    <p><strong>证书哈希：</strong><span class="hash-text">{{ certificateHash }}</span></p>
                    <p><strong>生成时间：</strong>{{ currentDate }}</p>
                  </div>
                </div>
              </div>

              <!-- 底部签章 -->
              <div class="certificate-footer">
                <div class="seal-section">
                  <div class="seal">
                    <div class="seal-inner">志愿服务<br/>管理中心</div>
                  </div>
                  <p class="seal-date">{{ currentDate }}</p>
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

      <div class="certificate-tips">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon>
          <p>• 证书数据来源于区块链，确保真实可信</p>
          <p>• 请先连接钱包并查看链上数据后再生成证书</p>
          <p>• 证书可下载为 PNG 图片格式保存</p>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Document, Clock, TrophyBase, Calendar, Wallet, WalletFilled, Link, View, 
  SwitchButton, DataLine, Medal, Download, Lock, ArrowDown, Star, Search,
  Tickets, CircleCheck, RefreshRight, ArrowRight
} from '@element-plus/icons-vue'
import { getTotalHours, bindWallet, getChainDataByWallet } from '@/api/blockchain'
import { queryCertificates, verifyCertificateEnhanced } from '@/api/certificate'
import { getInfo } from '@/api/volunteer'
import { getVolunteerStats } from '@/api/stats'
import html2canvas from 'html2canvas'

const router = useRouter()

const chainData = ref(null)
const walletConnected = ref(false)
const walletAddress = ref('')
const networkName = ref('Ethereum Mainnet')
const loadingChainData = ref(false)
const certificateRef = ref(null)
const myCertificates = ref([])
const verifying = ref(false)
const verifyResult = ref(null)
const verifyForm = ref({
  volunteerName: '',
  certificateNo: '',
  certificateHash: ''
})
const showCertificate = ref(false) // 控制证书预览的显示

// 志愿者统计数据（从后端获取）
const volunteerStats = ref({
  name: '',
  totalHours: 0,
  totalPoints: 0,
  activityCount: 0,
  certificateCount: 0
})

// 证书编号
const certificateId = computed(() => {
  if (!walletAddress.value) return 'CERT-XXXX-XXXX'
  return `CERT-${walletAddress.value.slice(2, 6).toUpperCase()}-${Date.now().toString().slice(-4)}`
})

// 证书哈希（前端展示用，实际由后端生成）
const certificateHash = computed(() => {
  if (!chainData.value) return '********************************'
  // 前端仅展示示例，实际哈希由后端SHA-256生成
  return '示例哈希 (由后端生成)'
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '未连接'
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

// 连接钱包 - 从数据库获取志愿者的钱包地址
const connectWallet = async () => {
  try {
    console.log('[Certificate] 开始连接钱包')
    
    // 从后端API获取当前登录志愿者的信息（使用JWT token）
    const res = await getInfo()
    
    console.log('[Certificate] 获取志愿者信息响应:', res)
    
    if (res.code === 200 && res.data) {
      const bindedWalletAddress = res.data.walletAddress
      
      console.log('[Certificate] 绑定的钱包地址:', bindedWalletAddress)
      
      // 检查钱包地址是否为空
      if (!bindedWalletAddress || bindedWalletAddress.trim() === '') {
        console.warn('[Certificate] 钱包地址为空')
        ElMessage.warning('您还未绑定钱包地址，请先在注册页面或个人信息中绑定')
        return
      }
      
      // 验证钱包地址格式
      const walletRegex = /^0x[a-fA-F0-9]{40}$/
      if (!walletRegex.test(bindedWalletAddress)) {
        console.error('[Certificate] 钱包地址格式不正确:', bindedWalletAddress)
        ElMessage.error('绑定的钱包地址格式不正确，请联系管理员重新绑定')
        return
      }
      
      // 连接到绑定的钱包地址
      walletAddress.value = bindedWalletAddress
      walletConnected.value = true
      networkName.value = 'Ethereum Mainnet'
      
      // 注意：不在这里重新加载统计数据
      // 统计数据应该保持独立，只在页面加载时获取一次
      // 钱包连接只用于查询链上数据，不影响顶部统计
      
      console.log('[Certificate] 钱包连接成功')
      ElMessage.success('已连接到您绑定的钱包地址: ' + bindedWalletAddress.slice(0, 6) + '...' + bindedWalletAddress.slice(-4))
    } else {
      console.warn('[Certificate] API返回错误:', res.message || '未知错误')
      ElMessage.warning('您还未绑定钱包地址，请先在注册页面或个人信息中绑定')
    }
  } catch (error) {
    console.error('[Certificate] 加载钱包地址失败:', error)
    ElMessage.error('加载钱包地址失败: ' + (error.message || '请稍后重试'))
  }
}

// 保存钱包地址到后端（该功能在此页面不使用，因为只能使用绑定的地址）
const saveWalletAddress = async (address) => {
  try {
    // 获取当前志愿者信息
    const res = await getInfo()
    if (res.code === 200 && res.data && res.data.id) {
      await bindWallet({ 
        volunteerId: res.data.id, 
        walletAddress: address 
      })
      ElMessage.success('钱包地址已保存')
    }
  } catch (error) {
    console.error('保存钱包地址失败:', error)
    ElMessage.warning('钱包地址保存失败，但仍可使用')
  }
}

// 断开钱包
const disconnectWallet = () => {
  walletConnected.value = false
  walletAddress.value = ''
  chainData.value = null
  ElMessage.success('钱包已断开')
}

// 获取网络名称（保留用于显示）
const getNetworkName = (chainId) => {
  const networks = {
    '0x1': 'Ethereum Mainnet',
    '0x5': 'Goerli Testnet',
    '0xaa36a7': 'Sepolia Testnet',
    '0x89': 'Polygon Mainnet'
  }
  return networks[chainId] || '未知网络'
}

// 处理钱包下拉菜单命令
const handleWalletCommand = (command) => {
  switch (command) {
    case 'connect':
      connectWallet()
      break
    case 'viewData':
      viewChainData()
      break
    case 'disconnect':
      disconnectWallet()
      break
  }
}

// 查看链上数据
const viewChainData = async () => {
  if (!walletConnected.value) {
    ElMessage.warning('请先连接钱包')
    return
  }
  
  if (!walletAddress.value) {
    ElMessage.warning('未找到钱包地址')
    return
  }
  
  console.log('[Certificate] 开始查询链上数据')
  console.log('[Certificate] 钱包地址:', walletAddress.value)
  
  loadingChainData.value = true
  try {
    console.log('[Certificate] 调用 API: getChainDataByWallet')
    console.log('[Certificate] 参数: walletAddress =', walletAddress.value)
    console.log('[Certificate] API URL: /api/blockchain/chain-data-by-wallet?walletAddress=' + walletAddress.value)
    
    // 使用已连接的钱包地址查询链上数据
    const res = await getChainDataByWallet(walletAddress.value)
    
    console.log('[Certificate] API响应:', res)
    console.log('[Certificate] 响应 code:', res.code)
    console.log('[Certificate] 响应 message:', res.message)
    console.log('[Certificate] 响应 data:', res.data)
    
    if (res.code === 200 && res.data) {
      chainData.value = res.data
      
      // 区块链节点不可用时仍然使用默认值，不再弹出提示
      if (res.data.blockchainError) {
        // 保留结构占位，不进行任何提示
      }
      
      // 注意：不再更新 volunteerStats
      // 顶部的4个统计标签应该保持独立，只使用 loadVolunteerStats 的数据
      // 链上数据仅用于证书预览部分
      
      // 显示证书预览
      showCertificate.value = true
      
      ElMessage.success('链上数据加载成功')
    } else {
      console.error('[Certificate] API返回错误 code:', res.code)
      console.error('[Certificate] API返回错误 message:', res.message)
      ElMessage.error('获取链上数据失败: ' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('[Certificate] 获取链上数据失败:', error)
    console.error('[Certificate] 错误详情:', {
      message: error.message,
      response: error.response,
      request: error.request,
      config: error.config,
      stack: error.stack
    })
    
    // 尝试从 error.response 中获取更多信息
    if (error.response) {
      console.error('[Certificate] HTTP响应状态:', error.response.status)
      console.error('[Certificate] HTTP响应数据:', error.response.data)
      ElMessage.error('获取链上数据失败: ' + (error.response.data?.message || error.message || '服务器错误'))
    } else if (error.request) {
      console.error('[Certificate] 请求已发出但未收到响应')
      ElMessage.error('获取链上数据失败: 网络超时或服务器无响应')
    } else {
      ElMessage.error('获取链上数据失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loadingChainData.value = false
  }
}

// 查看预览（展开链上数据和证书）
const viewPreview = async () => {
  if (!walletConnected.value) {
    // 先连接钱包（从数据库加载钱包地址）
    await connectWallet()
    if (!walletConnected.value) {
      return
    }
  }
  // 加载链上数据并展开证书预览
  await viewChainData()
}

// 跳转到证书下载页面
const gotoDownload = () => {
  router.push('/volunteer/certificate-download')
}

// 跳转到证书验伪页面
const gotoVerify = () => {
  window.open('/certificate-verify', '_blank')
}

// 查询我的证书
const queryMyCertificates = async () => {
  if (!walletConnected.value) {
    ElMessage.warning('请先连接钱包')
    return
  }
  
  try {
    const res = await queryCertificates({
      walletAddress: walletAddress.value
    })
    
    if (res.code === 200) {
      myCertificates.value = res.data
      volunteerStats.value.certificateCount = res.data.filter(c => c.status === 'issued').length
      ElMessage.success(`查询到 ${res.data.length} 张证书`)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询证书失败:', error)
    ElMessage.error('查询证书失败')
  }
}

// 证书验伪（增强版本 - 姓名+编号+哈希）
const handleVerifyEnhanced = async () => {
  if (!verifyForm.value.volunteerName || !verifyForm.value.certificateNo || !verifyForm.value.certificateHash) {
    ElMessage.warning('请填写完整的验证信息：姓名、证书编号、证书哈希')
    return
  }
  
  verifying.value = true
  verifyResult.value = null
  
  try {
    const res = await verifyCertificateEnhanced(verifyForm.value)
    verifyResult.value = res.data
    
    if (res.data.valid) {
      ElMessage.success('✓ 证书验证成功！已通过三重验证和链上数据校验')
    } else {
      ElMessage.error('✗ 证书验证失败：' + res.data.message)
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证失败，请检查网络连接')
  } finally {
    verifying.value = false
  }
}

// 重置验证表单
const resetVerifyForm = () => {
  verifyForm.value = {
    volunteerName: '',
    certificateNo: '',
    certificateHash: ''
  }
  verifyResult.value = null
}

// 生成证书
const generateCertificate = async () => {
  if (!walletConnected.value) {
    ElMessage.warning('请先连接钱包')
    return
  }
  
  if (!chainData.value) {
    ElMessage.warning('请先查看链上数据')
    return
  }
  
  try {
    ElMessage.info('正在生成证书，请稍候...')
    
    // 等待DOM更新
    await new Promise(resolve => setTimeout(resolve, 100))
    
    const element = certificateRef.value
    if (!element) {
      throw new Error('证书元素未找到')
    }
    
    // 使用 html2canvas 生成图片
    const canvas = await html2canvas(element, {
      scale: 2,
      backgroundColor: '#ffffff',
      logging: false,
      useCORS: true
    })
    
    // 转换为图片并下载
    const link = document.createElement('a')
    link.download = `志愿服务证书_${certificateId.value}_${Date.now()}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    
    ElMessage.success('证书生成成功！')
  } catch (error) {
    console.error('生成证书失败:', error)
    ElMessage.error('证书生成失败，请重试')
  }
}

// 加载志愿者统计数据（与个人中心使用相同的API）
const loadVolunteerStats = async () => {
  try {
    console.log('[Certificate] 开始加载统计数据')
    
    // 获取志愿者基本信息
    const infoRes = await getInfo()
    if (infoRes.code === 200 && infoRes.data) {
      const volunteerId = infoRes.data.id
      volunteerStats.value.name = infoRes.data.realName || '志愿者'
      
      console.log('[Certificate] 志愿者ID:', volunteerId)
      console.log('[Certificate] 志愿者姓名:', volunteerStats.value.name)
      
      // 调用统一的统计接口（与个人中心相同）
      const statsRes = await getVolunteerStats(volunteerId)
      if (statsRes.code === 200 && statsRes.data) {
        const statsData = statsRes.data
        
        console.log('[Certificate] 统计数据:', statsData)
        
        // 使用与个人中心相同的数据源
        volunteerStats.value.totalHours = statsData.totalHours || 0
        volunteerStats.value.totalPoints = statsData.totalPoints || 0
        volunteerStats.value.activityCount = statsData.activityCount || 0
        volunteerStats.value.certificateCount = statsData.certificateCount || 0
        
        console.log('[Certificate] 累计时长:', volunteerStats.value.totalHours, '小时')
        console.log('[Certificate] 累计积分:', volunteerStats.value.totalPoints)
        console.log('[Certificate] 参与活动数:', volunteerStats.value.activityCount)
        console.log('[Certificate] 已获证书:', volunteerStats.value.certificateCount)
      } else {
        console.warn('[Certificate] 统计数据获取失败')
      }
    }
  } catch (error) {
    console.error('[Certificate] 加载统计数据失败:', error)
  }
}

// 组件挂载时加载志愿者统计数据，但不自动连接钱包
onMounted(async () => {
  // 移除自动连接钱包的逻辑
  // 用户需要手动点击"钱包管理"按钮连接钱包
  
  // 加载统计数据（使用与个人中心相同的API）
  await loadVolunteerStats()
})
</script>

<style scoped>
.certificate-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
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

/* 钱包按钮 */
.wallet-button {
  font-size: 14px;
  padding: 10px 20px;
  height: auto;
}

/* 钱包下拉菜单 */
.wallet-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.wallet-info-item .label {
  color: #909399;
  font-size: 13px;
}

/* 警告提示 */
.wallet-alert {
  margin-bottom: 24px;
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


/* 功能入口卡片 */
.action-cards-row {
  margin-bottom: 24px;
}

.action-card {
  position: relative;
  cursor: pointer;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: left;
}

.action-card:hover::before {
  transform: scaleX(1);
}

.action-card:hover {
  transform: translateY(-8px);
}

.action-card-preview:hover {
  box-shadow: 0 12px 40px rgba(17, 153, 142, 0.25);
}

.action-card-download:hover {
  box-shadow: 0 12px 40px rgba(74, 0, 224, 0.25);
}

.action-card-verify:hover {
  box-shadow: 0 12px 40px rgba(255, 107, 107, 0.25);
}

.action-card-preview {
  border-top: 4px solid transparent;
}

.action-card-preview::before {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.action-card-download {
  border-top: 4px solid transparent;
}

.action-card-download::before {
  background: linear-gradient(90deg, #4A00E0 0%, #8E2DE2 100%);
}

.action-card-verify {
  border-top: 4px solid transparent;
}

.action-card-verify::before {
  background: linear-gradient(90deg, #FF6B6B 0%, #FF8E53 100%);
}

.action-card-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-card-preview .action-card-icon-wrapper {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.action-card-download .action-card-icon-wrapper {
  background: linear-gradient(135deg, #4A00E0 0%, #8E2DE2 100%);
}

.action-card-verify .action-card-icon-wrapper {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.action-card:hover .action-card-icon-wrapper {
  transform: scale(1.1) rotate(5deg);
}

.action-card-icon {
  font-size: 32px;
  color: white;
}

.action-card-content {
  flex: 1;
  text-align: left;
}

.action-card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  transition: color 0.3s;
}

.action-card-preview:hover .action-card-title {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.action-card-download:hover .action-card-title {
  background: linear-gradient(90deg, #4A00E0 0%, #8E2DE2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.action-card-verify:hover .action-card-title {
  background: linear-gradient(90deg, #FF6B6B 0%, #FF8E53 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.action-card-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.action-card-arrow {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.action-card-arrow .el-icon {
  font-size: 16px;
  color: #909399;
  transition: all 0.4s;
}

.action-card-preview:hover .action-card-arrow {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  transform: translateX(4px);
}

.action-card-download:hover .action-card-arrow {
  background: linear-gradient(135deg, #4A00E0 0%, #8E2DE2 100%);
  transform: translateX(4px);
}

.action-card-verify:hover .action-card-arrow {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  transform: translateX(4px);
}

.action-card:hover .action-card-arrow .el-icon {
  color: white;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .action-cards-row .el-col {
    margin-bottom: 16px;
  }
  
  .action-card {
    padding: 20px;
  }
  
  .action-card-icon-wrapper {
    width: 56px;
    height: 56px;
  }
  
  .action-card-icon {
    font-size: 28px;
  }
  
  .action-card-title {
    font-size: 16px;
  }
  
  .action-card-desc {
    font-size: 12px;
  }
}

/* 卡片通用样式 */
.chain-data-card,
.certificate-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 证书预览 */
.certificate-preview {
  margin: 24px 0;
  display: flex;
  justify-content: center;
}

.certificate-container {
  width: 100%;
  max-width: 900px;
}

/* 彩虹渐变边框 */
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
  border-radius: 12px;
  padding: 60px 80px;
  position: relative;
  min-height: 700px;
}

/* 证书头部 */
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

/* 证书主体 */
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
  padding: 20px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 12px;
}

.hours-number {
  font-size: 72px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 50%, #feca57 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-right: 8px;
}

.hours-unit {
  font-size: 28px;
  color: #606266;
}

/* 区块链验证 */
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

/* 证书底部 */
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

/* 装饰角 */
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

/* 证书提示 */
.certificate-tips {
  margin-top: 24px;
}

.certificate-tips :deep(.el-alert__description) p {
  margin: 4px 0;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 768px) {
  .certificate-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .certificate-content {
    padding: 40px 30px;
  }
  
  .certificate-title {
    font-size: 32px;
    letter-spacing: 4px;
  }
  
  .recipient-name {
    font-size: 28px;
  }
  
  .hours-number {
    font-size: 48px;
  }
  
  .hours-unit {
    font-size: 20px;
  }
  
  .content-text {
    font-size: 16px;
  }
}
</style>