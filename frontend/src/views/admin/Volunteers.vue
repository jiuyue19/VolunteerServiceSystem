<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>志愿者管理</h2>
        </div>
      </template>
      
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchText"
            placeholder="搜索用户名或真实姓名"
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
        <el-table-column prop="realName" label="真实姓名" />
         <el-table-column prop="avatar" label="头像" width="100">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" v-if="row.avatar" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" />
                <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
                <el-table-column prop="totalHours" label="总时长" width="100" />
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="walletAddress" label="钱包地址" width="230" />
        <el-table-column prop="province" label="省份" width="100" />
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column prop="district" label="区县" width="120" />


       
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleEdit(row)" v-if="isSuperAdmin">修改</el-button>
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
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="钱包地址">
          <el-input v-model="form.walletAddress" placeholder="可选：0x 开头的钱包地址" />
        </el-form-item>
        <el-form-item label="省市区">
          <el-input
            v-model="form.province"
            placeholder="省份"
            style="width: 90px; margin-right: 8px;"
          />
          <el-input
            v-model="form.city"
            placeholder="城市"
            style="width: 120px; margin-right: 8px;"
          />
          <el-input
            v-model="form.district"
            placeholder="区/县"
            style="width: 130px;"
          />
        </el-form-item>
        <el-form-item label="总时长">
          <el-input-number v-model="form.totalHours" :min="0" />
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="form.points" :min="0" />
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
import { getVolunteerList, addVolunteer, adminUpdateVolunteer, deleteVolunteer, batchDeleteVolunteer } from '@/api/volunteer'
import { getAdminInfo } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const volunteerList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = 5
const adminInfo = ref({
  role: ''
})

const form = ref({
  id: null,
  username: '',
  password: '',
  realName: '',
  gender: '男',
  idCard: '',
  email: '',
  phone: '',
  walletAddress: '',
  province: '',
  city: '',
  district: '',
  totalHours: 0,
  points: 0,
  avatar: ''
})

// 判断是否是超级管理员
const isSuperAdmin = computed(() => {
  return adminInfo.value.role === 'SUPER_ADMIN'
})

const filteredList = computed(() => {
  if (!searchText.value) {
    return volunteerList.value
  }
  return volunteerList.value.filter(item => 
    item.username?.includes(searchText.value) || 
    item.realName?.includes(searchText.value)
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
  dialogTitle.value = '新增志愿者'
  isEdit.value = false
  form.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    gender: '男',
    idCard: '',
    email: '',
    phone: '',
    walletAddress: '',
    province: '',
    city: '',
    district: '',
    totalHours: 0,
    points: 0,
    avatar: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '修改志愿者'
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      // 管理端使用专用的更新接口，不会被 token 中的 userId 覆盖
      await adminUpdateVolunteer(form.value)
      ElMessage.success('修改成功')
    } else {
      await addVolunteer(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadVolunteerList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该志愿者吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteVolunteer(row.id)
      ElMessage.success('删除成功')
      loadVolunteerList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个志愿者吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await batchDeleteVolunteer(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadVolunteerList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const loadVolunteerList = async () => {
  try {
    const res = await getVolunteerList()
    if (res.code === 200) {
      volunteerList.value = res.data
    } else {
      ElMessage.error(res.message || '加载志愿者列表失败')
    }
  } catch (error) {
    console.error('加载志愿者列表失败:', error)
    ElMessage.error(error.message || '加载志愿者列表失败')
  }
}

const loadAdminInfo = async () => {
  try {
    const res = await getAdminInfo()
    if (res.code === 200 && res.data) {
      adminInfo.value.role = res.data.role
      console.log('管理员信息:', res.data)
    } else {
      ElMessage.error(res.message || '加载管理员信息失败')
    }
  } catch (error) {
    console.error('加载管理员信息失败:', error)
    ElMessage.error(error.message || '加载管理员信息失败')
  }
}

onMounted(() => {
  loadAdminInfo()
  loadVolunteerList()
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
</style>