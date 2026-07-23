// 地图工具函数
import { ElMessage } from 'element-plus'
import { AMAP_CONFIG } from '@/config/amap'

/**
 * 检查高德地图API是否已加载
 */
export const checkAmapLoaded = () => {
  return typeof AMap !== 'undefined'
}

/**
 * 检查插件是否可用
 */
export const checkPlugin = (pluginName) => {
  if (!checkAmapLoaded()) return false
  return typeof AMap[pluginName] !== 'undefined'
}

/**
 * 安全创建地图实例
 */
export const createMapSafely = (containerId, options = {}) => {
  if (!checkAmapLoaded()) {
    throw new Error('高德地图API未加载')
  }
  
  const mapOptions = { ...AMAP_CONFIG.mapOptions, ...options }
  return new AMap.Map(containerId, mapOptions)
}

/**
 * 安全创建地理编码实例
 */
export const createGeocoderSafely = (options = {}) => {
  if (!checkPlugin('Geocoder')) {
    console.warn('Geocoder插件未加载')
    return null
  }
  
  const defaultOptions = {
    radius: 1000,
    extensions: 'all'
  }
  
  return new AMap.Geocoder({ ...defaultOptions, ...options })
}

/**
 * 安全创建地点搜索实例
 */
export const createPlaceSearchSafely = (map, options = {}) => {
  if (!checkPlugin('PlaceSearch')) {
    console.warn('PlaceSearch插件未加载')
    return null
  }
  
  const defaultOptions = {
    pageSize: 10,
    pageIndex: 1,
    city: '全国',
    citylimit: false,
    map: map,
    panel: false,
    extensions: 'all'  // 返回详细信息，包括地址组件等
  }
  
  return new AMap.PlaceSearch({ ...defaultOptions, ...options })
}

/**
 * 安全创建自动完成实例
 */
export const createAutocompleteSafely = (options = {}) => {
  if (!checkPlugin('Autocomplete')) {
    console.warn('Autocomplete插件未加载')
    return null
  }
  
  const defaultOptions = {
    city: '全国',
    citylimit: false
  }
  
  return new AMap.Autocomplete({ ...defaultOptions, ...options })
}

/**
 * 创建地图标记
 */
export const createMarker = (position, map, options = {}) => {
  if (!checkAmapLoaded()) return null
  
  const defaultOptions = {
    position: position,
    map: map
  }
  
  return new AMap.Marker({ ...defaultOptions, ...options })
}

/**
 * 地理编码（地址转坐标）
 */
export const geocodeAddress = (geocoder, address) => {
  return new Promise((resolve, reject) => {
    if (!geocoder) {
      reject(new Error('Geocoder未初始化'))
      return
    }
    
    geocoder.getLocation(address, (status, result) => {
      if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
        resolve(result.geocodes[0])
      } else {
        reject(new Error('地址解析失败'))
      }
    })
  })
}

/**
 * 逆地理编码（坐标转地址）
 */
export const reverseGeocode = (geocoder, lnglat) => {
  return new Promise((resolve, reject) => {
    if (!geocoder) {
      reject(new Error('Geocoder未初始化'))
      return
    }
    
    geocoder.getAddress(lnglat, (status, result) => {
      if (status === 'complete' && result.regeocode) {
        resolve(result.regeocode)
      } else {
        reject(new Error('位置解析失败'))
      }
    })
  })
}

/**
 * 地点搜索
 */
export const searchPlace = (placeSearch, keyword) => {
  return new Promise((resolve, reject) => {
    if (!placeSearch) {
      reject(new Error('PlaceSearch未初始化'))
      return
    }
    
    placeSearch.search(keyword, (status, result) => {
      // 详细调试日志
      console.log('PlaceSearch 搜索结果:', {
        status,
        keyword,
        resultType: typeof result,
        resultKeys: result && typeof result === 'object' ? Object.keys(result) : null,
        poiListLength: result?.poiList?.pois?.length || 0,
        fullResult: result
      })
      
      if (status === 'complete' && result && typeof result === 'object' && result.poiList && result.poiList.pois && result.poiList.pois.length > 0) {
        resolve(result.poiList.pois)
      } else if (status === 'error') {
        // result 可能是字符串（错误信息）或对象
        let errorInfo = '未知错误'
        if (typeof result === 'string') {
          errorInfo = result
        } else if (result && result.info) {
          errorInfo = result.info
        }
        console.error('PlaceSearch 错误详情:', errorInfo, '完整结果:', result)
        reject(new Error(`地点搜索服务出错 (${errorInfo})，请检查网络连接或稍后重试`))
      } else if (status === 'no_data') {
        reject(new Error('未找到相关地点，请尝试其他关键词'))
      } else {
        reject(new Error(`搜索失败 (状态: ${status})，请尝试其他关键词`))
      }
    })
  })
}

/**
 * 获取搜索建议
 */
export const getSearchSuggestions = (autocomplete, keyword) => {
  return new Promise((resolve, reject) => {
    if (!autocomplete) {
      resolve([]) // 如果没有autocomplete，返回空数组而不是错误
      return
    }
    
    autocomplete.search(keyword, (status, result) => {
      if (status === 'complete' && result.tips) {
        resolve(result.tips)
      } else {
        resolve([])
      }
    })
  })
}

/**
 * 显示错误信息
 */
export const showMapError = (message) => {
  ElMessage.error(message)
  console.error('地图错误:', message)
}
