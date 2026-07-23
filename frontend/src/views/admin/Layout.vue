<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <img src="/images/logo.jpg" alt="Logo" class="logo-img" />
        <span>志愿者服务系统</span>
      </div>
      <el-menu :default-active="$route.path" :unique-opened="true" router>
        <!-- 数据统计 - 所有管理员都可见 -->
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        
        <!-- 用户管理 -->
        <el-sub-menu index="user" v-if="hasPermission('admins') || hasPermission('volunteers')">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <!-- 管理员管理 - 仅超级管理员可见 -->
          <el-menu-item index="/admin/admins" v-if="hasPermission('admins')">管理员</el-menu-item>
          <!-- 志愿者管理 -->
          <el-menu-item index="/admin/volunteers" v-if="hasPermission('volunteers')">志愿者</el-menu-item>
        </el-sub-menu>
        
        <!-- 活动管理 -->
        <el-sub-menu index="activity" v-if="hasPermission('activities') || hasPermission('categories') || hasPermission('forum') || hasPermission('post-categories') || hasPermission('banners') || hasPermission('comments') || hasPermission('announcements')">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>活动管理</span>
          </template>
          <el-menu-item index="/admin/activities" v-if="hasPermission('activities')">志愿活动</el-menu-item>
          <el-menu-item index="/admin/categories" v-if="hasPermission('categories')">活动分类</el-menu-item>
          <el-menu-item index="/admin/forum" v-if="hasPermission('forum')">论坛帖子</el-menu-item>
          <el-menu-item index="/admin/post-categories" v-if="hasPermission('post-categories')">帖子分类</el-menu-item>
          <el-menu-item index="/admin/banners" v-if="hasPermission('banners')">轮播图信息</el-menu-item>
          <el-menu-item index="/admin/comments" v-if="hasPermission('comments')">评论信息</el-menu-item>
          <el-menu-item index="/admin/post-favorites" v-if="hasPermission('post-favorites')">收藏信息</el-menu-item>
          <el-menu-item index="/admin/announcements" v-if="hasPermission('announcements')">系统公告</el-menu-item>
          <el-menu-item index="/admin/hot-news" v-if="hasPermission('announcements')">热点信息</el-menu-item>
        </el-sub-menu>
        
        <!-- 志愿审核 -->
        <el-sub-menu index="audit" v-if="hasPermission('applications') || hasPermission('checkins') || hasPermission('replenish')">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>志愿审核</span>
          </template>
          <el-menu-item index="/admin/applications" v-if="hasPermission('applications')">报名申请</el-menu-item>
          <el-menu-item index="/admin/checkins" v-if="hasPermission('checkins')">打卡记录</el-menu-item>
          <el-menu-item index="/admin/replenish" v-if="hasPermission('replenish')">补录申请</el-menu-item>
        </el-sub-menu>
        
        <!-- 订单管理 -->
        <el-sub-menu index="order" v-if="hasPermission('goods') || hasPermission('orders')">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/admin/goods" v-if="hasPermission('goods')">积分商品</el-menu-item>
          <el-menu-item index="/admin/orders" v-if="hasPermission('orders')">兑换订单</el-menu-item>
        </el-sub-menu>
        
        <!-- 区块链管理 -->
        <el-sub-menu index="blockchain" v-if="hasPermission('blockchain-query') || hasPermission('blockchain') || hasPermission('certificate-library')">
          <template #title>
            <el-icon><Link /></el-icon>
            <span>区块链管理</span>
          </template>
          <el-menu-item index="/admin/blockchain-query" v-if="hasPermission('blockchain-query')">区块链服务查询</el-menu-item>
          <el-menu-item index="/admin/blockchain" v-if="hasPermission('blockchain')">区块链证书</el-menu-item>
          <el-menu-item index="/admin/certificate-library" v-if="hasPermission('certificate-library')">证书库管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="header-left">
            <el-button type="primary" @click="$router.push('/admin/dashboard')">首页</el-button>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :src="userInfo.avatar" :size="40">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userInfo.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { getAdminInfo } from '@/api/admin'
import { clearRoleStorage, setRoleItem, getRoleItem } from '@/utils/storage'

const router = useRouter()
const userInfo = ref({
  username: '管理员',
  avatar: '',
  role: '',
  permissions: []
})

// 判断是否是超级管理员
const isSuperAdmin = computed(() => {
  return userInfo.value.role === 'SUPER_ADMIN'
})

// 判断是否有某个权限
const hasPermission = (permission) => {
  if (isSuperAdmin.value) return true
  return userInfo.value.permissions.includes(permission)
}

const loadUserInfo = async () => {
  try {
    const res = await getAdminInfo()
    if (res.data) {
      // 解析权限
      let permissions = []
      if (res.data.permissions) {
        try {
          permissions = JSON.parse(res.data.permissions)
        } catch (e) {
          permissions = []
        }
      }
      
      userInfo.value = {
        username: res.data.username || '管理员',
        avatar: res.data.avatar || '',
        role: res.data.role || 'ADMIN',
        permissions: permissions
      }
      // 保存管理员信息到角色存储
      setRoleItem('adminRole', res.data.role, 'admin')
      setRoleItem('adminPermissions', JSON.stringify(permissions), 'admin')
      if (res.data.avatar) {
        localStorage.setItem('adminAvatar', res.data.avatar)
      }
      if (res.data.username) {
        localStorage.setItem('adminUsername', res.data.username)
      }
      console.log('管理员信息加载成功:', userInfo.value)
      console.log('是否是超级管理员:', isSuperAdmin.value)
      console.log('管理员权限:', userInfo.value.permissions)
    }
  } catch (error) {
    console.error('加载管理员信息失败:', error)
    const savedAvatar = localStorage.getItem('adminAvatar')
    const savedUsername = localStorage.getItem('adminUsername')
    const savedRole = getRoleItem('adminRole', 'admin')
    const savedPermissions = getRoleItem('adminPermissions', 'admin')
    
    if (savedAvatar) {
      userInfo.value.avatar = savedAvatar
    }
    if (savedUsername) {
      userInfo.value.username = savedUsername
    }
    if (savedRole) {
      userInfo.value.role = savedRole
    }
    if (savedPermissions) {
      try {
        userInfo.value.permissions = JSON.parse(savedPermissions)
      } catch (e) {
        userInfo.value.permissions = []
      }
    }
    console.log('使用缓存的管理员信息:', userInfo.value)
    console.log('是否是超级管理员:', isSuperAdmin.value)
    console.log('管理员权限:', userInfo.value.permissions)
  }
}

onMounted(() => {
  loadUserInfo()
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'logout') {
    clearRoleStorage('admin')
    localStorage.removeItem('adminAvatar')
    localStorage.removeItem('adminUsername')
    ElMessage.success('退出成功')
    router.push('/login')
  }
}

// 检查权限，阻止普通管理员访问受限页面
const checkRoutePermission = () => {
  const currentPath = router.currentRoute.value.path
  console.log('检查路由权限:', currentPath)
  console.log('当前用户角色:', userInfo.value.role)
  console.log('是否是超级管理员:', isSuperAdmin.value)
  
  if (!isSuperAdmin.value) {
    // 普通管理员不能访问的路由
    const restrictedRoutes = [
      '/admin/admins',
      '/admin/goods',
      '/admin/orders',
      '/admin/blockchain',
      '/admin/blockchain-query',
      '/admin/certificate-library'
    ]
    if (restrictedRoutes.includes(currentPath)) {
      console.log('访问受限页面，准备拦截')
      ElMessage.error('您没有权限访问该页面')
      router.push('/admin/dashboard')
    }
  } else {
    console.log('超级管理员，允许访问')
  }
}

// 使用 watch 监听路由变化和用户信息变化
watch(() => router.currentRoute.value.path, () => {
  // 确保用户信息已加载后再检查权限
  if (userInfo.value.role) {
    checkRoutePermission()
  }
}, { immediate: false })
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  background-color: #ce4c4c;
  border-bottom: 1px solid #a53d3d;
}

.logo-img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
}

.el-aside {
  background-color: #ce4c4c;
}

.el-menu {
  border-right: none;
  background-color: #ce4c4c;
}

.el-menu-item {
  color: #fff;
  background-color: transparent;
}

.el-menu-item:hover {
  background-color: #d97373 !important;
  color: #fff !important;
}

.el-menu-item.is-active {
  background-color: #a53d3d !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title) {
  color: #fff;
  background-color: transparent;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #d97373 !important;
  color: #fff !important;
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #fff !important;
}

:deep(.el-menu--inline) {
  background-color: #b84040 !important;
}

:deep(.el-menu--inline .el-menu-item) {
  background-color: #b84040 !important;
  color: #fff;
  padding-left: 50px !important;
}

:deep(.el-menu--inline .el-menu-item:hover) {
  background-color: #d97373 !important;
}

:deep(.el-menu--inline .el-menu-item.is-active) {
  background-color: #a53d3d !important;
}

.el-header {
  background-color: #ce4c4c;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-content :deep(.el-button--primary) {
  background-color: #fff;
  color: #ce4c4c;
  border-color: #fff;
}

.header-content :deep(.el-button--primary:hover) {
  background-color: #f0f0f0;
  color: #ce4c4c;
  border-color: #f0f0f0;
}

.header-left {
  display: flex;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  font-weight: normal;
  color: #fff;
}

.el-main {
  background-color: #f0f2f5;
}
</style>