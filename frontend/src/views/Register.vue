<template>
  <div class="register-container">
    <div class="register-card">
      <div class="title-header">
        <img src="/images/logo.jpg" alt="Logo" class="logo" />
        <h2>志愿者注册</h2>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="register-form">
        <el-form-item label="头像" prop="avatar" class="full-row-item">
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
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
          >
            <template #suffix>
              <el-icon @click="showPassword = !showPassword" style="cursor: pointer">
                <View v-if="!showPassword" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
          >
            <template #suffix>
              <el-icon @click="showConfirmPassword = !showConfirmPassword" style="cursor: pointer">
                <View v-if="!showConfirmPassword" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender" class="half-row-item">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="住址" class="half-row-item">
          <el-button @click="showMapDialog = true" size="small">在地图上选择位置</el-button>
          <span v-if="form.latitude && form.longitude" style="margin-left: 10px; color: #67c23a; font-size: 12px">
            已选择位置
          </span>
          <div v-if="form.detailedAddress" class="address-preview">
            当前地址：{{ form.detailedAddress }}
          </div>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard" class="full-row-item long-field">
          <el-input v-model="form.idCard" placeholder="请输入18位身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" class="full-row-item long-field">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email" class="full-row-item long-field">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="钱包地址" prop="walletAddress" class="full-row-item long-field">
          <el-input 
            v-model="form.walletAddress" 
            placeholder="可选：输入您的区块链钱包地址（以0x开头）"
          >
            <template #prepend>
              <el-icon><Wallet /></el-icon>
            </template>
          </el-input>
          <div style="font-size: 12px; color: rgba(206, 76, 76, 0.8); margin-top: 5px;">
            提示：绑定钱包地址后，您的志愿服务记录将自动上链存证
          </div>
        </el-form-item>
        <el-form-item class="full-row-item">
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog v-model="showMapDialog" title="选择位置" width="880px" @open="initMap">
      <div class="map-toolbar">
        <el-input
          v-model="mapSearchKeyword"
          placeholder="输入地点关键字搜索"
          size="small"
          style="width: 260px;"
          @keyup.enter="handleMapSearch"
        />
        <el-button size="small" @click="handleMapSearch">搜索</el-button>
        <el-button size="small" @click="locateByGPS">使用当前位置</el-button>
      </div>
      <div class="map-content">
        <div id="map-container" class="map-main"></div>
        <div class="poi-panel">
          <div class="poi-header">搜索结果</div>
          <div v-if="poiList.length" class="poi-list">
            <div
              v-for="(poi, index) in poiList"
              :key="index"
              class="poi-item"
              @click="handlePoiClick(poi)"
            >
              <div class="poi-name">{{ poi.name || poi.address }}</div>
              <div class="poi-address">{{ poi.address }}</div>
            </div>
          </div>
          <div v-else class="poi-empty">输入关键字搜索后，这里会显示附近地点列表</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showMapDialog = false">取消</el-button>
        <el-button type="primary" @click="showMapDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { volunteerRegister } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const showMapDialog = ref(false)
const map = ref(null)
const marker = ref(null)
const mapSearchKeyword = ref('')
const geoCoder = ref(null)
const poiList = ref([])

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  gender: '男',
  idCard: '',
  phone: '',
  email: '',
  avatar: '',
  walletAddress: '',
  province: '',
  city: '',
  district: '',
  detailedAddress: '',
  latitude: null,
  longitude: null
})

const regionOptions = ref([
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '浦东新区', label: '浦东新区' },
          { value: '静安区', label: '静安区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' }
        ]
      }
    ]
  }
])

const validatePass = (rule, value, callback) => {
  if (value !== form.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
    return
  }
  const idCardRegex = /^(^\d{15}$)|(^(\d{17})([0-9Xx])$)$/
  if (!idCardRegex.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const validateWalletAddress = (rule, value, callback) => {
  if (!value) {
    // 钱包地址是可选的
    callback()
    return
  }
  
  // 验证以太坊钱包地址格式：0x开头，42位字符
  const walletRegex = /^0x[a-fA-F0-9]{40}$/
  if (!walletRegex.test(value)) {
    callback(new Error('钱包地址格式不正确，应为0x开头的42位十六进制字符'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  walletAddress: [{ validator: validateWalletAddress, trigger: 'blur' }]
}

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
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const updateAddressByLngLat = (lng, lat) => {
  if (!window.AMap) return

  if (!geoCoder.value) {
    AMap.plugin('AMap.Geocoder', () => {
      geoCoder.value = new AMap.Geocoder({ city: '全国' })
      updateAddressByLngLat(lng, lat)
    })
    return
  }

  geoCoder.value.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result && result.regeocode) {
      const comp = result.regeocode.addressComponent || {}
      form.value.province = comp.province || ''
      form.value.city = comp.city || comp.province || ''
      form.value.district = comp.district || ''
      form.value.detailedAddress = result.regeocode.formattedAddress || ''
    }
  })
}

const initMap = () => {
  if (!window.AMap) {
    ElMessage.error('地图加载失败')
    return
  }
  
  setTimeout(() => {
    if (!map.value) {
      map.value = new AMap.Map('map-container', {
        zoom: 16,
        center: [116.397428, 39.90923]
      })

      AMap.plugin('AMap.Geocoder', () => {
        geoCoder.value = new AMap.Geocoder({
          city: '全国'
        })
      })

      map.value.on('click', (e) => {
        const { lng, lat } = e.lnglat
        form.value.longitude = lng
        form.value.latitude = lat
        updateAddressByLngLat(lng, lat)
        
        if (marker.value) {
          marker.value.setPosition([lng, lat])
        } else {
          marker.value = new AMap.Marker({
            position: [lng, lat],
            map: map.value
          })
        }
      })
    }
  }, 100)
}

const handleMapSearch = () => {
  if (!mapSearchKeyword.value) {
    ElMessage.warning('请输入要搜索的地点')
    return
  }
  if (!geoCoder.value) {
    AMap.plugin('AMap.Geocoder', () => {
      geoCoder.value = new AMap.Geocoder({ city: '全国' })
      handleMapSearch()
    })
    return
  }

  // 根据当前已解析的城市信息限制城市范围
  const city = form.value.city || form.value.province || '全国'
  if (geoCoder.value.setCity && city) {
    geoCoder.value.setCity(city)
  }

  geoCoder.value.getLocation(mapSearchKeyword.value, (status, result) => {
    if (status === 'complete' && result && result.geocodes && result.geocodes.length > 0) {
      const geocodes = result.geocodes
      poiList.value = geocodes.map(item => ({
        name: item.formattedAddress,
        address: item.formattedAddress,
        location: item.location
      }))

      const loc = geocodes[0].location
      if (loc) {
        const lng = loc.lng
        const lat = loc.lat
        form.value.longitude = lng
        form.value.latitude = lat
        if (marker.value) {
          marker.value.setPosition([lng, lat])
        } else if (map.value) {
          marker.value = new AMap.Marker({
            position: [lng, lat],
            map: map.value
          })
        }
        if (map.value) {
          map.value.setZoom(16)
          map.value.setCenter([lng, lat])
        }
        updateAddressByLngLat(lng, lat)
      }
    } else {
      ElMessage.warning('未找到相关地点，请尝试更精确的关键词')
      poiList.value = []
    }
  })
}

const handlePoiClick = (poi) => {
  if (!poi || !poi.location) return
  const lng = poi.location.lng
  const lat = poi.location.lat
  form.value.longitude = lng
  form.value.latitude = lat
  updateAddressByLngLat(lng, lat)

  if (marker.value) {
    marker.value.setPosition([lng, lat])
  } else if (map.value) {
    marker.value = new AMap.Marker({
      position: [lng, lat],
      map: map.value
    })
  }
  if (map.value) {
    map.value.setZoom(16)
    map.value.setCenter([lng, lat])
  }
}

const locateByGPS = () => {
  if (!navigator.geolocation) {
    ElMessage.error('当前浏览器不支持定位')
    return
  }

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      let lng = pos.coords.longitude
      let lat = pos.coords.latitude

      const applyPosition = (lng2, lat2) => {
        form.value.longitude = lng2
        form.value.latitude = lat2
        updateAddressByLngLat(lng2, lat2)
        if (map.value) {
          map.value.setZoom(16)
          map.value.setCenter([lng2, lat2])
          if (marker.value) {
            marker.value.setPosition([lng2, lat2])
          } else {
            marker.value = new AMap.Marker({
              position: [lng2, lat2],
              map: map.value
            })
          }
        }
      }

      if (window.AMap && AMap.convertFrom) {
        AMap.convertFrom([lng, lat], 'gps', (status, result) => {
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

const handleRegister = async () => {
  await formRef.value.validate()
  
  if (!form.value.latitude || !form.value.longitude) {
    ElMessage.warning('请在地图上选择您的位置')
    return
  }
  
  try {
    const registerData = {
      ...form.value
    }
    delete registerData.confirmPassword
    
    await volunteerRegister(registerData)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  const script = document.createElement('script')
  script.src = 'https://webapi.amap.com/maps?v=2.0&key=578d4cc6a789e7e3f7001f3a49d425ee'
  document.head.appendChild(script)
})
</script>

<style scoped>
.map-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('/images/background5.jpg') no-repeat center center;
  background-size: cover;
  padding: 20px 0;
}

.register-card {
  width: 720px;
  padding: 32px 48px 36px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  max-height: none;
  overflow: visible;
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

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed rgba(206, 76, 76, 0.4);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.1);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: rgba(206, 76, 76, 0.6);
  background: rgba(255, 255, 255, 0.15);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: rgba(206, 76, 76, 0.8);
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

.register-card :deep(.el-form-item__label) {
  color: #ce4c4c;
}

.register-card :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(206, 76, 76, 0.3);
  box-shadow: none;
}

.register-card :deep(.el-input__wrapper:hover),
.register-card :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(206, 76, 76, 0.5);
}

.register-card :deep(.el-input__inner) {
  color: #ce4c4c;
}

.register-card :deep(.el-input__inner::placeholder) {
  color: rgba(206, 76, 76, 0.6);
}

.register-card :deep(.el-input__suffix) {
  color: rgba(206, 76, 76, 0.8);
}

.register-card :deep(.el-radio__label) {
  color: #ce4c4c;
}

.register-card :deep(.el-radio__inner) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(206, 76, 76, 0.4);
}

.register-card :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #ce4c4c;
  border-color: #ce4c4c;
}

.register-card :deep(.el-button--primary) {
  background: rgba(206, 76, 76, 0.7);
  border: none;
  backdrop-filter: blur(5px);
  color: #fff;
}

.register-card :deep(.el-button--primary:hover) {
  background: rgba(206, 76, 76, 0.9);
}

.register-card :deep(.el-button--default) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(206, 76, 76, 0.3);
  color: #ce4c4c;
}

.register-card :deep(.el-button--default:hover) {
  background: rgba(255, 255, 255, 0.3);
}

.register-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 20px;
}

.register-form :deep(.el-form-item) {
  margin-right: 0;
}

.register-form :deep(.full-row-item) {
  grid-column: 1 / -1;
}

.register-form :deep(.half-row-item) {
  grid-column: span 1;
}

.register-form :deep(.long-field .el-form-item__content) {
  max-width: 460px;
}

.address-preview {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.6);
}

.map-content {
  display: flex;
  gap: 12px;
}

.map-main {
  flex: 2;
  height: 500px;
}

.poi-panel {
  flex: 1;
  max-height: 500px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  padding: 8px 10px;
  overflow-y: auto;
}

.poi-header {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.poi-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.poi-item {
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.poi-item:hover {
  background-color: rgba(206, 76, 76, 0.08);
}

.poi-name {
  font-size: 13px;
  color: #333;
}

.poi-address {
  font-size: 12px;
  color: #666;
}

.poi-empty {
  font-size: 12px;
  color: #999;
}
</style>