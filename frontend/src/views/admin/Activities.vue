<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>活动管理</h2>
        </div>
      </template>
      
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchText"
            placeholder="搜索活动标题"
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

        <el-table-column prop="title" label="活动标题" width="180" show-overflow-tooltip />
                <el-table-column prop="categoryId" label="活动分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.categoryId)" size="small">{{ getCategoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coverImage" label="封面图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 60px; height: 60px" fit="cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
         <el-table-column prop="targetNumber" label="目标人数" width="100" />
        <el-table-column prop="currentNumber" label="当前人数" width="100" />
        <el-table-column prop="serviceHours" label="服务时长" width="100">
          <template #default="{ row }">
            {{ row.serviceHours }}小时
          </template>
        </el-table-column>
        <el-table-column prop="rewardPoints" label="奖励积分" width="100">
          <template #default="{ row }">
            {{ row.rewardPoints }}分
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatTimeDisplay(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatTimeDisplay(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="基本地址" width="120" show-overflow-tooltip />
        <el-table-column prop="detailedAddress" label="详细地址" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.detailedAddress">{{ row.detailedAddress }}</span>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerField" label="志愿领域" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.volunteerField" type="primary" size="small">{{ row.volunteerField }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerTarget" label="志愿对象" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.volunteerTarget" type="success" size="small">{{ row.volunteerTarget }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="serviceLocation" label="志愿场所" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.serviceLocation" type="warning" size="small">{{ row.serviceLocation }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="venueImage" label="场所图片" width="120">
          <template #default="{ row }">
            <span v-if="getVenueImageArray(row.venueImage).length > 0" class="view-images-link" @click="handleViewVenueImages(row)">
              查看图片
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="organizationName" label="组织名称" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.organizationName">{{ row.organizationName }}</span>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100">
          <template #default="{ row }">
            <span v-if="row.contactPerson">{{ row.contactPerson }}</span>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="120">
          <template #default="{ row }">
            <span v-if="row.contactPhone">{{ row.contactPhone }}</span>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTimeDisplay(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleGenerateCode(row)">验证码</el-button>
            <el-button type="success" size="small" @click="handleEdit(row)">修改</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="封面图片">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :auto-upload="false" :on-change="handleCoverImageChange">
            <img v-if="form.coverImage" :src="form.coverImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：16:9，大小不超过2MB</div>
        </el-form-item>
        <el-form-item label="场所图片">
          <div class="multi-upload-container">
            <el-upload
              class="multi-uploader"
              action="#"
              :auto-upload="false"
              list-type="picture-card"
              :file-list="venueImageList"
              :on-change="handleVenueImageChange"
              :on-remove="handleVenueImageRemove"
              :limit="10"
            >
              <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="upload-tip">支持上传多张图片，建议尺寸：16:9，单张大小不超过2MB，最多10张</div>
        </el-form-item>
        <el-form-item label="活动分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option label="社区服务" :value="1" />
            <el-option label="环境保护" :value="2" />
            <el-option label="支教助学" :value="3" />
            <el-option label="景区引导" :value="4" />
            <el-option label="禁毒宣传" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="活动内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="活动地址">
          <el-input v-model="form.address" placeholder="如：广东省广州市天河区" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detailedAddress" placeholder="如：天河路XXX号XXX大厦" />
        </el-form-item>
        <el-form-item label="活动位置">
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-button size="small" @click="showLocationMapDialog = true">在地图上选择位置</el-button>
            <span v-if="form.latitude && form.longitude" class="location-summary">
              已选择：纬度 {{ form.latitude }}, 经度 {{ form.longitude }}
            </span>
            <span v-else class="location-summary empty">未选择位置</span>
          </div>
        </el-form-item>
        <el-form-item label="志愿领域">
          <el-select v-model="form.volunteerField" placeholder="请选择志愿领域">
            <el-option label="社区服务" value="社区服务" />
            <el-option label="环境保护" value="环境保护" />
            <el-option label="支教助学" value="支教助学" />
            <el-option label="医疗卫生" value="医疗卫生" />
            <el-option label="文化传承" value="文化传承" />
            <el-option label="禁毒宣传" value="禁毒宣传" />
            <el-option label="交通引导" value="交通引导" />
            <el-option label="应急救援" value="应急救援" />
            <el-option label="景区引导" value="景区引导" />
            <el-option label="扶老助残" value="扶老助残" />
            <el-option label="法律援助" value="法律援助" />
            <el-option label="科普宣传" value="科普宣传" />
            <el-option label="赛会服务" value="赛会服务" />
            <el-option label="爱心捐赠" value="爱心捐赠" />
          </el-select>
        </el-form-item>
        <el-form-item label="志愿对象">
          <el-select v-model="form.volunteerTarget" placeholder="请选择志愿对象">
            <el-option label="全体志愿者" value="全体志愿者" />
            <el-option label="老年人" value="老年人" />
            <el-option label="儿童" value="儿童" />
            <el-option label="青少年" value="青少年" />
            <el-option label="残疾人" value="残疾人" />
            <el-option label="困难群众" value="困难群众" />
            <el-option label="社区居民" value="社区居民" />
            <el-option label="游客" value="游客" />
            <el-option label="学生" value="学生" />
            <el-option label="农民工" value="农民工" />
            <el-option label="留守儿童" value="留守儿童" />
            <el-option label="空巢老人" value="空巢老人" />
          </el-select>
        </el-form-item>
        <el-form-item label="志愿场所">
          <el-select v-model="form.serviceLocation" placeholder="请选择志愿场所">
            <el-option label="社区" value="社区" />
            <el-option label="学校" value="学校" />
            <el-option label="医院" value="医院" />
            <el-option label="公园" value="公园" />
            <el-option label="养老院" value="养老院" />
            <el-option label="福利院" value="福利院" />
            <el-option label="图书馆" value="图书馆" />
            <el-option label="街道" value="街道" />
            <el-option label="景区" value="景区" />
            <el-option label="博物馆" value="博物馆" />
            <el-option label="体育馆" value="体育馆" />
            <el-option label="文化馆" value="文化馆" />
            <el-option label="火车站" value="火车站" />
            <el-option label="汽车站" value="汽车站" />
            <el-option label="机场" value="机场" />
            <el-option label="山区" value="山区" />
            <el-option label="农村" value="农村" />
          </el-select>
        </el-form-item>
        <el-form-item label="组织名称">
          <el-input v-model="form.organizationName" placeholder="如：XX社区居委会" />
        </el-form-item>
        <el-form-item label="组织图片">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :auto-upload="false" :on-change="handleOrganizationImageChange">
            <img v-if="form.organizationImage" :src="form.organizationImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">可选，组织/机构的Logo或图片</div>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" />
        </el-form-item>
        <el-form-item label="目标人数">
          <el-input-number v-model="form.targetNumber" :min="1" />
        </el-form-item>
        <el-form-item label="服务时长">
          <el-input v-model="calculatedHoursDisplay" disabled>
            <template #suffix>小时</template>
          </el-input>
          <div class="upload-tip">系统根据起止时间自动计算</div>
        </el-form-item>
        <el-form-item label="奖励积分">
          <el-input v-model="calculatedPointsDisplay" disabled>
            <template #suffix>分</template>
          </el-input>
          <div class="upload-tip">按 1小时10积分 计算</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 场所图片查看对话框 -->
    <el-dialog v-model="showVenueImagesDialog" title="场所图片" width="800px">
      <el-carousel :interval="5000" height="400px" v-if="currentVenueImages.length > 0">
        <el-carousel-item v-for="(img, index) in currentVenueImages" :key="index">
          <img :src="img" style="width: 100%; height: 100%; object-fit: contain;" />
        </el-carousel-item>
      </el-carousel>
      <div v-else style="text-align: center; padding: 50px; color: #999;">暂无场所图片</div>
    </el-dialog>

    <!-- 活动位置选择地图对话框 -->
    <el-dialog
      v-model="showLocationMapDialog"
      title="选择活动位置"
      width="900px"
      @open="initActivityMap"
    >
      <div class="map-toolbar">
        <el-input
          v-model="activityMapSearchKeyword"
          placeholder="输入地点关键字搜索（如：天河体育中心）"
          size="small"
          style="width: 260px;"
          @keyup.enter="handleActivityMapSearch"
        />
        <el-button size="small" @click="handleActivityMapSearch">搜索</el-button>
        <el-button size="small" @click="locateActivityByGPS">使用当前位置</el-button>
      </div>
      <div class="map-body">
        <div id="activity-map-container" class="map-main"></div>
        <div id="activity-poi-panel" class="map-poi-panel"></div>
      </div>
      <template #footer>
        <el-button @click="showLocationMapDialog = false">取消</el-button>
        <el-button type="primary" @click="showLocationMapDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityList, createActivity, updateActivity, deleteActivity } from '@/api/activity'
import { generateCode } from '@/api/checkin'

const activities = ref([])
const searchText = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = 5
const venueImageList = ref([])
const showVenueImagesDialog = ref(false)
const currentVenueImages = ref([])
const currentImageIndex = ref(0)

const showLocationMapDialog = ref(false)
const activityMap = ref(null)
const activityMarker = ref(null)
const activityGeocoder = ref(null)
const activityPlaceSearch = ref(null)
const activityMapSearchKeyword = ref('')

const form = ref({
  id: null,
  categoryId: null,
  title: '',
  content: '',
  address: '',
  detailedAddress: '',
  latitude: null,
  longitude: null,
  startTime: '',
  endTime: '',
  targetNumber: 10,
  currentNumber: 0,
  serviceHours: 0,
  rewardPoints: 0,
  status: '招募中',
  coverImage: '',
  venueImage: '[]',
  volunteerField: '',
  volunteerTarget: '',
  serviceLocation: '',
  organizationName: '',
  organizationImage: '',
  contactPerson: '',
  contactPhone: ''
})

const filteredList = computed(() => {
  if (!searchText.value) {
    return activities.value
  }
  return activities.value.filter(item =>
    item.title?.includes(searchText.value)
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

// 计算服务时长（小时）
const calculatedHoursDisplay = computed(() => {
  if (!form.value.startTime || !form.value.endTime) {
    return '0.00'
  }
  const start = new Date(form.value.startTime)
  const end = new Date(form.value.endTime)
  const minutes = (end - start) / 1000 / 60
  const hours = (minutes / 60).toFixed(2)
  return hours
})

// 计算奖励积分
const calculatedPointsDisplay = computed(() => {
  const hours = parseFloat(calculatedHoursDisplay.value)
  const points = Math.round(hours * 10)
  return points.toString()
})

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

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  isEdit.value = false
  form.value = {
    id: null,
    categoryId: null,
    title: '',
    content: '',
    address: '',
    detailedAddress: '',
    latitude: null,
    longitude: null,
    startTime: '',
    endTime: '',
    targetNumber: 10,
    currentNumber: 0,
    serviceHours: 0,
    rewardPoints: 0,
    status: '招募中',
    coverImage: '',
    venueImage: '[]',
    volunteerField: '',
    volunteerTarget: '',
    serviceLocation: '',
    organizationName: '',
    organizationImage: '',
    contactPerson: '',
    contactPhone: ''
  }
  venueImageList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  console.log('[Edit] 开始编辑活动, row:', row)
  console.log('[Edit] 原始venueImage:', row.venueImage)
  
  dialogTitle.value = '修改活动'
  isEdit.value = true
  form.value = {
    ...row,
    startTime: row.startTime ? new Date(row.startTime) : '',
    endTime: row.endTime ? new Date(row.endTime) : ''
  }
  
  // 解析场所图片数组（getVenueImageArray已经过滤了无效数据）
  const venueImages = getVenueImageArray(row.venueImage)
  console.log('[Edit] 解析后的venueImages:', venueImages)
  console.log('[Edit] 有效图片数量:', venueImages.length)
  
  // 检查是否有blob URL
  const blobUrls = venueImages.filter(url => url.startsWith('blob:'))
  if (blobUrls.length > 0) {
    console.warn('[Edit] 警告：数据库中保存了blob URL，这些图片将无法显示！', blobUrls)
    ElMessage.warning('检测到无效图片数据，请重新上传场所图片')
  }
  
  // 转换为el-upload需要的格式
  // 过滤掉blob URL，因为它们已经失效
  venueImageList.value = venueImages
    .filter(url => !url.startsWith('blob:'))
    .map((url, index) => ({
      uid: Date.now() + index,
      url: url,
      status: 'success'
    }))
  
  console.log('[Edit] 最终venueImageList（已过滤blob URL）:', venueImageList.value)
  dialogVisible.value = true
}

const initActivityMap = () => {
  if (!window.AMap) {
    ElMessage.error('地图加载失败')
    return
  }

  setTimeout(() => {
    if (!activityMap.value) {
      const center = form.value.longitude && form.value.latitude
        ? [form.value.longitude, form.value.latitude]
        : [113.264385, 23.12911]

      activityMap.value = new AMap.Map('activity-map-container', {
        zoom: 16,
        center
      })

      AMap.plugin('AMap.Geocoder', () => {
        activityGeocoder.value = new AMap.Geocoder({
          city: '全国'
        })
      })

      AMap.plugin('AMap.PlaceSearch', () => {
        const city = getActivitySearchCity()
        activityPlaceSearch.value = new AMap.PlaceSearch({
          map: activityMap.value,
          panel: 'activity-poi-panel',
          city: city || '全国'
        })
      })

      activityMap.value.on('click', (e) => {
        const { lng, lat } = e.lnglat
        form.value.longitude = lng
        form.value.latitude = lat

        if (activityMarker.value) {
          activityMarker.value.setPosition([lng, lat])
        } else {
          activityMarker.value = new AMap.Marker({
            position: [lng, lat],
            map: activityMap.value
          })
        }
      })
    } else {
      const center = form.value.longitude && form.value.latitude
        ? [form.value.longitude, form.value.latitude]
        : activityMap.value.getCenter()
      activityMap.value.setCenter(center)
      if (form.value.longitude && form.value.latitude) {
        if (activityMarker.value) {
          activityMarker.value.setPosition([form.value.longitude, form.value.latitude])
        } else {
          activityMarker.value = new AMap.Marker({
            position: [form.value.longitude, form.value.latitude],
            map: activityMap.value
          })
        }
      }
    }
  }, 100)
}

const handleActivityMapSearch = () => {
  if (!activityMapSearchKeyword.value) {
    ElMessage.warning('请输入要搜索的地点')
    return
  }
  if (!window.AMap) {
    ElMessage.error('地图未加载，无法搜索位置')
    return
  }

  const city = getActivitySearchCity()

  console.log('[活动地图] 搜索点击, 关键词 =', activityMapSearchKeyword.value, '城市 =', city)

  const ensureGeocoder = (callback) => {
    if (activityGeocoder.value) {
      callback()
      return
    }
    AMap.plugin('AMap.Geocoder', () => {
      activityGeocoder.value = new AMap.Geocoder({
        city: city || '全国'
      })
      callback()
    })
  }

  const ensurePlaceSearch = (centerLngLat) => {
    AMap.plugin('AMap.PlaceSearch', () => {
      if (!activityPlaceSearch.value) {
        activityPlaceSearch.value = new AMap.PlaceSearch({
          map: activityMap.value,
          panel: 'activity-poi-panel',
          city: city || '全国'
        })
      } else if (city && activityPlaceSearch.value.setCity) {
        activityPlaceSearch.value.setCity(city)
      }

      if (centerLngLat) {
        // 以解析出的坐标为中心进行周边搜索
        activityPlaceSearch.value.searchNearBy(
          activityMapSearchKeyword.value,
          centerLngLat,
          2000
        )
      } else {
        activityPlaceSearch.value.search(activityMapSearchKeyword.value)
      }
    })
  }

  ensureGeocoder(() => {
    if (!activityGeocoder.value) return

    if (city && activityGeocoder.value.setCity) {
      activityGeocoder.value.setCity(city)
    }

    activityGeocoder.value.getLocation(activityMapSearchKeyword.value, (status, result) => {
      console.log('[活动地图] Geocoder 返回状态 =', status, '结果 =', result)
      if (status === 'complete' && result && result.geocodes && result.geocodes.length > 0) {
        const loc = result.geocodes[0].location
        if (loc) {
          const lng = loc.lng
          const lat = loc.lat

          // 更新表单坐标
          form.value.longitude = lng
          form.value.latitude = lat

          // 更新地图与标记
          if (activityMap.value) {
            activityMap.value.setZoom(16)
            activityMap.value.setCenter([lng, lat])
            if (activityMarker.value) {
              activityMarker.value.setPosition([lng, lat])
            } else {
              activityMarker.value = new AMap.Marker({
                position: [lng, lat],
                map: activityMap.value
              })
            }
          }

          // 显示附近 POI 列表
          ensurePlaceSearch([lng, lat])
          console.log('[活动地图] Geocoder 定位成功, 已更新地图与标记并开始周边搜索', lng, lat)
          return
        }
      }

      ElMessage.warning('未找到相关地点，请尝试更精确的关键词')
      console.warn('[活动地图] Geocoder 未找到地点, 关键词 =', activityMapSearchKeyword.value)
    })
  })
}

const locateActivityByGPS = () => {
  if (!navigator.geolocation) {
    ElMessage.error('当前浏览器不支持定位')
    return
  }

  console.log('[活动地图] 使用当前位置 按钮点击')

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      let lng = pos.coords.longitude
      let lat = pos.coords.latitude

      const applyPosition = (lng2, lat2) => {
        // 简单判断是否在中国大陆范围内（经度约 73~136，纬度约 3~54）
        const inChina = lng2 >= 73 && lng2 <= 136 && lat2 >= 3 && lat2 <= 54
        if (!inChina) {
          console.warn('[活动地图] 定位结果不在中国大陆范围内，lng =', lng2, 'lat =', lat2)
          ElMessage.warning('当前设备返回的定位不在中国境内，可能是浏览器或网络环境导致的，请使用上方搜索来选择活动位置')
          return
        }

        form.value.longitude = lng2
        form.value.latitude = lat2
        if (activityMap.value) {
          activityMap.value.setZoom(16)
          activityMap.value.setCenter([lng2, lat2])
          if (activityMarker.value) {
            activityMarker.value.setPosition([lng2, lat2])
          } else {
            activityMarker.value = new AMap.Marker({
              position: [lng2, lat2],
              map: activityMap.value
            })
          }
        }

        console.log('[活动地图] GPS 定位完成, 位置 =', lng2, lat2)

        if (window.AMap) {
          const city = getActivitySearchCity()
          const center = [lng2, lat2]
          const keyword = activityMapSearchKeyword.value || ''

          AMap.plugin('AMap.PlaceSearch', () => {
            if (!activityPlaceSearch.value) {
              activityPlaceSearch.value = new AMap.PlaceSearch({
                map: activityMap.value,
                panel: 'activity-poi-panel',
                city: city || '全国'
              })
            } else if (city && activityPlaceSearch.value.setCity) {
              activityPlaceSearch.value.setCity(city)
            }

            console.log('[活动地图] GPS 触发周边搜索, 关键词 =', keyword, '城市 =', city, '中心 =', center)

            // 如果有关键词，就按关键词周边搜；否则用空关键字尝试获取附近POI
            if (keyword) {
              activityPlaceSearch.value.searchNearBy(keyword, center, 2000)
            } else {
              activityPlaceSearch.value.searchNearBy('', center, 2000)
            }
          })
        }
      }

      if (window.AMap && AMap.convertFrom) {
        AMap.convertFrom([lng, lat], 'gps', (status, result) => {
          console.log('[活动地图] 调用 AMap.convertFrom, 状态 =', status, '结果 =', result)
          if (status === 'complete' && result.locations && result.locations.length > 0) {
            const loc = result.locations[0]
            applyPosition(loc.lng, loc.lat)
          } else {
            applyPosition(lng, lat)
          }
        })
      } else {
        applyPosition(lng, lat)
      }
    },
    () => {
      ElMessage.error('获取当前位置失败，请检查浏览器定位权限')
    }
  )
}

// 根据表单中的活动地址推断搜索城市（尽量限定在当前城市/省）
const getActivitySearchCity = () => {
  const addr = `${form.value.address || ''} ${form.value.detailedAddress || ''}`
  if (!addr) return ''

  // 直辖市优先匹配
  const directCities = ['北京市', '上海市', '天津市', '重庆市']
  for (const c of directCities) {
    if (addr.includes(c)) return c
  }

  // 普通地级市：匹配 2-3 个汉字 + “市”
  const cityMatch = addr.match(/([\u4e00-\u9fa5]{2,3}市)/)
  if (cityMatch && cityMatch[1]) {
    return cityMatch[1]
  }

  // 省份：匹配 2-3 个汉字 + “省”
  const provinceMatch = addr.match(/([\u4e00-\u9fa5]{2,3}省)/)
  if (provinceMatch && provinceMatch[1]) {
    return provinceMatch[1]
  }

  return ''
}

const handleSubmit = async () => {
  try {
    // 等待所有图片转换完成
    const imagePromises = venueImageList.value.map(file => {
      if (file.raw && !file.base64) {
        // 如果有原始文件但还没有base64，等待转换完成
        return new Promise((resolve) => {
          const reader = new FileReader()
          reader.onload = (e) => {
            file.base64 = e.target.result
            resolve()
          }
          reader.onerror = () => resolve()
          reader.readAsDataURL(file.raw)
        })
      }
      return Promise.resolve()
    })
    
    await Promise.all(imagePromises)
    
    // 将场所图片列表转换为JSON数组字符串
    const venueImages = venueImageList.value.map(file => {
      // 优先使用base64（新上传的图片）
      if (file.base64) {
        return file.base64
      }
      // 如果有url且不是blob URL（已有的图片）
      if (file.url && !file.url.startsWith('blob:')) {
        return file.url
      }
      return null
    }).filter(url => url !== null && url !== '' && url.trim() !== '')
    
    console.log('[Submit] 提交的场所图片数量:', venueImages.length)
    console.log('[Submit] 场所图片数据（前100字符）:', venueImages.map(img => img.substring(0, 100)))
    
    // 检查是否有blob URL
    const hasBlobUrl = venueImages.some(url => url.startsWith('blob:'))
    if (hasBlobUrl) {
      console.error('[Submit] 警告：检测到blob URL，这将导致刷新后图片无法显示！')
      ElMessage.error('图片处理失败，请重新上传')
      return
    }
    
    const submitData = {
      ...form.value,
      startTime: form.value.startTime ? formatDateTime(form.value.startTime) : null,
      endTime: form.value.endTime ? formatDateTime(form.value.endTime) : null,
      venueImage: JSON.stringify(venueImages)
    }
    
    if (isEdit.value) {
      await updateActivity(submitData)
      ElMessage.success('修改成功')
    } else {
      await createActivity(submitData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadActivities()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该活动吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteActivity(row.id)
      ElMessage.success('删除成功')
      loadActivities()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个活动吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await Promise.all(selectedIds.value.map(id => deleteActivity(id)))
      ElMessage.success('批量删除成功')
      loadActivities()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

const formatDateTime = (date) => {
  if (!date) return null
  if (typeof date === 'string') return date
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

// 格式化时间显示
const formatTimeDisplay = (dateTime) => {
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

const handleCoverImageChange = (file) => {
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.coverImage = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleVenueImageChange = (file, fileList) => {
  if (file.raw && file.raw.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    // 移除超出大小的文件
    const index = fileList.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileList.splice(index, 1)
    }
    return
  }
  
  if (file.raw && !['image/jpeg', 'image/jpg', 'image/png', 'image/gif'].includes(file.raw.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    // 移除不支持的文件
    const index = fileList.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileList.splice(index, 1)
    }
    return
  }
  
  // 更新venueImageList
  venueImageList.value = [...fileList]
  
  // 异步读取图片为base64
  if (file.raw) {
    const reader = new FileReader()
    reader.onload = (e) => {
      file.base64 = e.target.result
    }
    reader.onerror = () => {
      ElMessage.error('图片读取失败')
    }
    reader.readAsDataURL(file.raw)
  }
}

const handleVenueImageRemove = (file) => {
  const index = venueImageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    venueImageList.value.splice(index, 1)
  }
}

const getVenueImageArray = (venueImage) => {
  if (!venueImage || venueImage === '[]') return []
  try {
    const parsed = JSON.parse(venueImage)
    if (Array.isArray(parsed)) {
      // 过滤掉空字符串、null、undefined等无效值
      return parsed.filter(img => {
        return img && 
               typeof img === 'string' && 
               img.trim() !== '' && 
               img !== 'null' && 
               img !== 'undefined'
      })
    }
    return []
  } catch (error) {
    console.error('[getVenueImageArray] JSON解析失败:', error, venueImage)
    // 如果解析失败且venueImage看起来是有效URL，返回单个元素数组
    if (venueImage && typeof venueImage === 'string' && venueImage.trim() !== '') {
      return [venueImage]
    }
    return []
  }
}

const handleViewVenueImages = (row) => {
  currentVenueImages.value = getVenueImageArray(row.venueImage)
  currentImageIndex.value = 0
  showVenueImagesDialog.value = true
}

const handleOrganizationImageChange = (file) => {
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.organizationImage = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 分类名称映射
const getCategoryName = (categoryId) => {
  const categoryMap = {
    1: '社区服务',
    2: '环境保护',
    3: '支教助学',
    4: '景区引导',
    5: '禁毒宣传'
  }
  return categoryMap[categoryId] || '未分类'
}

// 分类标签类型
const getCategoryType = (categoryId) => {
  const typeMap = {
    1: '',
    2: 'success',
    3: 'warning',
    4: 'info',
    5: 'danger'
  }
  return typeMap[categoryId] || ''
}

// 状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    '未开始': 'info',
    '招募中': 'success',
    '进行中': 'warning',
    '已结束': '',
    '已取消': 'danger'
  }
  return typeMap[status] || ''
}

const loadActivities = async () => {
  try {
    const res = await getActivityList()
    activities.value = res.data || []
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  }
}

const handleGenerateCode = async (row) => {
  try {
    const res = await generateCode(row.id)
    const code = res.data.code
    
    await ElMessageBox.alert(
      `活动验证码已生成，请告知志愿者：\n\n验证码：${code}\n\n注意：此验证码用于志愿者现场签到打卡`,
      '验证码生成成功',
      {
        confirmButtonText: '复制验证码',
        type: 'success',
        dangerouslyUseHTMLString: false
      }
    )
    
    // 复制到剪贴板
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(code)
      ElMessage.success('验证码已复制到剪贴板')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('生成验证码失败:', error)
      ElMessage.error('生成验证码失败')
    }
  }
}

onMounted(() => {
  loadActivities()

  if (!window.AMap) {
    const script = document.createElement('script')
    script.src = 'https://webapi.amap.com/maps?v=2.0&key=578d4cc6a789e7e3f7001f3a49d425ee'
    document.head.appendChild(script)
  }
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

:deep(.el-button--primary) {
  background-color: #ce4c4c;
  border-color: #ce4c4c;
}

:deep(.el-button--primary:hover) {
  background-color: #d97373;
  border-color: #d97373;
}

.text-placeholder {
  color: #c0c4cc;
  font-style: italic;
}

:deep(.el-table__header th) {
  background-color: #f5f5f5;
  color: #333;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 178px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #ce4c4c;
}

.avatar {
  width: 178px;
  height: 100px;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.multi-upload-container {
  width: 100%;
}

.multi-uploader {
  width: 100%;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.view-images-link {
  color: #ff4d4f;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.view-images-link:hover {
  color: #ff7875;
  text-decoration: underline;
}

.map-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.map-body {
  display: flex;
  gap: 10px;
}

.map-main {
  flex: 2;
  height: 500px;
}

.map-poi-panel {
  flex: 1;
  height: 500px;
  overflow-y: auto;
  border-left: 1px solid #eee;
  font-size: 12px;
}

.location-summary {
  font-size: 12px;
  color: #606266;
}

.location-summary.empty {
  color: #c0c4cc;
}
</style>