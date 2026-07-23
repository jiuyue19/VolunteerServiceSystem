<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="header"><h2>论坛帖子管理</h2></div></template>
      <div class="toolbar">
        <div class="search-box">
          <el-input v-model="searchText" placeholder="搜索帖子标题" clearable style="width: 300px" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div>
          <el-button type="primary" @click="handleAdd">新增</el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量删除</el-button>
        </div>
      </div>
      <el-table :data="paginatedList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="封面" width="140">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="fullImageUrl(row.imageUrl)"
              fit="cover"
              style="width: 120px; height: 70px; border-radius: 4px;"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="220">
          <template #default="{ row }">
            {{ truncateContent(row.content) }}
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            {{ row.username || row.author }}
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="likes" label="点赞" width="90" />
        <el-table-column prop="favorites" label="收藏" width="90" />
        <el-table-column prop="comments" label="评论数" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">修改</el-button>
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleApprove(row)">通过审核</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" size="small" @click="handleReject(row)">驳回</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-button :disabled="currentPage === 1" @click="prevPage"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-button :disabled="currentPage === totalPages" @click="nextPage"><el-icon><ArrowRight /></el-icon></el-button>
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" />
          <div class="content-preview" v-html="form.content" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            name="file"
            accept="image/*"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :limit="1"
            list-type="picture-card"
          >
            <el-button type="primary" size="small">上传封面</el-button>
          </el-upload>
          <el-image
            v-if="form.imageUrl"
            :src="fullImageUrl(form.imageUrl)"
            fit="cover"
            style="width: 120px; height: 70px; margin-left: 10px; border-radius: 4px;"
          />
        </el-form-item>
        <el-form-item v-if="isEdit && form.status && (form.status === 'approved' || form.status === 'rejected')" label="审核状态">
          <el-tag :type="statusTagType(form.status)">{{ statusText(form.status) }}</el-tag>
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">（已审核的帖子无法修改状态）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostCategoryList, getForumPostList, addForumPost, updateForumPost, deleteForumPost, batchDeleteForumPost, getPostInteraction, reviewForumPost } from '@/api/forum'

const dataList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = 5
const categoryOptions = ref([])
const uploadUrl = 'http://localhost:8080/api/upload/image'

const form = ref({ id: null, categoryId: null, title: '', content: '', imageUrl: '', status: '', reviewReason: '' })

const filteredList = computed(() => searchText.value ? dataList.value.filter(item => item.title?.includes(searchText.value)) : dataList.value)
const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize) || 1)
const paginatedList = computed(() => filteredList.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize))

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handleSearch = () => { currentPage.value = 1 }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
const handleSelectionChange = (selection) => { selectedIds.value = selection.map(item => item.id) }
const handleAdd = () => {
  dialogTitle.value = '新增帖子'
  isEdit.value = false
  form.value = { id: null, categoryId: null, title: '', content: '', imageUrl: '', status: '', reviewReason: '' }
  dialogVisible.value = true
}
const handleEdit = (row) => {
  dialogTitle.value = '修改帖子'
  isEdit.value = true
  form.value = {
    id: row.id,
    categoryId: row.categoryId,
    title: row.title,
    content: row.content,
    imageUrl: row.imageUrl,
    status: row.status,
    reviewReason: row.reviewReason
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateForumPost(form.value)
    } else {
      const payload = { ...form.value }
      if (!payload.status) {
        payload.status = 'pending'
      }
      await addForumPost(payload)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const fullImageUrl = (path) => {
  if (!path) return ''
  if (typeof path === 'string' && path.startsWith('http')) return path
  return `http://localhost:8080${path}`
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteForumPost(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await batchDeleteForumPost(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const truncateContent = (content) => {
  const maxLength = 60
  if (!content) return ''

  let text = content

  if (/<[a-z][\s\S]*>/i.test(content)) {
    text = text.replace(/<img[^>]*>/gi, '')
    text = text.replace(/<[^>]+>/g, '')
  }

  text = text.replace(/&nbsp;/g, ' ').trim()

  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const statusText = (status) => {
  if (!status) return '-'
  switch (status) {
    case 'approved':
    case 'published':
      return '已通过'
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
      return 'success'  // 已通过 - 绿色
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

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.value.imageUrl = response.data?.url || response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const loadCategories = async () => {
  try {
    const res = await getPostCategoryList()
    categoryOptions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const loadData = async () => {
  try {
    const res = await getForumPostList()
    const list = res.data || []

    // 为每条帖子加载点赞、收藏和评论数
    const tasks = list.map(async (post) => {
      if (!post || !post.id) return
      try {
        const statRes = await getPostInteraction(post.id)
        if (statRes && statRes.code === 200 && statRes.data) {
          post.likes = statRes.data.likes ?? 0
          post.favorites = statRes.data.favorites ?? 0
          post.comments = statRes.data.comments ?? 0
        }
      } catch (e) {
        // 管理端统计失败时不阻塞整体渲染
      }
    })

    await Promise.all(tasks)
    dataList.value = list
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleApprove = async (row) => {
  try {
    await reviewForumPost({ id: row.id, status: 'approved', reviewReason: '' })
    ElMessage.success('审核通过')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  }
}

const handleReject = async (row) => {
  try {
    await reviewForumPost({ id: row.id, status: 'rejected', reviewReason: '系统驳回' })
    ElMessage.success('已驳回')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.header h2 { margin: 0; color: #ce4c4c; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.search-box { display: flex; gap: 10px; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 20px; margin-top: 20px; }
.page-info { font-size: 14px; color: #606266; }
:deep(.el-button--primary) { background-color: #ce4c4c; border-color: #ce4c4c; }
:deep(.el-button--primary:hover) { background-color: #d97373; border-color: #d97373; }
:deep(.el-table__header th) { background-color: #f5f5f5; color: #333; }
.content-preview { max-height: 260px; overflow-y: auto; padding: 10px; border: 1px solid #ebeef5; border-radius: 4px; background-color: #fafafa; }
.content-preview :deep(img) { max-width: 100%; display: block; margin: 6px 0; }
</style>