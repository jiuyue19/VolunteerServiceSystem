<template>
  <div class="my-comments">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>我的评论</h2>
        </div>
      </template>
      
      <div class="page-content">
        <!-- 搜索和筛选栏 -->
        <div class="toolbar">
          <div class="search-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索评论内容..."
              clearable
              style="width: 300px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="filter-bar">
            <el-select v-model="typeFilter" placeholder="类型筛选" style="width: 120px" @change="handleFilter">
              <el-option label="全部" value="" />
              <el-option label="帖子评论" value="post" />
              <el-option label="活动评论" value="activity" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
              @change="handleFilter"
            />
          </div>
        </div>
        
        <!-- 评论列表 -->
        <div class="comments-list" v-loading="loading">
          <el-card 
            v-for="comment in paginatedComments" 
            :key="comment.id" 
            class="comment-card"
            shadow="hover"
          >
            <div class="comment-header">
              <div class="target-info">
                <el-tag :type="comment.type === 'post' ? 'primary' : 'success'" size="small">
                  {{ comment.type === 'post' ? '帖子评论' : '活动评论' }}
                </el-tag>
                <span class="target-title" @click="viewTarget(comment)">
                  {{ comment.targetTitle }}
                </span>
              </div>
              <div class="comment-time">
                {{ formatTime(comment.createTime) }}
              </div>
            </div>
            
            <div class="comment-content">
              <div v-if="isImageContent(comment.content)" class="image-content">
                <el-image 
                  :src="extractImageUrl(comment.content)" 
                  fit="contain" 
                  class="comment-image"
                  :preview-src-list="[extractImageUrl(comment.content)]"
                  preview-teleported
                />
              </div>
              <p v-else>{{ comment.content }}</p>
            </div>
            
            <div class="comment-meta">
              <div class="meta-left">
                <span class="likes" v-if="comment.likes > 0">
                  <el-icon><Star /></el-icon>
                  {{ comment.likes }} 个赞
                </span>
                <span class="replies" v-if="comment.replyCount > 0">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ comment.replyCount }} 条回复
                </span>
              </div>
              <div class="meta-right">
                <el-button size="small" @click="viewTarget(comment)">查看原文</el-button>
                <el-button size="small" type="danger" @click="deleteCommentHandler(comment)">删除</el-button>
              </div>
            </div>
            
            <!-- 回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies-section">
              <div class="replies-header">
                <span>回复 ({{ comment.replies.length }})</span>
              </div>
              <div class="replies-list">
                <div 
                  v-for="reply in comment.replies" 
                  :key="reply.id" 
                  class="reply-item"
                >
                  <div class="reply-author">{{ reply.authorName }}</div>
                  <div class="reply-content">{{ reply.content }}</div>
                  <div class="reply-time">{{ formatTime(reply.createTime) }}</div>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 空状态 -->
          <div v-if="!loading && filteredComments.length === 0" class="empty-state">
            <el-empty description="还没有发表任何评论">
              <el-button type="primary" @click="goToForum">去论坛看看</el-button>
            </el-empty>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination" v-if="totalPages > 1">
          <el-button :disabled="currentPage === 1" @click="prevPage">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
          <el-button :disabled="currentPage === totalPages" @click="nextPage">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-card>
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Star, ChatDotRound, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getMyComments, deleteComment } from '@/api/comment'
import { getRoleItem } from '@/utils/storage'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const typeFilter = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = 10
const comments = ref([])

// 计算属性
const filteredComments = computed(() => {
  let result = [...comments.value]
  
  // 搜索筛选
  if (searchKeyword.value) {
    result = result.filter(comment => 
      comment.content.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      comment.targetTitle.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  // 类型筛选
  if (typeFilter.value) {
    result = result.filter(comment => comment.type === typeFilter.value)
  }
  
  // 日期筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    result = result.filter(comment => {
      const commentDate = new Date(comment.createTime)
      return commentDate >= startDate && commentDate <= endDate
    })
  }
  
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredComments.value.length / pageSize) || 1
})

const paginatedComments = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredComments.value.slice(start, end)
})

// 方法
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const viewTarget = (comment) => {
  if (comment.type === 'post') {
    // 跳转到帖子详情页
    router.push(`/volunteer/post/${comment.targetId}`)
  } else {
    // 跳转到活动详情页
    router.push(`/volunteer/activity-detail?id=${comment.targetId}`)
  }
}


const deleteCommentHandler = async (comment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteComment(comment.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadComments()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

const goToForum = () => {
  router.push('/volunteer/forum')
}

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

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

// 判断内容是否为图片
const isImageContent = (content) => {
  return content && content.includes('<img') && content.includes('src=')
}

// 提取图片URL
const extractImageUrl = (content) => {
  if (!content) return ''
  const match = content.match(/src="([^"]+)"/)
  return match ? match[1] : ''
}

const loadComments = async () => {
  try {
    loading.value = true
    
    // 使用 getRoleItem 获取志愿者用户信息
    const user = getRoleItem('user', 'volunteer')
    const volunteerId = user?.id
    if (!volunteerId) {
      ElMessage.error('未获取到当前志愿者信息，请重新登录')
      return
    }
    
    const res = await getMyComments(volunteerId)
    if (res.code === 200) {
      // 转换数据格式以匹配前端显示需求
      comments.value = (res.data || []).map(comment => ({
        id: comment.id,
        type: comment.targetType === '帖子' ? 'post' : 'activity',
        targetId: comment.targetId,
        targetTitle: comment.postTitle || '未知标题',
        content: comment.content,
        likes: comment.likes || 0,
        replyCount: 0, // 暂时设为0，后续可以添加回复统计
        createTime: comment.createdAt,
        replies: [] // 暂时为空，后续可以添加回复功能
      }))
    } else {
      ElMessage.error(res.message || '获取评论失败')
    }
    
  } catch (error) {
    ElMessage.error('加载评论失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.my-comments {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.main-card {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #ce4c4c;
  font-size: 24px;
  font-weight: 600;
}

.page-content {
  padding: 20px 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.comments-list {
  min-height: 400px;
}

.comment-card {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.comment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.target-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.target-title {
  font-weight: 500;
  color: #333;
  cursor: pointer;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.target-title:hover {
  color: #ce4c4c;
}

.comment-time {
  font-size: 14px;
  color: #999;
}

.comment-content {
  margin-bottom: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.comment-content p {
  margin: 0;
  line-height: 1.6;
  color: #333;
}

.comment-content .image-content {
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 100%;
  overflow: hidden;
}

.comment-image {
  max-width: 400px;
  max-height: 300px;
  width: auto;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.comment-image:hover {
  transform: scale(1.02);
  cursor: pointer;
}

.comment-content :deep(.el-image__error) {
  font-size: 12px;
  color: #999;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.meta-left {
  display: flex;
  gap: 15px;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.meta-left span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-right {
  display: flex;
  gap: 8px;
}

.replies-section {
  border-top: 1px solid #eee;
  padding-top: 15px;
  margin-top: 10px;
}

.replies-header {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin-bottom: 10px;
}

.replies-list {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 10px;
}

.reply-item {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-author {
  font-size: 12px;
  font-weight: 500;
  color: #ce4c4c;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.4;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page-info {
  font-size: 14px;
  color: #606266;
  margin: 0 10px;
}

/* 全局样式 */
:deep(.el-button--primary) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-button--primary:hover) {
  background-color: #d97373;
  border-color: #d97373;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar,
  .filter-bar {
    justify-content: center;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .comment-meta {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .meta-right {
    justify-content: center;
    width: 100%;
  }
}
</style>
