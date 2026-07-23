<template>
  <div class="home">
    <!-- 轮播图和热点信息 -->
    <div class="top-section">
      <!-- 轮播图 -->
      <div class="carousel-wrapper">
        <el-carousel height="400px" type="card" class="carousel" :autoplay="true" :interval="4000">
          <el-carousel-item v-for="(item, index) in carouselImages" :key="`carousel-${currentImageIndex}-${index}`">
            <img :src="item" alt="轮播图" class="carousel-image" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 热点信息 -->
      <div class="news-section">
        <div class="news-header">
          <div class="news-header-left">
            <el-icon class="hot-icon" color="#ff4d4f"><Promotion /></el-icon>
            <span class="news-title">热点信息</span>
          </div>
          <el-button text type="primary" size="small" class="more-btn">
            更多 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="news-list">
          <transition-group name="news-slide">
            <div class="news-item" v-for="(news, index) in displayedNews" :key="news.id">
              <div class="news-content">
                <div class="news-text">{{ news.title }}</div>
                <div class="news-meta">
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ news.time }}
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ news.views }}
                  </span>
                </div>
              </div>
            </div>
          </transition-group>
        </div>
      </div>
    </div>

    <!-- 最新志愿活动 -->
    <el-card class="section-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">最新志愿活动</span>
          <el-button text type="primary" @click="$router.push('/volunteer/activities')">查看更多 →</el-button>
        </div>
      </template>
      <el-empty v-if="activities.length === 0" description="暂无活动" />
      <div class="activity-scroll-container" v-else ref="activityScrollRef">
        <el-card shadow="hover" class="activity-card" v-for="activity in activities" :key="activity.id">
          <h4>{{ activity.title }}</h4>
          <p class="activity-info">
            <el-icon><Location /></el-icon>
            {{ activity.address }}
          </p>
          <p class="activity-info">
            <el-icon><Clock /></el-icon>
            {{ formatTime(activity.startTime) }}
          </p>
          <el-button type="primary" size="small" @click="viewDetail(activity)">查看详情</el-button>
        </el-card>
      </div>
    </el-card>

    <!-- 志愿论坛 -->
    <el-card class="section-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">志愿论坛</span>
          <el-button text type="primary" @click="$router.push('/volunteer/forum')">查看更多 →</el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12" v-for="post in forumPosts" :key="post.id">
          <div class="post-card" @click="goPostDetail(post)">
            <div class="post-image">
              <el-image 
                :src="getPostImageUrl(post)" 
                fit="cover" 
                :lazy="true"
                @error="handleImageError"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="post-info">
              <h4 class="post-title">{{ post.title }}</h4>
              <p class="post-content">{{ truncateContent(post.content) }}</p>
              <div class="post-meta">
                <span><el-icon><Flag /></el-icon> {{ post.category || '未分类' }}</span>
                <span><el-icon><View /></el-icon> {{ post.views || 0 }}</span>
                <span><el-icon><Clock /></el-icon> {{ formatTime(post.createdAt || post.createTime) }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getActivityList } from '@/api/activity'
import { getForumPostList } from '@/api/forum'
import { getHotNews } from '@/api/news'
import { Picture, Folder, View, Clock, Location, Promotion, ArrowRight, Flag } from '@element-plus/icons-vue'

const router = useRouter()

// 轮播图
const carouselImages = ref([])
const allCarouselImages = ref([]) // 存储所有轮播图
const currentImageIndex = ref(0) // 当前显示的起始索引
let carouselRotateTimer = null

// 获取轮播图数据
const loadCarouselImages = async () => {
  try {
    const response = await fetch('/api/carousel/list')
    const result = await response.json()
    if (result.code === 200 && result.data) {
      allCarouselImages.value = result.data
        .filter(item => item.status === 1 && item.image) // 只显示启用且有图片的轮播图
        .map(item => getImageUrl(item.image))
      
      // 如果图片数量大于3张，启动轮换显示
      if (allCarouselImages.value.length > 3) {
        updateDisplayedImages()
        startImageRotation()
      } else {
        // 如果图片数量不超过3张，直接显示所有图片
        carouselImages.value = allCarouselImages.value
      }
    }
  } catch (error) {
    console.error('获取轮播图失败:', error)
    // 如果获取失败，使用默认图片
    carouselImages.value = [
      '/images/1.jpg',
      '/images/2.jpg',
      '/images/3.jpg'
    ]
  }
}

const loadHotNews = async () => {
  try {
    const res = await getHotNews(8)
    const list = res.data || []
    allNews.value = list.map(item => ({
      id: item.id,
      title: item.title,
      time: formatTime(item.createdAt),
      views: item.views || 0
    }))
  } catch (error) {
    console.error('加载热点信息失败:', error)
    allNews.value = []
  }

  // 数据加载完成后再启动轮播
  if (allNews.value.length) {
    newsStartIndex.value = 0
    startNewsRotation()
  }
}

// 更新显示的图片
const updateDisplayedImages = () => {
  const total = allCarouselImages.value.length
  if (total <= 3) {
    carouselImages.value = allCarouselImages.value
    return
  }
  
  const displayed = []
  for (let i = 0; i < 3; i++) {
    const index = (currentImageIndex.value + i) % total
    displayed.push(allCarouselImages.value[index])
  }
  carouselImages.value = displayed
}

// 启动图片轮换
const startImageRotation = () => {
  if (allCarouselImages.value.length <= 3) return
  
  carouselRotateTimer = setInterval(() => {
    currentImageIndex.value = (currentImageIndex.value + 1) % allCarouselImages.value.length
    updateDisplayedImages()
  }, 6000) // 每6秒轮换一次显示的图片组
}

// 获取图片完整URL
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return `http://localhost:8080${imagePath}`
}

// 热点信息
const allNews = ref([])

const newsStartIndex = ref(0)
const displaySize = 4
let newsTimer = null

const displayedNews = computed(() => {
  if (!allNews.value.length) return []
  const size = Math.min(displaySize, allNews.value.length)
  return allNews.value.slice(newsStartIndex.value, newsStartIndex.value + size)
})

const startNewsRotation = () => {
  if (newsTimer) {
    clearInterval(newsTimer)
    newsTimer = null
  }
  if (!allNews.value.length) return

  const total = allNews.value.length
  const maxIndex = Math.max(total - displaySize, 0)

  newsTimer = setInterval(() => {
    newsStartIndex.value = (newsStartIndex.value + 1) % (maxIndex + 1)
  }, 3000)
}

// 最新活动
const activities = ref([])
const activityScrollRef = ref(null)
let activityScrollTimer = null

// 论坛帖子
const forumPosts = ref([])

const truncateContent = (content) => {
  if (!content) return ''
  
  // 移除HTML标签，只保留纯文本
  const textContent = content.replace(/<[^>]*>/g, '').trim()
  
  const maxLength = 80
  return textContent.length > maxLength ? textContent.substring(0, maxLength) + '...' : textContent
}

const formatTime = (time) => {
  if (!time) return ''
  try {
    // 处理不同的时间格式
    if (time.includes('T')) {
      return time.substring(0, 16).replace('T', ' ')
    }
    // 如果是其他格式，尝试格式化
    const date = new Date(time)
    if (!isNaN(date.getTime())) {
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }).replace(/\//g, '-')
    }
    return time
  } catch (error) {
    console.error('时间格式化错误:', error)
    return time || ''
  }
}

// 获取帖子图片URL
const getPostImageUrl = (post) => {
  // 如果帖子有封面图片
  if (post.imageUrl) {
    return getImageUrl(post.imageUrl)
  }
  
  // 如果帖子内容中有图片，提取第一张
  if (post.content && post.content.includes('<img')) {
    const match = post.content.match(/<img[^>]+src="([^"]+)"/)
    if (match && match[1]) {
      return getImageUrl(match[1])
    }
  }
  
  // 默认图片
  return '/images/logo.jpg'
}

// 图片加载错误处理
const handleImageError = (e) => {
  console.warn('图片加载失败:', e)
}

const loadActivities = async () => {
  try {
    const res = await getActivityList()
    activities.value = (res.data || []).slice(0, 8)
  } catch (error) {
    console.error(error)
  }
}

const loadForumPosts = async () => {
  try {
    const res = await getForumPostList()
    const posts = (res.data || []).slice(0, 4)
    
    // 调试：打印帖子数据结构
    if (posts.length > 0) {
      console.log('论坛帖子数据结构:', posts[0])
    }
    
    forumPosts.value = posts
  } catch (error) {
    console.error('加载论坛帖子失败:', error)
  }
}

const viewDetail = (activity) => {
  router.push(`/volunteer/activity/${activity.id}`)
}

const goPostDetail = (post) => {
  router.push(`/volunteer/post/${post.id}`)
}

const startActivityScroll = () => {
  if (!activityScrollRef.value || activities.value.length <= 4) return
  
  activityScrollTimer = setInterval(() => {
    const container = activityScrollRef.value
    const scrollAmount = 300 // 每次滚动300px
    
    if (container.scrollLeft + container.clientWidth >= container.scrollWidth) {
      // 滚动到末尾，回到开始
      container.scrollTo({ left: 0, behavior: 'smooth' })
    } else {
      // 继续向右滚动
      container.scrollBy({ left: scrollAmount, behavior: 'smooth' })
    }
  }, 3000)
}

onMounted(() => {
  loadCarouselImages() // 加载轮播图
  loadActivities().then(() => {
    setTimeout(() => {
      startActivityScroll()
    }, 500)
  })
  loadForumPosts()
  loadHotNews()
})

onUnmounted(() => {
  if (newsTimer) {
    clearInterval(newsTimer)
  }
  if (activityScrollTimer) {
    clearInterval(activityScrollTimer)
  }
  if (carouselRotateTimer) {
    clearInterval(carouselRotateTimer)
  }
})
</script>

<style scoped>
.home {
  padding: 20px;
  background: #f5f5f5;
}

.top-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.carousel-wrapper {
  flex: 2;
  overflow: hidden;
  position: relative;
}

.carousel {
  border-radius: 8px;
  overflow: hidden;
  padding: 0 10px;
  height: 400px;
}

.carousel :deep(.el-carousel__container) {
  height: 100%;
}

.carousel :deep(.el-carousel__item) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  width: 55% !important;
  margin: 0 1px;
}

.carousel :deep(.el-carousel__item.is-active) {
  box-shadow: 0 8px 24px rgba(206, 76, 76, 0.3);
  width: 65% !important;
  transform: scale(1.1);
  z-index: 10;
}

.carousel :deep(.el-carousel__item.is-in-stage) {
  width: 50% !important;
  opacity: 0.85;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.news-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.news-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hot-icon {
  font-size: 18px;
  animation: flame 1.5s ease-in-out infinite;
}

@keyframes flame {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.news-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.more-btn {
  color: #ce4c4c;
  padding: 0;
}

.more-btn:hover {
  color: #d97373;
}

.news-list {
  flex: 1;
  padding: 20px;
  overflow: hidden;
  position: relative;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #ce4c4c;
}

.news-item {
  padding: 15px 0;
  border-bottom: none;
  cursor: pointer;
  transition: all 0.3s;
}

.news-item:hover {
  background: #fafafa;
  padding-left: 10px;
  padding-right: 10px;
  margin-left: -10px;
  margin-right: -10px;
  border-radius: 4px;
}


.news-content {
  width: 100%;
}

.news-text {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-item:hover .news-text {
  color: #ce4c4c;
}

.news-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item .el-icon {
  font-size: 14px;
}

.activity-scroll-container {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 10px 0;
  scroll-behavior: smooth;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.activity-scroll-container::-webkit-scrollbar {
  display: none;
}

.activity-card {
  min-width: 280px;
  flex-shrink: 0;
  transition: transform 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
}

.activity-card h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.activity-info {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.post-card {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
  cursor: pointer;
  height: 120px; /* 固定高度，确保所有卡片高度一致 */
  overflow: hidden;
}

.post-card:hover {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.post-image {
  flex-shrink: 0;
  width: 100px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  order: 1; /* 图片放在左侧 */
}

.post-image :deep(.el-image) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #909399;
  font-size: 24px;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0; /* 确保文本能正确截断 */
  order: 2; /* 内容放在右侧 */
}

.post-title {
  font-size: 15px;
  margin: 0 0 6px 0;
  color: #333;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 标题最多显示2行 */
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.post-content {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 内容最多显示2行 */
  -webkit-box-orient: vertical;
  flex: 1;
}

.post-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  flex-shrink: 0; /* 确保meta信息不被压缩 */
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.news-slide-move,
.news-slide-enter-active,
.news-slide-leave-active {
  transition: opacity 1.2s ease-in-out;
}

.news-slide-enter-from {
  opacity: 0;
}

.news-slide-leave-to {
  opacity: 0;
}

.news-slide-leave-active {
  position: absolute;
  width: calc(100% - 40px);
  left: 20px;
  z-index: 0;
  background: white;
}

.news-slide-enter-active {
  z-index: 1;
  background: white;
}
</style>