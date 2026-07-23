<template>
  <div class="data-screen">
    <!-- 顶部数字看板 KPI -->
    <div class="kpi-section">
      <div class="kpi-card" v-for="(item, index) in kpiData" :key="index">
        <div class="kpi-icon" :style="{ background: `linear-gradient(135deg, ${item.gradient[0]} 0%, ${item.gradient[1]} 100%)` }">
          <el-icon :size="36"><component :is="item.icon" /></el-icon>
        </div>
        <div class="kpi-content">
          <div class="kpi-value">
            <span class="kpi-number">{{ item.value }}</span>
            <span class="kpi-unit">{{ item.unit }}</span>
          </div>
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-trend" :class="item.trend > 0 ? 'up' : 'down'">
            <el-icon><CaretTop v-if="item.trend > 0" /><CaretBottom v-else /></el-icon>
            {{ Math.abs(item.trend) }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容区 - 数据大屏式布局 -->
    <!-- 上部分：围绕地图的三栏布局 -->
    <div class="main-content">
      <!-- 左侧数据统计区 -->
      <div class="left-panel">
        <div class="chart-card bar-card">
          <div class="chart-header">
            <el-icon><Histogram /></el-icon>
            <span>活动参与人数</span>
          </div>
          <div class="chart-container" ref="participantsChart"></div>
        </div>

        <div class="chart-card line-card">
          <div class="chart-header">
            <el-icon><TrendCharts /></el-icon>
            <span>志愿时长趋势</span>
          </div>
          <div class="chart-container" ref="hoursTrendChart"></div>
        </div>
      </div>

      <!-- 中间地图热力图区 -->
      <div class="center-panel">
        <div class="chart-card map-card">
          <div class="chart-header">
            <el-icon><Location /></el-icon>
            <span>志愿者地域分布热力图</span>
          </div>
          <div class="chart-container map-large" ref="mapChart"></div>
        </div>
      </div>

      <!-- 右侧数据统计区 -->
      <div class="right-panel">
        <div class="chart-card line-card">
          <div class="chart-header">
            <el-icon><DataLine /></el-icon>
            <span>补录时长趋势</span>
          </div>
          <div class="chart-container" ref="replenishChart"></div>
        </div>

        <div class="chart-card bar-card">
          <div class="chart-header">
            <el-icon><Trophy /></el-icon>
            <span>积分排行榜</span>
          </div>
          <div class="chart-container" ref="rankingChart"></div>
        </div>
      </div>
    </div>

    <!-- 下部分：活动类型占比和论坛帖子分布等宽分布 -->
    <div class="bottom-charts">
      <div class="chart-card pie-card">
        <div class="chart-header">
          <el-icon><PieChart /></el-icon>
          <span>活动类型占比</span>
        </div>
        <div class="chart-container" ref="activityPieChart"></div>
      </div>

      <div class="chart-card pie-card">
        <div class="chart-header">
          <el-icon><Message /></el-icon>
          <span>论坛帖子分布</span>
        </div>
        <div class="chart-container" ref="forumPieChart"></div>
      </div>
    </div>

    <!-- 底部实时事件滚动列表 -->
    <div class="timeline-section">
      <div class="timeline-header">
        <el-icon><Link /></el-icon>
        <span>实时链上事件</span>
      </div>
      <div class="timeline-list" ref="timelineList">
        <div class="timeline-item" v-for="(event, index) in recentEvents" :key="index">
          <div class="event-icon-wrapper" :style="{ background: getEventColor(event.type) }">
            <el-icon :size="20"><component :is="getEventIcon(event.type)" /></el-icon>
          </div>
          <div class="timeline-content">
            <div class="event-header">
              <span class="event-activity">{{ event.activityName }}</span>
              <span class="timeline-time">{{ formatTime(event.time) }}</span>
            </div>
            <div class="event-body">
              <span class="event-user">{{ event.userName }}</span>
              <span class="timeline-text">{{ event.text }}</span>
            </div>
            <div class="event-footer">
              <el-icon :size="12"><Link /></el-icon>
              <span class="timeline-hash">{{ event.hash }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import {
  TrendCharts, Histogram, DataLine, PieChart,
  Trophy, Clock, CaretTop, CaretBottom, Message, Location,
  Tickets, Timer, DocumentChecked, Edit, Link, Calendar, Star, Document
} from '@element-plus/icons-vue'
import axios from 'axios'
import {
  getKpiData,
  getActivityParticipants,
  getHoursTrend,
  getReplenishTrend,
  getActivityDistribution,
  getPointsRanking,
  getForumDistribution,
  getVolunteerLocation,
  getRecentEvents,
  getAllStats
} from '@/api/adminStats'

// KPI 数据
const kpiData = ref([
  { label: '累计志愿时长', value: 0, unit: 'h', icon: 'Timer', trend: 0, gradient: ['#667eea', '#764ba2'] },
  { label: '累计志愿活动', value: 0, unit: '个', icon: 'Calendar', trend: 0, gradient: ['#4facfe', '#00f2fe'] },
  { label: '累计补录时长', value: 0, unit: 'h', icon: 'Edit', trend: 0, gradient: ['#f093fb', '#f5576c'] },
  { label: '累计积分总额', value: 0, unit: '分', icon: 'Star', trend: 0, gradient: ['#feca57', '#ff9068'] },
  { label: '已发放证书', value: 0, unit: '份', icon: 'Document', trend: 0, gradient: ['#11998e', '#38ef7d'] }
])

// 图表实例
let charts = {
  hoursTrend: null,
  participants: null,
  replenish: null,
  activityPie: null,
  ranking: null,
  forumPie: null,
  map: null
}

// 图表引用
const hoursTrendChart = ref(null)
const participantsChart = ref(null)
const replenishChart = ref(null)
const activityPieChart = ref(null)
const rankingChart = ref(null)
const forumPieChart = ref(null)
const mapChart = ref(null)

// 实时事件数据
const recentEvents = ref([])

// 加载KPI数据
const loadKpiData = async () => {
  try {
    console.log('[Dashboard] 开始加载KPI数据...')
    const response = await getKpiData()
    console.log('[Dashboard] KPI响应:', response)
    if (response.code === 200) {
      const data = response.data
      console.log('[Dashboard] KPI数据:', data)
      kpiData.value[0].value = typeof data.totalHours === 'number' ? data.totalHours.toFixed(2) : '0.00'
      kpiData.value[0].trend = data.hoursTrend || 0
      kpiData.value[1].value = data.totalActivities || 0
      kpiData.value[1].trend = data.activitiesTrend || 0
      kpiData.value[2].value = typeof data.totalReplenishHours === 'number' ? data.totalReplenishHours.toFixed(2) : '0.00'
      kpiData.value[2].trend = data.replenishTrend || 0
      kpiData.value[3].value = data.totalPoints || 0
      kpiData.value[3].trend = data.pointsTrend || 0
      kpiData.value[4].value = data.totalCertificates || 0
      kpiData.value[4].trend = data.certificatesTrend || 0
      console.log('[Dashboard] KPI数据加载完成:', kpiData.value)
    } else {
      console.error('[Dashboard] KPI响应码错误:', response.code, response.message)
    }
  } catch (error) {
    console.error('加载KPI数据失败:', error)
  }
}

// 加载最近事件
const loadRecentEvents = async () => {
  try {
    const response = await getRecentEvents(10)
    if (response.code === 200 && response.data.events) {
      recentEvents.value = response.data.events.map(event => ({
        type: event.eventType,
        activityName: event.activityName,
        userName: event.volunteerName,
        time: new Date(event.createTime),
        text: event.description,
        hash: event.blockchainHash || '0x0000...0000'
      }))
    }
  } catch (error) {
    console.error('加载最近事件失败:', error)
  }
}

// 初始化图表
const initCharts = async () => {
  // 志愿时长趋势折线图
  if (hoursTrendChart.value) {
    charts.hoursTrend = echarts.init(hoursTrendChart.value)
    
    // 加载真实数据
    try {
      const response = await getHoursTrend()
      if (response.code === 200) {
        const data = response.data
        charts.hoursTrend.setOption({
          grid: { left: '12%', right: '8%', top: '15%', bottom: '15%' },
          xAxis: {
            type: 'category',
            data: data.months || ['1月', '2月', '3月', '4月', '5月', '6月'],
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', fontSize: 12 }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', fontSize: 12 },
            splitLine: { lineStyle: { color: '#e8e8e8' } }
          },
          series: [{
            data: data.hours || [0, 0, 0, 0, 0, 0],
            type: 'line',
            smooth: true,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(24, 144, 255, 0.4)' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
              ])
            },
            lineStyle: { color: '#1890ff', width: 3 },
            itemStyle: { color: '#1890ff', borderWidth: 2, borderColor: '#fff' },
            symbol: 'circle',
            symbolSize: 8
          }]
        })
      }
    } catch (error) {
      console.error('加载志愿时长趋势失败:', error)
    }
  }

  // 活动参与人数柱状图
  if (participantsChart.value) {
    charts.participants = echarts.init(participantsChart.value)
    
    // 加载真实数据
    try {
      const response = await getActivityParticipants()
      if (response.code === 200) {
        const data = response.data
        const colors = [
          ['#ff6b6b', '#ff8e8e'],
          ['#4ecb73', '#7fd89f'],
          ['#1890ff', '#5ab3ff'],
          ['#faad14', '#ffc658'],
          ['#9254de', '#b37feb']
        ]
        
        const chartData = (data.counts || []).map((value, index) => ({
          value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: colors[index % colors.length][0] },
              { offset: 1, color: colors[index % colors.length][1] }
            ])
          }
        }))
        
        charts.participants.setOption({
          grid: { left: '18%', right: '8%', top: '15%', bottom: '20%' },
          xAxis: {
            type: 'category',
            data: data.categories || [],
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', rotate: 25, fontSize: 11 }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', fontSize: 12 },
            splitLine: { lineStyle: { color: '#e8e8e8' } }
          },
          series: [{
            data: chartData,
            type: 'bar',
            barWidth: '50%',
            itemStyle: {
              borderRadius: [8, 8, 0, 0]
            },
            label: {
              show: true,
              position: 'top',
              color: '#666',
              fontSize: 12
            }
          }]
        })
      }
    } catch (error) {
      console.error('加载活动参与人数失败:', error)
    }
  }

  // 补录时长趋势折线图
  if (replenishChart.value) {
    charts.replenish = echarts.init(replenishChart.value)
    
    // 加载真实数据
    try {
      const response = await getReplenishTrend()
      if (response.code === 200) {
        const data = response.data
        charts.replenish.setOption({
          grid: { left: '12%', right: '8%', top: '15%', bottom: '15%' },
          xAxis: {
            type: 'category',
            data: data.months || ['1月', '2月', '3月', '4月', '5月', '6月'],
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', fontSize: 12 }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', fontSize: 12 },
            splitLine: { lineStyle: { color: '#e8e8e8' } }
          },
          series: [{
            data: data.hours || [0, 0, 0, 0, 0, 0],
            type: 'line',
            smooth: true,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(250, 173, 20, 0.3)' },
                { offset: 1, color: 'rgba(250, 173, 20, 0.05)' }
              ])
            },
            lineStyle: { color: '#faad14', width: 3 },
            itemStyle: { color: '#faad14', borderWidth: 2, borderColor: '#fff' },
            symbol: 'circle',
            symbolSize: 8
          }]
        })
      }
    } catch (error) {
      console.error('加载补录时长趋势失败:', error)
    }
  }

  // 活动类型占比饼图
  if (activityPieChart.value) {
    charts.activityPie = echarts.init(activityPieChart.value)
    
    // 加载真实数据
    try {
      const response = await getActivityDistribution()
      if (response.code === 200) {
        const data = response.data
        const colors = [
          ['#ff6b6b', '#ff9999'],
          ['#4ecb73', '#7fd89f'],
          ['#1890ff', '#5ab3ff'],
          ['#faad14', '#ffc658'],
          ['#9254de', '#b37feb']
        ]
        
        const chartData = (data.data || []).map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: colors[index % colors.length][0] },
              { offset: 1, color: colors[index % colors.length][1] }
            ])
          }
        }))
        
        charts.activityPie.setOption({
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}个活动 ({d}%)'
          },
          legend: {
            bottom: '0%',
            left: 'center',
            itemGap: 12,
            textStyle: { color: '#666', fontSize: 10 }
          },
          series: [{
            type: 'pie',
            radius: ['40%', '60%'],
            center: ['50%', '45%'],
            label: {
              show: true,
              formatter: '{b}\n{d}%',
              color: '#333',
              fontSize: 11,
              distanceToLabelLine: 8
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10,
              smooth: true
            },
            emphasis: {
              label: { show: true, fontSize: 11, fontWeight: 'bold' },
              itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.3)' }
            },
            data: chartData
          }]
        })
      }
    } catch (error) {
      console.error('加载活动类型占比失败:', error)
    }
  }

  // 积分排行榜
  if (rankingChart.value) {
    charts.ranking = echarts.init(rankingChart.value)
    
    // 加载真实数据
    try {
      const response = await getPointsRanking()
      if (response.code === 200) {
        const data = response.data
        const colors = [
          ['#ff6b6b', '#ff9999'],
          ['#faad14', '#ffc658'],
          ['#1890ff', '#69b1ff'],
          ['#4ecb73', '#7fd89f'],
          ['#9254de', '#b37feb']
        ]
        
        const chartData = (data.points || []).map((value, index) => ({
          value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: colors[index % colors.length][0] },
              { offset: 1, color: colors[index % colors.length][1] }
            ])
          }
        }))
        
        charts.ranking.setOption({
          grid: { left: '22%', right: '15%', top: '10%', bottom: '10%' },
          xAxis: {
            type: 'value',
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: '#666', fontSize: 11 },
            splitLine: { lineStyle: { color: '#e8e8e8' } }
          },
          yAxis: {
            type: 'category',
            data: data.names || [],
            axisLine: { lineStyle: { color: '#999' } },
            axisLabel: { color: '#666', fontSize: 11 },
            axisTick: { show: false },
            inverse: true
          },
          series: [{
            type: 'bar',
            data: chartData,
            barWidth: '60%',
            itemStyle: {
              borderRadius: [0, 20, 20, 0]
            },
            label: {
              show: true,
              position: 'right',
              formatter: '{c}分',
              color: '#666',
              fontSize: 11
            }
          }]
        })
      }
    } catch (error) {
      console.error('加载积分排行榜失败:', error)
    }
  }

  // 论坛帖子分布饼图
  if (forumPieChart.value) {
    charts.forumPie = echarts.init(forumPieChart.value)
    
    // 加载真实数据
    try {
      const response = await getForumDistribution()
      if (response.code === 200) {
        const data = response.data
        const colors = [
          ['#722ed1', '#9254de'],
          ['#13c2c2', '#5cdbd3'],
          ['#eb2f96', '#f759ab'],
          ['#faad14', '#ffc53d']
        ]
        
        const chartData = (data.data || []).map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: colors[index % colors.length][0] },
              { offset: 1, color: colors[index % colors.length][1] }
            ])
          }
        }))
        
        charts.forumPie.setOption({
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}篇 ({d}%)'
          },
          legend: {
            bottom: '0%',
            left: 'center',
            itemGap: 12,
            textStyle: { color: '#666', fontSize: 10 }
          },
          series: [{
            type: 'pie',
            radius: ['40%', '60%'],
            center: ['50%', '45%'],
            label: {
              show: true,
              formatter: '{b}\n{d}%',
              color: '#333',
              fontSize: 11,
              distanceToLabelLine: 8
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10,
              smooth: true
            },
            emphasis: {
              label: { show: true, fontSize: 11, fontWeight: 'bold' },
              itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.3)' }
            },
            data: chartData
          }]
        })
      }
    } catch (error) {
      console.error('加载论坛帖子分布失败:', error)
    }
  }

  // 地图热力图
  if (mapChart.value) {
    charts.map = echarts.init(mapChart.value)
    
    // 异步加载中国地图并初始化
    loadChinaMapAndData()
  }
}

// 加载中国地图JSON并初始化
const loadChinaMapAndData = async () => {
  try {
    // 使用在线CDN加载中国地图JSON
    const response = await axios.get('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    echarts.registerMap('china', response.data)
    
    // 设置地图基础配置
    charts.map.setOption({
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          if (params.componentSubType === 'scatter') {
            return `${params.name}<br/>志愿者人数: ${params.value[2]}人`
          }
          return params.name
        }
      },
      geo: {
        map: 'china',
        roam: false,
        label: {
          show: false
        },
        itemStyle: {
          areaColor: '#ffffff',
          borderColor: '#d9d9d9',
          borderWidth: 0.8
        },
        emphasis: {
          itemStyle: {
            areaColor: '#f5f5f5'
          },
          label: {
            show: true,
            color: '#333'
          }
        }
      },
      visualMap: {
        min: 0,
        max: 100,
        left: 'left',
        top: 'bottom',
        text: ['高', '低'],
        calculable: true,
        inRange: {
          color: ['#ffe0e0', '#ffb3b3', '#ff6b6b', '#d94848']
        },
        textStyle: {
          color: '#666'
        }
      },
      series: []
    })
    
    // 加载志愿者数据
    await loadVolunteerLocationData()
  } catch (error) {
    console.error('加载中国地图失败:', error)
    if (charts.map) {
      charts.map.showLoading('default', {
        text: '地图加载失败',
        color: '#1890ff',
        textColor: '#666',
        maskColor: 'rgba(255, 255, 255, 0.8)'
      })
    }
  }
}

// 加载志愿者地域分布数据
const loadVolunteerLocationData = async () => {
  try {
    const response = await axios.get('/api/volunteer/location-stats')
    const locationData = response.data.data || []
    
    // 省份名称到坐标的映射
    const provinceCoordMap = {
      '北京': [116.4074, 39.9042],
      '天津': [117.2010, 39.0842],
      '上海': [121.4737, 31.2304],
      '重庆': [106.5516, 29.5630],
      '河北': [114.5024, 38.0453],
      '山西': [112.5625, 37.8730],
      '河南': [113.6254, 34.7466],
      '辽宁': [123.4328, 41.8045],
      '吉林': [125.3245, 43.8868],
      '黑龙江': [126.6433, 45.7567],
      '内蒙古': [111.6708, 40.8183],
      '江苏': [118.7969, 32.0603],
      '山东': [117.1205, 36.6519],
      '安徽': [117.2830, 31.8612],
      '浙江': [120.1551, 30.2741],
      '福建': [119.2965, 26.1004],
      '湖北': [114.3055, 30.5931],
      '湖南': [112.9388, 28.2282],
      '广东': [113.2644, 23.1291],
      '广西': [108.3661, 22.8172],
      '江西': [115.8921, 28.6765],
      '四川': [104.0668, 30.5728],
      '海南': [110.3312, 20.0311],
      '贵州': [106.7073, 26.5982],
      '云南': [102.7103, 25.0406],
      '西藏': [91.1320, 29.6600],
      '陕西': [108.9398, 34.3416],
      '甘肃': [103.8236, 36.0581],
      '青海': [101.7782, 36.6171],
      '宁夏': [106.2586, 38.4681],
      '新疆': [87.6278, 43.7928]
    }
    
    // 处理地址数据，提取省份并统计
    const provinceStats = {}
    locationData.forEach(item => {
      if (item.address) {
        // 提取省份（假设地址格式为：省份+城市+详细地址）
        let province = ''
        for (const prov in provinceCoordMap) {
          if (item.address.includes(prov)) {
            province = prov
            break
          }
        }
        if (province) {
          provinceStats[province] = (provinceStats[province] || 0) + 1
        }
      }
    })
    
    // 转换为数组格式
    const volunteerData = Object.keys(provinceStats).map(province => ({
      name: province,
      value: provinceStats[province]
    }))
    
    // 如果没有数据，使用模拟数据
    if (volunteerData.length === 0) {
      volunteerData.push(
        { name: '北京', value: 15 },
        { name: '上海', value: 12 },
        { name: '广东', value: 28 },
        { name: '浙江', value: 18 },
        { name: '江苏', value: 22 }
      )
    }
    
    const convertData = (data) => {
      return data.map(item => {
        const coord = provinceCoordMap[item.name]
        return coord ? {
          name: item.name,
          value: coord.concat(item.value)
        } : null
      }).filter(item => item !== null)
    }
    
    // 计算最大值用于视觉映射
    const maxValue = Math.max(...volunteerData.map(item => item.value), 10)
    
    // 更新图表数据
    charts.map.setOption({
      visualMap: {
        max: maxValue
      },
      series: [
        {
          name: '志愿者分布',
          type: 'scatter',
          coordinateSystem: 'geo',
          data: convertData(volunteerData),
          symbolSize: (val) => Math.max(val[2] * 2, 10),
          encode: {
            value: 2
          },
          label: {
            formatter: '{b}',
            position: 'right',
            show: false
          },
          itemStyle: {
            color: new echarts.graphic.RadialGradient(0.5, 0.5, 0.8, [
              { offset: 0, color: 'rgba(255, 107, 107, 0.9)' },
              { offset: 0.5, color: 'rgba(255, 107, 107, 0.6)' },
              { offset: 1, color: 'rgba(255, 107, 107, 0.3)' }
            ]),
            shadowBlur: 10,
            shadowColor: 'rgba(255, 107, 107, 0.5)'
          },
          emphasis: {
            label: {
              show: true,
              color: '#333',
              fontSize: 12,
              fontWeight: 'bold'
            },
            itemStyle: {
              color: '#ff6b6b',
              shadowBlur: 20,
              shadowColor: 'rgba(255, 107, 107, 0.8)'
            }
          }
        },
        {
          name: '热力图',
          type: 'heatmap',
          coordinateSystem: 'geo',
          data: convertData(volunteerData),
          pointSize: 15,
          blurSize: 30
        }
      ]
    })
  } catch (error) {
    console.error('加载志愿者地域数据失败:', error)
    // 如果请求失败，显示提示信息
    if (mapChart.value) {
      const emptyChart = echarts.init(mapChart.value)
      emptyChart.showLoading('default', {
        text: '暂无地域数据',
        color: '#1890ff',
        textColor: '#666',
        maskColor: 'rgba(255, 255, 255, 0.8)'
      })
    }
  }
}

// 事件颜色
const getEventColor = (type) => {
  const colors = {
    activity: '#4facfe',
    hours: '#11998e',
    certificate: '#f093fb',
    replenish: '#FF8E53'
  }
  return colors[type] || '#4a90e2'
}

// 获取事件图标
const getEventIcon = (type) => {
  const icons = {
    activity: Tickets,
    hours: Timer,
    certificate: DocumentChecked,
    replenish: Edit
  }
  return icons[type] || Link
}

// 格式化时间
const formatTime = (time) => {
  const now = new Date()
  const diff = Math.floor((now - time) / 1000)
  if (diff < 60) return `${diff}秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

// 窗口尺寸变化时重绘图表
const handleResize = () => {
  Object.values(charts).forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(async () => {
  // 加载KPI数据
  await loadKpiData()
  
  // 加载最近事件
  await loadRecentEvents()
  
  // 初始化所有图表
  await initCharts()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  
  // 定时刷新数据（每30秒）
  setInterval(async () => {
    await loadKpiData()
    await loadRecentEvents()
  }, 30000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(charts).forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<style scoped>
.data-screen {
  min-height: 100vh;
  background: #ffffff;
  padding: 20px;
  overflow-x: hidden;
}

/* 顶部KPI区域 */
.kpi-section {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.kpi-card {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.kpi-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.kpi-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
}

.kpi-content {
  flex: 1;
}

.kpi-value {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: baseline;
}

.kpi-number {
  font-size: 32px;
  font-weight: 700;
  color: #333;
}

.kpi-unit {
  font-size: 16px;
  margin-left: 4px;
  color: #999;
}

.kpi-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.kpi-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kpi-trend.up {
  color: #67c23a;
}

.kpi-trend.down {
  color: #f56c6c;
}

/* 主体内容区 - 数据大屏式布局 */
.main-content {
  display: grid;
  grid-template-columns: 1.2fr 1.6fr 1.2fr;
  gap: 20px;
  margin-bottom: 20px;
}

/* 左侧面板 */
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: slideInLeft 0.6s ease-out;
}

/* 中间地图面板 */
.center-panel {
  display: flex;
  animation: slideInUp 0.6s ease-out 0.2s both;
}

.center-panel .map-card {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: slideInRight 0.6s ease-out 0.4s both;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chart-card {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.chart-container {
  height: 260px;
}

.chart-container.large {
  height: 280px;
}

.chart-container.map-large {
  flex: 1;
  height: 100%;
  min-height: 480px;
}

/* 底部图表区 - 两栏等宽布局 */
.bottom-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.bottom-charts .chart-card {
  animation: slideInUp 0.6s ease-out 0.6s both;
}

/* 地图卡片特殊样式 */
.map-card {
  background: #ffffff;
  border: 2px solid #ff6b6b;
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.15);
}

.map-card .chart-header {
  background: linear-gradient(90deg, #ff6b6b, #ff8e8e);
  color: white;
  margin: -16px -16px 16px -16px;
  padding: 16px;
  border-radius: 12px 12px 0 0;
  border-bottom: none;
}

.map-card .chart-header .el-icon {
  color: white;
}

/* 图表分组标识色条 */
.pie-card {
  position: relative;
}

.pie-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #722ed1, #eb2f96);
  border-radius: 12px 0 0 12px;
}

.line-card {
  position: relative;
}

.line-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #1890ff, #faad14);
  border-radius: 12px 0 0 12px;
}

.bar-card {
  position: relative;
}

.bar-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #ff6b6b, #4ecb73);
  border-radius: 12px 0 0 12px;
}

/* 底部时间线 */
.timeline-section {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.timeline-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 8px 0;
}

.timeline-list::-webkit-scrollbar {
  height: 8px;
}

.timeline-list::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.timeline-list::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 4px;
  transition: background 0.3s;
}

.timeline-list::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}

.timeline-item {
  min-width: 320px;
  max-width: 320px;
  display: flex;
  gap: 14px;
  align-items: flex-start;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.timeline-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #d0d0d0;
}

.event-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.timeline-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.event-activity {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timeline-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  flex-shrink: 0;
}

.event-body {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 13px;
}

.event-user {
  color: #1890ff;
  font-weight: 500;
  flex-shrink: 0;
}

.timeline-text {
  color: #666;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-top: 4px;
  border-top: 1px dashed #e8e8e8;
}

.timeline-hash {
  font-size: 11px;
  color: #999;
  font-family: 'Monaco', 'Courier New', monospace;
  letter-spacing: 0.5px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .kpi-section {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .main-content {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  
  .left-panel,
  .center-panel,
  .right-panel {
    animation: none;
  }
  
  .chart-container.map-large {
    height: 380px;
    min-height: 380px;
  }

  .chart-container.large {
    height: 260px;
  }
}
</style>