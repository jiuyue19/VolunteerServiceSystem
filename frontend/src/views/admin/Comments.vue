<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="header"><h2>评论信息管理</h2></div></template>
      <div class="toolbar">
        <div class="search-box">
          <el-input v-model="searchText" placeholder="搜索评论内容" clearable style="width: 300px" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量删除</el-button>
        </div>
      </div>
      <el-table :data="paginatedList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="评论内容" min-width="350">
          <template #default="{ row }">
            <div v-if="isImageContent(row.content)" class="image-content">
              <el-image 
                :src="extractImageUrl(row.content)" 
                fit="contain" 
                class="admin-comment-image"
                :preview-src-list="[extractImageUrl(row.content)]"
                preview-teleported
              />
            </div>
            <div v-else class="text-content">
              {{ row.content }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="评论人" width="130" />
        <el-table-column prop="postTitle" label="帖子标题" width="200" />
        <el-table-column prop="createdAt" label="评论时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentList, deleteComment, batchDeleteComment } from '@/api/comment'

const dataList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = 5

const filteredList = computed(() => searchText.value ? dataList.value.filter(item => item.content?.includes(searchText.value)) : dataList.value)
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
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteComment(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条评论吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await batchDeleteComment(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const isImageContent = (content) => {
  return content && content.includes('<img') && content.includes('src=')
}

const extractImageUrl = (content) => {
  if (!content) return ''
  const match = content.match(/src="([^"]+)"/)
  return match ? match[1] : ''
}

const loadData = async () => {
  try {
    const res = await getCommentList()
    dataList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
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

.image-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80px;
  overflow: hidden;
}

.admin-comment-image {
  max-width: 120px;
  max-height: 80px;
  width: auto;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.admin-comment-image:hover {
  transform: scale(1.05);
}

.text-content {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-image__error) {
  font-size: 12px;
  color: #999;
}
</style>