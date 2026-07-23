<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>管理员管理</h2>
        </div>
      </template>
      
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchText"
            placeholder="搜索用户名或姓名"
            clearable
            style="width: 300px"
          />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div>
          <el-button type="primary" @click="handleAdd">新增</el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量删除</el-button>
        </div>
      </div>

      <el-table :data="paginatedList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="avatar" label="头像" width="100">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" v-if="row.avatar" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger">超级管理员</el-tag>
            <el-tag v-else type="success">普通管理员</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%" @change="handleRoleChange">
            <el-option label="普通管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限" v-if="form.role === 'ADMIN'">
          <div class="permission-container">
            <el-collapse v-model="activePermissionPanels">
              <!-- 用户管理 -->
              <el-collapse-item title="用户管理" name="user">
                <el-checkbox-group v-model="selectedPermissions">
                  <el-checkbox label="volunteers">志愿者管理</el-checkbox>
                </el-checkbox-group>
              </el-collapse-item>
              
              <!-- 活动管理 -->
              <el-collapse-item title="活动管理" name="activity">
                <el-checkbox-group v-model="selectedPermissions">
                  <div class="checkbox-grid">
                    <el-checkbox label="activities">志愿活动</el-checkbox>
                    <el-checkbox label="categories">活动分类</el-checkbox>
                    <el-checkbox label="forum">论坛帖子</el-checkbox>
                    <el-checkbox label="post-categories">帖子分类</el-checkbox>
                    <el-checkbox label="banners">轮播图信息</el-checkbox>
                    <el-checkbox label="comments">评论信息</el-checkbox>
                    <el-checkbox label="announcements">系统公告</el-checkbox>
                  </div>
                </el-checkbox-group>
              </el-collapse-item>
              
              <!-- 志愿审核 -->
              <el-collapse-item title="志愿审核" name="audit">
                <el-checkbox-group v-model="selectedPermissions">
                  <div class="checkbox-grid">
                    <el-checkbox label="applications">报名申请</el-checkbox>
                    <el-checkbox label="checkins">打卡记录</el-checkbox>
                    <el-checkbox label="replenish">补录申请</el-checkbox>
                  </div>
                </el-checkbox-group>
              </el-collapse-item>
              
              <!-- 订单管理 -->
              <el-collapse-item title="订单管理" name="order">
                <el-checkbox-group v-model="selectedPermissions">
                  <el-checkbox label="goods">积分商品</el-checkbox>
                  <el-checkbox label="orders">兑换订单</el-checkbox>
                </el-checkbox-group>
              </el-collapse-item>
              
              <!-- 区块链管理 -->
              <el-collapse-item title="区块链管理" name="blockchain">
                <el-checkbox-group v-model="selectedPermissions">
                  <div class="checkbox-grid">
                    <el-checkbox label="blockchain-query">区块链服务查询</el-checkbox>
                    <el-checkbox label="blockchain">区块链证书</el-checkbox>
                    <el-checkbox label="certificate-library">证书库管理</el-checkbox>
                  </div>
                </el-checkbox-group>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
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
import { ArrowLeft, ArrowRight, Plus } from '@element-plus/icons-vue'
import { getAdminList, addAdmin, updateAdminById, deleteAdmin, batchDeleteAdmin } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const adminList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = 5

const form = ref({
  id: null,
  username: '',
  password: '',
  name: '',
  role: 'ADMIN',
  email: '',
  phone: '',
  avatar: '',
  permissions: ''
})

const selectedPermissions = ref([])
const activePermissionPanels = ref([])

// 所有可用权限
const allPermissions = [
  'admins', 'volunteers', 'activities', 'categories', 'forum', 'post-categories',
  'banners', 'comments', 'announcements', 'applications', 'checkins', 'replenish',
  'goods', 'orders', 'blockchain', 'blockchain-query', 'certificate-library'
]

const filteredList = computed(() => {
  if (!searchText.value) {
    return adminList.value
  }
  return adminList.value.filter(item => 
    item.username?.includes(searchText.value) || 
    item.name?.includes(searchText.value)
  )
})

const totalPages = computed(() => {
  return Math.ceil(filteredList.value.length / pageSize) || 1
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredList.value.slice(start, end)
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

const handleSearch = () => {
  currentPage.value = 1
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

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAvatarChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleAdd = () => {
  dialogTitle.value = '新增管理员'
  isEdit.value = false
  form.value = {
    id: null,
    username: '',
    password: '',
    name: '',
    role: 'ADMIN',
    email: '',
    phone: '',
    avatar: '',
    permissions: ''
  }
  // 默认普通管理员拥有所有权限（除了管理员管理）
  selectedPermissions.value = ['volunteers', 'activities', 'categories', 'forum', 'post-categories', 'banners', 'comments', 'announcements', 'applications', 'checkins', 'replenish', 'goods', 'orders', 'blockchain', 'blockchain-query', 'certificate-library']
  activePermissionPanels.value = ['user', 'activity', 'audit', 'order', 'blockchain']
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '修改管理员'
  isEdit.value = true
  form.value = { ...row }
  // 解析权限
  if (row.permissions) {
    try {
      selectedPermissions.value = JSON.parse(row.permissions)
    } catch (e) {
      selectedPermissions.value = []
    }
  } else {
    selectedPermissions.value = []
  }
  activePermissionPanels.value = ['user', 'activity', 'audit', 'order', 'blockchain']
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    // 设置权限
    if (form.value.role === 'SUPER_ADMIN') {
      form.value.permissions = JSON.stringify(allPermissions)
    } else {
      form.value.permissions = JSON.stringify(selectedPermissions.value)
    }
    
    if (isEdit.value) {
      await updateAdminById(form.value)
      ElMessage.success('修改成功')
    } else {
      await addAdmin(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadAdminList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
  }
}

const handleRoleChange = (role) => {
  if (role === 'SUPER_ADMIN') {
    selectedPermissions.value = [...allPermissions]
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该管理员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAdmin(row.id)
      ElMessage.success('删除成功')
      loadAdminList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个管理员吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await batchDeleteAdmin(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadAdminList()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

const loadAdminList = async () => {
  try {
    const res = await getAdminList()
    if (res.code === 200) {
      adminList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载管理员列表失败')
  }
}

onMounted(() => {
  loadAdminList()
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

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #ce4c4c;
}

.avatar {
  width: 178px;
  height: 178px;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
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

/* 权限容器样式 */
.permission-container {
  width: 100%;
  max-height: 280px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px;
  background-color: #fafafa;
}

.permission-container::-webkit-scrollbar {
  width: 6px;
}

.permission-container::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.permission-container::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

/* 优化折叠面板样式 */
.permission-container :deep(.el-collapse) {
  border: none;
}

.permission-container :deep(.el-collapse-item) {
  margin-bottom: 4px;
}

.permission-container :deep(.el-collapse-item__header) {
  height: 40px;
  line-height: 40px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 14px;
  font-weight: 500;
}

.permission-container :deep(.el-collapse-item__wrap) {
  border: none;
  background-color: transparent;
}

.permission-container :deep(.el-collapse-item__content) {
  padding: 10px 12px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-top: none;
  border-radius: 0 0 4px 4px;
}

/* checkbox布局 */
.checkbox-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.permission-container :deep(.el-checkbox) {
  margin-right: 0;
  font-size: 13px;
}

.permission-container :deep(.el-checkbox-group) {
  line-height: normal;
}
</style>