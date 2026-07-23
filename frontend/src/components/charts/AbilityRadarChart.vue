<template>
  <div class="ability-radar-chart">
    <div class="chart-header">
      <h3>🎯 志愿能力雷达图</h3>
      <el-tooltip content="多维度展示您的志愿服务能力" placement="top">
        <el-icon><QuestionFilled /></el-icon>
      </el-tooltip>
    </div>
    <div class="chart-container" ref="chartRef" v-loading="loading"></div>
    <div class="legend-container">
      <div v-for="(indicator, index) in indicators" :key="index" class="legend-item">
        <div class="legend-dot" :style="{ background: getColor(indicator.value) }"></div>
        <div class="legend-info">
          <div class="legend-name">{{ indicator.name }}</div>
          <div class="legend-value">{{ indicator.value }}/100</div>
        </div>
        <el-tooltip :content="indicator.description" placement="top">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getAbilityRadar } from '@/api/stats'
import { ElMessage } from 'element-plus'
import { QuestionFilled, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  volunteerId: {
    type: Number,
    required: true
  }
})

const chartRef = ref(null)
const loading = ref(false)
const indicators = ref([])
let chartInstance = null

// 根据分数获取颜色
const getColor = (value) => {
  if (value >= 80) return '#52c41a'
  if (value >= 60) return '#1890ff'
  if (value >= 40) return '#faad14'
  return '#f5222d'
}

// 加载图表数据
const loadChartData = async () => {
  loading.value = true
  try {
    const res = await getAbilityRadar(props.volunteerId)
    if (res && res.data && res.data.indicators) {
      indicators.value = res.data.indicators
      renderChart(res.data.indicators)
    }
  } catch (error) {
    console.error('加载能力雷达图数据失败:', error)
    ElMessage.error('加载能力雷达图数据失败')
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
  
  // 准备雷达图指标
  const radarIndicators = data.map(item => ({
    name: item.name,
    max: item.max || 100
  }))
  
  // 准备数据
  const seriesData = data.map(item => item.value)
  
  // 图表配置
  const option = {
    tooltip: {
      show: true,
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ce4c4c',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      },
      confine: true,
      formatter: function(params) {
        return ''  // 使用自定义tooltip，这里返回空
      }
    },
    radar: {
      indicator: radarIndicators,
      shape: 'polygon',
      center: ['50%', '50%'],
      radius: '65%',
      splitNumber: 4,
      name: {
        textStyle: {
          color: '#666',
          fontSize: 13,
          fontWeight: 'bold'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#ddd',
          width: 1
        }
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(206, 76, 76, 0.05)', 'rgba(206, 76, 76, 0.1)', 
                  'rgba(206, 76, 76, 0.05)', 'rgba(206, 76, 76, 0.1)']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      }
    },
    series: [
      {
        name: '能力指标',
        type: 'radar',
        data: [
          {
            value: seriesData,
            name: '能力评估',
            symbol: 'circle',
            symbolSize: 10,
            lineStyle: {
              width: 3,
              color: '#ce4c4c'
            },
            itemStyle: {
              color: '#ce4c4c',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                { offset: 0, color: 'rgba(206, 76, 76, 0.4)' },
                { offset: 1, color: 'rgba(206, 76, 76, 0.1)' }
              ])
            },
            emphasis: {
              lineStyle: {
                width: 4
              },
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(206, 76, 76, 0.5)'
              }
            }
          }
        ],
        // 添加动画
        animationDuration: 1500,
        animationEasing: 'elasticOut'
      }
    ]
  }
  
  chartInstance.setOption(option)
  
  // 使用全局鼠标事件监听，实现轴向tooltip
  let currentAxisIndex = -1
  
  const chartDom = chartRef.value
  if (chartDom) {
    // 移除旧的事件监听
    chartDom.onmousemove = null
    chartDom.onmouseleave = null
    
    chartDom.onmousemove = (event) => {
      const rect = chartDom.getBoundingClientRect()
      const x = event.clientX - rect.left
      const y = event.clientY - rect.top
      
      // 获取图表尺寸和中心点
      const width = chartInstance.getWidth()
      const height = chartInstance.getHeight()
      const centerX = width * 0.5
      const centerY = height * 0.5
      
      // 计算鼠标相对于中心的角度
      const dx = x - centerX
      const dy = y - centerY
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      // 只在雷达图区域内响应（距离中心在合理范围内）
      const maxRadius = Math.min(width, height) * 0.35
      if (distance < 20 || distance > maxRadius) {
        if (currentAxisIndex !== -1) {
          chartInstance.dispatchAction({ type: 'hideTip' })
          currentAxisIndex = -1
        }
        return
      }
      
      // 计算角度（0度在右侧，逆时针增加）
      let angle = Math.atan2(-dy, dx) * 180 / Math.PI  // 注意：y轴向下为正，所以取负
      angle = (angle + 360) % 360
      
      // ECharts雷达图的第一个指标在顶部（90度位置），然后顺时针排列
      // 调整角度：将0度对应到顶部
      angle = (angle + 90) % 360
      
      // 计算每个轴的角度
      const indicatorCount = radarIndicators.length
      const anglePerIndicator = 360 / indicatorCount
      
      // 找到最接近的轴
      let visualIndex = Math.round(angle / anglePerIndicator) % indicatorCount
      
      // 数据顺序映射：后端返回顺序是 [参与频率, 服务准时率, 活动完成率, 积分获得率, 论坛活跃率, 积分兑换率]
      // 视觉顺序（从顶部顺时针）：0=参与频率, 1=积分兑换率, 2=论坛活跃率, 3=积分获得率, 4=活动完成率, 5=服务准时率
      // 映射关系：visualIndex -> dataIndex
      const indexMap = [0, 5, 4, 3, 2, 1]  // 视觉位置到数据索引的映射
      let closestIndex = indexMap[visualIndex]
      
      // 计算到最近轴的角度差
      const closestAngle = visualIndex * anglePerIndicator
      let angleDiff = Math.abs(angle - closestAngle)
      if (angleDiff > 180) angleDiff = 360 - angleDiff
      
      // 只有在靠近某个轴时才显示tooltip（角度差小于25度）
      if (angleDiff < 25) {
        if (currentAxisIndex !== closestIndex) {
          currentAxisIndex = closestIndex
          const indicator = data[closestIndex]
          const value = seriesData[closestIndex]
          
          // 更新并显示tooltip
          chartInstance.setOption({
            tooltip: {
              formatter: function() {
                return `
                  <div style="padding: 8px;">
                    <div style="font-weight: bold; margin-bottom: 8px; color: #ce4c4c;">
                      ${indicator.name}
                    </div>
                    <div style="margin-bottom: 5px;">
                      <span style="color: #666;">得分：</span>
                      <strong style="color: #ce4c4c; font-size: 16px;">${value}</strong>
                      <span style="color: #999;"> / 100</span>
                    </div>
                    <div style="color: #666; font-size: 12px; line-height: 1.5;">
                      ${indicator.description}
                    </div>
                    <div style="margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e8e8e8; color: #999; font-size: 11px;">
                      💡 得分越高，表示您在全系统中的相对贡献越大
                    </div>
                  </div>
                `
              }
            }
          })
          
          chartInstance.dispatchAction({
            type: 'showTip',
            seriesIndex: 0,
            dataIndex: 0,
            position: [x, y]
          })
        }
      } else {
        if (currentAxisIndex !== -1) {
          chartInstance.dispatchAction({ type: 'hideTip' })
          currentAxisIndex = -1
        }
      }
    }
    
    chartDom.onmouseleave = () => {
      if (currentAxisIndex !== -1) {
        chartInstance.dispatchAction({ type: 'hideTip' })
        currentAxisIndex = -1
      }
    }
  }
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
.ability-radar-chart {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s ease-out 0.2s backwards;
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

.chart-header .el-icon {
  font-size: 18px;
  color: #999;
  cursor: help;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.legend-container {
  margin-top: 15px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f9f9f9;
  border-radius: 6px;
  transition: all 0.3s;
}

.legend-item:hover {
  background: #f0f0f0;
  transform: translateX(5px);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-info {
  flex: 1;
}

.legend-name {
  font-size: 13px;
  color: #666;
  margin-bottom: 3px;
}

.legend-value {
  font-size: 15px;
  font-weight: bold;
  color: #333;
}

.info-icon {
  font-size: 16px;
  color: #999;
  cursor: help;
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
  
  .legend-container {
    grid-template-columns: 1fr;
  }
}
</style>
