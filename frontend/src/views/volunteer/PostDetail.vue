        <template>
  <div class="post-detail">
    <el-card v-loading="loading">
      <div class="back-row">
        <el-button text type="primary" @click="goBack">
          ← 返回论坛
        </el-button>
      </div>
      <div class="header">
        <div class="title-row">
          <h2 class="title centered-title">{{ post.title }}</h2>
          <div v-if="post.sharedFromPostId" class="shared-indicator-right">
            <el-icon><Share /></el-icon>
            <span>转发</span>
          </div>
        </div>
        <div class="meta-row">
          <div class="meta-left">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ post.username || post.author || '匿名志愿者' }}</span>
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(post.createdAt) }}</span>
            </span>
            <span class="meta-item" v-if="post.category">
              <el-icon><Flag /></el-icon>
              <span>{{ post.category }}</span>
            </span>
          </div>
          <div class="meta-right">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              <span>{{ post.views || 0 }} 阅读</span>
            </span>
            <span class="meta-item" v-if="interaction.likes !== null">
              <el-icon>
                <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M512 896c-17.664 0-34.176-7.04-46.528-19.84L139.2 539.52C50.176 450.496 50.176 304.64 139.2 215.616c89.024-89.024 234.88-89.024 323.904 0l48.896 48.896 48.896-48.896c89.024-89.024 234.88-89.024 323.904 0 89.024 89.024 89.024 234.88 0 323.904L558.528 876.16C546.176 888.96 529.664 896 512 896z" fill="#ff4d4f"/>
                </svg>
              </el-icon>
              <span>{{ interaction.likes || 0 }} 喜欢</span>
            </span>
            <span class="meta-item" v-if="interaction.comments !== null">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ interaction.comments }} 评论</span>
            </span>
          </div>
        </div>
      </div>

      <div class="cover" v-if="post.imageUrl">
        <el-image :src="fullImageUrl(post.imageUrl)" fit="cover" />
      </div>

      <div class="content" v-if="post.content">
        <div class="content-inner" v-html="renderContent(post.content)"></div>
      </div>

      <div class="extra-info">
        <span>来源：{{ post.category || '志愿论坛' }}</span>
        <span>编辑：{{ post.username || post.author || '志愿者' }}</span>
        <span>审核状态：{{ getStatusText(post.status) }}</span>
      </div>

      <div class="interaction-bar">
        <el-button
          class="action-btn like-btn"
          :class="{ 'is-liked': interaction.liked }"
          @click="toggleLike"
        >
          <el-icon class="icon heart-icon" :class="{ 'is-active': interaction.liked }">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path d="M512 896c-17.664 0-34.176-7.04-46.528-19.84L139.2 539.52C50.176 450.496 50.176 304.64 139.2 215.616c89.024-89.024 234.88-89.024 323.904 0l48.896 48.896 48.896-48.896c89.024-89.024 234.88-89.024 323.904 0 89.024 89.024 89.024 234.88 0 323.904L558.528 876.16C546.176 888.96 529.664 896 512 896z" />
            </svg>
          </el-icon>
          <span class="text">{{ interaction.liked ? '已喜欢' : '喜欢' }}</span>
          <span class="count" v-if="interaction.likes > 1">{{ interaction.likes }}</span>
        </el-button>
        <el-button
          class="action-btn favorite-btn"
          :class="{ 'is-favorited': interaction.favorited }"
          @click="toggleFavorite"
        >
          <el-icon class="icon star-icon" :class="{ 'is-active': interaction.favorited }">
            <Star />
          </el-icon>
          <span class="text">{{ interaction.favorited ? '已收藏' : '收藏' }}</span>
          <span class="count" v-if="interaction.favorites > 1">{{ interaction.favorites }}</span>
        </el-button>
        <el-button
          class="action-btn share-btn"
          :class="{ 'is-shared': hasShared }"
          :disabled="hasShared"
          @click="sharePost"
        >
          <el-icon class="icon arrow-icon" :class="{ 'is-active': hasShared }">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path d="M714.24 170.666667a42.666667 42.666667 0 0 1 60.330667 0l170.666666 170.666666a42.666667 42.666667 0 0 1 0 60.330667l-170.666666 170.666667a42.666667 42.666667 0 1 1-60.330667-60.330667L798.293333 426.666667H341.333333c-94.293333 0-170.666667 76.373333-170.666666 170.666666v213.333334a42.666667 42.666667 0 1 1-85.333334 0v-213.333334c0-141.141333 114.858667-256 256-256h456.96L714.24 230.997333a42.666667 42.666667 0 0 1 0-60.330666z" />
            </svg>
          </el-icon>
          <span class="text">{{ hasShared ? '已转发' : '转发' }}</span>
        </el-button>
      </div>

      <div class="comments-section">
        <div class="comments-header">
          <h3>评论区</h3>
          <div class="comment-sort">
            <span
              :class="['sort-item', { active: sortType === 'latest' }]"
              @click="sortType = 'latest'"
            >最新</span>
            <span
              :class="['sort-item', { active: sortType === 'hottest' }]"
              @click="sortType = 'hottest'"
            >最热</span>
          </div>
        </div>

        <div :class="['comment-editor', { 'is-fullscreen': isFullscreenEditor }]">
          <div class="comment-toolbar">
            <div class="toolbar-left">
              <span class="toolbar-icon" @click="toggleEmojiPanel">😊</span>
              <el-upload
                :action="uploadUrl"
                name="file"
                :show-file-list="false"
                :on-success="handleGifSuccess"
                :before-upload="beforeGifUpload"
                accept="image/gif"
                class="toolbar-upload gif-upload"
              >
                <span class="toolbar-icon gif">GIF</span>
              </el-upload>
              <span class="toolbar-icon" @click="showPasteHint">✂</span>
              <span class="toolbar-icon">📁</span>
              <el-upload
                :action="uploadUrl"
                name="file"
                :show-file-list="false"
                :on-success="handleImageSuccess"
                :before-upload="beforeImageUpload"
                accept="image/*"
                class="toolbar-upload"
              >
                <span class="toolbar-icon">
                  🖼
                </span>
              </el-upload>
              <span class="toolbar-icon">⋯</span>
            </div>
            <div class="toolbar-right">
              <span class="toolbar-icon" @click="toggleEditorFullscreen">⤢</span>
              <span class="toolbar-icon" @click="toggleScheduleMode">🕒</span>
            </div>
          </div>

          <div v-if="showEmojiPanel" class="emoji-panel">
            <span v-for="e in emojiList" :key="e" class="emoji" @click="appendEmoji(e)">{{ e }}</span>
          </div>

          <el-input
            v-model="newComment"
            type="textarea"
            :rows="4"
            placeholder="写下你的想法，可以插入表情和图片..."
            @paste="handlePaste"
          />

          <div v-if="newCommentImages.length" class="comment-image-preview">
            <el-image
              v-for="(img, idx) in newCommentImages"
              :key="img + idx"
              :src="img"
              :preview-src-list="newCommentImages"
              :initial-index="idx"
              fit="cover"
              class="comment-image-thumb"
            />
          </div>

          <div class="comment-footer">
            <div class="footer-right">
              <el-button size="small" @click="resetComment">关闭</el-button>
              <el-button type="primary" size="small" @click="submitComment">发送</el-button>
            </div>
          </div>
        </div>

        <div v-if="comments.length === 0" class="empty-comments">
          <el-empty description="还没有评论，抢沙发~" />
        </div>

        <div v-else class="comments-list">
          <div v-for="comment in sortedRootComments" :key="comment.id" class="comment-item">
            <div class="comment-main">
              <div class="comment-left">
                <el-avatar 
                  :key="comment.id + '-' + comment.avatar" 
                  :size="32" 
                  :src="getFullAvatarUrl(comment.avatar)"
                  fit="cover"
                >
                  {{ (comment.userName || '志')[0] }}
                </el-avatar>
                <div class="comment-author">{{ comment.userName || '志愿者' }}</div>
              </div>
              <div class="comment-right">
                <el-button 
                  text 
                  size="small" 
                  :class="{ 'is-liked': comment.liked }"
                  @click="toggleCommentLike(comment)"
                  class="like-btn"
                >
                  <el-icon>
                    <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                      <path d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3.1-35.3-7-69.6-20.9-101.9z"/>
                    </svg>
                  </el-icon>
                  {{ comment.likes || 0 }}
                </el-button>
              </div>
            </div>
            <div
              class="comment-content"
              v-html="renderContent(comment.content)"
              @click="handleCommentContentClick($event)"
            ></div>
            <div class="comment-time-bottom">{{ formatCommentTime(comment.createdAt) }}</div>
            <div class="comment-actions">
              <el-button text size="small" @click="startReply(comment)">回复</el-button>
              <el-button
                v-if="currentVolunteerId && comment.volunteerId === currentVolunteerId"
                text
                size="small"
                type="danger"
                @click="removeComment(comment)"
              >删除</el-button>
            </div>

            <div v-if="comment.replies && comment.replies.length" class="replies">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <div class="reply-main">
                  <div class="reply-left">
                    <el-avatar 
                      :key="reply.id + '-' + reply.avatar" 
                      :size="28" 
                      :src="getFullAvatarUrl(reply.avatar)"
                      fit="cover"
                    >
                      {{ (reply.userName || '志')[0] }}
                    </el-avatar>
                    <div class="reply-author">{{ reply.userName || '志愿者' }}</div>
                  </div>
                  <div class="reply-right">
                    <el-button 
                      text 
                      size="small" 
                      :class="{ 'is-liked': reply.liked }"
                      @click="toggleCommentLike(reply)"
                      class="like-btn"
                    >
                      <el-icon>
                        <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                          <path d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3.1-35.3-7-69.6-20.9-101.9z"/>
                        </svg>
                      </el-icon>
                      {{ reply.likes || 0 }}
                    </el-button>
                  </div>
                </div>
                <div
                  class="reply-content"
                  v-html="renderContent(reply.content)"
                  @click="handleCommentContentClick($event)"
                ></div>
                <div class="reply-time-bottom">{{ formatCommentTime(reply.createdAt) }}</div>
                <div class="reply-actions">
                  <el-button
                    v-if="currentVolunteerId && reply.volunteerId === currentVolunteerId"
                    text
                    size="small"
                    type="danger"
                    @click="removeComment(reply)"
                  >删除</el-button>
                </div>
              </div>
            </div>

            <div v-if="replyingTo && replyingTo.id === comment.id" class="reply-editor">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="3"
                placeholder="回复 {{ comment.userName || '志愿者' }}..."
              />
              <div v-if="replyImages.length" class="comment-image-preview">
                <el-image
                  v-for="(img, idx) in replyImages"
                  :key="img + idx"
                  :src="img"
                  :preview-src-list="replyImages"
                  :initial-index="idx"
                  fit="cover"
                  class="comment-image-thumb"
                />
              </div>
              <div class="reply-toolbar">
                <div class="emoji-list">
                  <span v-for="e in emojiList" :key="e" class="emoji" @click="appendReplyEmoji(e)">{{ e }}</span>
                </div>
                <el-upload
                  :action="uploadUrl"
                  name="file"
                  :show-file-list="false"
                  :on-success="handleReplyImageSuccess"
                  :before-upload="beforeImageUpload"
                  accept="image/*"
                  class="toolbar-upload"
                >
                  <span class="toolbar-icon">🖼</span>
                </el-upload>
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button type="primary" size="small" @click="submitReply">发送回复</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-image-viewer
        v-if="showImageViewer"
        :url-list="previewImages"
        :initial-index="previewIndex"
        @close="showImageViewer = false"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Clock, Folder, View, ChatDotRound, Star, Share, Flag } from '@element-plus/icons-vue'
import { getForumPostDetail, getPostInteraction, likePost, unlikePost, favoritePost, unfavoritePost } from '@/api/forum'
import { getCommentsByTarget, addComment, deleteComment as deleteCommentApi, likeComment, unlikeComment } from '@/api/comment'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const post = ref({})
const interaction = ref({ likes: null, favorites: null, comments: null, liked: false, favorited: false })
const comments = ref([])

const newComment = ref('')
const newCommentImages = ref([])
const replyingTo = ref(null)
const replyContent = ref('')
const replyImages = ref([])

const emojiList = [
  '😀', '😁', '😂', '🤣', '😅', '😊', '😍', '😘', '😎',
  '😭', '😡', '😱', '👍', '🙏', '👏', '🎉', '🌟', '💪',
  '❤️', '✨', '🍀', '🎓', '📚', '🚀'
]
const uploadUrl = 'http://localhost:8080/api/upload/image'
const hasShared = ref(false)
const showEmojiPanel = ref(false)
const sortType = ref('latest')
const currentVolunteerId = ref(null)
const isFullscreenEditor = ref(false)
const isScheduleMode = ref(false)

const showImageViewer = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

const rootComments = computed(() => {
  const roots = comments.value.filter(c => !c.parentId)
  const map = {}
  comments.value.forEach(c => {
    map[c.id] = { ...c, replies: [] }
  })
  comments.value.forEach(c => {
    if (c.parentId && map[c.parentId]) {
      map[c.parentId].replies.push(map[c.id] || c)
    }
  })
  return roots.map(c => map[c.id] || c)
})

const sortedRootComments = computed(() => {
  const list = [...rootComments.value]
  if (sortType.value === 'hottest') {
    return list.sort((a, b) => {
      // 按点赞数排序，点赞数相同则按回复数，最后按时间
      const likesA = a.likes || 0
      const likesB = b.likes || 0
      if (likesB !== likesA) return likesB - likesA
      
      const ra = a.replies ? a.replies.length : 0
      const rb = b.replies ? b.replies.length : 0
      if (rb !== ra) return rb - ra
      
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    })
  }
  // 最新：按时间倒序
  return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
})

const decodeTokenPayload = (token) => {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const base = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base.padEnd(base.length + (4 - (base.length % 4 || 4)) % 4, '=')
    const json = atob(padded)
    return JSON.parse(json)
  } catch (e) {
    return null
  }
}

const toggleEditorFullscreen = () => {
  isFullscreenEditor.value = !isFullscreenEditor.value
}

const toggleScheduleMode = () => {
  isScheduleMode.value = !isScheduleMode.value
  if (isScheduleMode.value) {
    ElMessage.info('已开启定时发布，点击发送后将延迟 10 秒发布评论')
  } else {
    ElMessage.info('已关闭定时发布')
  }
}

const handlePaste = async (event) => {
  const items = event.clipboardData?.items || []
  for (const item of items) {
    if (item.type && item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (!file) continue
      if (!beforeImageUpload(file)) return
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await request.post('/upload/image', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (res.code === 200 && res.data?.url) {
          const url = fullImageUrl(res.data.url)
          newCommentImages.value.push(url)
          ElMessage.success('截图已添加到评论')
        } else {
          ElMessage.error(res.message || '截图上传失败')
        }
      } catch (e) {
        ElMessage.error('截图上传失败')
      }
      break
    }
  }
}

const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value
}

const resetComment = () => {
  newComment.value = ''
  newCommentImages.value = []
  showEmojiPanel.value = false
}

const showPasteHint = () => {
  ElMessage.info('可以在输入框使用 Ctrl+V 粘贴截图，我们会自动上传并添加到评论中')
}

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
    if (token) {
      const payload = decodeTokenPayload(token)
      return payload?.userId || null
    }
    return null
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
    const mi = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${mi}`
  } catch (e) {
    return time
  }
}

const fullImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const commentAvatar = (comment) => {
  if (!comment || !comment.avatar) return '/images/logo.jpg'
  return fullImageUrl(comment.avatar)
}

const renderContent = (content) => {
  if (!content) return ''
  // 如果包含HTML标签（富文本），直接渲染，否则仅将换行转换为<br/>
  if (/<[a-z][\s\S]*>/i.test(content)) {
    return content
  }
  return content.replace(/\n/g, '<br/>')
}

const getStatusText = (status) => {
  const statusMap = {
    'draft': '草稿',
    'pending': '待审核',
    'approved': '通过审核',
    'published': '通过审核',
    'rejected': '审核未通过'
  }
  return statusMap[status] || '未知状态'
}

const handleCommentContentClick = (event) => {
  const target = event.target
  if (!target || target.tagName !== 'IMG') return

  const src = target.getAttribute('src')
  if (!src) return

  const container = target.closest('.comment-content, .reply-content')
  let urls = [src]
  if (container) {
    const imgs = Array.from(container.querySelectorAll('img'))
    urls = imgs
      .map(img => img.getAttribute('src'))
      .filter(Boolean)
  }

  previewImages.value = urls
  previewIndex.value = urls.indexOf(src)
  if (previewIndex.value < 0) previewIndex.value = 0
  showImageViewer.value = true
}

const loadPost = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getForumPostDetail(id)
    post.value = res.data || {}
  } catch (e) {
    ElMessage.error('加载帖子详情失败')
  } finally {
    loading.value = false
  }
}

const loadInteraction = async () => {
  try {
    const id = route.params.id
    const volunteerId = getVolunteerId()
    const res = await getPostInteraction(id, volunteerId)
    interaction.value = {
      likes: res.data.likes,
      favorites: res.data.favorites,
      comments: res.data.comments,
      liked: !!res.data.liked,
      favorited: !!res.data.favorited
    }
  } catch (e) {
    // 允许失败，只做提示
    console.error(e)
  }
}

const loadComments = async () => {
  try {
    const id = route.params.id
    const volunteerId = getVolunteerId()
    const res = await getCommentsByTarget('帖子', id, volunteerId)
    comments.value = res.data || []
  } catch (e) {
    ElMessage.error('加载评论失败')
  }
}

const appendEmoji = (emoji) => {
  newComment.value += emoji
}

const appendReplyEmoji = (emoji) => {
  replyContent.value += emoji
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleImageSuccess = (response) => {
  if (response.code === 200 && response.data?.url) {
    const url = fullImageUrl(response.data.url)
    newCommentImages.value.push(url)
    ElMessage.success('图片已添加到评论')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleReplyImageSuccess = (response) => {
  if (response.code === 200 && response.data?.url) {
    const url = fullImageUrl(response.data.url)
    replyImages.value.push(url)
    ElMessage.success('图片已添加到回复')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const beforeGifUpload = (file) => {
  if (file.type !== 'image/gif') {
    ElMessage.error('请选择 GIF 图片文件')
    return false
  }
  return beforeImageUpload(file)
}

const handleGifSuccess = (response) => {
  handleImageSuccess(response)
}

const buildCommentContent = () => {
  let html = ''
  if (newComment.value.trim()) {
    html = newComment.value.replace(/\n/g, '<br/>')
  }
  if (newCommentImages.value.length) {
    const imgsHtml = newCommentImages.value
      .map(url => `<img src="${url}" style="max-width: 100%; display: block; margin: 6px 0;" />`)
      .join('')
    html += (html ? '<br/>' : '') + imgsHtml
  }
  return html
}

const buildReplyContent = () => {
  let html = ''
  if (replyContent.value.trim()) {
    html = replyContent.value.replace(/\n/g, '<br/>')
  }
  if (replyImages.value.length) {
    const imgsHtml = replyImages.value
      .map(url => `<img src="${url}" style="max-width: 100%; display: block; margin: 6px 0;" />`)
      .join('')
    html += (html ? '<br/>' : '') + imgsHtml
  }
  return html
}

const doSubmitComment = async (content) => {
  const volunteerId = getVolunteerId()
  if (!volunteerId) {
    ElMessage.error('请先登录')
    return
  }
  try {
    await addComment({
      volunteerId,
      targetType: '帖子',
      targetId: Number(route.params.id),
      content,
      parentId: null
    })
    ElMessage.success('评论成功')
    newComment.value = ''
    newCommentImages.value = []
    await loadComments()
    await loadInteraction()
  } catch (e) {
    ElMessage.error('评论失败')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim() && !newCommentImages.value.length) {
    ElMessage.warning('请先输入评论内容或上传图片')
    return
  }
  const contentSnapshot = buildCommentContent()
  if (isScheduleMode.value) {
    ElMessage.success('已开启定时发布，10 秒后自动发送评论')
    setTimeout(() => {
      if (contentSnapshot && contentSnapshot.trim()) {
        doSubmitComment(contentSnapshot)
      }
    }, 10000)
    return
  }
  await doSubmitComment(contentSnapshot)
}

const startReply = (comment) => {
  replyingTo.value = comment
  const name = comment.userName || '志愿者'
  replyContent.value = `@${name} `
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
  replyImages.value = []
}

const submitReply = async () => {
  if (!replyingTo.value) return
  if (!replyContent.value.trim() && !replyImages.value.length) {
    ElMessage.warning('请先输入回复内容或上传图片')
    return
  }
  const volunteerId = getVolunteerId()
  if (!volunteerId) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const contentSnapshot = buildReplyContent()
    await addComment({
      volunteerId,
      targetType: '帖子',
      targetId: Number(route.params.id),
      content: contentSnapshot,
      parentId: replyingTo.value.id
    })
    ElMessage.success('回复成功')
    replyingTo.value = null
    replyContent.value = ''
    replyImages.value = []
    await loadComments()
    await loadInteraction()
  } catch (e) {
    ElMessage.error('回复失败')
  }
}

const removeComment = async (item) => {
  if (!item || !item.id) return
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCommentApi(item.id)
    ElMessage.success('删除成功')
    await loadComments()
    await loadInteraction()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const toggleLike = async () => {
  const volunteerId = getVolunteerId()
  if (!volunteerId) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const id = Number(route.params.id)
    if (interaction.value.liked) {
      await unlikePost({ postId: id, volunteerId })
    } else {
      await likePost({ postId: id, volunteerId })
    }
    await loadInteraction()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const toggleFavorite = async () => {
  const volunteerId = getVolunteerId()
  if (!volunteerId) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const id = Number(route.params.id)
    if (interaction.value.favorited) {
      await unfavoritePost({ postId: id, volunteerId })
    } else {
      await favoritePost({ postId: id, volunteerId })
    }
    await loadInteraction()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const sharePost = async () => {
  if (hasShared.value) return
  
  try {
    await ElMessageBox.confirm('确认要转发该帖子吗？转发后将在您的帖子列表中显示。', '转发确认', {
      confirmButtonText: '确认转发',
      cancelButtonText: '取消',
      type: 'info'
    })

    const volunteerId = getVolunteerId()
    if (!volunteerId) {
      ElMessage.error('请先登录')
      return
    }

    try {
      // 调用转发API
      await request.post('/forum-post/share', {
        postId: Number(route.params.id),
        volunteerId
      })
      
      hasShared.value = true
      ElMessage.success('转发成功！已添加到您的帖子列表')
    } catch (error) {
      ElMessage.error(error.message || '转发失败')
    }
  } catch (e) {
    // 用户取消转发
  }
}

const goBack = () => {
  router.push('/volunteer/forum')
}

// 获取完整的头像URL
const getFullAvatarUrl = (avatar) => {
  if (!avatar || avatar === null || avatar === undefined) {
    return '' // 返回空字符串，让el-avatar显示默认内容
  }
  // 处理base64格式的头像
  if (typeof avatar === 'string' && avatar.startsWith('data:image/')) {
    return avatar
  }
  // 处理完整URL
  if (typeof avatar === 'string' && avatar.startsWith('http')) {
    return avatar
  }
  // 处理相对路径
  return `http://localhost:8080${avatar}`
}

// 格式化评论时间
const formatCommentTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const commentTime = new Date(time)
  const diffMs = now.getTime() - commentTime.getTime()
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const diffMonths = Math.floor(diffDays / 30)
  
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) {
    if (diffHours < 1) return '刚刚'
    return `今天 ${commentTime.getHours().toString().padStart(2, '0')}:${commentTime.getMinutes().toString().padStart(2, '0')}`
  }
  if (diffDays === 1) {
    return `昨天 ${commentTime.getHours().toString().padStart(2, '0')}:${commentTime.getMinutes().toString().padStart(2, '0')}`
  }
  if (diffDays < 7) return `${diffDays}天前`
  if (diffMonths < 1) return `${Math.floor(diffDays / 7)}周前`
  if (diffMonths < 12) return `${diffMonths}个月前`
  return `${Math.floor(diffMonths / 12)}年前`
}

// 切换评论点赞
const toggleCommentLike = async (comment) => {
  const volunteerId = getVolunteerId()
  if (!volunteerId) {
    ElMessage.error('请先登录')
    return
  }

  try {
    const originalLiked = comment.liked
    const originalLikes = comment.likes || 0
    
    // 乐观更新UI
    comment.liked = !originalLiked
    comment.likes = originalLiked ? Math.max(0, originalLikes - 1) : originalLikes + 1
    
    // 调用后端API
    if (originalLiked) {
      await unlikeComment(comment.id, volunteerId)
    } else {
      await likeComment(comment.id, volunteerId)
    }
    
    // 重新加载评论数据以确保数据一致性
    await loadComments()
    
  } catch (error) {
    // 如果API调用失败，回滚UI更新
    comment.liked = originalLiked
    comment.likes = originalLikes
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(async () => {
  if (!route.params.id) {
    router.push('/volunteer/forum')
    return
  }
  currentVolunteerId.value = getVolunteerId()
  await loadPost()
  await loadInteraction()
  await loadComments()
})
</script>

<style scoped>
.post-detail {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.post-detail :deep(.el-card) {
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  border: none;
  padding: 22px 26px 26px;
  background: #ffffff;
}

.header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
  margin-bottom: 22px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 8px;
}

.shared-indicator-right {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background-color: #f0f5ff;
  border: 1px solid #adc6ff;
  border-radius: 12px;
  color: #597ef7;
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0;
}

.shared-indicator-right .el-icon {
  font-size: 14px;
}

.title {
  margin: 0;
  font-size: 24px;
  color: #333;
  flex: 1;
  text-align: left;
}

.centered-title {
  text-align: center !important;
  width: 100%;
}

.back-row {
  margin-bottom: 4px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.meta-left,
.meta-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #888;
}

.cover {
  margin: 16px 0 20px;
  border-radius: 10px;
  overflow: hidden;
}

.cover :deep(.el-image) {
  width: 100%;
  max-height: 360px;
  border-radius: 0;
}

.content {
  margin-bottom: 20px;
}

.content-inner {
  line-height: 1.8;
  color: #444;
  font-size: 15px;
  max-width: 900px;
  margin: 0 auto;
}

.content-inner :deep(img) {
  max-width: 80%;
  width: 80%;
  height: auto;
  max-height: 460px;
  display: block;
  margin: 12px auto;
  border-radius: 8px;
}

.extra-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
}

.interaction-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
}

.interaction-bar .action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.action-btn .icon {
  display: inline-flex;
  align-items: center;
}

.action-btn .thumb {
  font-size: 18px;
}

.action-btn .count {
  margin-left: 4px;
  font-size: 13px;
  color: #888;
}

/* Heart icon styles */
.heart-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.heart-icon.is-active {
  color: #ff4d4f;
  animation: heartBeat 0.3s ease;
}

.like-btn.is-liked {
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.like-btn.is-liked:hover {
  background-color: #fff1f0;
}

/* Star icon styles */
.star-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.star-icon.is-active {
  color: #fadb14;
  animation: starRotate 0.3s ease;
}

.favorite-btn.is-favorited {
  color: #fadb14;
  border-color: #fadb14;
}

.favorite-btn.is-favorited:hover {
  background-color: #fffbe6;
}

/* Arrow icon styles */
.arrow-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.arrow-icon.is-active {
  color: #999;
}

.share-btn.is-shared {
  color: #999;
  border-color: #d9d9d9;
  background-color: #f5f5f5;
  cursor: not-allowed;
}

/* Animations */
@keyframes heartBeat {
  0%, 100% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.2);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes starRotate {
  0% {
    transform: rotate(0deg) scale(1);
  }
  50% {
    transform: rotate(180deg) scale(1.2);
  }
  100% {
    transform: rotate(360deg) scale(1);
  }
}

.comments-section {
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px solid #f2f2f2;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comments-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.comment-sort {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #888;
}

.sort-item {
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 12px;
}

.sort-item.active {
  background: #f5f5f5;
  color: #333;
}

.comment-editor {
  margin-bottom: 20px;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #e5e5e5;
  padding: 8px 10px 10px;
}

.comment-image-preview {
  margin-top: 8px;
}

.comment-image-thumb {
  width: 80px;
  height: 80px;
  margin-right: 6px;
  border-radius: 4px;
  overflow: hidden;
}

.comment-editor.is-fullscreen {
  position: fixed;
  inset: 60px 80px 80px 80px;
  z-index: 3000;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.comment-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 6px;
  border-bottom: 1px solid #eee;
  margin-bottom: 6px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.toolbar-icon {
  font-size: 16px;
  color: #666;
  cursor: pointer;
  padding: 2px 4px;
}

.toolbar-icon.gif {
  font-size: 12px;
  border-radius: 3px;
  border: 1px solid #ccc;
}

.toolbar-upload :deep(.el-upload) {
  display: inline-flex;
}

.gif-upload :deep(.el-upload) {
  display: inline-flex;
}

.emoji-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 4px 0 6px;
}

.emoji {
  cursor: pointer;
  font-size: 18px;
}

.comment-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.footer-right {
  display: flex;
  gap: 8px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #f3f3f3;
  background: #fafafa;
}

.comment-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.comment-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.comment-author {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.comment-time-bottom {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  margin-bottom: 4px;
}

.comment-right {
  flex-shrink: 0;
}

.like-btn {
  color: #666;
  font-size: 13px;
}

.like-btn.is-liked {
  color: #ff4d4f;
}

.like-btn .el-icon svg {
  width: 16px;
  height: 16px;
}

.comment-content {
  font-size: 14px;
  color: #444;
  line-height: 1.7;
}

.comment-content :deep(img),
.reply-content :deep(img) {
  max-width: 260px;
  max-height: 260px;
  width: 100%;
  height: auto;
  object-fit: contain;
  display: block;
  margin: 6px 0;
  border-radius: 6px;
  cursor: pointer;
}

.comment-actions {
  margin-top: 6px;
  display: flex;
  gap: 8px;
  align-items: center;
}


.reply-actions {
  margin-top: 4px;
  display: flex;
  gap: 6px;
  align-items: center;
}

.replies {
  margin-top: 8px;
  padding-left: 16px;
  padding-top: 4px;
  border-left: 2px solid #e8e8e8;
}

.reply-item {
  margin-bottom: 6px;
}

.reply-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.reply-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.reply-author {
  font-weight: 500;
  color: #333;
  font-size: 13px;
}

.reply-time-bottom {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  margin-bottom: 3px;
}

.reply-right {
  flex-shrink: 0;
}

.reply-content {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
}

.reply-editor {
  margin-top: 10px;
}

.reply-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.empty-comments {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .meta-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .extra-info {
    flex-direction: column;
    gap: 4px;
  }

  .interaction-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
