<template>
  <div class="dashboard">
    <div class="header">
      <h2>个人中心</h2>
      <el-button type="primary" size="small" @click="handleRecalculateStats">同步数据</el-button>
    </div>
    
    <el-row :gutter="20" style="margin-top: 30px">
      <el-col :span="6">
        <el-card class="stat-card" @click="openEventDialog('hours')">
          <div class="stat-item">
            <el-icon size="40" color="#ce4c4c"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formattedTotalHours }}</div>
              <div class="stat-label">累计服务时长</div>
            </div>
          </div>
          <div class="view-detail">点击查看详情 →</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" @click="openEventDialog('points')">
          <div class="stat-item">
            <el-icon size="40" color="#ce4c4c"><TrophyBase /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ userInfo.points || 0 }}</div>
              <div class="stat-label">累计积分</div>
            </div>
          </div>
          <div class="view-detail">点击查看详情 →</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" @click="openEventDialog('activity')">
          <div class="stat-item">
            <el-icon size="40" color="#ce4c4c"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ userInfo.activityCount || 0 }}</div>
              <div class="stat-label">参与活动数</div>
            </div>
          </div>
          <div class="view-detail">点击查看详情 →</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" @click="openEventDialog('certificate')">
          <div class="stat-item">
            <el-icon size="40" color="#ce4c4c"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ userInfo.certificateCount || 0 }}</div>
              <div class="stat-label">获得证书</div>
            </div>
          </div>
          <div class="view-detail">点击查看详情 →</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据可视化图表区域 -->
    <el-row :gutter="20" style="margin-top: 30px" class="charts-row">
      <!-- 成长轨迹折线图 -->
      <el-col :span="12">
        <GrowthTrendChart v-if="volunteerId" :volunteerId="volunteerId" />
      </el-col>
      <!-- 能力雷达图 -->
      <el-col :span="12">
        <AbilityRadarChart v-if="volunteerId" :volunteerId="volunteerId" />
      </el-col>
    </el-row>

    <!-- 事件日志弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="800px"
      class="event-dialog"
    >
      <!-- 时间轴 -->
      <div class="timeline-container" v-loading="timelineLoading">
        <el-empty v-if="filteredEvents.length === 0" description="暂无事件记录" />
        <div v-else class="timeline">
          <div 
            v-for="(event, index) in filteredEvents" 
            :key="index" 
            class="timeline-item"
            :class="`timeline-item-${event.category}`"
          >
            <!-- 左侧图标 -->
            <div class="timeline-icon-wrapper">
              <div class="timeline-icon" :class="`icon-${event.category}`">
                <el-icon :size="24">
                  <TrophyBase v-if="event.category === 'points'" />
                  <Clock v-if="event.category === 'hours'" />
                  <Calendar v-if="event.category === 'activity'" />
                  <Document v-if="event.category === 'certificate'" />
                </el-icon>
              </div>
              <div v-if="index < filteredEvents.length - 1" class="timeline-line"></div>
            </div>
            
            <!-- 右侧内容卡片 -->
            <div class="timeline-card">
              <div class="card-header">
                <div class="card-title">{{ event.title }}</div>
                <div class="card-time">{{ formatEventTime(event.time) }}</div>
              </div>
              <div class="card-description">{{ event.description }}</div>
              <div class="card-footer">
                <!-- 积分变化 -->
                <div v-if="event.pointsChange" class="change-badge" :class="event.pointsChange > 0 ? 'badge-success' : 'badge-danger'">
                  <span class="change-value">{{ event.pointsChange > 0 ? '+' : '' }}{{ event.pointsChange }}</span>
                  <span class="change-unit">积分</span>
                </div>
                <!-- 时长变化 -->
                <div v-if="event.hoursChange" class="change-badge badge-success">
                  <span class="change-value">+{{ event.hoursChange }}</span>
                  <span class="change-unit">小时</span>
                </div>
                <!-- 补录标识 -->
                <el-tag v-if="event.isReplenish" size="small" type="warning" effect="dark">
                  📝 补录
                </el-tag>
                <!-- 兑换标识 -->
                <el-tag v-if="event.isExchange" size="small" type="info" effect="dark">
                  🎁 兑换
                </el-tag>
                <!-- 退款标识 -->
                <el-tag v-if="event.isRefund" size="small" type="success" effect="dark">
                  💰 退款
                </el-tag>
                <!-- 状态标签 -->
                <el-tag v-if="event.status" size="small" :type="getStatusType(event.status)" class="status-tag">
                  {{ event.status }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getVolunteerInfo, recalculateStats } from '@/api/volunteer'
import { getMyApplicationsWithReplenish } from '@/api/activity'
import { getVolunteerEventLogs } from '@/api/eventLog'
import { calculatePoints } from '@/api/points'
import { getVolunteerStats } from '@/api/stats'
import GrowthTrendChart from '@/components/charts/GrowthTrendChart.vue'
import AbilityRadarChart from '@/components/charts/AbilityRadarChart.vue'

const userInfo = ref({
  totalHours: 0,
  points: 0,
  activityCount: 0,
  certificateCount: 0
})

// 事件日志相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentCategory = ref('')
const timelineLoading = ref(false)
const eventLogs = ref([])
const volunteerId = ref(null)

// 格式化时长显示（与"我的申请"页面保持一致）
const formattedTotalHours = computed(() => {
  const totalHours = userInfo.value.totalHours || 0
  
  if (totalHours === 0) return '0小时0分'
  
  const hours = Math.floor(totalHours)
  const minutes = Math.round((totalHours - hours) * 60)
  
  return `${hours}小时${minutes}分`
})

const loadUserInfo = async () => {
  try {
    // 获取基本用户信息
    const userRes = await getVolunteerInfo()
    if (!userRes || !userRes.data) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const volunteerId = userRes.data.id || localStorage.getItem('userId')
    if (!volunteerId) {
      ElMessage.error('无法获取用户ID')
      return
    }
    
    console.log('===== 调用后端统一统计接口 =====')
    console.log('志愿者ID:', volunteerId)
    
    // 调用后端统一统计接口，获取所有统计数据
    const statsRes = await getVolunteerStats(volunteerId)
    if (!statsRes || !statsRes.data) {
      console.error('后端统计数据返回为空')
      ElMessage.error('获取统计数据失败')
      return
    }
    
    const statsData = statsRes.data
    console.log('后端统计数据:', statsData)
    console.log('业务逻辑:')
    console.log('  累计时长 =', statsData.totalHours, '小时')
    console.log('  累计积分 =', statsData.totalPoints, '（公式:', statsData.pointsDetail.formula, '\uff09')
    console.log('  参与活动数 =', statsData.activityCount, '（已完成活动 + 补录活动）')
    console.log('  获得证书 =', statsData.certificateCount, '（只记录已颁发的证书）')
    
    // 使用后端计算的所有统计数据
    userInfo.value = {
      totalHours: statsData.totalHours || 0,
      points: statsData.totalPoints || 0,
      activityCount: statsData.activityCount || 0,
      certificateCount: statsData.certificateCount || 0
    }
    
    console.log('===== 统计数据加载完成 =====')
    console.log('最终显示:', userInfo.value)
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败: ' + (error.message || '未知错误'))
  }
}

const handleRecalculateStats = async () => {
  try {
    await recalculateStats()
    ElMessage.success('数据同步成功')
    // 重新加载数据
    await loadUserInfo()
  } catch (error) {
    ElMessage.error('数据同步失败')
    console.error(error)
  }
}

// 路由监听
const route = useRoute()

onMounted(async () => {
  console.log('Dashboard mounted, loading user info...')
  await loadUserInfo()
  
  // 获取志愿者ID
  const userRes = await getVolunteerInfo()
  if (userRes && userRes.data && userRes.data.id) {
    volunteerId.value = userRes.data.id
    await loadEventLogs()
  }
})

// 加载事件日志
const loadEventLogs = async () => {
  if (!volunteerId.value) return
  
  timelineLoading.value = true
  try {
    const res = await getVolunteerEventLogs(volunteerId.value)
    if (res && res.data) {
      eventLogs.value = res.data
      console.log('[EventLogs] 加载事件日志:', eventLogs.value)
    }
  } catch (error) {
    console.error('[EventLogs] 加载失败:', error)
    ElMessage.error('加载事件日志失败')
  } finally {
    timelineLoading.value = false
  }
}

// 过滤事件（根据当前选中的类别）
const filteredEvents = computed(() => {
  if (!currentCategory.value) {
    return eventLogs.value
  }
  
  const filtered = eventLogs.value.filter(event => event.category === currentCategory.value)
  console.log(`[EventLogs] 过滤 ${currentCategory.value}: ${filtered.length}/${eventLogs.value.length}`)
  return filtered
})

// 打开事件对话框
const openEventDialog = async (category) => {
  console.log('[EventLogs] 打开弹窗，类别:', category)
  currentCategory.value = category
  
  // 设置弹窗标题
  const titleMap = {
    'points': '积分明细',
    'hours': '服务时长明细',
    'activity': '活动参与记录',
    'certificate': '证书获取记录'
  }
  dialogTitle.value = titleMap[category] || '事件记录'
  
  // 先显示弹窗
  dialogVisible.value = true
  
  // 加载数据（每次打开都重新加载）
  if (volunteerId.value) {
    await loadEventLogs()
    console.log('[EventLogs] 总事件数:', eventLogs.value.length)
    console.log('[EventLogs] 过滤后事件数:', filteredEvents.value.length)
  }
}

// 格式化事件时间
const formatEventTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes < 1 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  
  if (year === now.getFullYear()) {
    return `${month}-${day} ${hour}:${minute}`
  }
  return `${year}-${month}-${day}`
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '已完成': 'success',
    '进行中': 'primary',
    '已签到': 'info',
    '已签退': 'success',
    '待审核': 'warning',
    '已通过': 'success',
    '已拒绝': 'danger'
  }
  return typeMap[status] || 'info'
}

// 当路由变化时重新加载数据
watch(() => route.path, (newPath) => {
  if (newPath === '/volunteer/dashboard') {
    console.log('Route changed to dashboard, reloading data...')
    loadUserInfo()
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #ce4c4c;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

/* 统计卡片样式 */
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(206, 76, 76, 0.2);
}

.view-detail {
  text-align: center;
  margin-top: 15px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  color: #ce4c4c;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transform: translateY(-5px);
  transition: all 0.3s ease;
}

.stat-card:hover .view-detail {
  opacity: 1;
  transform: translateY(0);
}

/* 时间轴容器 */
.timeline-container {
  padding: 30px 20px;
  max-height: 600px;
  overflow-y: auto;
  background: #fafafa;
  border-radius: 8px;
}

.timeline {
  position: relative;
}

/* 时间轴项 */
.timeline-item {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  animation: slideIn 0.5s ease-out forwards;
  opacity: 0;
}

.timeline-item:nth-child(1) { animation-delay: 0.1s; }
.timeline-item:nth-child(2) { animation-delay: 0.2s; }
.timeline-item:nth-child(3) { animation-delay: 0.3s; }
.timeline-item:nth-child(4) { animation-delay: 0.4s; }
.timeline-item:nth-child(5) { animation-delay: 0.5s; }

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 左侧图标区域 */
.timeline-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.timeline-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
  position: relative;
  z-index: 2;
}

.icon-points {
  background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
}

.icon-hours {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-activity {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.icon-certificate {
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
}

.timeline-icon:hover {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

/* 连接线 */
.timeline-line {
  width: 3px;
  flex: 1;
  background: linear-gradient(to bottom, 
    rgba(206, 76, 76, 0.3) 0%, 
    rgba(206, 76, 76, 0.1) 100%
  );
  margin-top: 8px;
  border-radius: 2px;
}

/* 右侧内容卡片 */
.timeline-card {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border-left: 4px solid #ce4c4c;
}

.timeline-item-points .timeline-card {
  border-left-color: #f5af19;
}

.timeline-item-hours .timeline-card {
  border-left-color: #4facfe;
}

.timeline-item-activity .timeline-card {
  border-left-color: #667eea;
}

.timeline-item-certificate .timeline-card {
  border-left-color: #ffd700;
}

.timeline-card:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-time {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 卡片描述 */
.card-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

/* 变化徽章 */
.change-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.change-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.badge-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.badge-danger {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  color: white;
}

.change-value {
  font-size: 18px;
  font-weight: bold;
}

.change-unit {
  font-size: 12px;
  opacity: 0.9;
}

/* 状态标签 */
.status-tag {
  margin-left: auto;
}

/* 弹窗样式 */
.event-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #ce4c4c 0%, #e67e7e 100%);
  padding: 20px;
}

.event-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.event-dialog :deep(.el-dialog__close) {
  color: white;
  font-size: 20px;
}

.event-dialog :deep(.el-dialog__close:hover) {
  color: #fff;
}

/* 滚动条样式 */
.timeline-container::-webkit-scrollbar {
  width: 6px;
}

.timeline-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.timeline-container::-webkit-scrollbar-thumb {
  background: #ce4c4c;
  border-radius: 3px;
}

.timeline-container::-webkit-scrollbar-thumb:hover {
  background: #b03c3c;
}

/* 图表行样式 */
.charts-row {
  display: flex;
  align-items: stretch;
}

.charts-row .el-col {
  display: flex;
}

.charts-row .el-col > * {
  flex: 1;
  width: 100%;
}

/* 响应式 */
@media (max-width: 768px) {
  .timeline {
    padding-left: 30px;
  }
  
  .timeline-dot {
    left: -30px;
    width: 36px;
    height: 36px;
  }
  
  .timeline-content {
    padding: 15px;
  }
  
  .charts-row {
    flex-direction: column;
  }
}
</style>