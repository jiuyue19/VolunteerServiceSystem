<template>
  <div class="page-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>页面标题</h2>
        </div>
      </template>
      
      <!-- 页面内容区域 -->
      <div class="page-content">
        <p>这是一个页面模板，您可以基于此模板快速创建新页面</p>
        
        <!-- 示例：搜索栏 -->
        <div class="search-bar">
          <el-input 
            v-model="searchKeyword" 
            placeholder="请输入搜索关键词"
            clearable
            style="width: 300px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        
        <!-- 示例：数据表格 -->
        <el-table :data="tableData" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 示例：分页 -->
        <div class="pagination">
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
import { ElMessage } from 'element-plus'
import { Search, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10
const tableData = ref([])

// 计算属性
const totalPages = computed(() => {
  return Math.ceil(tableData.value.length / pageSize) || 1
})

// 方法
const handleSearch = () => {
  ElMessage.info('搜索功能待实现')
}

const handleEdit = (row) => {
  ElMessage.info(`编辑项目: ${row.name}`)
}

const handleDelete = (row) => {
  ElMessage.info(`删除项目: ${row.name}`)
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

const loadData = async () => {
  try {
    loading.value = true
    
    // TODO: 调用API获取数据
    // const res = await getDataList()
    // tableData.value = res.data || []
    
    // 模拟数据
    tableData.value = [
      { id: 1, name: '示例数据1', description: '这是示例描述1', createTime: '2025-01-01 10:00:00' },
      { id: 2, name: '示例数据2', description: '这是示例描述2', createTime: '2025-01-02 10:00:00' },
      { id: 3, name: '示例数据3', description: '这是示例描述3', createTime: '2025-01-03 10:00:00' }
    ]
    
  } catch (error) {
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
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

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
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

:deep(.el-table__header th) {
  background-color: #f5f5f5;
  color: #333;
}
</style>
