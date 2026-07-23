<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>权限管理</h2>
          <el-tag type="warning">仅超级管理员可见</el-tag>
        </div>
      </template>

      <el-table :data="adminList" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'SUPER_ADMIN'" type="danger">超级管理员</el-tag>
            <el-tag v-else type="success">普通管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限数量" width="120">
          <template #default="{ row }">
            <span v-if="row.role === 'SUPER_ADMIN'">全部权限</span>
            <span v-else>{{ row.permissionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEditPermission(row)"
              :disabled="row.role === 'SUPER_ADMIN'"
            >
              配置权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`配置权限 - ${currentAdmin.username}`"
      width="700px"
    >
      <div class="permission-dialog">
        <el-alert
          title="提示"
          description="请勾选该管理员可以访问的菜单权限"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        />

        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          :default-checked-keys="selectedPermissionIds"
          :default-expand-all="true"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span>{{ data.permissionName }}</span>
              <span class="permission-desc">{{ data.description }}</span>
            </span>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminList } from '@/api/admin'
import { getAllPermissions, getAdminPermissions, assignPermissions } from '@/api/permission'

const adminList = ref([])
const allPermissions = ref([])
const dialogVisible = ref(false)
const currentAdmin = ref({})
const selectedPermissionIds = ref([])
const treeRef = ref(null)
const loading = ref(false)

const treeProps = {
  children: 'children',
  label: 'permissionName'
}

// 构建权限树
const permissionTree = computed(() => {
  const tree = []
  const map = {}

  // 创建节点映射
  allPermissions.value.forEach(permission => {
    map[permission.id] = {
      ...permission,
      children: []
    }
  })

  // 构建树结构
  allPermissions.value.forEach(permission => {
    const node = map[permission.id]
    if (permission.parentId === 0) {
      tree.push(node)
    } else {
      const parent = map[permission.parentId]
      if (parent) {
        parent.children.push(node)
      }
    }
  })

  return tree
})

// 加载管理员列表
const loadAdminList = async () => {
  try {
    const res = await getAdminList()
    if (res.code === 200) {
      adminList.value = res.data
      // 为每个普通管理员加载权限数量
      for (const admin of adminList.value) {
        if (admin.role !== 'SUPER_ADMIN') {
          const permRes = await getAdminPermissions(admin.id)
          if (permRes.code === 200) {
            admin.permissionCount = permRes.data.permissions.length
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载管理员列表失败')
  }
}

// 加载所有权限
const loadAllPermissions = async () => {
  try {
    const res = await getAllPermissions()
    if (res.code === 200) {
      allPermissions.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载权限列表失败')
  }
}

// 编辑权限
const handleEditPermission = async (admin) => {
  currentAdmin.value = admin
  dialogVisible.value = true

  try {
    const res = await getAdminPermissions(admin.id)
    if (res.code === 200) {
      selectedPermissionIds.value = res.data.permissions.map(p => p.id)
    }
  } catch (error) {
    ElMessage.error('加载管理员权限失败')
  }
}

// 提交权限配置
const handleSubmit = async () => {
  loading.value = true
  try {
    const checkedKeys = treeRef.value.getCheckedKeys()
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]

    await assignPermissions({
      adminId: currentAdmin.value.id,
      permissionIds
    })

    ElMessage.success('权限配置成功')
    dialogVisible.value = false
    loadAdminList()
  } catch (error) {
    ElMessage.error('权限配置失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAdminList()
  loadAllPermissions()
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

.permission-dialog {
  max-height: 500px;
  overflow-y: auto;
}

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  padding-right: 20px;
}

.permission-desc {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
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
