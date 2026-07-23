<template>
  <div class="profile">
    <el-card>
      <h2>个人信息</h2>
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
        class="profile-form"
        style="max-width: 900px; margin-top: 30px"
      >
        <el-form-item label="头像" class="full-row-item">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            action="#"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" disabled />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" disabled />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="区块链钱包">
          <el-input v-model="form.walletAddress" disabled placeholder="未绑定钱包地址" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-cascader
            v-model="form.address"
            :options="regionOptions"
            :props="{ value: 'name', label: 'name', children: 'children' }"
            placeholder="请选择省市区"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailedAddress">
          <el-input v-model="form.detailedAddress" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item class="full-row-item">
          <el-button type="primary" @click="handleSubmit">保存修改</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getVolunteerInfo, updateVolunteerInfo } from '@/api/volunteer'
import { regionData } from '@/utils/regions'

const formRef = ref(null)

const form = ref({
  username: '',
  realName: '',
  gender: '男',
  phone: '',
  email: '',
  avatar: '',
  idCard: '',
  walletAddress: '',
  address: [],
  detailedAddress: ''
})

const regionOptions = regionData

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const loadUserInfo = async () => {
  try {
    const res = await getVolunteerInfo()
    if (res.data) {
      const address = []
      if (res.data.province) address.push(res.data.province)
      if (res.data.city) address.push(res.data.city)
      if (res.data.district) address.push(res.data.district)
      form.value = {
        username: res.data.username,
        realName: res.data.realName,
        gender: res.data.gender || '男',
        phone: res.data.phone,
        email: res.data.email,
        avatar: res.data.avatar || '',
        idCard: res.data.idCard || '',
        walletAddress: res.data.walletAddress || '',
        address,
        detailedAddress: res.data.detailedAddress || ''
      }
      if (res.data.avatar) {
        localStorage.setItem('userAvatar', res.data.avatar)
      }
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    console.error(error)
  }
}

onMounted(() => {
  loadUserInfo()
})

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

const handleAvatarChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      
      let width = img.width
      let height = img.height
      const maxSize = 200
      
      if (width > height && width > maxSize) {
        height = (height * maxSize) / width
        width = maxSize
      } else if (height > maxSize) {
        width = (width * maxSize) / height
        height = maxSize
      }
      
      canvas.width = width
      canvas.height = height
      ctx.drawImage(img, 0, 0, width, height)
      
      form.value.avatar = canvas.toDataURL('image/jpeg', 0.7)
    }
    img.src = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const updateData = {
      username: form.value.username,
      gender: form.value.gender,
      phone: form.value.phone,
      email: form.value.email,
      avatar: form.value.avatar,
      detailedAddress: form.value.detailedAddress
    }
    if (form.value.address && form.value.address.length > 0) {
      updateData.province = form.value.address[0] || ''
      updateData.city = form.value.address[1] || ''
      updateData.district = form.value.address[2] || ''
    }
    await updateVolunteerInfo(updateData)
    localStorage.setItem('userAvatar', form.value.avatar)
    ElMessage.success('保存成功')
    window.location.reload()
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.profile {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #ce4c4c;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.profile-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 24px;
}

.profile-form :deep(.el-form-item) {
  margin-right: 0;
}

.profile-form :deep(.full-row-item) {
  grid-column: 1 / -1;
}
</style>