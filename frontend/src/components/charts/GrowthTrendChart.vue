<template>
  <div class="growth-trend-chart">
    <div class="chart-header">
      <h3>📈 志愿者成长轨迹</h3>
      <div class="period-selector">
        <el-radio-group v-model="currentPeriod" size="small" @change="handlePeriodChange">
          <el-radio-button label="day">日</el-radio-button>
          <el-radio-button label="week">周</el-radio-button>
          <el-radio-button label="month">月</el-radio-button>
          <el-radio-button label="year">年</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <div class="chart-container" ref="chartRef" v-loading="loading"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getGrowthTrend } from '@/api/stats'
import { ElMessage } from 'element-plus'

const props = defineProps({
  volunteerId: {
    type: Number,
    required: true
  }
})

const chartRef = ref(null)
const loading = ref(false)
const currentPeriod = ref('month')
let chartInstance = null

// 加载图表数据
const loadChartData = async () => {
  loading.value = true
  try {
    const res = await getGrowthTrend(props.volunteerId, currentPeriod.value)
    if (res && res.data) {
      const chartData = res.data.data || []
      renderChart(chartData)
    }
  } catch (error) {
    console.error('加载成长轨迹数据失败:', error)
    ElMessage.error('加载成长轨迹数据失败')
  } finally {
    loading.value = false
  }
}

// 渲染图表
const renderChart = (data) => {
  if (!chartRef.value) return
  
  // 初始化或获取图表实例
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  // 处理数据和X轴标签
  let xAxisData = []
  let seriesData = []
  let xAxisName = ''
  
  if (currentPeriod.value === 'day') {
    // 日维度：24小时
    xAxisName = '时间（小时）'
    const hourMap = new Map()
    data.forEach(item => {
      const hour = item.hour || 0
      hourMap.set(hour, Number(item.hours) || 0)
    })
    // 填充24小时，显示整数
    for (let i = 0; i < 24; i++) {
      xAxisData.push(i + '时')
      seriesData.push(hourMap.get(i) || 0)
    }
  } else if (currentPeriod.value === 'week') {
    // 周维度：7天（周日到周六）
    xAxisName = '星期'
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const dayMap = new Map()
    data.forEach(item => {
      const dayOfWeek = item.dayOfWeek || 1 // MySQL DAYOFWEEK: 1=周日, 2=周一, ..., 7=周六
      dayMap.set(dayOfWeek, (dayMap.get(dayOfWeek) || 0) + (Number(item.hours) || 0))
    })
    // 填充7天
    for (let i = 1; i <= 7; i++) {
      xAxisData.push(weekDays[i - 1])
      seriesData.push(dayMap.get(i) || 0)
    }
  } else if (currentPeriod.value === 'month') {
    // 月维度：当月每天（1-31）
    xAxisName = '日期'
    const dayMap = new Map()
    data.forEach(item => {
      const day = item.day || 1
      dayMap.set(day, Number(item.hours) || 0)
    })
    // 获取当月天数
    const now = new Date()
    const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
    for (let i = 1; i <= daysInMonth; i++) {
      xAxisData.push(i + '日')
      seriesData.push(dayMap.get(i) || 0)
    }
  } else if (currentPeriod.value === 'year') {
    // 年维度：12个月
    xAxisName = '月份'
    const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    const monthMap = new Map()
    data.forEach(item => {
      const monthNum = item.monthNum || 1
      monthMap.set(monthNum, (monthMap.get(monthNum) || 0) + (Number(item.hours) || 0))
    })
    // 填充12个月
    for (let i = 1; i <= 12; i++) {
      xAxisData.push(monthNames[i - 1])
      seriesData.push(monthMap.get(i) || 0)
    }
  }
  
  // 图表配置
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ce4c4c',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      },
      formatter: function(params) {
        const param = params[0]
        return `
          <div style="padding: 5px;">
            <div style="font-weight: bold; margin-bottom: 5px;">${param.name}</div>
            <div style="color: #ce4c4c;">
              <span style="display: inline-block; width: 10px; height: 10px; background: #ce4c4c; border-radius: 50%; margin-right: 5px;"></span>
              服务时长：<strong>${param.value.toFixed(1)} 小时</strong>
            </div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      name: xAxisName,
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      data: xAxisData,
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      },
      axisLabel: {
        color: '#666',
        rotate: currentPeriod.value === 'month' ? 45 : 0,
        interval: currentPeriod.value === 'month' ? 'auto' : 0,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '服务时长（小时）',
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: '#ddd'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: '服务时长',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: seriesData,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#ce4c4c' },
            { offset: 1, color: '#e67e7e' }
          ])
        },
        itemStyle: {
          color: '#ce4c4c',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(206, 76, 76, 0.3)' },
            { offset: 1, color: 'rgba(206, 76, 76, 0.05)' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: '#ce4c4c',
            borderColor: '#fff',
            borderWidth: 3,
            shadowBlur: 10,
            shadowColor: 'rgba(206, 76, 76, 0.5)'
          }
        },
        // 添加动画
        animationDuration: 1500,
        animationEasing: 'cubicOut'
      }
    ]
  }
  
  chartInstance.setOption(option)
}

// 周期切换
const handlePeriodChange = () => {
  loadChartData()
}

// 响应式调整
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  loadChartData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 监听volunteerId变化
watch(() => props.volunteerId, () => {
  loadChartData()
})
</script>

<style scoped>
.growth-trend-chart {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.period-selector {
  display: flex;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}

/* 响应式 */
@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>
