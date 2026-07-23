<template>
  <div class="forum">
    <div class="header-row">
      <h2>志愿论坛</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon> 发布帖子
      </el-button>
    </div>
    
    <div class="forum-header">
      <div class="categories">
        <el-button :type="activeCategory === null ? 'primary' : ''" @click="filterByCategory(null)">全部</el-button>
        <el-button :type="activeCategory === 1 ? 'primary' : ''" @click="filterByCategory(1)">志愿事迹</el-button>
        <el-button :type="activeCategory === 2 ? 'primary' : ''" @click="filterByCategory(2)">志愿寄语</el-button>
        <el-button :type="activeCategory === 3 ? 'primary' : ''" @click="filterByCategory(3)">志愿点滴</el-button>
        <el-button :type="activeCategory === 4 ? 'primary' : ''" @click="filterByCategory(4)">悸动心声</el-button>
      </div>
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="搜索帖子标题或发布者" clearable @input="handleSearch" @clear="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover" @click="goDetail(post)">
      <div class="post-layout">
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
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="showCreateDialog" :title="isEditMode ? '编辑帖子' : '发布帖子'" width="600px">
      <el-form :model="newPost" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="newPost.categoryId" placeholder="请选择分类">
            <el-option label="志愿事迹" :value="1" />
            <el-option label="志愿寄语" :value="2" />
            <el-option label="志愿点滴" :value="3" />
            <el-option label="悸动心声" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="newPost.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="内容">
          <div ref="editorRef" class="quill-editor"></div>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            name="file"
            accept="image/*"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :limit="1"
            list-type="picture">
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="newPost.saveAsDraft">保存至草稿箱</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="isEditMode ? handleUpdate() : handleCreate()">{{ isEditMode ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { getForumPostList, addForumPost, getForumPostDetail, updateForumPost } from '@/api/forum'
import { Search, Plus, User, Flag, View, Clock, Share } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const showCreateDialog = ref(false)
const activeCategory = ref(null)
const searchKeyword = ref('')
const posts = ref([])
const editorRef = ref(null)
const isEditMode = ref(false)
const editingPostId = ref(null)

let quillInstance = null

const newPost = ref({
  categoryId: null,
  title: '',
  content: '',
  imageUrl: '',
  saveAsDraft: false
})

const uploadUrl = 'http://localhost:8080/api/upload/image'

const initEditor = async () => {
  if (quillInstance || !editorRef.value) return
  quillInstance = new Quill(editorRef.value, {
    theme: 'snow',
    placeholder: '请输入帖子内容...',
    modules: {
      toolbar: [
        [{ header: [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ list: 'ordered' }, { list: 'bullet' }],
        ['link', 'image'],
        ['clean']
      ]
    }
  })

  quillInstance.on('text-change', () => {
    newPost.value.content = quillInstance.root.innerHTML
  })
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

const loadPosts = async (categoryId = null, keyword = null) => {
  try {
    const params = {}
    if (categoryId) params.categoryId = categoryId
    if (keyword) params.keyword = keyword
    const res = await getForumPostList(params)
    if (res.code === 200) {
      posts.value = res.data
      // 调试：打印第一条帖子数据，查看字段结构
      if (res.data && res.data.length > 0) {
        console.log('论坛帖子数据结构:', res.data[0])
        console.log('头像字段:', res.data[0].avatar)
        console.log('用户名字段:', res.data[0].username)
      }
    }
  } catch (error) {
    ElMessage.error('加载帖子失败')
  }
}

const filterByCategory = (categoryId) => {
  activeCategory.value = categoryId
  searchKeyword.value = ''
  loadPosts(categoryId)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    activeCategory.value = null
    loadPosts(null, searchKeyword.value)
  } else {
    loadPosts(activeCategory.value)
  }
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    newPost.value.imageUrl = response.data?.url || response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

const handleCreate = async () => {
  if (!newPost.value.categoryId || !newPost.value.title || !newPost.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const volunteerId = JSON.parse(localStorage.getItem('user'))?.id
    const status = newPost.value.saveAsDraft ? 'draft' : 'pending'

    const { saveAsDraft, ...payload } = newPost.value

    const res = await addForumPost({
      ...payload,
      volunteerId,
      status
    })
    if (res.code === 200) {
      ElMessage.success(newPost.value.saveAsDraft ? '已保存到草稿箱' : '发布成功，等待管理员审核')
      showCreateDialog.value = false
      newPost.value = { categoryId: null, title: '', content: '', imageUrl: '', saveAsDraft: false }
      if (quillInstance) {
        quillInstance.setContents([])
      }
      loadPosts(activeCategory.value, searchKeyword.value)
    }
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const handleUpdate = async () => {
  if (!newPost.value.categoryId || !newPost.value.title || !newPost.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await updateForumPost({
      id: editingPostId.value,
      categoryId: newPost.value.categoryId,
      title: newPost.value.title,
      content: newPost.value.content,
      imageUrl: newPost.value.imageUrl
    })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      showCreateDialog.value = false
      isEditMode.value = false
      editingPostId.value = null
      newPost.value = { categoryId: null, title: '', content: '', imageUrl: '', saveAsDraft: false }
      if (quillInstance) {
        quillInstance.setContents([])
      }
      loadPosts(activeCategory.value, searchKeyword.value)
    }
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const loadPostForEdit = async (postId) => {
  try {
    const res = await getForumPostDetail(postId)
    if (res.code === 200 && res.data) {
      const postData = res.data
      newPost.value = {
        categoryId: postData.categoryId,
        title: postData.title,
        content: postData.content || '',
        imageUrl: postData.imageUrl || '',
        saveAsDraft: false
      }
      isEditMode.value = true
      editingPostId.value = postId
      showCreateDialog.value = true
    } else {
      ElMessage.error('获取帖子详情失败')
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
  }
}

onMounted(async () => {
  await loadPosts()
  
  // 检查是否有editId参数
  const editId = route.query.editId
  if (editId) {
    await loadPostForEdit(editId)
  }
})

watch(showCreateDialog, async (val) => {
  if (val) {
    await nextTick()
    await initEditor()
    // 如果是编辑模式，设置内容
    if (isEditMode.value && newPost.value.content) {
      if (quillInstance) {
        quillInstance.root.innerHTML = newPost.value.content
      }
    } else {
      if (quillInstance) {
        quillInstance.setContents([])
      }
      if (!isEditMode.value) {
        newPost.value.content = ''
      }
    }
  } else {
    // 关闭对话框时重置编辑状态
    if (!val) {
      isEditMode.value = false
      editingPostId.value = null
    }
  }
})

const goDetail = (post) => {
  if (!post || !post.id) return
  router.push(`/volunteer/post/${post.id}`)
}
</script>

<style scoped>
.forum {
  padding: 20px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-row h2 {
  margin: 0;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.categories {
  display: flex;
  gap: 10px;
}

.search-box {
  width: 300px;
}

.post-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.meta-all-left {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
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

.quill-editor {
  width: 100%;
  height: 240px;
}

.quill-editor :deep(.ql-editor) {
  min-height: 180px;
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