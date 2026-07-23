<template>
  <div class="change-password">
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h2>修改密码</h2>
        </div>
      </template>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="120px"
        style="max-width: 500px; margin: 0 auto;"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="form.currentPassword" 
            type="password" 
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="form.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            确认修改
          </el-button>
          <el-button type="default" @click="handleCancel">
            取消
          </el-button>
          <el-button type="text" @click="openForgotDialog">
            忘记密码？
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog
      v-model="forgotDialogVisible"
      title="忘记密码"
      width="480px"
    >
      <el-form
        :model="forgotForm"
        :rules="forgotRules"
        ref="forgotFormRef"
        label-width="100px"
      >
        <el-form-item label="找回方式" prop="type">
          <el-radio-group v-model="forgotForm.type">
            <el-radio-button label="phone">手机号</el-radio-button>
            <el-radio-button label="email">邮箱</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="forgotForm.type === 'phone'" label="手机号" prop="phone">
          <el-input v-model="forgotForm.phone" placeholder="请输入绑定的手机号" />
        </el-form-item>

        <el-form-item v-if="forgotForm.type === 'email'" label="邮箱" prop="email">
          <el-input v-model="forgotForm.email" placeholder="请输入绑定的邮箱" />
        </el-form-item>

        <el-form-item label="验证码" prop="code">
          <el-input v-model="forgotForm.code" placeholder="请输入验证码">
            <template #append>
              <el-button
                type="primary"
                @click="handleSendCode"
                :loading="forgotLoading"
              >发送验证码</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="forgotDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="forgotLoading" @click="handleForgotSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendVolunteerResetCode, resetVolunteerPasswordByCode, changeVolunteerPassword } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

// 忘记密码弹窗相关
const forgotDialogVisible = ref(false)
const forgotFormRef = ref()
const forgotLoading = ref(false)
const forgotForm = ref({
  type: 'phone',
  phone: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const forgotRules = {
  type: [{ required: true, message: '请选择方式', trigger: 'change' }],
  phone: [
    {
      validator: (rule, value, callback) => {
        if (forgotForm.value.type === 'phone') {
          if (!value) {
            callback(new Error('请输入手机号'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (rule, value, callback) => {
        if (forgotForm.value.type === 'email') {
          if (!value) {
            callback(new Error('请输入邮箱'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const form = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    await changeVolunteerPassword({
      currentPassword: form.value.currentPassword,
      newPassword: form.value.newPassword
    })
    
    ElMessage.success('密码修改成功，请重新登录')
    handleReset()
    // 清理本地登录状态并跳转登录页
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userType')
    router.push('/login')
    
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error('密码修改失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  form.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  formRef.value?.clearValidate()
}

const openForgotDialog = () => {
  forgotForm.value = {
    type: 'phone',
    phone: '',
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: ''
  }
  forgotFormRef.value?.clearValidate()
  forgotDialogVisible.value = true
}

const handleSendCode = async () => {
  try {
    if (forgotForm.value.type === 'phone') {
      if (!forgotForm.value.phone) {
        ElMessage.error('请输入手机号')
        return
      }
    } else if (forgotForm.value.type === 'email') {
      if (!forgotForm.value.email) {
        ElMessage.error('请输入邮箱')
        return
      }
    }
    forgotLoading.value = true
    await sendVolunteerResetCode({
      type: forgotForm.value.type,
      phone: forgotForm.value.phone,
      email: forgotForm.value.email
    })
    ElMessage.success('验证码已发送，请注意查收')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('验证码发送失败: ' + (error.message || '未知错误'))
    }
  } finally {
    forgotLoading.value = false
  }
}

const handleForgotSubmit = async () => {
  try {
    await forgotFormRef.value.validate()
    forgotLoading.value = true
    await resetVolunteerPasswordByCode({
      type: forgotForm.value.type,
      phone: forgotForm.value.phone,
      email: forgotForm.value.email,
      code: forgotForm.value.code,
      newPassword: forgotForm.value.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    forgotDialogVisible.value = false
    // 清理本地登录状态并跳转登录页
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userType')
    router.push('/login')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('密码重置失败: ' + (error.message || '未知错误'))
    }
  } finally {
    forgotLoading.value = false
  }
}

const handleCancel = () => {
  router.go(-1) // 返回上一页
}
</script>

<style scoped>
.change-password {
  padding: 20px;
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-card {
  width: 100%;
  max-width: 600px;
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

:deep(.el-button--primary) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-button--primary:hover) {
  background-color: #d97373;
  border-color: #d97373;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

.forgot-link-button {
  margin-left: auto;
}
</style>
