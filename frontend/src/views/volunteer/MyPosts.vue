<template>
  <div class="my-posts">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>我的帖子</h2>
        </div>
      </template>
      
      <div class="page-content">
        <!-- 搜索和操作栏 -->
        <div class="toolbar">
          <div class="search-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索我的帖子..."
              clearable
              style="width: 300px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleCreatePost">发布新帖子</el-button>
          </div>
          
          <div class="filter-bar">
            <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 140px" @change="handleFilter">
              <el-option label="全部" value="" />
              <el-option label="已发布" value="published" />
              <el-option label="待审核" value="pending" />
              <el-option label="草稿" value="draft" />
              <el-option label="已驳回" value="rejected" />
            </el-select>
            <el-select v-model="categoryFilter" placeholder="分类筛选" style="width: 150px" @change="handleFilter">
              <el-option label="全部分类" value="" />
              <el-option label="志愿活动" value="志愿活动" />
              <el-option label="经验分享" value="经验分享" />
              <el-option label="问题求助" value="问题求助" />
            </el-select>
            <el-button type="info" plain class="draft-box-btn" @click="showDraftBox">
              <el-icon><Document /></el-icon>
              草稿箱
            </el-button>
          </div>
        </div>
        
        <!-- 帖子列表 -->
        <div class="posts-list" v-loading="loading">
          <el-card 
            v-for="post in paginatedPosts" 
            :key="post.id" 
            class="post-card"
            shadow="hover"
          >
            <div class="post-body">
              <div class="post-cover" v-if="post.imageUrl">
                <el-image
                  :src="fullImageUrl(post.imageUrl)"
                  fit="cover"
                  style="width: 160px; height: 100px; border-radius: 4px;"
                />
              </div>
              <div class="post-main">
                <div class="post-header">
                  <div class="title-wrapper">
                    <h3 class="post-title" @click="viewPost(post)">{{ post.title }}</h3>
                    <el-tag v-if="post.sharedFromPostId" type="info" size="small" class="shared-tag">
                      <el-icon><Share /></el-icon>
                      转发自 @{{ post.sharedFromAuthor || '志愿者' }}
                    </el-tag>
                  </div>
                  <div class="post-status">
                    <el-tag :type="statusTagType(post.status)">
                      {{ statusText(post.status) }}
                    </el-tag>
                  </div>
                </div>
                
                <div class="post-content">
                  <p class="post-excerpt">{{ truncateContent(post.content) }}</p>
                </div>
                
                <div class="post-meta">
                  <div class="meta-left">
                    <span class="category">{{ post.category || '未分类' }}</span>
                    <span class="create-time">{{ formatTime(post.createdAt) }}</span>
                  </div>
                  <div class="meta-right">
                    <span class="views">
                      <el-icon><View /></el-icon>
                      {{ post.views || 0 }}
                    </span>
                    <span class="comments">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ post.comments || 0 }}
                    </span>
                    <span class="likes">
                      <el-icon><Star /></el-icon>
                      {{ post.likes || 0 }}
                    </span>
                  </div>
                </div>
                <div v-if="post.status === 'rejected' && post.reviewReason" class="reject-reason">
                  驳回原因：{{ post.reviewReason }}
                </div>
                
                <div class="post-actions">
                  <el-button size="small" @click="editPost(post)">编辑</el-button>
                  <el-button size="small" type="success" @click="viewPost(post)">查看</el-button>
                  <el-button size="small" type="danger" @click="deletePost(post)">删除</el-button>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 空状态 -->
          <div v-if="!loading && filteredPosts.length === 0" class="empty-state">
            <el-empty description="还没有发布任何帖子">
              <el-button type="primary" @click="handleCreatePost">发布第一个帖子</el-button>
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

    <!-- 编辑帖子对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      title="编辑帖子" 
      width="700px"
      :close-on-click-modal="false"
      @close="handleEditDialogClose"
    >
      <el-form :model="editForm" label-width="80px" class="edit-form">
        <el-form-item label="分类">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类">
            <el-option label="志愿事迹" :value="1" />
            <el-option label="志愿寄语" :value="2" />
            <el-option label="志愿点滴" :value="3" />
            <el-option label="悸动心声" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="内容">
          <div class="editor-wrapper">
            <div ref="editorRef" class="quill-editor"></div>
          </div>
        </el-form-item>
        <el-form-item label="图片">
          <div class="image-upload-container">
            <!-- 已上传的图片预览 -->
            <div v-if="editForm.imageUrl" class="image-preview-wrapper">
              <div class="image-preview-card">
                <el-image 
                  :src="fullImageUrl(editForm.imageUrl)" 
                  fit="cover"
                  class="preview-image"
                  :preview-src-list="[fullImageUrl(editForm.imageUrl)]"
                />
              </div>
              <div class="image-actions-row">
                <el-upload
                  :action="uploadUrl"
                  name="file"
                  accept="image/*"
                  :on-success="handleUploadSuccess"
                  :before-upload="beforeUpload"
                  :show-file-list="false"
                >
                  <el-button 
                    type="primary" 
                    size="default"
                  >
                    <el-icon><Upload /></el-icon>
                    更换图片
                  </el-button>
                </el-upload>
                <el-button 
                  type="danger" 
                  size="default"
                  @click="removeImage"
                >
                  <el-icon><Delete /></el-icon>
                  删除图片
                </el-button>
              </div>
            </div>
            
            <!-- 上传按钮区域 -->
            <div v-else class="upload-area">
              <el-upload
                class="image-uploader"
                :action="uploadUrl"
                name="file"
                accept="image/*"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
                :show-file-list="false"
                drag>
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击或拖拽上传图片</div>
                <div class="upload-tip">支持 jpg/png 格式，不超过 2MB</div>
              </el-upload>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, ChatDotRound, Star, ArrowLeft, ArrowRight, Box, Share, Document, Plus, Upload, Delete } from '@element-plus/icons-vue'
import { getMyForumPosts, getPostInteraction, updateForumPost, getForumPostDetail } from '@/api/forum'
import { getRoleItem } from '@/utils/storage'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const pageSize = 10
const posts = ref([])

// 编辑相关
const showEditDialog = ref(false)
const editForm = ref({
  id: null,
  categoryId: null,
  title: '',
  content: '',
  imageUrl: ''
})
const editorRef = ref(null)
let quillInstance = null
const uploadUrl = 'http://localhost:8080/api/upload/image'

// 计算属性
const filteredPosts = computed(() => {
  let result = [...posts.value]
  
  // 搜索筛选
  if (searchKeyword.value) {
    result = result.filter(post => 
      post.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      post.content.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  // 状态筛选（字符串状态：approved / published / pending / draft / rejected）
  if (statusFilter.value !== '') {
    result = result.filter(post => {
      const status = String(post.status)
      // 支持 published 和 approved 都匹配"已发布"筛选
      if (statusFilter.value === 'published') {
        return status === 'published' || status === 'approved'
      }
      return status === statusFilter.value
    })
  }
  
  // 分类筛选
  if (categoryFilter.value) {
    result = result.filter(post => post.category === categoryFilter.value)
  }
  
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredPosts.value.length / pageSize) || 1
})

const paginatedPosts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredPosts.value.slice(start, end)
})

// 方法
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const showDraftBox = () => {
  statusFilter.value = 'draft'
  currentPage.value = 1
}

const handleCreatePost = () => {
  ElMessage.info('跳转到发布帖子页面')
  // router.push('/volunteer/create-post')
}

const viewPost = (post) => {
  router.push(`/volunteer/post/${post.id}`)
}

const editPost = async (post) => {
  try {
    const res = await getForumPostDetail(post.id)
    if (res.code === 200 && res.data) {
      const postData = res.data
      editForm.value = {
        id: postData.id,
        categoryId: postData.categoryId,
        title: postData.title,
        content: postData.content || '',
        imageUrl: postData.imageUrl || ''
      }
      showEditDialog.value = true
    } else {
      ElMessage.error('获取帖子详情失败')
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败: ' + (error.message || '未知错误'))
  }
}

const deletePost = async (post) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除帖子"${post.title}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用删除API
    // await deletePostApi(post.id)
    
    ElMessage.success('删除成功')
    loadPosts()
  } catch {
    // 用户取消删除
  }
}

// 初始化编辑器
const initEditor = async () => {
  if (!editorRef.value) return
  
  await nextTick()
  
  // 清空容器内容，移除所有子元素
  editorRef.value.innerHTML = ''
  
  // 如果有旧实例，置空
  if (quillInstance) {
    quillInstance = null
  }
  
  // 创建新的编辑器实例
  quillInstance = new Quill(editorRef.value, {
    theme: 'snow',
    placeholder: '请输入帖子内容...',
    modules: {
      toolbar: [
        ['bold', 'italic', 'underline', 'strike'],
        ['blockquote', 'code-block'],
        [{ 'header': 1 }, { 'header': 2 }],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        [{ 'indent': '-1'}, { 'indent': '+1' }],
        [{ 'size': ['small', false, 'large', 'huge'] }],
        [{ 'color': [] }, { 'background': [] }],
        ['link', 'image'],
        ['clean']
      ]
    }
  })
  
  // 设置内容
  if (editForm.value.content) {
    quillInstance.root.innerHTML = editForm.value.content
  }
}

// 处理编辑对话框关闭
const handleEditDialogClose = () => {
  // 清理编辑器
  if (editorRef.value) {
    editorRef.value.innerHTML = ''
  }
  quillInstance = null
  
  // 重置表单
  editForm.value = {
    id: null,
    categoryId: null,
    title: '',
    content: '',
    imageUrl: ''
  }
}

// 处理更新
const handleUpdate = async () => {
  if (!editForm.value.title || !editForm.value.categoryId) {
    ElMessage.warning('请填写标题和分类')
    return
  }
  
  try {
    const content = quillInstance ? quillInstance.root.innerHTML : editForm.value.content
    
    const res = await updateForumPost({
      id: editForm.value.id,
      categoryId: editForm.value.categoryId,
      title: editForm.value.title,
      content: content,
      imageUrl: editForm.value.imageUrl
    })
    
    if (res.code === 200) {
      ElMessage.success('修改成功')
      showEditDialog.value = false
      loadPosts()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败: ' + (error.message || '未知错误'))
  }
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  console.log('上传响应:', response)
  if (response.code === 200) {
    // 处理不同的响应格式
    const imageUrl = response.data?.url || response.data
    editForm.value.imageUrl = imageUrl
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 删除图片
const removeImage = () => {
  editForm.value.imageUrl = ''
  ElMessage.success('图片已删除')
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

const truncateContent = (content) => {
  const maxLength = 60
  if (!content) return ''

  let text = content

  // 如果是 HTML 内容，去掉图片和所有标签，只保留纯文本
  if (/<[a-z][\s\S]*>/i.test(content)) {
    text = text.replace(/<img[^>]*>/gi, '')
    text = text.replace(/<[^>]+>/g, '')
  }

  text = text.replace(/&nbsp;/g, ' ').trim()

  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const getCategoryName = (category) => {
  const categories = {
    volunteer: '志愿活动',
    experience: '经验分享',
    help: '问题求助'
  }
  return categories[category] || '其他'
}

const formatTime = (time) => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString()
  } catch {
    return time
  }
}

const fullImageUrl = (path) => {
	if (!path) return ''
	if (typeof path === 'string' && path.startsWith('http')) return path
	return `http://localhost:8080${path}`
}

const statusText = (status) => {
  if (!status) return '未知状态'
  switch (status) {
    case 'approved':
    case 'published':
      return '已发布'
    case 'pending':
      return '待审核'
    case 'draft':
      return '草稿'
    case 'rejected':
      return '已驳回'
    default:
      return status
  }
}

const statusTagType = (status) => {
  switch (status) {
    case 'approved':
    case 'published':
      return 'success'  // 已发布 - 绿色
    case 'pending':
      return 'warning'  // 待审核 - 橙色
    case 'draft':
      return ''         // 草稿 - 灰色
    case 'rejected':
      return 'danger'   // 已驳回 - 红色
    default:
      return ''
  }
}

const loadPosts = async () => {
  try {
    loading.value = true
    // 使用 getRoleItem 获取志愿者用户信息
    const user = getRoleItem('user', 'volunteer')
    const volunteerId = user?.id
    if (!volunteerId) {
      ElMessage.error('未获取到当前志愿者信息，请重新登录')
      return
    }

    // 直接调用后端接口获取当前志愿者的帖子
    const res = await getMyForumPosts({ volunteerId })
    let list = res.data || []

    // 获取每个帖子的互动统计
    const tasks = list.map(async (post) => {
      try {
        const statRes = await getPostInteraction(post.id, volunteerId)
        if (statRes && statRes.code === 200 && statRes.data) {
          post.likes = statRes.data.likes ?? 0
          post.comments = statRes.data.comments ?? 0
        }
      } catch (e) {
        // 单条统计失败忽略
      }
    })

    await Promise.all(tasks)
    posts.value = list
  } catch (error) {
    ElMessage.error('加载帖子失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 生命周期
// 监听编辑对话框打开
watch(showEditDialog, async (val) => {
  if (val) {
    await nextTick()
    await initEditor()
  }
})

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.my-posts {
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

.posts-list {
  min-height: 400px;
}

.post-card {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.post-body {
	display: flex;
	gap: 16px;
}

.post-cover {
	flex-shrink: 0;
}

.post-main {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.title-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-right: 10px;
}

.post-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
}

.post-title:hover {
  color: #ce4c4c;
}

.shared-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: #f0f5ff;
  border-color: #adc6ff;
  color: #597ef7;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  width: fit-content;
}

.shared-tag .el-icon {
  font-size: 14px;
}

.post-content {
  margin-bottom: 15px;
}

.post-excerpt {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 14px;
  color: #999;
}

.meta-left {
  display: flex;
  gap: 15px;
  align-items: center;
}

.meta-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.meta-right span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.category {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.post-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
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
  padding: 20px 0;
}

.page-info {
  font-size: 14px;
  color: #606266;
  margin: 0 10px;
}

.draft-box-btn {
  height: 32px;
  line-height: 32px;
  display: inline-flex;
  align-items: center;
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
  
  .post-meta {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .post-actions {
    justify-content: center;
  }
}

/* 编辑对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

/* 编辑表单样式 */
.edit-form {
  padding: 0;
}

.edit-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  line-height: 32px;
}

.edit-form :deep(.el-input__inner) {
  border-radius: 4px;
}

.edit-form :deep(.el-select) {
  width: 100%;
}

/* Quill编辑器样式 */
.editor-wrapper {
  width: 100%;
}

.quill-editor {
  width: 100%;
  height: 280px;
  background: white;
}

:deep(.ql-toolbar) {
  border: 1px solid #dcdfe6;
  border-bottom: none;
  background-color: #fafafa;
  border-radius: 4px 4px 0 0;
}

:deep(.ql-container) {
  height: 220px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 0 0 4px 4px;
}

:deep(.ql-editor) {
  min-height: 200px;
  line-height: 1.6;
}

:deep(.ql-editor.ql-blank::before) {
  color: #c0c4cc;
  font-style: normal;
}

:deep(.ql-snow .ql-picker) {
  line-height: 20px;
}

:deep(.ql-snow .ql-stroke) {
  stroke: #606266;
}

:deep(.ql-snow .ql-fill) {
  fill: #606266;
}

.reject-reason {
  padding: 8px 12px;
  background-color: #fef0f0;
  border-left: 3px solid #f56c6c;
  color: #f56c6c;
  font-size: 13px;
  margin-bottom: 10px;
  border-radius: 4px;
}

/* 图片上传样式 */
.image-upload-container {
  width: 100%;
}

.image-preview-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background-color: #ffffff;
}

.image-preview-card {
  flex-shrink: 0;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.preview-image {
  width: 220px;
  height: 150px;
  display: block;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preview-image:hover {
  opacity: 0.9;
}

.image-actions-row {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 8px;
}

.image-actions-row .el-upload {
  display: block;
}

.image-actions-row .el-button {
  width: 140px;
  font-size: 14px;
  font-weight: 500;
}

.image-actions-row .el-button .el-icon {
  margin-right: 6px;
}

.upload-area {
  width: 100%;
}

.image-uploader {
  width: 100%;
}

:deep(.image-uploader .el-upload) {
  width: 100%;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

:deep(.image-uploader .el-upload:hover) {
  border-color: #ce4c4c;
}

:deep(.image-uploader .el-upload-dragger) {
  width: 100%;
  height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  border: none;
  border-radius: 6px;
  padding: 20px;
}

:deep(.image-uploader .el-upload-dragger:hover) {
  background-color: #f0f2f5;
}

.upload-icon {
  font-size: 40px;
  color: #909399;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
