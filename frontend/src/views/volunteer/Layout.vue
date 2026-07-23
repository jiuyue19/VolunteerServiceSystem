<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <img src="/images/logo.jpg" alt="Logo" class="logo-img" />
          <span>志愿者服务系统</span>
        </div>
        <el-menu mode="horizontal" :default-active="$route.path" router class="nav-menu">
          <el-menu-item index="/volunteer/home">首页</el-menu-item>
          <el-menu-item index="/volunteer/activities">活动中心</el-menu-item>
          <el-menu-item index="/volunteer/forum">志愿论坛</el-menu-item>
          <el-menu-item index="/volunteer/exchange">积分兑换</el-menu-item>
          <el-menu-item index="/volunteer/my-applications">我的申请</el-menu-item>
          <el-menu-item index="/volunteer/certificate">服务证书</el-menu-item>
          <el-menu-item index="/volunteer/dashboard">个人中心</el-menu-item>
        </el-menu>
        <div class="city-selector-wrapper">
          <span class="city-text" @click="showCitySelector = !showCitySelector">
            <el-icon><Location /></el-icon>
            {{ currentCity || '选择城市' }}
          </span>
          <div v-if="showCitySelector" class="city-selector-panel">
            <div class="selector-row">
              <el-select v-model="tempProvince" placeholder="省份" @change="handleProvinceChange" style="width: 100%">
                <el-option v-for="item in cityOptions" :key="item.name" :label="item.name" :value="item.name" />
              </el-select>
            </div>
            <div class="selector-row" v-if="cityList.length > 0">
              <el-select v-model="tempCity" placeholder="城市" @change="handleCitySelect" style="width: 100%">
                <el-option v-for="item in cityList" :key="item.name" :label="item.name" :value="item.name" />
              </el-select>
            </div>
            <div class="selector-row" v-if="districtList.length > 0">
              <el-select v-model="tempDistrict" placeholder="区县" @change="handleDistrictSelect" style="width: 100%">
                <el-option v-for="item in districtList" :key="item.name" :label="item.name" :value="item.name" />
              </el-select>
            </div>
          </div>
        </div>
        <el-dropdown @command="handleCommand">
          <div class="avatar-wrapper">
            <el-avatar :src="userInfo.avatar" :size="40">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item command="my-posts">
                <el-icon><Document /></el-icon>
                我的帖子
              </el-dropdown-item>
              <el-dropdown-item command="my-comments">
                <el-icon><ChatDotRound /></el-icon>
                我的评论
              </el-dropdown-item>
              <el-dropdown-item command="my-favorites">
                <el-icon><Star /></el-icon>
                我的收藏
              </el-dropdown-item>
              <el-dropdown-item command="my-exchanges">
                <el-icon><Present /></el-icon>
                我的兑换
              </el-dropdown-item>
              <el-dropdown-item command="my-orders">
                <el-icon><List /></el-icon>
                我的订单
              </el-dropdown-item>
              <el-dropdown-item command="address-book">
                <el-icon><Location /></el-icon>
                收货地址
              </el-dropdown-item>
              <el-dropdown-item command="change-password" divided>
                <el-icon><Lock /></el-icon>
                修改密码
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 系统公告 -->
    <div class="announcement-bar" v-if="publishedAnnouncements.length > 0">
      <el-icon class="announcement-icon"><Bell /></el-icon>
      <div class="announcement-content">
        <transition name="slide-up" mode="out-in">
          <span :key="currentAnnouncementIndex">{{ currentAnnouncement }}</span>
        </transition>
      </div>
    </div>
    
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVolunteerInfo, updateVolunteerInfo } from '@/api/volunteer'
import { getAnnouncementList } from '@/api/announcement'
import { regionData } from '@/utils/regions'
import { clearRoleStorage } from '@/utils/storage'

const router = useRouter()
const userInfo = ref({
  avatar: '',
  province: '',
  city: '',
  district: ''
})

const currentCity = ref('')
const showCitySelector = ref(false)
const tempProvince = ref('')
const tempCity = ref('')
const tempDistrict = ref('')
const cityList = ref([])
const districtList = ref([])

const cityOptions = regionData

// 系统公告
const announcements = ref([])
const currentAnnouncementIndex = ref(0)
let announcementTimer = null

const publishedAnnouncements = computed(() => {
  return announcements.value.filter(item => item.status === 1)
})

const currentAnnouncement = computed(() => {
  if (publishedAnnouncements.value.length === 0) return ''
  return publishedAnnouncements.value[currentAnnouncementIndex.value]?.content || ''
})

const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncementList()
    announcements.value = res.data || []
    startAnnouncementRotation()
  } catch (error) {
    console.error(error)
  }
}

const startAnnouncementRotation = () => {
  if (publishedAnnouncements.value.length <= 1) return
  announcementTimer = setInterval(() => {
    currentAnnouncementIndex.value = (currentAnnouncementIndex.value + 1) % publishedAnnouncements.value.length
  }, 3000)
}

const loadUserInfo = async () => {
  try {
    const res = await getVolunteerInfo()
    if (res.data) {
      userInfo.value = {
        avatar: res.data.avatar || '',
        province: res.data.province || '',
        city: res.data.city || '',
        district: res.data.district || ''
      }
      if (res.data.avatar) {
        localStorage.setItem('userAvatar', res.data.avatar)
      }
      if (res.data.city) {
        currentCity.value = res.data.city
      }
    }
  } catch (error) {
    const savedAvatar = localStorage.getItem('userAvatar')
    if (savedAvatar) {
      userInfo.value.avatar = savedAvatar
    }
  }
}

const handleProvinceChange = (value) => {
  tempCity.value = ''
  tempDistrict.value = ''
  districtList.value = []
  const province = cityOptions.find(p => p.name === value)
  cityList.value = province?.children || []
}

const handleCitySelect = (value) => {
  tempDistrict.value = ''
  const city = cityList.value.find(c => c.name === value)
  districtList.value = city?.children || []
  if (districtList.value.length === 0) {
    saveCityChange()
  }
}

const handleDistrictSelect = () => {
  saveCityChange()
}

const saveCityChange = async () => {
  if (tempProvince.value && tempCity.value) {
    try {
      await updateVolunteerInfo({
        province: tempProvince.value,
        city: tempCity.value,
        district: tempDistrict.value || ''
      })
      currentCity.value = tempCity.value
      userInfo.value.province = tempProvince.value
      userInfo.value.city = tempCity.value
      userInfo.value.district = tempDistrict.value || ''
      showCitySelector.value = false
      tempProvince.value = ''
      tempCity.value = ''
      tempDistrict.value = ''
      cityList.value = []
      districtList.value = []
      ElMessage.success('城市切换成功')
    } catch (error) {
      ElMessage.error('城市切换失败')
      console.error(error)
    }
  }
}

onMounted(() => {
  loadUserInfo()
  loadAnnouncements()
})

onUnmounted(() => {
  if (announcementTimer) {
    clearInterval(announcementTimer)
  }
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/volunteer/profile')
      break
    case 'my-posts':
      router.push('/volunteer/my-posts')
      break
    case 'my-comments':
      router.push('/volunteer/my-comments')
      break
    case 'my-favorites':
      router.push('/volunteer/my-favorites')
      break
    case 'my-exchanges':
      router.push('/volunteer/my-exchanges')
      break
    case 'my-orders':
      router.push('/volunteer/my-orders')
      break
    case 'address-book':
      router.push('/volunteer/address-book')
      break
    case 'change-password':
      router.push('/volunteer/change-password')
      break
    case 'logout':
      clearRoleStorage('volunteer')
      localStorage.removeItem('userAvatar')
      ElMessage.success('退出成功')
      router.push('/login')
      break
    default:
      break
  }
}

</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #ce4c4c;
  margin-right: 40px;
  white-space: nowrap;
}

.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.nav-menu {
  flex: 1;
  border: none;
}

.nav-menu :deep(.el-menu-item) {
  border-bottom: none;
}

.avatar-wrapper {
  cursor: pointer;
  margin-left: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-wrapper:hover {
  opacity: 0.8;
}

.city-selector-wrapper {
  position: relative;
  margin-right: 20px;
}

.city-text {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.city-text:hover {
  background-color: #f5f5f5;
}

.city-selector-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 5px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 10px;
  width: 200px;
  z-index: 2000;
}

.selector-row {
  margin-bottom: 10px;
}

.selector-row:last-child {
  margin-bottom: 0;
}

.announcement-bar {
  display: flex;
  align-items: center;
  background: white;
  color: #ce4c4c;
  padding: 8px 20px;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.announcement-icon {
  font-size: 16px;
  margin-right: 10px;
  color: #ce4c4c;
  animation: ring 2s ease-in-out infinite;
}

@keyframes ring {
  0%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(-15deg); }
  20%, 40% { transform: rotate(15deg); }
}

.announcement-content {
  flex: 1;
  overflow: hidden;
  height: 20px;
  line-height: 20px;
  font-size: 14px;
}

.announcement-content span {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.5s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.main-content {
  flex: 1;
  background: #f0f2f5;
  overflow-y: auto;
  padding: 0;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 14px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  color: #606266;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #ce4c4c;
}
</style>