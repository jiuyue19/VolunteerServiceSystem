<template>
  <div class="my-favorites">
    <div class="header-row">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回论坛
      </el-button>
      <h2 class="page-title">我的收藏</h2>
      <div class="spacer"></div>
    </div>

    <el-empty v-if="!loading && posts.length === 0" description="暂无收藏的帖子">
      <el-button type="primary" @click="goToForum">去论坛逛逛</el-button>
    </el-empty>

    <el-card 
      v-for="post in posts" 
      :key="post.id" 
      class="post-card" 
      shadow="hover"
      v-loading="loading"
    >
      <div class="post-layout" @click="goDetail(post)">
        <div class="post-image">
          <el-image :src="fullImageUrl(post.imageUrl)" fit="cover" />
        </div>
        <div class="post-info">
          <div class="title-row">
            <h3 class="post-title">{{ post.title }}</h3>
            <div v-if="post.sharedFromPostId" class="shared-badge">
              <el-icon><Share /></el-icon>
              <span>转发</span>
            </div>
          </div>
          <p class="post-content">{{ truncateContent(post.content) }}</p>
          <div class="post-meta">
            <div class="meta-all-left">
              <div class="avatar-author">
                <el-avatar :size="24" :src="getAvatarUrl(post.avatar)">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="author-name">{{ post.username || post.author }}</span>
              </div>
              <span class="meta-item">
                <el-icon><Flag /></el-icon>
                {{ post.category }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ post.views || 0 }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(post.createdAt) }}
              </span>
            </div>
            <div class="meta-right">
              <el-button
                type="warning"
                :icon="Star"
                circle
                @click.stop="removeFavorite(post)"
                title="取消收藏"
              />
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Flag, View, Clock, Share, Star, ArrowLeft } from '@element-plus/icons-vue'
import { getFavoritePosts, unfavoritePost } from '@/api/forum'

const router = useRouter()
const loading = ref(false)
const posts = ref([])

const getVolunteerId = () => {
  try {
    // Try volunteer-prefixed storage first
    const stored = localStorage.getItem('volunteer_user')
    if (stored) {
      const user = JSON.parse(stored)
      if (user && user.id) return user.id
    }
    // Fallback to plain storage
    const plainStored = localStorage.getItem('user')
    if (plainStored) {
      const user = JSON.parse(plainStored)
      if (user && user.id) return user.id
    }
    // Try token-based approach
    const token = localStorage.getItem('volunteer_token') || localStorage.getItem('token')
    if (!token) return null
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const base = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base.padEnd(base.length + (4 - (base.length % 4 || 4)) % 4, '=')
    const json = atob(padded)
    const payload = JSON.parse(json)
    return payload?.userId || null
  } catch (e) {
    return null
  }
}

const formatTime = (time) => {
  if (!time) return ''
  try {
    const d = new Date(time)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${min}`
  } catch (e) {
    return time
  }
}

const truncateContent = (content) => {
  const maxLength = 100
  if (!content) return ''

  let text = content

  if (/<[a-z][\s\S]*>/i.test(content)) {
    text = text.replace(/<img[^>]*>/gi, '')
    text = text.replace(/<[^>]+>/g, '')
  }

  text = text.replace(/&nbsp;/g, ' ').trim()

  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const fullImageUrl = (path) => {
  if (!path) return '/images/logo.jpg'
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const getAvatarUrl = (avatar) => {
  if (!avatar || avatar === null || avatar === undefined) {
    return ''
  }
  if (typeof avatar === 'string' && avatar.startsWith('data:image/')) {
    return avatar
  }
  if (typeof avatar === 'string' && avatar.startsWith('http')) {
    return avatar
  }
  return `http://localhost:8080${avatar}`
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const volunteerId = getVolunteerId()
    if (!volunteerId) {
      ElMessage.error('请先登录')
      router.push('/login')
      return
    }
    const res = await getFavoritePosts({ volunteerId })
    if (res.code === 200) {
      posts.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载收藏列表失败')
    }
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (post) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这篇帖子吗？', '取消收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const volunteerId = getVolunteerId()
    if (!volunteerId) {
      ElMessage.error('请先登录')
      return
    }

    await unfavoritePost({ postId: post.id, volunteerId })
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消收藏失败')
    }
  }
}

const goDetail = (post) => {
  if (!post || !post.id) return
  router.push(`/volunteer/post/${post.id}`)
}

const goBack = () => {
  router.push('/volunteer/forum')
}

const goToForum = () => {
  router.push('/volunteer/forum')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.my-favorites {
  padding: 20px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  color: #ce4c4c;
  font-size: 24px;
  font-weight: bold;
}

.spacer {
  width: 100px;
}

.post-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.post-layout {
  display: flex;
  gap: 20px;
}

.post-image {
  flex-shrink: 0;
  width: 240px;
  height: 135px;
  border-radius: 8px;
  overflow: hidden;
}

.post-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.post-title {
  font-size: 18px;
  margin: 0;
  color: #333;
  font-weight: 600;
  flex: 1;
}

.post-content {
  color: #666;
  line-height: 1.6;
  margin: 0 0 15px 0;
  flex: 1;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.meta-all-left {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.meta-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
}

.meta-item .el-icon {
  font-size: 14px;
}

.shared-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: #f0f5ff;
  border: 1px solid #adc6ff;
  color: #597ef7;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  margin-bottom: 8px;
  width: fit-content;
}

.shared-badge .el-icon {
  font-size: 14px;
}
</style>
