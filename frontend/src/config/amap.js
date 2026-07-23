// 高德地图配置
export const AMAP_CONFIG = {
  // 高德地图API密钥（Web API Key）
  key: '578d4cc6a789e7e3f7001f3a49d425ee',
  
  // Web JSAPI 安全密钥（SCODE）- 用于前端 JS 调用
  securityJsCode: '',
  
  // API版本
  version: '1.4.15',
  
  // 需要加载的插件
  plugins: [
    'AMap.Geocoder',      // 地理编码
    'AMap.PlaceSearch',   // 地点搜索
    'AMap.Autocomplete',  // 输入提示（注意大小写）
    'AMap.Geolocation'    // 定位服务
  ],
  
  // 地图默认配置
  mapOptions: {
    zoom: 11,
    center: [116.397428, 39.90923], // 北京市中心
    mapStyle: 'amap://styles/normal',
    canvas: true,
    willReadFrequently: true  // 优化Canvas性能，避免getImageData警告
  }
}

// 构建高德地图API URL
export const buildAmapUrl = () => {
  const { key, version, plugins } = AMAP_CONFIG
  const pluginStr = plugins.join(',')
  // 开发环境：禁用 SCODE 校验，以便正常使用 PlaceSearch 服务
  // 生产环境应该启用 SCODE 校验以提高安全性
  return `https://webapi.amap.com/maps?v=${version}&key=${key}&plugin=${pluginStr}`
}
