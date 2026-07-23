<template>
  <div class="profile">
    <el-card>
      <h2>个人信息</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px; margin-top: 30px">
        <el-form-item label="头像">
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
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item>
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
import { getAdminInfo, updateAdminInfo } from '@/api/admin'

const formRef = ref(null)
const form = ref({
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const loadUserInfo = async () => {
  try {
    const res = await getAdminInfo()
    if (res.data) {
      form.value = {
        username: res.data.username || '管理员',
        realName: res.data.realName || '系统管理员',
        phone: res.data.phone || '',
        email: res.data.email || '',
        avatar: res.data.avatar || ''
      }
      if (res.data.avatar) {
        localStorage.setItem('adminAvatar', res.data.avatar)
      }
      if (res.data.username) {
        localStorage.setItem('adminUsername', res.data.username)
      }
    }
  } catch (error) {
    const savedAvatar = localStorage.getItem('adminAvatar')
    const savedUsername = localStorage.getItem('adminUsername')
    form.value = {
      username: savedUsername || '管理员',
      realName: '系统管理员',
      phone: '',
      email: '',
      avatar: savedAvatar || ''
    }
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
      
      form.value.avatar = canvas.toDataURL('image/jpeg', 0.8)
    }
    img.src = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await updateAdminInfo({
      username: form.value.username,
      phone: form.value.phone,
      email: form.value.email,
      avatar: form.value.avatar
    })
    localStorage.setItem('adminAvatar', form.value.avatar)
    localStorage.setItem('adminUsername', form.value.username)
    ElMessage.success('保存成功')
    window.location.reload()
  } catch (error) {
    ElMessage.error('保存失败')
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
</style>