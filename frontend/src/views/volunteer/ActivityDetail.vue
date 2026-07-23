<template>
  <div class="activity-detail">
    <el-card v-loading="loading">
      <div class="detail-header">
        <el-button class="back-button" @click="router.push('/volunteer/activities')" icon="ArrowLeft">
          返回活动中心
        </el-button>
        <h2 class="centered-title">{{ activity.title }}</h2>
        <div class="info-row">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span class="label">活动日期：</span>
            <span>{{ formatDateRange(activity.startTime, activity.endTime) }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span class="label">招募人数：</span>
            <span>{{ activity.currentNumber || volunteers.length }}/{{ activity.targetNumber }}</span>
          </div>
          <div class="info-item clickable" @click="showMapDialog = true">
            <el-icon><Location /></el-icon>
            <span class="label">活动地址：</span>
            <span class="address-link">{{ activity.detailedAddress || activity.address }}</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="tags-row">
          <div class="tag-item">
            <el-icon><Briefcase /></el-icon>
            <span class="tag-label">志愿领域：</span>
            <el-tag type="primary">{{ activity.volunteerField }}</el-tag>
          </div>
          <div class="tag-item">
            <el-icon><UserFilled /></el-icon>
            <span class="tag-label">志愿对象：</span>
            <el-tag type="success">{{ activity.volunteerTarget }}</el-tag>
          </div>
          <div class="tag-item">
            <el-icon><OfficeBuilding /></el-icon>
            <span class="tag-label">志愿场所：</span>
            <el-tag type="warning">{{ activity.serviceLocation }}</el-tag>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <h3>活动内容详情</h3>
        <div class="content-box">{{ activity.content }}</div>
      </div>

      <div class="detail-section" v-if="venueImages.length > 0">
        <h3>活动场所直览</h3>
        <div class="venue-gallery" ref="galleryRef">
          <div class="venue-image" v-for="(img, index) in venueImages" :key="index" @click="previewImage(index)">
            <img :src="img" alt="活动场所图片" @error="handleImageError" />
          </div>
        </div>
      </div>

      <el-image-viewer
        v-if="showImageViewer"
        :url-list="venueImages"
        :initial-index="currentImageIndex"
        @close="showImageViewer = false"
      />

      <div class="detail-section" v-if="activity.organizationName">
        <h3>活动场所联系</h3>
        <div class="contact-card">
          <img :src="activity.organizationImage || '/images/logo.jpg'" class="org-image" />
          <div class="contact-info">
            <h4>{{ activity.organizationName }}</h4>
            <p><el-icon><User /></el-icon> {{ activity.contactPerson }}</p>
            <p><el-icon><Phone /></el-icon> {{ activity.contactPhone }}</p>
          </div>
        </div>
      </div>

      <div class="detail-section" v-if="volunteers.length > 0">
        <div class="volunteer-header">
          <h3>已报名志愿者（{{ volunteers.length }}人）</h3>
          <el-button text type="primary" @click="showAllVolunteers = true" v-if="volunteers.length > 10">
            查看更多
          </el-button>
        </div>
        <div class="volunteer-list">
          <div
            class="volunteer-item"
            v-for="vol in displayedVolunteers"
            :key="vol.id"
          >
            <el-avatar
              :src="vol.avatar || '/images/logo.jpg'"
              :size="50"
            />
            <div class="volunteer-name">
              {{ vol.username }}
            </div>
          </div>
        </div>
      </div>

      <div class="action-bar">
        <el-button
          :type="buttonConfig.type || 'primary'"
          size="large"
          @click="handleButtonClick"
          :disabled="buttonConfig.disabled"
        >
          {{ buttonConfig.text }}
        </el-button>
      </div>
    </el-card>

    <el-dialog v-model="showAllVolunteers" title="所有报名志愿者" width="600px">
      <div class="all-volunteers-grid">
        <div
          class="volunteer-item"
          v-for="vol in volunteers"
          :key="vol.id"
        >
          <el-avatar
            :src="vol.avatar || '/images/logo.jpg'"
            :size="60"
          />
          <div class="volunteer-name">
            {{ vol.username }}
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showMapDialog" title="活动地址与路线规划" width="900px">
      <div id="map-dialog" class="map-container"></div>
      
      <div class="distance-info" v-if="distance">
        <el-icon><Location /></el-icon>
        <span>距离您约 <strong>{{ distance }}</strong> 公里</span>
      </div>

      <div class="route-buttons">
        <el-button
          v-for="mode in routeModes"
          :key="mode.value"
          :type="selectedMode === mode.value ? 'primary' : 'default'"
          @click="planRoute(mode.value)"
        >
          {{ mode.label }}
        </el-button>
      </div>

      <div class="route-info" v-if="routeInfo">
        <div class="route-item">
          <span class="route-label">预计时间：</span>
          <span class="route-value">{{ routeInfo.time }}</span>
        </div>
        <div class="route-item">
          <span class="route-label">路线距离：</span>
          <span class="route-value">{{ routeInfo.distance }}</span>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showApplyDialog" title="报名申请" width="600px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="applyForm.realName" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="applyForm.phone" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="applyForm.email" disabled />
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入您的申请原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleApply" :loading="applying">确认报名</el-button>
      </template>
    </el-dialog>

    <!-- 签到对话框 -->
    <el-dialog v-model="showCheckinDialog" title="活动签到" width="500px">
      <div class="checkin-dialog">
        <div class="checkin-info">
          <el-alert
            title="签到须知"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>1. 请输入管理员提供的6位验证码</p>
              <p>2. 使用您在系统中登记的地理位置进行签到验证</p>
              <p>3. 登记位置需在活动地点1公里范围内</p>
              <p>4. 签到成功后开始计算志愿服务时长</p>
            </template>
          </el-alert>
        </div>
        
        <el-form :model="checkinForm" label-width="100px" style="margin-top: 20px;">
          <el-form-item label="验证码" required>
            <el-input
              v-model="checkinForm.code"
              placeholder="请输入6位验证码"
              maxlength="6"
              style="width: 200px;"
              @input="checkinForm.code = checkinForm.code.toUpperCase()"
            />
          </el-form-item>
          <el-form-item label="当前位置">
            <div class="location-info">
              <div class="location-text-wrapper">
                <template v-if="volunteerLocation">
                  <div class="location-row">
                    <span class="location-label">距志愿场所：</span>
                    <span class="location-distance" :class="{ 'is-ok': locationWithinRange, 'is-bad': !locationWithinRange }">
                      {{ locationDistance }} 公里
                    </span>
                  </div>
                  <div class="location-status" :class="{ 'is-ok': locationWithinRange, 'is-bad': !locationWithinRange }">
                    <span v-if="locationWithinRange">满足1公里内签到条件</span>
                    <span v-else>超出1公里范围，无法签到</span>
                  </div>
                </template>
                <span v-else class="location-text error location-empty">
                  未登记地理位置信息
                </span>
              </div>
              <el-button size="small" @click="loadVolunteerLocation" class="location-refresh-btn">
                刷新距离
              </el-button>
            </div>
          </el-form-item>
          
          <!-- 暂时注释掉位置验证相关UI -->
          <!--
          <el-form-item label="当前位置">
            <div class="location-info">
              <span v-if="checkinForm.latitude && checkinForm.longitude" class="location-text">
                <el-icon><Location /></el-icon>
                已获取位置信息
              </span>
              <span v-else class="location-text error">
                <el-icon><Warning /></el-icon>
                未获取位置信息
              </span>
              <el-button size="small" @click="getCurrentLocation" style="margin-left: 10px;">
                重新获取
              </el-button>
            </div>
          </el-form-item>
          -->
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showCheckinDialog = false">取消</el-button>
        <el-button 
          type="success" 
          @click="handleCheckin"
          :disabled="!checkinForm.code"
        >
          确认签到
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { ElImageViewer } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getActivityDetail, applyActivity, getParticipants, getMyApplications } from '@/api/activity'
import { getVolunteerInfo } from '@/api/volunteer'
import { getCheckinStatus, checkin, checkout } from '@/api/checkin'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activity = ref({})
const volunteers = ref([])
const venueImages = ref([])
const galleryRef = ref(null)
const showImageViewer = ref(false)
const currentImageIndex = ref(0)
const scrollInterval = ref(null)
const showAllVolunteers = ref(false)
const showMapDialog = ref(false)
const showApplyDialog = ref(false)
const volunteerLocation = ref(null)
const applying = ref(false)
const applicationStatus = ref(null)
const checkinStatus = ref(null)
const showCheckinDialog = ref(false)
const checkinForm = ref({
  code: '',
  latitude: null,
  longitude: null
})
const locationDistance = ref(null) // 当前与活动地点的距离（公里，字符串形式，保留两位小数）
const locationWithinRange = ref(null) // 是否在1公里范围内：true/false/null(未知)
const locationLoading = ref(false)
const serviceTimer = ref(null)
const currentServiceTime = ref('')
const applyFormRef = ref(null)
const currentVolunteerId = ref(null)
const applyForm = ref({
  realName: '',
  phone: '',
  email: '',
  reason: ''
})

const applyRules = {
  reason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' },
    { min: 10, message: '申请原因至少10个字符', trigger: 'blur' }
  ]
}
const distance = ref(null)
const map = ref(null)
const driving = ref(null)
const walking = ref(null)
const riding = ref(null)
const selectedMode = ref(null)
const routeInfo = ref(null)

const routeModes = [
  { value: 'driving', label: '驾车' },
  { value: 'walking', label: '步行' },
  { value: 'riding', label: '骑行' }
]

const formatDateRange = (startTime, endTime) => {
  if (!startTime) return ''
  const formatDateTime = (dt) => {
    if (!dt) return ''
    const d = new Date(dt)
    return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  }
  const start = formatDateTime(startTime)
  const end = endTime ? formatDateTime(endTime) : ''
  return end ? `${start} - ${end}` : start
}

const displayedVolunteers = computed(() => {
  return volunteers.value.slice(0, 10)
})

const activityTimeStatus = computed(() => {
  if (!activity.value.startTime || !activity.value.endTime) return 'unknown'
  const now = new Date().getTime()
  const startTime = new Date(activity.value.startTime).getTime()
  const endTime = new Date(activity.value.endTime).getTime()
  
  if (now < startTime) return 'not-started'
  if (now >= startTime && now <= endTime) return 'ongoing'
  if (now > endTime) return 'ended'
  return 'unknown'
})

const countdown = ref('')
const updateCountdown = () => {
  if (!activity.value.startTime) return
  const now = new Date().getTime()
  const startTime = new Date(activity.value.startTime).getTime()
  const diff = startTime - now
  
  if (diff <= 0) {
    countdown.value = ''
    return
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  if (days > 0) {
    countdown.value = `${days}天${hours}小时${minutes}分${seconds}秒`
  } else if (hours > 0) {
    countdown.value = `${hours}小时${minutes}分${seconds}秒`
  } else if (minutes > 0) {
    countdown.value = `${minutes}分${seconds}秒`
  } else {
    countdown.value = `${seconds}秒`
  }
}

const buttonConfig = computed(() => {
  const timeStatus = activityTimeStatus.value
  console.log('按钮配置计算 - 活动时间状态:', timeStatus, '申请状态:', applicationStatus.value, '签到状态:', checkinStatus.value)
  
  // 如果正在审核中
  if (applicationStatus.value === '待审核') {
    return {
      text: '审核中',
      disabled: true,
      action: 'none'
    }
  }
  
  // 如果已经审核通过（待参与或已通过）
  if (applicationStatus.value === '待参与' || applicationStatus.value === '已通过') {
    if (timeStatus === 'not-started') {
      return {
        text: countdown.value ? `距活动开始 ${countdown.value}` : '活动未开始',
        disabled: true,
        action: 'none'
      }
    } else if (timeStatus === 'ongoing') {
      // 活动进行中，根据签到状态显示不同按钮
      if (checkinStatus.value?.status === 'not_checkin') {
        return {
          text: '参与活动',
          disabled: false,
          action: 'checkin',
          type: 'success'
        }
      } else if (checkinStatus.value?.status === 'checkin') {
        return {
          text: currentServiceTime.value ? `签退 (${currentServiceTime.value})` : '签退',
          disabled: false,
          action: 'checkout',
          type: 'warning'
        }
      } else if (checkinStatus.value?.status === 'checkout') {
        return {
          text: `已完成（${checkinStatus.value.serviceHours}小时）`,
          disabled: true,
          action: 'none',
          type: 'info'
        }
      } else {
        return {
          text: '参与活动',
          disabled: false,
          action: 'checkin',
          type: 'success'
        }
      }
    } else if (timeStatus === 'ended') {
      if (checkinStatus.value?.status === 'checkout') {
        return {
          text: `已完成（${checkinStatus.value.serviceHours}小时）`,
          disabled: true,
          action: 'none',
          type: 'info'
        }
      } else {
        return {
          text: '活动已结束',
          disabled: true,
          action: 'none'
        }
      }
    }
  }
  
  // 未报名状态：需要判断活动时间
  if (timeStatus === 'ended') {
    return {
      text: '活动已结束',
      disabled: true,
      action: 'none'
    }
  }
  
  if (timeStatus === 'ongoing') {
    return {
      text: '活动进行中，不可报名',
      disabled: true,
      action: 'none'
    }
  }
  
  if (timeStatus === 'not-started') {
    return {
      text: '立即报名',
      disabled: false,
      action: 'apply',
      type: 'primary'
    }
  }
  
  return {
    text: '立即报名',
    disabled: false,
    action: 'apply',
    type: 'primary'
  }
})

const handleButtonClick = () => {
  const config = buttonConfig.value
  if (config.disabled) return
  
  switch (config.action) {
    case 'apply':
      showApplyDialog.value = true
      break
    case 'checkin':
      showCheckinDialog.value = true
      // 使用志愿者登记的经纬度计算距离
      loadVolunteerLocation()
      break
    case 'checkout':
      handleCheckout()
      break
  }
}

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon/2) * Math.sin(dLon/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return (R * c).toFixed(2)
}

const loadVolunteerLocation = async () => {
  try {
    const res = await getVolunteerInfo()
    if (res.data && res.data.latitude && res.data.longitude) {
      volunteerLocation.value = {
        latitude: res.data.latitude,
        longitude: res.data.longitude
      }
      
      // 计算志愿者登记位置与活动地点的距离
      if (activity.value && activity.value.latitude && activity.value.longitude) {
        try {
          const volunteerLat = parseFloat(res.data.latitude)
          const volunteerLng = parseFloat(res.data.longitude)
          const activityLat = parseFloat(activity.value.latitude)
          const activityLng = parseFloat(activity.value.longitude)
          
          const distance = calculateDistance(volunteerLat, volunteerLng, activityLat, activityLng)
          locationDistance.value = distance
          locationWithinRange.value = parseFloat(distance) <= 1.0
          
          console.log('[Checkin] 志愿者登记位置距离活动地点:', distance, '公里')
        } catch (error) {
          console.error('计算距离失败:', error)
          locationDistance.value = null
          locationWithinRange.value = null
        }
      }
    } else {
      volunteerLocation.value = null
      locationDistance.value = null
      locationWithinRange.value = null
    }
  } catch (error) {
    console.error('获取志愿者位置失败', error)
    volunteerLocation.value = null
    locationDistance.value = null
    locationWithinRange.value = null
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    const activityId = route.params.id
    console.log('[ActivityDetail] 加载活动, ID:', activityId)
    const res = await getActivityDetail(activityId)
    console.log('[ActivityDetail] API返回数据:', res.data)
    activity.value = res.data
    
    // 解析场所图片数组
    console.log('[ActivityDetail] 原始venueImage:', activity.value.venueImage)
    if (activity.value.venueImage) {
      try {
        const parsed = JSON.parse(activity.value.venueImage)
        console.log('[ActivityDetail] 解析后的数据:', parsed)
        if (Array.isArray(parsed)) {
          // 过滤掉空字符串、null、undefined等无效数据
          venueImages.value = parsed.filter(img => {
            return img && 
                   typeof img === 'string' && 
                   img.trim() !== '' && 
                   img !== 'null' && 
                   img !== 'undefined'
          })
          console.log('[ActivityDetail] 过滤后的有效图片:', venueImages.value)
          console.log('[ActivityDetail] 有效图片数量:', venueImages.value.length)
        } else {
          venueImages.value = []
        }
      } catch (error) {
        console.error('[ActivityDetail] JSON解析失败:', error, activity.value.venueImage)
        // 如果解析失败，尝试作为单个URL处理
        if (activity.value.venueImage && 
            typeof activity.value.venueImage === 'string' && 
            activity.value.venueImage.trim() !== '' &&
            activity.value.venueImage !== '[]') {
          venueImages.value = [activity.value.venueImage]
        } else {
          venueImages.value = []
        }
      }
    } else {
      venueImages.value = []
    }
    console.log('[ActivityDetail] venueImages数量:', venueImages.value.length)
    
    await loadParticipants()
    checkApplicationStatus()
  } catch (error) {
    console.error('[ActivityDetail] 加载失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadParticipants = async () => {
  try {
    const activityId = route.params.id
    const res = await getParticipants(activityId)
    volunteers.value = res.data || []
  } catch (error) {
    console.error('获取志愿者列表失败', error)
  }
}

const loadVolunteerInfo = async () => {
  try {
    const res = await getVolunteerInfo()
    if (res.data) {
      currentVolunteerId.value = res.data.id
      applyForm.value.realName = res.data.realName
      applyForm.value.phone = res.data.phone
      applyForm.value.email = res.data.email
    }
  } catch (error) {
    console.error('获取志愿者信息失败', error)
  }
}

const checkApplicationStatus = async () => {
  try {
    const volunteerId = currentVolunteerId.value || localStorage.getItem('userId')
    if (!volunteerId || !activity.value?.id) return
    console.log('检查申请状态 - 志愿者ID:', volunteerId, '活动ID:', activity.value.id)
    const res = await getMyApplications(volunteerId)
    const list = res.data || []
    console.log('申请列表:', list)
    const existing = list.find(app => app.activityId === activity.value.id)
    console.log('当前活动的申请记录:', existing)
    applicationStatus.value = existing ? existing.status : null
    console.log('设置申请状态为:', applicationStatus.value)
    
    // 如果已报名，检查签到状态
    if (applicationStatus.value === '待参与' || applicationStatus.value === '已通过') {
      await checkCheckinStatus()
    }
  } catch (error) {
    console.error('检查申请状态失败', error)
  }
}

const checkCheckinStatus = async () => {
  try {
    const volunteerId = currentVolunteerId.value || localStorage.getItem('userId')
    if (!volunteerId || !activity.value?.id) return
    
    const res = await getCheckinStatus(activity.value.id, volunteerId)
    checkinStatus.value = res.data
    console.log('签到状态:', checkinStatus.value)
    
    // 如果已签到但未签退，开始计时
    if (checkinStatus.value?.status === 'checkin' && checkinStatus.value?.checkinTime) {
      startServiceTimer(checkinStatus.value.checkinTime)
    } else {
      stopServiceTimer()
    }
  } catch (error) {
    console.error('检查签到状态失败', error)
  }
}

// 开始服务计时
const startServiceTimer = (checkinTime) => {
  stopServiceTimer() // 先停止之前的计时器
  
  const updateTimer = () => {
    const now = new Date()
    const start = new Date(checkinTime)
    const diff = now - start
    
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)
    
    currentServiceTime.value = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  }
  
  updateTimer() // 立即更新一次
  serviceTimer.value = setInterval(updateTimer, 1000) // 每秒更新
}

// 停止服务计时
const stopServiceTimer = () => {
  if (serviceTimer.value) {
    clearInterval(serviceTimer.value)
    serviceTimer.value = null
  }
  currentServiceTime.value = ''
}

const getCurrentLocation = () => {
  if (navigator.geolocation) {
    locationLoading.value = true
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const rawLat = position.coords.latitude
        const rawLng = position.coords.longitude
        console.log('[Checkin] 浏览器原始定位坐标 (WGS84):', rawLat, rawLng)

        const applyLocation = (lat, lng, source) => {
          checkinForm.value.latitude = lat
          checkinForm.value.longitude = lng
          console.log('[Checkin] 使用坐标来源 =', source, '坐标 =', lat, lng)

          // 如果活动有设置经纬度，则计算距离
          if (activity.value.latitude && activity.value.longitude) {
            try {
              const actLat = parseFloat(activity.value.latitude)
              const actLng = parseFloat(activity.value.longitude)
              const userLat = lat
              const userLng = lng
              const d = calculateDistance(userLat, userLng, actLat, actLng)
              locationDistance.value = d
              locationWithinRange.value = parseFloat(d) <= 1.0
              console.log('[Checkin] 距离计算: 用户(', userLat, ',', userLng, ') 活动(', actLat, ',', actLng, '), 距离 =', d, '公里')
            } catch (e) {
              console.error('计算距离失败:', e)
              locationDistance.value = null
              locationWithinRange.value = null
            }
          } else {
            // 活动未设置坐标
            locationDistance.value = null
            locationWithinRange.value = null
          }
        }

        // 优先使用高德提供的坐标转换（GPS -> 高德坐标系）
        if (window.AMap && AMap.convertFrom) {
          AMap.convertFrom([rawLng, rawLat], 'gps', (status, result) => {
            if (status === 'complete' && result && result.locations && result.locations.length > 0) {
              const loc = result.locations[0]
              applyLocation(loc.lat, loc.lng, 'AMap.convertFrom')
            } else {
              console.warn('[Checkin] AMap.convertFrom 失败，使用浏览器原始坐标')
              applyLocation(rawLat, rawLng, 'navigator.geolocation')
            }
            locationLoading.value = false
          })
        } else {
          applyLocation(rawLat, rawLng, 'navigator.geolocation')
          locationLoading.value = false
        }
      },
      (error) => {
        console.error('获取位置失败:', error)
        ElMessage.error('获取位置失败，请允许浏览器访问位置')
        locationLoading.value = false
      }
    )
  } else {
    ElMessage.error('浏览器不支持地理定位')
  }
}

const handleCheckin = async () => {
  try {
    if (!checkinForm.value.code.trim()) {
      ElMessage.error('请输入验证码')
      return
    }

    // 使用志愿者登记的经纬度，不需要浏览器定位
    if (!volunteerLocation.value) {
      ElMessage.error('未获取到您登记的地理位置信息，请联系管理员添加')
      return
    }
    
    const volunteerId = currentVolunteerId.value || localStorage.getItem('userId')
    const data = {
      activityId: activity.value.id,
      volunteerId: volunteerId,
      code: checkinForm.value.code.trim().toUpperCase()
      // 不再传递latitude和longitude，后端会使用志愿者登记的经纬度
    }
    
    await checkin(data)
    ElMessage.success('签到成功！开始计时')
    showCheckinDialog.value = false
    checkinForm.value.code = ''
    await checkCheckinStatus() // 这会自动开始计时
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

const handleCheckout = async () => {
  try {
    await ElMessageBox.confirm('确认要签退吗？签退后将计算您的志愿服务时长。', '确认签退', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const volunteerId = currentVolunteerId.value || localStorage.getItem('userId')
    const data = {
      activityId: activity.value.id,
      volunteerId: volunteerId
    }
    
    const result = await checkout(data)
    stopServiceTimer() // 停止计时
    
    // 显示详细的结算信息
    if (result.data) {
      const { serviceHours, earnedPoints } = result.data
      ElMessage.success(`签退成功！服务时长：${serviceHours}小时，获得积分：${earnedPoints}分`)
    } else {
      ElMessage.success('签退成功！服务时长已记录')
    }
    
    await checkCheckinStatus()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('签退失败:', error)
      ElMessage.error(error.response?.data?.message || '签退失败')
    }
  }
}

const initMapDialog = () => {
  if (!activity.value.latitude || !activity.value.longitude) return
  
  setTimeout(() => {
    const activityPos = [activity.value.longitude, activity.value.latitude]
    
    map.value = new AMap.Map('map-dialog', {
      zoom: 13,
      center: activityPos
    })
    
    new AMap.Marker({
      position: activityPos,
      map: map.value,
      title: '活动地点',
      icon: new AMap.Icon({
        size: new AMap.Size(25, 34),
        image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
        imageSize: new AMap.Size(25, 34)
      })
    })
    
    if (volunteerLocation.value) {
      const volunteerPos = [volunteerLocation.value.longitude, volunteerLocation.value.latitude]
      
      new AMap.Marker({
        position: volunteerPos,
        map: map.value,
        title: '我的位置',
        icon: new AMap.Icon({
          size: new AMap.Size(25, 34),
          image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
          imageSize: new AMap.Size(25, 34)
        })
      })
      
      distance.value = calculateDistance(
        volunteerLocation.value.latitude,
        volunteerLocation.value.longitude,
        activity.value.latitude,
        activity.value.longitude
      )
      
      map.value.setFitView()
    }
    
    AMap.plugin(['AMap.Driving', 'AMap.Walking', 'AMap.Riding'], () => {
      driving.value = new AMap.Driving({
        map: map.value,
        hideMarkers: false
      })
      
      walking.value = new AMap.Walking({
        map: map.value,
        hideMarkers: false
      })
      
      riding.value = new AMap.Riding({
        map: map.value,
        hideMarkers: false
      })
    })
  }, 100)
}

const planRoute = (mode) => {
  if (!volunteerLocation.value) {
    ElMessage.warning('请先在个人信息中设置您的位置')
    return
  }
  
  selectedMode.value = mode
  routeInfo.value = null
  
  map.value.clearMap()
  
  const activityPos = [activity.value.longitude, activity.value.latitude]
  const volunteerPos = [volunteerLocation.value.longitude, volunteerLocation.value.latitude]
  
  new AMap.Marker({
    position: activityPos,
    map: map.value,
    title: '活动地点',
    icon: new AMap.Icon({
      size: new AMap.Size(25, 34),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
      imageSize: new AMap.Size(25, 34)
    })
  })
  
  new AMap.Marker({
    position: volunteerPos,
    map: map.value,
    title: '我的位置',
    icon: new AMap.Icon({
      size: new AMap.Size(25, 34),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
      imageSize: new AMap.Size(25, 34)
    })
  })
  
  const polyline = new AMap.Polyline({
    path: [volunteerPos, activityPos],
    strokeColor: '#409EFF',
    strokeWeight: 4,
    strokeStyle: 'dashed',
    strokeOpacity: 0.8,
    map: map.value
  })
  
  map.value.setFitView()
  
  const dist = parseFloat(distance.value)
  let time, speed
  
  if (mode === 'driving') {
    speed = 40
    time = Math.ceil((dist / speed) * 60)
  } else if (mode === 'walking') {
    speed = 5
    time = Math.ceil((dist / speed) * 60)
  } else if (mode === 'riding') {
    speed = 15
    time = Math.ceil((dist / speed) * 60)
  }
  
  routeInfo.value = {
    time: time < 60 ? `约${time}分钟` : `约${Math.floor(time/60)}小时${time%60}分钟`,
    distance: `约${dist}公里（直线距离）`
  }
}

watch(showMapDialog, (newVal) => {
  if (newVal) {
    selectedMode.value = null
    routeInfo.value = null
    initMapDialog()
  }
})

const handleApply = async () => {
  if (!applyFormRef.value) return
  
  await applyFormRef.value.validate()
  
  if (!currentVolunteerId.value) {
    ElMessage.error('无法获取志愿者信息，请重新登录')
    return
  }
  
  applying.value = true
  try {
    try {
      await applyActivity({
        activityId: activity.value.id,
        volunteerId: currentVolunteerId.value,
        reason: applyForm.value.reason
      })
    } catch (apiError) {
      console.log('API调用失败，继续保存到本地', apiError)
    }
    
    const applications = JSON.parse(localStorage.getItem('myApplications') || '[]')
    applications.push({
      id: Date.now(),
      activityId: activity.value.id,
      activityTitle: activity.value.title,
      activityAddress: activity.value.detailedAddress || activity.value.address,
      activityStartTime: activity.value.startTime,
      activityEndTime: activity.value.endTime,
      applyTime: new Date().toISOString(),
      reason: applyForm.value.reason,
      status: '待审核',
      statusText: '审核中'
    })
    localStorage.setItem('myApplications', JSON.stringify(applications))
    
    applicationStatus.value = '待审核'
    showApplyDialog.value = false
    
    ElMessage.success('报名成功！请关注邮箱获取活动信息')
  } catch (error) {
    ElMessage.error('报名失败：' + (error.message || '未知错误'))
  } finally {
    applying.value = false
  }
}

const countdownInterval = ref(null)

onMounted(async () => {
  const activityId = route.params.id
  console.log('[ActivityDetail] 页面加载, 活动ID:', activityId)
  
  if (!activityId) {
    console.error('[ActivityDetail] 没有活动ID')
    ElMessage.error('活动ID不存在')
    router.push('/volunteer/activities')
    return
  }
  
  // 立即加载活动数据和志愿者信息（不依赖地图）
  await loadVolunteerInfo()
  await loadDetail()
  
  // 启动倒计时更新
  updateCountdown()
  countdownInterval.value = setInterval(updateCountdown, 1000)
  
  // 异步加载地图（失败不影响数据显示）
  try {
    const script = document.createElement('script')
    script.src = 'https://webapi.amap.com/maps?v=2.0&key=578d4cc6a789e7e3f7001f3a49d425ee'
    script.onload = async () => {
      await loadVolunteerLocation()
      console.log('[ActivityDetail] 地图加载成功')
    }
    script.onerror = () => {
      console.warn('[ActivityDetail] 地图加载失败，但不影响数据显示')
    }
    document.head.appendChild(script)
  } catch (error) {
    console.warn('[ActivityDetail] 地图初始化失败:', error)
  }
  
  if (venueImages.value.length > 3) {
    scrollInterval.value = setInterval(() => {
      if (galleryRef.value) {
        const scrollWidth = galleryRef.value.scrollWidth
        const clientWidth = galleryRef.value.clientWidth
        const currentScroll = galleryRef.value.scrollLeft
        
        if (currentScroll + clientWidth >= scrollWidth) {
          galleryRef.value.scrollLeft = 0
        } else {
          galleryRef.value.scrollLeft += 315
        }
      }
    }, 3000)
  }
})

const previewImage = (index) => {
  currentImageIndex.value = index
  showImageViewer.value = true
}

const handleImageError = (event) => {
  console.error('[ActivityDetail] 图片加载失败:', event.target.src)
  // 隐藏加载失败的图片
  event.target.style.display = 'none'
}

onBeforeUnmount(() => {
  if (scrollInterval.value) {
    clearInterval(scrollInterval.value)
  }
  if (countdownInterval.value) {
    clearInterval(countdownInterval.value)
  }
  if (serviceTimer.value) {
    clearInterval(serviceTimer.value)
  }
})
</script>

<style scoped>
.activity-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 20px;
  margin-bottom: 30px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  top: 0;
}

.centered-title {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 24px;
  text-align: center;
  padding: 0 150px;
}

.info-row {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 15px;
}

.info-item .el-icon {
  color: #ce4c4c;
  font-size: 18px;
}

.info-item .label {
  font-weight: 600;
  color: #333;
}

.info-item.clickable {
  cursor: pointer;
  transition: all 0.3s;
}

.info-item.clickable:hover {
  color: #ce4c4c;
}

.address-link {
  color: #409eff;
  text-decoration: underline;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  color: #ce4c4c;
  font-size: 18px;
}

.tags-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-item .el-icon {
  color: #ce4c4c;
  font-size: 18px;
}

.tag-label {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.tag-item .el-tag {
  font-size: 14px;
  padding: 8px 16px;
}

.content-box {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 12px;
  line-height: 1.8;
  color: #666;
  white-space: pre-wrap;
}

.venue-gallery {
  display: flex;
  gap: 15px;
  overflow-x: auto;
  padding: 10px 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.venue-gallery::-webkit-scrollbar {
  display: none;
}

.venue-gallery {
  scroll-behavior: smooth;
}

.venue-image {
  flex-shrink: 0;
  width: 300px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  cursor: pointer;
}

.venue-image:hover {
  transform: scale(1.05);
}

.venue-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.contact-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 12px;
}

.org-image {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  object-fit: cover;
}

.contact-info h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.contact-info p {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.map-container {
  width: 100%;
  height: 450px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.distance-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 15px;
  color: #0284c7;
  font-size: 15px;
}

.distance-info .el-icon {
  font-size: 20px;
}

.distance-info strong {
  color: #0369a1;
  font-size: 18px;
}

.route-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.route-buttons .el-button {
  flex: 1;
}

.route-info {
  padding: 15px;
  background: #f9fafb;
  border-radius: 8px;
  display: flex;
  gap: 30px;
}

.route-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.route-label {
  color: #666;
  font-size: 14px;
}

.route-value {
  color: #333;
  font-weight: 600;
  font-size: 16px;
}

.volunteer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.volunteer-header h3 {
  margin: 0;
}

.volunteer-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.all-volunteers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 15px;
  padding: 10px;
}

.volunteer-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 70px;
}

.volunteer-name {
  margin-top: 6px;
  font-size: 12px;
  color: #666;
  text-align: center;
  max-width: 70px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-bar {
  text-align: center;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

/* 签到对话框样式 */
.checkin-dialog {
  padding: 10px 0;
}

.checkin-info {
  margin-bottom: 20px;
}

.checkin-info p {
  margin: 5px 0;
  font-size: 14px;
}

.location-info {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.location-text {
  font-size: 14px;
  color: #606266;
}

.location-text.error {
  color: #f56c6c;
}

.location-text-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.location-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.location-label {
  font-size: 14px;
  color: #606266;
}

.location-distance {
  font-size: 16px;
  font-weight: 600;
}

.location-distance.is-ok,
.location-status.is-ok {
  color: #67c23a;
}

.location-distance.is-bad,
.location-status.is-bad {
  color: #f56c6c;
}

.location-status {
  font-size: 12px;
}

.location-empty {
  color: #909399;
}

.location-refresh-btn {
  margin-left: 16px;
}
</style>