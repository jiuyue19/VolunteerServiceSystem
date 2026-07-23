<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="header"><h2>积分商品管理</h2></div></template>
      <div class="toolbar">
        <div class="search-box">
          <el-input v-model="searchText" placeholder="搜索商品名称" clearable style="width: 300px" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div>
          <el-button type="primary" @click="handleAdd">新增</el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量删除</el-button>
        </div>
      </div>
      <el-table :data="paginatedList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="商品名称" width="150" />
        <el-table-column prop="image" label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width: 60px; height: 60px" fit="cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="150" show-overflow-tooltip />
        <el-table-column prop="points" label="所需积分" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="material" label="材质" width="100">
          <template #default="{ row }">
            {{ row.material || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="100">
          <template #default="{ row }">
            {{ row.color || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetGroup" label="适用人群" width="100">
          <template #default="{ row }">
            {{ row.targetGroup || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="season" label="适用季节" width="100">
          <template #default="{ row }">
            {{ row.season || '-' }}
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
        <el-button :disabled="currentPage === 1" @click="prevPage"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <el-button :disabled="currentPage === totalPages" @click="nextPage"><el-icon><ArrowRight /></el-icon></el-button>
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="商品图片">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :auto-upload="false" :on-change="handleImageChange">
            <img v-if="form.image" :src="form.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="材质">
          <el-checkbox-group v-model="form.materialList">
            <el-checkbox label="纯棉">纯棉</el-checkbox>
            <el-checkbox label="混纺">混纺</el-checkbox>
            <el-checkbox label="塑料">塑料</el-checkbox>
            <el-checkbox label="金属">金属</el-checkbox>
            <el-checkbox label="皮革">皮革</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="颜色">
          <el-checkbox-group v-model="form.colorList">
            <el-checkbox label="红色">红色</el-checkbox>
            <el-checkbox label="蓝色">蓝色</el-checkbox>
            <el-checkbox label="绿色">绿色</el-checkbox>
            <el-checkbox label="黄色">黄色</el-checkbox>
            <el-checkbox label="白色">白色</el-checkbox>
            <el-checkbox label="黑色">黑色</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="适用人群">
          <el-checkbox-group v-model="form.targetGroupList">
            <el-checkbox label="成人">成人</el-checkbox>
            <el-checkbox label="儿童">儿童</el-checkbox>
            <el-checkbox label="老人">老人</el-checkbox>
            <el-checkbox label="学生">学生</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="适用季节">
          <el-checkbox-group v-model="form.seasonList">
            <el-checkbox label="春季">春季</el-checkbox>
            <el-checkbox label="夏季">夏季</el-checkbox>
            <el-checkbox label="秋季">秋季</el-checkbox>
            <el-checkbox label="冬季">冬季</el-checkbox>
            <el-checkbox label="四季">四季</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="其他特性">
          <el-checkbox-group v-model="form.featuresList">
            <el-checkbox label="防水">防水</el-checkbox>
            <el-checkbox label="透气">透气</el-checkbox>
            <el-checkbox label="保温">保温</el-checkbox>
            <el-checkbox label="耐磨">耐磨</el-checkbox>
            <el-checkbox label="易清洗">易清洗</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="所需积分"><el-input-number v-model="form.points" :min="0" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoodsList, addGoods, updateGoods, deleteGoods, batchDeleteGoods } from '@/api/goods'

const dataList = ref([])
const searchText = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = 5
const form = ref({ 
  id: null, 
  name: '', 
  image: '', 
  points: 0, 
  stock: 0, 
  description: '', 
  status: 1,
  material: '',
  color: '',
  targetGroup: '',
  season: '',
  features: '',
  materialList: [],
  colorList: [],
  targetGroupList: [],
  seasonList: [],
  featuresList: []
})

const filteredList = computed(() => searchText.value ? dataList.value.filter(item => item.name?.includes(searchText.value)) : dataList.value)
const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize) || 1)
const paginatedList = computed(() => filteredList.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize))

const handleSearch = () => { currentPage.value = 1 }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
const handleSelectionChange = (selection) => { selectedIds.value = selection.map(item => item.id) }
const handleImageChange = (file) => { 
  // 检查文件大小（限制为2MB）
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  // 检查文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => { 
    form.value.image = e.target.result 
  }
  reader.readAsDataURL(file.raw)
}
const handleAdd = () => { 
  dialogTitle.value = '新增商品'
  isEdit.value = false
  form.value = { 
    id: null, 
    name: '', 
    image: '', 
    points: 0, 
    stock: 0, 
    description: '', 
    status: 1,
    material: '',
    color: '',
    targetGroup: '',
    season: '',
    features: '',
    materialList: [],
    colorList: [],
    targetGroupList: [],
    seasonList: [],
    featuresList: []
  }
  dialogVisible.value = true 
}
const handleEdit = (row) => { 
  dialogTitle.value = '修改商品'
  isEdit.value = true
  form.value = { 
    ...row,
    materialList: row.material ? row.material.split(',') : [],
    colorList: row.color ? row.color.split(',') : [],
    targetGroupList: row.targetGroup ? row.targetGroup.split(',') : [],
    seasonList: row.season ? row.season.split(',') : [],
    featuresList: row.features ? row.features.split(',') : []
  }
  dialogVisible.value = true 
}
const handleSubmit = async () => {
  try {
    // 将勾选的数组转换为逗号分隔的字符串
    const submitData = {
      ...form.value,
      material: form.value.materialList.join(','),
      color: form.value.colorList.join(','),
      targetGroup: form.value.targetGroupList.join(','),
      season: form.value.seasonList.join(','),
      features: form.value.featuresList.join(',')
    }
    
    if (isEdit.value) {
      await updateGoods(submitData)
    } else {
      await addGoods(submitData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteGoods(row.id)
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
      await batchDeleteGoods(selectedIds.value)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  })
}

const loadData = async () => {
  try {
    const res = await getGoodsList()
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
.avatar-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; width: 178px; height: 178px; display: flex; align-items: center; justify-content: center; }
.avatar-uploader:hover { border-color: #ce4c4c; }
.avatar { width: 178px; height: 178px; object-fit: cover; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; }
:deep(.el-button--primary) { background-color: #ce4c4c; border-color: #ce4c4c; }
:deep(.el-button--primary:hover) { background-color: #d97373; border-color: #d97373; }
:deep(.el-table__header th) { background-color: #f5f5f5; color: #333; }
</style>