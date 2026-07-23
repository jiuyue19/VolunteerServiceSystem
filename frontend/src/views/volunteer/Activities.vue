<template>
  <div class="activities">
    <div class="header-section">
      <h2>活动中心</h2>
      <div class="date-selector-wrapper">
        <el-icon class="arrow-icon left" @click="scrollLeft" v-if="showLeftArrow">
          <ArrowLeft />
        </el-icon>
        <div class="date-selector" ref="dateScrollRef" @scroll="checkArrows">
          <div
            v-for="date in dateList"
            :key="date.value"
            :class="['date-item', { active: selectedDate === date.value }]"
            @click="toggleDate(date.value)"
          >
            <div class="weekday">{{ date.weekday }}</div>
            <div class="date">{{ date.display }}</div>
          </div>
        </div>
        <el-icon class="arrow-icon right" @click="scrollRight" v-if="showRightArrow">
          <ArrowRight />
        </el-icon>
      </div>
      <el-cascader
        v-model="selectedRegion"
        :options="regionData"
        :props="{ label: 'name', value: 'name', children: 'children' }"
        placeholder="选择地区"
        clearable
        class="region-selector"
        popper-class="region-cascader-popper"
      />
      <el-date-picker
        v-model="selectedTime"
        type="date"
        placeholder="选择日期"
        clearable
        class="time-selector"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
      />
    </div>
    
    <!-- 搜索和分类 -->
    <div class="filter-section">
      <div class="category-tabs">
        <el-button
          v-for="category in categories"
          :key="category.value"
          :type="activeCategory === category.value ? 'primary' : ''"
          @click="handleCategoryChange(category.value)"
        >
          {{ category.label }}
        </el-button>
      </div>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索活动名称"
        class="search-input"
        clearable
        @input="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 活动列表 -->
    <el-empty v-if="filteredActivities.length === 0" description="暂无活动" />
    <div class="activities-grid" v-else>
      <div class="activity-item" v-for="activity in paginatedActivities" :key="activity.id">
        <el-card shadow="hover" class="activity-card" @click="viewDetail(activity)">
          <img v-if="activity.coverImage" :src="activity.coverImage" class="cover-image" />
          <div class="activity-content">
            <h3>{{ activity.title }}</h3>
            <p><el-icon><Location /></el-icon> {{ activity.address }}</p>
            <p><el-icon><Clock /></el-icon> {{ formatDateTime(activity.startTime) }}</p>
            <p><el-icon><Clock /></el-icon> {{ formatDateTime(activity.endTime) }}</p>
            <p><el-icon><User /></el-icon> {{ activity.currentNumber }}/{{ activity.targetNumber }}</p>
            <div class="activity-footer">
              <el-tag size="small">{{ formatHours(activity.serviceHours) }}</el-tag>
              <el-tag type="success" size="small">{{ activity.rewardPoints }}积分</el-tag>
              <el-button type="primary" size="small" @click.stop="handleApply(activity)" v-if="canApply(activity)">报名</el-button>
              <el-tag :type="getStatusType(activity)" size="small">{{ getActivityStatus(activity) }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="filteredActivities.length > pageSize" class="pagination">
      <el-button :disabled="currentPage === 1" @click="prevPage">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <el-button :disabled="currentPage === totalPages" @click="nextPage">
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityList, applyActivity } from '@/api/activity'
import { getVolunteerInfo } from '@/api/volunteer'
import { regionData } from '@/utils/regions'

const router = useRouter()

const activities = ref([])
const searchKeyword = ref('')
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedDate = ref('')
const selectedTime = ref('')
const selectedRegion = ref([])
const dateScrollRef = ref(null)
const showLeftArrow = ref(false)
const showRightArrow = ref(false)

// 生成日期列表（当前月所有日期）
const dateList = computed(() => {
  const dates = []
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const today = now.getDate()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    const weekday = weekdays[date.getDay()]
    dates.push({
      value: `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
      display: `${month + 1}.${day}`,
      weekday: day === today ? '今天' : `周${weekday}`,
      isToday: day === today
    })
  }
  return dates
})

const categories = [
  { label: '全部', value: 'all' },
  { label: '社区服务', value: 1 },
  { label: '环境保护', value: 2 },
  { label: '支教助学', value: 3 },
  { label: '景区引导', value: 4 },
  { label: '禁毒宣传', value: 5 }
]

const loadActivities = async () => {
  try {
    const params = {}
    if (selectedDate.value) params.date = selectedDate.value
    if (selectedTime.value) params.date = selectedTime.value
    if (selectedRegion.value && selectedRegion.value.length > 0) {
      params.province = selectedRegion.value[0]
      if (selectedRegion.value[1]) params.city = selectedRegion.value[1]
      if (selectedRegion.value[2]) params.district = selectedRegion.value[2]
    }
    const res = await getActivityList(params)
    activities.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const toggleDate = (dateValue) => {
  selectedDate.value = selectedDate.value === dateValue ? '' : dateValue
}

const scrollLeft = () => {
  if (dateScrollRef.value) {
    dateScrollRef.value.scrollBy({ left: -200, behavior: 'smooth' })
  }
}

const scrollRight = () => {
  if (dateScrollRef.value) {
    dateScrollRef.value.scrollBy({ left: 200, behavior: 'smooth' })
  }
}

const checkArrows = () => {
  if (dateScrollRef.value) {
    const { scrollLeft, scrollWidth, clientWidth } = dateScrollRef.value
    showLeftArrow.value = scrollLeft > 0
    showRightArrow.value = scrollLeft + clientWidth < scrollWidth - 1
  }
}

watch([selectedDate, selectedTime, selectedRegion], () => {
  currentPage.value = 1
  loadActivities()
})

// 监听地区选择器的变化，切换城市后刷新页面
watch(selectedRegion, (newValue, oldValue) => {
  // 只在用户手动切换时才刷新（oldValue不为null）
  if (oldValue && oldValue.length > 0 && newValue && newValue.length > 0) {
    const oldCity = oldValue[1] || oldValue[0]
    const newCity = newValue[1] || newValue[0]
    if (oldCity !== newCity) {
      console.log('城市切换，刷新页面:', oldCity, '->', newCity)
      // 延迟刷新，确保数据已加载
      setTimeout(() => {
        window.location.reload()
      }, 300)
    }
  }
}, { deep: true })

// 筛选后的活动列表
const filteredActivities = computed(() => {
  let result = activities.value

  // 按分类筛选
  if (activeCategory.value !== 'all') {
    result = result.filter(activity => activity.categoryId === activeCategory.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    result = result.filter(activity =>
      activity.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }

  return result
})

// 分页后的活动列表
const paginatedActivities = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredActivities.value.slice(start, end)
})

const handleCategoryChange = (category) => {
  activeCategory.value = category
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const totalPages = computed(() => {
  return Math.ceil(filteredActivities.value.length / pageSize.value)
})

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

const viewDetail = (activity) => {
  router.push(`/volunteer/activity/${activity.id}`)
}

const handleApply = async (activity) => {
  try {
    const volunteerId = localStorage.getItem('userId')
    if (!volunteerId) {
      ElMessage.error('请先登录')
      return
    }
    await applyActivity({
      activityId: activity.id,
      volunteerId
    })
    ElMessage.success('报名成功，等待审核')
  } catch (error) {
    console.error(error)
    ElMessage.error('报名失败')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化小时数
const formatHours = (hours) => {
  if (!hours) return '0小时'
  const h = Math.floor(hours)
  const m = Math.round((hours - h) * 60)
  return m > 0 ? `${h}小时${m}分` : `${h}小时`
}

// 计算活动状态
const getActivityStatus = (activity) => {
  const now = new Date()
  const startTime = new Date(activity.startTime)
  const endTime = new Date(activity.endTime)
  
  if (activity.currentNumber >= activity.targetNumber) {
    return '已满员'
  }
  if (now < startTime) {
    return '未开始'
  }
  if (now >= startTime && now <= endTime) {
    return '进行中'
  }
  if (now > endTime) {
    return '已结束'
  }
  return '未开始'
}

// 获取状态对应的标签类型
const getStatusType = (activity) => {
  const status = getActivityStatus(activity)
  const typeMap = {
    '未开始': '',
    '进行中': 'success',
    '已结束': 'info',
    '已满员': 'warning'
  }
  return typeMap[status] || ''
}

// 判断是否可以报名
const canApply = (activity) => {
  const status = getActivityStatus(activity)
  return status === '未开始' || status === '进行中'
}

// 滚动到今天的日期
const scrollToToday = () => {
  nextTick(() => {
    if (dateScrollRef.value) {
      const todayIndex = dateList.value.findIndex(d => d.isToday)
      if (todayIndex !== -1) {
        const itemWidth = 80 // 日期项的大小
        const scrollPosition = Math.max(0, (todayIndex - 2) * itemWidth)
        dateScrollRef.value.scrollTo({ left: scrollPosition, behavior: 'smooth' })
      }
    }
  })
}

onMounted(async () => {
  // 直接加载活动列表，不再自动填充用户地址
  await loadActivities()
  nextTick(() => {
    checkArrows()
    scrollToToday()
  })
})
</script>

<style scoped>
.activities {
  padding: 20px;
}

.header-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  flex-shrink: 0;
}

.date-selector-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
  gap: 10px;
  min-width: 0;
}

.arrow-icon {
  font-size: 20px;
  color: #ce4c4c;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.3s;
}

.arrow-icon:hover {
  color: #d97373;
}

.date-selector {
  display: flex;
  gap: 10px;
  flex: 1;
  overflow-x: auto;
  padding: 5px 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.date-selector::-webkit-scrollbar {
  display: none;
}

.date-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  min-width: 60px;
  flex-shrink: 0;
  transition: all 0.3s;
}

.date-item:hover {
  border-color: #ce4c4c;
}

.date-item.active {
  background: #ce4c4c;
  color: white;
  border-color: #ce4c4c;
}

.weekday {
  font-size: 12px;
  margin-bottom: 4px;
}

.date {
  font-size: 14px;
  font-weight: 500;
}

.time-selector {
  width: 200px;
  flex-shrink: 0;
}

.region-selector {
  width: 250px;
  flex-shrink: 0;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.category-tabs {
  display: flex;
  gap: 10px;
  flex: 1;
}

.search-input {
  width: 300px;
  flex-shrink: 0;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.activity-item {
  width: 100%;
}

.activity-card {
  margin-bottom: 20px;
  height: 380px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
}

.activity-card :deep(.el-card__body) {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cover-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.activity-content {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

h3 {
  font-size: 16px;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

p {
  font-size: 14px;
  color: #666;
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 5px;
  margin-top: auto;
}

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

.region-cascader-popper {
  margin-top: 5px !important;
}
</style>