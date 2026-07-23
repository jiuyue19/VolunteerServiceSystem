import { createRouter, createWebHistory } from 'vue-router'
import { getRoleItem, getRoleTypeByPath } from '@/utils/storage'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/Layout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'activities',
        name: 'AdminActivities',
        component: () => import('@/views/admin/Activities.vue')
      },
      {
        path: 'applications',
        name: 'AdminApplications',
        component: () => import('@/views/admin/Applications.vue')
      },
      {
        path: 'blockchain',
        name: 'AdminBlockchain',
        component: () => import('@/views/admin/Blockchain.vue')
      },
      {
        path: 'blockchain-query',
        name: 'BlockchainQuery',
        component: () => import('@/views/admin/BlockchainQuery.vue')
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue')
      },
      {
        path: 'admins',
        name: 'AdminManage',
        component: () => import('@/views/admin/Admins.vue')
      },
      {
        path: 'volunteers',
        name: 'VolunteersManage',
        component: () => import('@/views/admin/Volunteers.vue')
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/admin/Categories.vue')
      },
      {
        path: 'forum',
        name: 'AdminForum',
        component: () => import('@/views/admin/Forum.vue')
      },
      {
        path: 'post-categories',
        name: 'PostCategories',
        component: () => import('@/views/admin/PostCategories.vue')
      },
      {
        path: 'banners',
        name: 'Banners',
        component: () => import('@/views/admin/Banners.vue')
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('@/views/admin/Comments.vue')
      },
      {
        path: 'post-favorites',
        name: 'PostFavorites',
        component: () => import('@/views/admin/PostFavorites.vue')
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/admin/Announcements.vue')
      },
      {
        path: 'hot-news',
        name: 'HotNews',
        component: () => import('@/views/admin/HotNews.vue')
      },
      {
        path: 'checkins',
        name: 'Checkins',
        component: () => import('@/views/admin/Checkins.vue')
      },
      {
        path: 'replenish',
        name: 'Replenish',
        component: () => import('@/views/admin/Replenish.vue')
      },
      {
        path: 'goods',
        name: 'Goods',
        component: () => import('@/views/admin/Goods.vue')
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/admin/Orders.vue')
      },
      {
        path: 'certificate-library',
        name: 'CertificateLibrary',
        component: () => import('@/views/admin/CertificateLibrary.vue')
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('@/views/admin/Permissions.vue')
      }
    ]
  },
  {
    path: '/volunteer',
    name: 'VolunteerLayout',
    component: () => import('@/views/volunteer/Layout.vue'),
    children: [
      {
        path: 'home',
        name: 'VolunteerHome',
        component: () => import('@/views/volunteer/Home.vue')
      },
      {
        path: 'dashboard',
        name: 'VolunteerDashboard',
        component: () => import('@/views/volunteer/Dashboard.vue')
      },
      {
        path: 'activities',
        name: 'VolunteerActivities',
        component: () => import('@/views/volunteer/Activities.vue')
      },
      {
        path: 'activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/volunteer/ActivityDetail.vue')
      },
      {
        path: 'forum',
        name: 'Forum',
        component: () => import('@/views/volunteer/Forum.vue')
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/volunteer/PostDetail.vue')
      },
      {
        path: 'exchange',
        name: 'Exchange',
        component: () => import('@/views/volunteer/Exchange.vue')
      },
      {
        path: 'my-applications',
        name: 'MyApplications',
        component: () => import('@/views/volunteer/MyApplications.vue')
      },
      {
        path: 'certificate',
        name: 'Certificate',
        component: () => import('@/views/volunteer/Certificate.vue')
      },
      {
        path: 'profile',
        name: 'VolunteerProfile',
        component: () => import('@/views/volunteer/Profile.vue')
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('@/views/volunteer/ChangePassword.vue')
      },
      {
        path: 'my-posts',
        name: 'MyPosts',
        component: () => import('@/views/volunteer/MyPosts.vue')
      },
      {
        path: 'my-favorites',
        name: 'MyFavorites',
        component: () => import('@/views/volunteer/MyFavorites.vue')
      },
      {
        path: 'my-comments',
        name: 'MyComments',
        component: () => import('@/views/volunteer/MyComments.vue')
      },
      {
        path: 'my-exchanges',
        name: 'MyExchanges',
        component: () => import('@/views/volunteer/MyExchanges.vue')
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/volunteer/MyOrders.vue')
      },
      {
        path: 'address-book',
        name: 'AddressBook',
        component: () => import('@/views/volunteer/AddressBook.vue')
      },
      {
        path: 'certificate-download',
        name: 'CertificateDownload',
        component: () => import('@/views/volunteer/CertificateDownload.vue')
      }
    ]
  },
  {
    path: '/certificate-verify',
    name: 'PublicCertificateVerify',
    component: () => import('@/views/public/CertificateVerify.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 证书验伪页面无需登录即可访问
  if (to.path === '/certificate-verify') {
    next()
    return
  }
  
  // 登录和注册页面直接放行
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }
  
  // 根据目标路由获取对应角色的token
  const roleType = getRoleTypeByPath(to.path)
  const token = getRoleItem('token', roleType)
  
  if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router