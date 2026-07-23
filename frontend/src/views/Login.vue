<template>
  <div class="login-container">
    <div class="login-card">
      <div class="title-header">
        <img src="/images/logo.jpg" alt="Logo" class="logo" />
        <h2>志愿者服务系统</h2>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
            prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon @click="showPassword = !showPassword" style="cursor: pointer">
                <View v-if="!showPassword" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <div class="role-switch">
          <div
            :class="['role-dot', { active: userType === 'volunteer' }]"
            @click="userType = 'volunteer'"
            title="志愿者"
          >
            <el-icon><User /></el-icon>
          </div>
          <div
            :class="['role-dot', { active: userType === 'admin' }]"
            @click="userType = 'admin'"
            title="管理员"
          >
            <el-icon><UserFilled /></el-icon>
          </div>
        </div>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
      <div v-if="userType === 'volunteer'" class="register-link">
        <el-button text @click="$router.push('/register')">还没有账号？立即注册</el-button>
        <el-button text @click="openForgotDialog">忘记密码？</el-button>
      </div>
    </div>

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

        <el-form-item label="确认密码" prop="confirmPassword" >
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
import { adminLogin, volunteerLogin, sendVolunteerResetCode, resetVolunteerPasswordByCode } from '@/api/auth'
import { setRoleItem, setCurrentUserType } from '@/utils/storage'

const router = useRouter()
const formRef = ref(null)
const userType = ref('volunteer')
const showPassword = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const forgotDialogVisible = ref(false)
const forgotFormRef = ref(null)
const forgotLoading = ref(false)
const forgotForm = ref({
  type: 'email',
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

const decodeTokenPayload = (token) => {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const base = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base.padEnd(base.length + (4 - (base.length % 4 || 4)) % 4, '=')
    const json = atob(padded)
    return JSON.parse(json)
  } catch (e) {
    return null
  }
}

const handleLogin = async () => {
  await formRef.value.validate()
  try {
    const loginFn = userType.value === 'volunteer' ? volunteerLogin : adminLogin
    const res = await loginFn(form.value)
    
    // 使用角色前缀存储，避免不同角色数据互相影响
    const roleType = userType.value
    setRoleItem('token', res.data.token, roleType)
    
    if (res.data.refreshToken) {
      setRoleItem('refreshToken', res.data.refreshToken, roleType)
    }
    
    // 设置当前激活的用户类型
    setCurrentUserType(roleType)
    
    const payload = decodeTokenPayload(res.data.token)
    if (payload?.userId) {
      const user = {
        id: payload.userId,
        username: payload.sub || payload.username || form.value.username,
        role: payload.role
      }
      setRoleItem('user', user, roleType)
      setRoleItem('userId', payload.userId, roleType)
    }
    
    ElMessage.success('登录成功')
    router.push(userType.value === 'volunteer' ? '/volunteer/home' : '/admin/dashboard')
  } catch (error) {
    console.error(error)
  }
}

const openForgotDialog = () => {
  forgotForm.value = {
    type: 'email',
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
    console.error(error)
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
  } catch (error) {
    console.error(error)
  } finally {
    forgotLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('/images/background5.jpg') no-repeat center center;
  background-size: cover;
}

.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.title-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 30px;
}

.logo {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
}

h2 {
  color: #ce4c4c;
  font-weight: 600;
  margin: 0;
}

.role-switch {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin: 15px 0 20px;
}

.role-dot {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(206, 76, 76, 0.2);
  border: 2px solid rgba(206, 76, 76, 0.4);
  color: #ce4c4c;
}

.role-dot:hover {
  background: rgba(206, 76, 76, 0.3);
  transform: scale(1.1);
}

.role-dot.active {
  background: rgba(206, 76, 76, 0.6);
  border-color: #ce4c4c;
  box-shadow: 0 0 15px rgba(206, 76, 76, 0.5);
}

.login-card :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(206, 76, 76, 0.3);
  box-shadow: none;
}

.login-card :deep(.el-input__wrapper:hover),
.login-card :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(206, 76, 76, 0.5);
}

.login-card :deep(.el-input__inner) {
  color: #ce4c4c;
}

.login-card :deep(.el-input__inner::placeholder) {
  color: rgba(206, 76, 76, 0.6);
}

.login-card :deep(.el-input__prefix),
.login-card :deep(.el-input__suffix) {
  color: rgba(206, 76, 76, 0.8);
}

.login-card :deep(.el-button--primary) {
  background: rgba(206, 76, 76, 0.7);
  border: none;
  backdrop-filter: blur(5px);
  color: #fff;
}

.login-card :deep(.el-button--primary:hover) {
  background: rgba(206, 76, 76, 0.9);
}

.register-link {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.register-link :deep(.el-button--text) {
  color: #ce4c4c;
}
</style>