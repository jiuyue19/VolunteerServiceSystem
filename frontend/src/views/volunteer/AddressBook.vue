<template>
  <div class="address-book">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>收货地址管理</h2>
          <div class="header-actions">
            <el-button v-if="!isManageMode" @click="enterManageMode">
              <el-icon><Setting /></el-icon>
              管理
            </el-button>
            <template v-if="isManageMode">
              <el-checkbox 
                v-model="selectAll" 
                @change="handleSelectAll"
                :indeterminate="isIndeterminate"
              >
                全选
              </el-checkbox>
              <el-button 
                type="danger" 
                @click="batchDelete"
                :disabled="selectedAddresses.length === 0"
              >
                <el-icon><Delete /></el-icon>
                删除选中 ({{ selectedAddresses.length }})
              </el-button>
              <el-button @click="exitManageMode">取消</el-button>
            </template>
            <el-button type="primary" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              添加新地址
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="page-content">
        <!-- 地址列表 -->
        <div class="address-list" v-loading="loading">
          <el-card 
            v-for="address in addresses" 
            :key="address.id" 
            class="address-card"
            :class="{ 'selected': selectedAddresses.includes(address.id) }"
            shadow="hover"
          >
            <div class="address-header">
              <div class="address-info">
                <el-checkbox 
                  v-if="isManageMode" 
                  v-model="selectedAddresses" 
                  :value="address.id"
                  class="address-checkbox"
                />
                <span class="contact-name">{{ address.contactName }}</span>
                <span class="contact-phone">{{ address.contactPhone }}</span>
                <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
              </div>
              <div class="address-actions" v-if="!isManageMode">
                <el-button size="small" @click="editAddress(address)">编辑</el-button>
                <el-button 
                  v-if="!address.isDefault" 
                  size="small" 
                  type="primary" 
                  @click="setDefault(address)"
                >
                  设为默认
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="deleteAddress(address)"
                  :disabled="address.isDefault"
                >
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="address-content">
              <div class="address-detail">
                <el-icon><Location /></el-icon>
                <span class="address-text">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                </span>
              </div>
              <div class="address-label" v-if="address.label">
                <el-icon><Discount /></el-icon>
                <span>{{ address.label }}</span>
              </div>
            </div>
          </el-card>
          
          <!-- 空状态 -->
          <div v-if="!loading && addresses.length === 0" class="empty-state">
            <el-empty description="还没有收货地址">
              <el-button type="primary" @click="showAddDialog">添加第一个地址</el-button>
            </el-empty>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑地址' : '添加地址'" 
      width="600px"
      @close="resetForm"
    >
      <el-form 
        :model="addressForm" 
        :rules="formRules" 
        ref="formRef" 
        label-width="100px"
      >
        <el-form-item label="联系人" prop="contactName">
          <el-input 
            v-model="addressForm.contactName" 
            placeholder="请输入联系人姓名"
            maxlength="20"
          />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input 
            v-model="addressForm.contactPhone" 
            placeholder="请输入联系电话"
            maxlength="11"
          />
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <div class="region-selector">
            <el-select 
              v-model="addressForm.province" 
              placeholder="省份" 
              @change="handleProvinceChange"
              style="width: 120px"
            >
              <el-option 
                v-for="province in provinces" 
                :key="province.name" 
                :label="province.name" 
                :value="province.name" 
              />
            </el-select>
            <el-select 
              v-model="addressForm.city" 
              placeholder="城市" 
              @change="handleCityChange"
              style="width: 120px"
              :disabled="!addressForm.province"
            >
              <el-option 
                v-for="city in cities" 
                :key="city.name" 
                :label="city.name" 
                :value="city.name" 
              />
            </el-select>
            <el-select 
              v-model="addressForm.district" 
              placeholder="区县" 
              style="width: 120px"
              :disabled="!addressForm.city"
            >
              <el-option 
                v-for="district in districts" 
                :key="district.name" 
                :label="district.name" 
                :value="district.name" 
              />
            </el-select>
          </div>
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="addressForm.detailAddress" 
            type="textarea" 
            :rows="3"
            placeholder="请输入详细地址，如街道、门牌号等"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="地址标签">
          <el-radio-group v-model="addressForm.label">
            <el-radio value="">无标签</el-radio>
            <el-radio value="家">家</el-radio>
            <el-radio value="公司">公司</el-radio>
            <el-radio value="学校">学校</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="saving">
          {{ isEdit ? '更新' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Location, Discount, Setting, Delete } from '@element-plus/icons-vue'
import { regionData } from '@/utils/regions'
import { getAddressList, addAddress, updateAddress, deleteAddress as deleteAddressApi, batchDeleteAddresses, setDefaultAddress } from '@/api/address'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const addresses = ref([])
const formRef = ref()

// 管理模式相关
const isManageMode = ref(false)
const selectedAddresses = ref([])
const selectAll = ref(false)
const isIndeterminate = ref(false)

// 地区数据
const provinces = regionData
const cities = ref([])
const districts = ref([])

// 表单数据
const addressForm = reactive({
  id: null,
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  label: '',
  isDefault: false
})

// 表单验证规则
const formRules = {
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { 
      validator: (rule, value, callback) => {
        if (!addressForm.province || !addressForm.city) {
          callback(new Error('请选择完整的地区信息'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '详细地址长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 监听选中地址变化
watch(selectedAddresses, () => {
  updateSelectAllState()
}, { deep: true })

// 方法
const loadAddresses = async () => {
  try {
    loading.value = true
    const res = await getAddressList()
    const list = Array.isArray(res.data) ? res.data : []
    addresses.value = list.map(item => ({
      ...item,
      isDefault: !!item.isDefault
    }))
  } catch (error) {
    ElMessage.error('加载地址列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const editAddress = (address) => {
  isEdit.value = true
  dialogVisible.value = true
  
  // 填充表单数据
  Object.assign(addressForm, {
    id: address.id,
    contactName: address.contactName,
    contactPhone: address.contactPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detailAddress: address.detailAddress,
    label: address.label,
    isDefault: address.isDefault
  })
  
  // 更新地区选择器
  handleProvinceChange(address.province)
  handleCityChange(address.city)
}

const resetForm = () => {
  Object.assign(addressForm, {
    id: null,
    contactName: '',
    contactPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    label: '',
    isDefault: false
  })
  cities.value = []
  districts.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleProvinceChange = (provinceName) => {
  addressForm.city = ''
  addressForm.district = ''
  districts.value = []
  
  const province = provinces.find(p => p.name === provinceName)
  cities.value = province?.children || []
}

const handleCityChange = (cityName) => {
  addressForm.district = ''
  
  const city = cities.value.find(c => c.name === cityName)
  districts.value = city?.children || []
}

const saveAddress = async () => {
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    const payload = {
      ...addressForm,
      isDefault: addressForm.isDefault ? 1 : 0
    }
    if (isEdit.value) {
      await updateAddress(payload)
      ElMessage.success('地址更新成功')
    } else {
      await addAddress(payload)
      ElMessage.success('地址添加成功')
    }
    
    dialogVisible.value = false
    loadAddresses()
    
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

const setDefault = async (address) => {
  try {
    await setDefaultAddress(address.id)
    // 更新本地数据
    addresses.value.forEach(addr => {
      addr.isDefault = addr.id === address.id
    })
    ElMessage.success('默认地址设置成功')
  } catch (error) {
    ElMessage.error('设置失败: ' + (error.message || '未知错误'))
  }
}

const deleteAddress = async (address) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除地址"${address.contactName} ${address.contactPhone}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteAddressApi(address.id)
    ElMessage.success('地址删除成功')
    loadAddresses()
  } catch {
    // 用户取消删除
  }
}

// 管理模式方法
const enterManageMode = () => {
  isManageMode.value = true
  selectedAddresses.value = []
  selectAll.value = false
  isIndeterminate.value = false
}

const exitManageMode = () => {
  isManageMode.value = false
  selectedAddresses.value = []
  selectAll.value = false
  isIndeterminate.value = false
}

const handleSelectAll = (checked) => {
  if (checked) {
    selectedAddresses.value = addresses.value.map(addr => addr.id)
  } else {
    selectedAddresses.value = []
  }
  updateSelectAllState()
}

const updateSelectAllState = () => {
  const selectedCount = selectedAddresses.value.length
  const totalCount = addresses.value.length
  
  if (selectedCount === 0) {
    selectAll.value = false
    isIndeterminate.value = false
  } else if (selectedCount === totalCount) {
    selectAll.value = true
    isIndeterminate.value = false
  } else {
    selectAll.value = false
    isIndeterminate.value = true
  }
}

const batchDelete = async () => {
  if (selectedAddresses.value.length === 0) {
    ElMessage.warning('请选择要删除的地址')
    return
  }
  
  // 检查是否包含默认地址
  const hasDefaultAddress = addresses.value.some(addr => 
    selectedAddresses.value.includes(addr.id) && addr.isDefault
  )
  
  if (hasDefaultAddress) {
    ElMessage.warning('不能删除默认地址，请先设置其他地址为默认地址')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedAddresses.value.length} 个地址吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await batchDeleteAddresses(selectedAddresses.value)
    ElMessage.success('批量删除成功')
    loadAddresses()
    exitManageMode()
  } catch {
    // 用户取消删除
  }
}


// 监听选中地址变化
const watchSelectedAddresses = () => {
  updateSelectAllState()
}

// 生命周期
onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.address-book {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.main-card {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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

.address-list {
  min-height: 400px;
}

.address-card {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.address-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.address-card.selected {
  border: 2px solid #ce4c4c;
  background-color: #fef7f7;
}

.address-checkbox {
  margin-right: 10px;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.contact-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.contact-phone {
  font-size: 14px;
  color: #666;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.address-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-detail {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.address-text {
  flex: 1;
}

.address-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

/* 对话框内容样式 */
.region-selector {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .address-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .address-info {
    flex-direction: column;
    gap: 5px;
    align-items: flex-start;
  }
  
  .address-actions {
    justify-content: center;
    width: 100%;
  }
  
  .region-selector {
    flex-direction: column;
  }
  
  .region-selector .el-select {
    width: 100% !important;
  }
}
</style>
