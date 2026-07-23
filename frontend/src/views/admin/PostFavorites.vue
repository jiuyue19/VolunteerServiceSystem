<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="header"><h2>帖子收藏信息管理</h2></div></template>
      <div class="toolbar">
        <div class="search-box">
          <el-input v-model="searchText" placeholder="搜索帖子标题或志愿者姓名" clearable style="width: 300px" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </div>
      <el-table :data="paginatedList" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="postTitle" label="帖子标题" min-width="250" />
        <el-table-column prop="postCategory" label="帖子分类" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.postCategory }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerName" label="志愿者姓名" width="120" />
        <el-table-column prop="volunteerPhone" label="联系电话" width="130" />
        <el-table-column prop="favoriteTime" label="收藏时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.favoriteTime) }}
          </template>
        </el-table-column>
        <el-table-column label="帖子状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPostStatusType(row.postStatus)">
              {{ getPostStatusText(row.postStatus) }}
            </el-tag>
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
import axios from 'axios'

const dataList = ref([])
const searchText = ref('')
const currentPage = ref(1)
const pageSize = 10

const filteredList = computed(() => {
  if (!searchText.value) return dataList.value
  return dataList.value.filter(item => 
    item.postTitle?.includes(searchText.value) || 
    item.volunteerName?.includes(searchText.value)
  )
})

const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize) || 1)
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

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

const getPostStatusType = (status) => {
  switch (status) {
    case 'approved':
    case 'published': return 'success'
    case 'pending': return 'warning'
    case 'rejected': return 'danger'
    default: return ''
  }
}

const getPostStatusText = (status) => {
  switch (status) {
    case 'approved':
    case 'published': return '已通过'
    case 'pending': return '待审核'
    case 'rejected': return '已驳回'
    default: return status || '-'
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该收藏记录吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await axios.delete(`/api/admin/post-favorites/${row.id}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
      })
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  })
}

const loadData = async () => {
  try {
    const res = await axios.get('/api/admin/post-favorites', {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    dataList.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.header h2 {
  margin: 0;
  color: #ce4c4c;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
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

:deep(.el-button--primary) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-button--primary:hover) {
  background-color: #d97373;
  border-color: #d97373;
}

:deep(.el-table__header th) {
  background-color: #f5f5f5;
  color: #333;
}
</style>
