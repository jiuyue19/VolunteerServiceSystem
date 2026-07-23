<template>
  <div class="my-applications">
    <div class="page-header">
      <div class="header-top">
        <h2>我的申请</h2>
        <div class="header-actions">
          <el-button type="primary" @click="showReplenishDialog = true">
            补录申请
          </el-button>
        </div>
      </div>
      <div class="stats-summary">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalApplications }}</div>
            <div class="stat-label">总申请数</div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ participatedActivities }}</div>
            <div class="stat-label">参与活动数</div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ replenishApplications }}</div>
            <div class="stat-label">补录申请数</div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalServiceTime }}</div>
            <div class="stat-label">累计时长</div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userPoints }}</div>
            <div class="stat-label">累计积分</div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 活动申请卡片布局 -->
    <div class="applications-grid">
      <div 
        v-for="app in applications" 
        :key="app.id" 
        class="application-card"
        :class="{ 'replenish-card': app.isReplenish }"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="activity-info">
            <h3 class="activity-title">
              {{ app.activityName }}
              <el-tag v-if="app.isReplenish" type="warning" size="small" class="replenish-tag">补录</el-tag>
            </h3>
            <p class="activity-address">
              <el-icon><Location /></el-icon>
              {{ app.activityAddress }}
            </p>
          </div>
          <div class="status-badges">
            <el-tag 
              :type="getApplicationStatusType(app.status)" 
              class="status-tag"
            >
              {{ getApplicationStatusText(app.status) }}
            </el-tag>
            <el-tag 
              :type="getParticipationStatusType(app.checkinStatus)" 
              class="participation-tag"
            >
              {{ getParticipationStatusText(app.checkinStatus) }}
            </el-tag>
          </div>
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <div class="info-row">
            <div class="info-item">
              <span class="label">活动时间</span>
              <span class="value">{{ formatDate(app.activityStartTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">申请时间</span>
              <span class="value">{{ formatDateTime(app.applyTime) }}</span>
            </div>
          </div>
          
          <div class="info-row">
            <div class="info-item">
              <span class="label">服务时长</span>
              <span class="value highlight">
                <span v-if="app.serviceHours">{{ formatServiceHours(app.serviceHours) }}</span>
                <span v-else-if="app.checkinStatus === 'checkin'" class="service-time">{{ getServiceTime(app.activityId) }}</span>
                <span v-else>-</span>
              </span>
            </div>
            <div class="info-item">
              <span class="label">获得积分</span>
              <span class="value highlight">
                <span v-if="app.earnedPoints">{{ formatPoints(app.earnedPoints) }}</span>
                <span v-else>-</span>
              </span>
            </div>
          </div>
        </div>

        <!-- 卡片底部操作 -->
        <div class="card-footer">
          <el-button type="primary" size="small" @click="showActivityLog(app)" class="detail-btn">
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="applications.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无申请记录">
        <el-button type="primary" @click="showReplenishDialog = true">提交补录申请</el-button>
      </el-empty>
    </div>

    <!-- 活动日志弹窗 -->
    <el-dialog v-model="showLogDialog" :title="`${currentActivity?.activityName} - 活动日志`" width="800px">
      <div class="activity-log">
        <div class="activity-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="活动名称">{{ currentActivity?.activityName }}</el-descriptions-item>
            <el-descriptions-item label="活动地址">{{ currentActivity?.activityAddress }}</el-descriptions-item>
            <el-descriptions-item label="活动时间">{{ formatDateTime(currentActivity?.activityStartTime) }}</el-descriptions-item>
            <el-descriptions-item label="申请状态">
              <el-tag :type="getStatusType(currentActivity?.status)">{{ getStatusText(currentActivity?.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="参与状态">
              <el-tag :type="getCheckinStatusType(currentActivity?.checkinStatus)">
                {{ getCheckinStatusText(currentActivity?.checkinStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="服务时长">
              {{ currentActivity?.serviceHours ? `${currentActivity.serviceHours}小时` : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="timeline-section">
          <h3>活动时间线</h3>
          <el-timeline>
            <el-timeline-item
              v-for="log in activityLogs"
              :key="log.id"
              :timestamp="formatDateTime(log.timestamp)"
              :type="log.type"
              :icon="log.icon"
            >
              <div class="log-content">
                <div class="log-title">{{ log.title }}</div>
                <div class="log-description" v-if="log.description">{{ log.description }}</div>
                <div class="log-details" v-if="log.details">
                  <el-tag v-for="detail in log.details" :key="detail" size="small" style="margin-right: 8px;">
                    {{ detail }}
                  </el-tag>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showLogDialog = false">关闭</el-button>
        <el-button type="primary" @click="goToActivityDetail(currentActivity)">前往活动页面</el-button>
      </template>
    </el-dialog>

    <!-- 补录申请弹窗 -->
    <el-dialog v-model="showReplenishDialog" title="补录申请" width="600px">
      <el-form :model="replenishForm" :rules="replenishRules" ref="replenishFormRef" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="replenishForm.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="补录时长" prop="hours">
          <el-input-number 
            v-model="replenishForm.hours" 
            :min="0.1" 
            :max="24" 
            :step="0.1" 
            :precision="1"
            placeholder="请输入补录时长（小时）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="补录积分" prop="earnedPoints">
          <el-input-number 
            v-model="replenishForm.earnedPoints" 
            :min="1" 
            :max="1000" 
            placeholder="请输入补录积分"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="补录原因" prop="reason">
          <el-input 
            v-model="replenishForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请详细说明补录原因"
          />
        </el-form-item>
        <el-form-item label="佐证材料">
          <el-upload
            ref="uploadRef"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            :auto-upload="false"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
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
        <el-button @click="showReplenishDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReplenish" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMyApplications, getMyApplicationsWithCheckin, getMyApplicationsWithReplenish } from '@/api/activity'
import { getCheckinStatus } from '@/api/checkin'
import { getVolunteerInfo } from '@/api/volunteer'
import { applyReplenish, getMyReplenish } from '@/api/replenish'
import { calculatePoints } from '@/api/points'
import { UploadFilled, Location, View } from '@element-plus/icons-vue'

const router = useRouter()
const applications = ref([])
const loading = ref(false)
const serviceTimers = ref(new Map()) // 存储每个活动的计时器
const showLogDialog = ref(false)
const currentActivity = ref(null)
const activityLogs = ref([])
const userPoints = ref(0)

// 补录申请相关
const showReplenishDialog = ref(false)
const submitting = ref(false)
const replenishFormRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])

const replenishForm = ref({
  activityName: '',
  hours: null,
  earnedPoints: null,
  reason: '',
  evidenceFiles: ''
})

const replenishRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入补录时长', trigger: 'blur' }],
  earnedPoints: [{ required: true, message: '请输入补录积分', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入补录原因', trigger: 'blur' }]
}

// 时间格式化函数
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return '-'
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '-'
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return '-'
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (error) {
    console.error('日期时间格式化错误:', error)
    return '-'
  }
}

// 精确时长格式化函数（x.x小时格式）
const formatServiceHours = (hours) => {
  if (!hours && hours !== 0) return '-'
  try {
    const numHours = parseFloat(hours)
    if (isNaN(numHours)) return '-'
    return `${numHours.toFixed(1)}小时`
  } catch (error) {
    console.error('时长格式化错误:', error)
    return '-'
  }
}

// 积分格式化函数
const formatPoints = (points) => {
  if (!points && points !== 0) return '-'
  try {
    const numPoints = parseInt(points)
    if (isNaN(numPoints)) return '-'
    return `${numPoints}分`
  } catch (error) {
    console.error('积分格式化错误:', error)
    return '-'
  }
}

// 实时服务时长计算（返回x.x小时格式）
const getServiceTime = (activityId) => {
  const app = applications.value.find(a => a.activityId === activityId)
  if (!app || !app.checkinTime) return '-'
  
  try {
    const now = new Date()
    const start = new Date(app.checkinTime)
    if (isNaN(start.getTime())) return '-'
    
    const diffMs = now - start
    const hours = diffMs / (1000 * 60 * 60)
    return `${hours.toFixed(1)}小时`
  } catch (error) {
    console.error('实时时长计算错误:', error)
    return '-'
  }
}

// 计算统计数据
const totalApplications = computed(() => applications.value.length)

// 参与活动总数（已签到或已完成的活动）
const participatedActivities = computed(() => {
  return applications.value.filter(app => 
    app.checkinStatus === 'checkin' || app.checkinStatus === 'checkout'
  ).length
})

// 累计时长（精确到时分）
const totalServiceTime = computed(() => {
  try {
    const totalHours = applications.value
      .filter(app => {
        // 只计算已完成的活动和已通过的补录申请
        return (app.serviceHours && app.serviceHours > 0) && 
               (app.checkinStatus === 'checkout' || (app.isReplenish && app.status === '已通过'))
      })
      .reduce((total, app) => {
        const hours = parseFloat(app.serviceHours || 0)
        return total + (isNaN(hours) ? 0 : hours)
      }, 0)
    
    if (totalHours === 0) return '0小时0分'
    
    const hours = Math.floor(totalHours)
    const minutes = Math.round((totalHours - hours) * 60)
    
    if (hours === 0) {
      return `${minutes}分`
    } else if (minutes === 0) {
      return `${hours}小时`
    } else {
      return `${hours}小时${minutes}分`
    }
  } catch (error) {
    console.error('计算总时长错误:', error)
    return '0小时0分'
  }
})

// 累计积分 - 确保数据一致性
const totalPoints = computed(() => {
  try {
    const total = applications.value
      .filter(app => {
        // 只计算已完成的活动和已通过的补录申请
        return (app.earnedPoints && app.earnedPoints > 0) && 
               (app.checkinStatus === 'checkout' || (app.isReplenish && app.status === '已通过'))
      })
      .reduce((sum, app) => {
        const points = parseInt(app.earnedPoints || 0)
        return sum + (isNaN(points) ? 0 : points)
      }, 0)
    
    return total
  } catch (error) {
    console.error('计算总积分错误:', error)
    return 0
  }
})

// 补录申请数
const replenishApplications = computed(() => {
  return applications.value.filter(app => app.isReplenish).length
})

const loadApplications = async () => {
  loading.value = true
  try {
    const info = await getVolunteerInfo()
    const volunteerId = info?.data?.id || localStorage.getItem('userId')
    
    // 调用后端统一积分计算接口
    const pointsRes = await calculatePoints(volunteerId)
    if (pointsRes && pointsRes.data) {
      userPoints.value = pointsRes.data.totalPoints
      console.log('后端计算积分:', pointsRes.data)
      console.log('积分公式:', pointsRes.data.formula)
    } else {
      userPoints.value = 0
    }
    
    const res = await getMyApplicationsWithReplenish(volunteerId)
    let appList = (res.data || []).sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime))
    
    // 数据验证和标准化
    appList = appList.map(app => {
      // 确保数据类型正确
      const standardizedApp = {
        ...app,
        serviceHours: app.serviceHours ? parseFloat(app.serviceHours) : null,
        earnedPoints: app.earnedPoints ? parseInt(app.earnedPoints) : null,
        // 确保状态字段存在
        status: app.status || '待审核',
        checkinStatus: app.checkinStatus || null,
        // 确保时间字段格式正确
        applyTime: app.applyTime || null,
        activityStartTime: app.activityStartTime || null,
        activityEndTime: app.activityEndTime || null,
        checkinTime: app.checkinTime || null,
        checkoutTime: app.checkoutTime || null,
        // 确保补录标识正确
        isReplenish: Boolean(app.isReplenish)
      }
      
      // 验证数据完整性
      if (!standardizedApp.activityName) {
        console.warn('活动名称缺失:', app)
      }
      if (!standardizedApp.activityAddress) {
        console.warn('活动地址缺失:', app)
      }
      
      return standardizedApp
    })
    
    // 处理计时器
    for (const app of appList) {
      // 如果正在进行中，启动计时器
      if (app.checkinStatus === 'checkin' && app.checkinTime) {
        startServiceTimer(app.activityId, app.checkinTime)
      }
    }
    
    applications.value = appList
    
    // 输出数据统计用于调试
    console.log('加载申请数据:', {
      总数: appList.length,
      已完成: appList.filter(app => app.checkinStatus === 'checkout').length,
      进行中: appList.filter(app => app.checkinStatus === 'checkin').length,
      补录申请: appList.filter(app => app.isReplenish).length,
      总时长: totalServiceTime.value,
      总积分: totalPoints.value
    })
  } catch (error) {
    console.error('加载申请记录失败', error)
    ElMessage.error('加载申请记录失败')
  } finally {
    loading.value = false
  }
}


// 开始服务计时
const startServiceTimer = (activityId, checkinTime) => {
  // 清除已存在的计时器
  if (serviceTimers.value.has(activityId)) {
    clearInterval(serviceTimers.value.get(activityId))
  }
  
  const updateTimer = () => {
    const now = new Date()
    const start = new Date(checkinTime)
    const diff = now - start
    
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)
    
    const timeStr = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    
    // 更新对应申请的计时显示
    const app = applications.value.find(a => a.activityId === activityId)
    if (app) {
      app.currentServiceTime = timeStr
    }
  }
  
  updateTimer() // 立即更新一次
  const timer = setInterval(updateTimer, 1000)
  serviceTimers.value.set(activityId, timer)
}

// 状态显示相关函数
const getStatusType = (status) => {
  switch (status) {
    case '待审核': return 'warning'
    case '待参与':
    case '已通过': return 'success'
    case '已拒绝': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case '待审核': return '审核中'
    case '待参与':
    case '已通过': return '已通过'
    case '已拒绝': return '已拒绝'
    default: return status || '-'
  }
}

const getCheckinStatusType = (status) => {
  switch (status) {
    case 'not_checkin': return 'info'
    case 'checkin': return 'warning'
    case 'checkout': return 'success'
    default: return 'info'
  }
}

const getCheckinStatusText = (status) => {
  switch (status) {
    case 'not_checkin': return '未签到'
    case 'checkin': return '进行中'
    case 'checkout': return '已完成'
    default: return '-'
  }
}

// 生成活动日志
const generateActivityLogs = (activity) => {
  const logs = []
  
  // 判断是否为补录申请
  if (activity.isReplenish) {
    return generateReplenishLogs(activity)
  }
  
  // 1. 报名申请
  logs.push({
    id: 1,
    timestamp: activity.applyTime,
    type: 'primary',
    icon: 'Edit',
    title: '提交申请',
    description: '志愿者提交活动报名申请',
    details: [`申请原因: ${activity.reason || '无'}`]
  })
  
  // 2. 审核结果
  if (activity.status !== '待审核') {
    const auditTime = activity.auditTime || activity.applyTime // 如果没有审核时间，使用申请时间
    if (activity.status === '已通过' || activity.status === '待参与') {
      logs.push({
        id: 2,
        timestamp: auditTime,
        type: 'success',
        icon: 'Check',
        title: '审核通过',
        description: '管理员审核通过，可以参与活动',
        details: ['状态: 待参与']
      })
    } else if (activity.status === '已拒绝') {
      logs.push({
        id: 2,
        timestamp: auditTime,
        type: 'danger',
        icon: 'Close',
        title: '审核拒绝',
        description: '管理员审核未通过',
        details: activity.rejectReason ? [`拒绝原因: ${activity.rejectReason}`] : []
      })
      return logs // 如果被拒绝，后续流程不会有
    }
  }
  
  // 3. 签到记录
  if (activity.checkinStatus === 'checkin' || activity.checkinStatus === 'checkout') {
    logs.push({
      id: 3,
      timestamp: activity.checkinTime,
      type: 'warning',
      icon: 'Clock',
      title: '活动签到',
      description: '志愿者成功签到，开始参与活动',
      details: ['开始计时']
    })
  }
  
  // 4. 签退记录
  if (activity.checkinStatus === 'checkout') {
    logs.push({
      id: 4,
      timestamp: activity.checkoutTime,
      type: 'success',
      icon: 'CircleCheck',
      title: '活动签退',
      description: '志愿者完成活动并签退',
      details: [
        `服务时长: ${formatServiceHours(activity.serviceHours)}`,
        `获得积分: ${formatPoints(activity.earnedPoints)}`
      ]
    })
  }
  
  return logs.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
}

// 生成补录申请日志
const generateReplenishLogs = (activity) => {
  const logs = []
  
  // 1. 补录申请提交
  logs.push({
    id: 1,
    timestamp: activity.applyTime,
    type: 'warning',
    icon: 'DocumentAdd',
    title: '提交补录申请',
    description: '志愿者提交服务时长补录申请',
    details: [
      `补录活动: ${activity.activityName}`,
      `补录时长: ${formatServiceHours(activity.serviceHours)}`,
      `补录积分: ${formatPoints(activity.earnedPoints)}`,
      `补录原因: ${activity.reason || '无'}`
    ]
  })
  
  // 2. 管理员审核
  if (activity.status === '已通过') {
    logs.push({
      id: 2,
      timestamp: activity.checkoutTime, // 使用审核时间
      type: 'success',
      icon: 'Check',
      title: '补录审核通过',
      description: '管理员审核通过，补录时长和积分已生效',
      details: [
        '审核状态: 通过',
        `确认时长: ${formatServiceHours(activity.serviceHours)}`,
        `确认积分: ${formatPoints(activity.earnedPoints)}`,
        '数据已计入个人统计'
      ]
    })
    
    // 3. 补录完成
    logs.push({
      id: 3,
      timestamp: activity.checkoutTime,
      type: 'success',
      icon: 'CircleCheck',
      title: '补录完成',
      description: '补录申请处理完成，服务记录已更新',
      details: [
        '补录状态: 已完成',
        '统计数据: 已更新',
        '积分奖励: 已发放'
      ]
    })
  } else if (activity.status === '已拒绝') {
    logs.push({
      id: 2,
      timestamp: activity.checkoutTime,
      type: 'danger',
      icon: 'Close',
      title: '补录审核拒绝',
      description: '管理员审核未通过，补录申请被拒绝',
      details: activity.rejectReason ? [`拒绝原因: ${activity.rejectReason}`] : ['未提供拒绝原因']
    })
  } else {
    // 待审核状态
    logs.push({
      id: 2,
      timestamp: new Date(),
      type: 'info',
      icon: 'Clock',
      title: '等待审核',
      description: '补录申请已提交，等待管理员审核',
      details: ['审核状态: 待处理', '预计处理时间: 1-3个工作日']
    })
  }
  
  return logs.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))
}

const showActivityLog = (row) => {
  currentActivity.value = row
  activityLogs.value = generateActivityLogs(row)
  showLogDialog.value = true
}

const goToActivityDetail = (row) => {
  router.push(`/volunteer/activity/${row.activityId}`)
}

// 状态处理方法
const getApplicationStatusType = (status) => {
  switch (status) {
    case '待审核': return 'warning'
    case '待参与':
    case '已通过': return 'success'
    case '已拒绝': return 'danger'
    default: return 'info'
  }
}

const getApplicationStatusText = (status) => {
  switch (status) {
    case '待审核': return '审核中'
    case '待参与':
    case '已通过': return '已通过'
    case '已拒绝': return '已拒绝'
    default: return status
  }
}

const getParticipationStatusType = (checkinStatus) => {
  switch (checkinStatus) {
    case 'checkout': return 'success'
    case 'checkin': return 'primary'
    default: return 'info'
  }
}

const getParticipationStatusText = (checkinStatus) => {
  switch (checkinStatus) {
    case 'checkout': return '已完成'
    case 'checkin': return '进行中'
    default: return '未开始'
  }
}

// 文件上传相关方法
const handleFileChange = (file, uploadFileList) => {
  console.log('文件变化:', file, uploadFileList)
  fileList.value = uploadFileList
}

const handleFileRemove = (file, uploadFileList) => {
  console.log('移除文件:', file)
  fileList.value = uploadFileList
}

const beforeUpload = (file) => {
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
  
  console.log('文件通过验证:', file.name, file.type, file.size)
  return false // 阻止自动上传，我们手动处理
}

// 将文件转换为Base64（图片会进行压缩）
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    if (file.type.startsWith('image/')) {
      // 对图片进行压缩
      compressImage(file).then(resolve).catch(reject)
    } else {
      // 非图片文件直接转换
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = () => resolve(reader.result)
      reader.onerror = error => reject(error)
    }
  })
}

// 图片压缩函数
const compressImage = (file, maxWidth = 800, quality = 0.8) => {
  return new Promise((resolve, reject) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()
    
    img.onload = () => {
      // 计算压缩后的尺寸
      let { width, height } = img
      if (width > maxWidth) {
        height = (height * maxWidth) / width
        width = maxWidth
      }
      
      canvas.width = width
      canvas.height = height
      
      // 绘制压缩后的图片
      ctx.drawImage(img, 0, 0, width, height)
      
      // 转换为Base64
      const compressedBase64 = canvas.toDataURL(file.type, quality)
      resolve(compressedBase64)
    }
    
    img.onerror = reject
    
    // 读取原始图片
    const reader = new FileReader()
    reader.onload = (e) => {
      img.src = e.target.result
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 提交补录申请
const submitReplenish = async () => {
  if (!replenishFormRef.value) return
  
  try {
    await replenishFormRef.value.validate()
    submitting.value = true
    
    // 处理文件上传 - 将文件转换为Base64或文件信息
    let evidenceFiles = ''
    if (fileList.value && fileList.value.length > 0) {
      const fileInfos = []
      for (const fileItem of fileList.value) {
        const file = fileItem.raw || fileItem
        if (file) {
          try {
            // 对于图片文件，转换为Base64；对于其他文件，保存文件信息
            if (file.type && file.type.startsWith('image/')) {
              const base64 = await fileToBase64(file)
              
              // 检查压缩后的数据大小（Base64大约比原文件大33%）
              const base64Size = base64.length * 0.75 // 估算原始大小
              if (base64Size > 5 * 1024 * 1024) { // 5MB限制
                ElMessage.warning(`图片 ${file.name} 压缩后仍然过大，建议使用更小的图片`)
                continue
              }
              
              fileInfos.push({
                name: file.name,
                type: file.type,
                size: file.size,
                originalSize: file.size,
                compressedSize: Math.round(base64Size),
                data: base64
              })
            } else {
              // 非图片文件只保存基本信息，不存储内容
              fileInfos.push({
                name: file.name,
                type: file.type,
                size: file.size,
                data: null
              })
            }
          } catch (error) {
            console.error('处理文件失败:', file.name, error)
            ElMessage.warning(`文件 ${file.name} 处理失败，将跳过`)
          }
        }
      }
      evidenceFiles = JSON.stringify(fileInfos)
      
      // 检查最终JSON数据大小
      const jsonSize = new Blob([evidenceFiles]).size
      if (jsonSize > 10 * 1024 * 1024) { // 10MB限制
        ElMessage.error('佐证材料总大小超过限制，请减少文件数量或使用更小的图片')
        return
      }
      
      console.log(`佐证材料数据大小: ${(jsonSize / 1024 / 1024).toFixed(2)} MB`)
    }
    
    const formData = {
      activityName: replenishForm.value.activityName,
      hours: replenishForm.value.hours,
      earnedPoints: replenishForm.value.earnedPoints,
      reason: replenishForm.value.reason,
      evidenceFiles: evidenceFiles
    }
    
    console.log('提交补录申请数据:', formData)
    console.log('文件列表:', fileList.value)
    console.log('佐证材料JSON:', evidenceFiles)
    
    await applyReplenish(formData)
    ElMessage.success('补录申请提交成功，请等待审核')
    
    // 重置表单
    replenishFormRef.value.resetFields()
    replenishForm.value = {
      activityName: '',
      hours: null,
      earnedPoints: null,
      reason: '',
      evidenceFiles: ''
    }
    fileList.value = []
    showReplenishDialog.value = false
    
    // 重新加载数据
    await loadApplications()
    
  } catch (error) {
    console.error('提交补录申请失败:', error)
    ElMessage.error('提交补录申请失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadApplications()
})

onBeforeUnmount(() => {
  // 清理所有计时器
  serviceTimers.value.forEach(timer => clearInterval(timer))
  serviceTimers.value.clear()
})
</script>

<style scoped>
.my-applications {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #ce4c4c;
  font-size: 24px;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: 1fr;
  }
}

/* 卡片布局样式 */
.applications-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.application-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
}

.application-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.replenish-card {
  border-left: 4px solid #e6a23c;
}

.card-header {
  padding: 20px 20px 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.activity-info {
  flex: 1;
}

.activity-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.replenish-tag {
  margin-left: 8px;
}

.activity-address {
  margin: 0;
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-badges {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.status-tag,
.participation-tag {
  font-size: 12px;
}

.card-content {
  padding: 15px 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.info-item .value.highlight {
  color: #409eff;
  font-weight: 600;
}

.card-footer {
  padding: 0 20px 20px 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  margin-top: 40px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .applications-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .applications-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .application-card {
    margin: 0 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .status-badges {
    flex-direction: row;
    align-items: flex-start;
  }
  
  .info-row {
    flex-direction: column;
    gap: 8px;
  }
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 活动日志弹窗样式 */
.activity-log {
  padding: 10px 0;
}

.activity-info {
  margin-bottom: 30px;
}

.timeline-section {
  margin-top: 20px;
}

.timeline-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.log-content {
  padding-left: 10px;
}

.log-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.log-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.log-details {
  margin-top: 8px;
}
</style>