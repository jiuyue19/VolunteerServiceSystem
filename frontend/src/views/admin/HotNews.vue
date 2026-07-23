<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>热点信息管理</h2>
        </div>
      </template>
      <div class="toolbar">
        <div class="search-box">
          <el-input v-model="searchText" placeholder="搜索新闻标题" clearable style="width: 300px" />
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
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleEdit(row)">修改</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-button :disabled="currentPage === 1" @click="prevPage">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-button :disabled="currentPage === totalPages" @click="nextPage">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="浏览量">
          <el-input-number v-model="form.views" :min="0" />
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
import { getNewsList, addNews, updateNews, deleteNews, batchDeleteNews } from '@/api/news'
import { formatDateTime } from '@/utils/formatDate'

const dataList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = 5
const form = ref({ id: null, title: '', content: '', views: 0 })

const filteredList = computed(() =>
  searchText.value
    ? dataList.value.filter(item => item.title?.includes(searchText.value))
    : dataList.value
)
const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize) || 1)
const paginatedList = computed(() =>
  filteredList.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize)
)

const handleSearch = () => {
  currentPage.value = 1
}

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}

const handleSelectionChange = selection => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  dialogTitle.value = '新增热点信息'
  isEdit.value = false
  form.value = { id: null, title: '', content: '', views: 0 }
  dialogVisible.value = true
}

const handleEdit = row => {
  dialogTitle.value = '修改热点信息'
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateNews(form.value)
    } else {
      await addNews(form.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = row => {
  ElMessageBox.confirm('确定要删除该条热点信息吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteNews(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条热点信息吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await batchDeleteNews(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const loadData = async () => {
  try {
    const res = await getNewsList()
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
.page-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
