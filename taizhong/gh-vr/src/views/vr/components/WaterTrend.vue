<template>
  <Box title="水质指标趋势" height="240px" class="vr-panel water-trend">
    <!-- 更新时间放在标题栏右侧插槽 -->
    <template #titleRight>
      <span class="wt-time">更新时间：{{ waterTrend.updateTime }}</span>
    </template>

    <div class="wt-body">
      <!-- 顶部四指标：背景图卡片 + flex 布局，数值蓝色 -->
      <div class="wt-indicators">
        <div
          v-for="ind in waterTrend.indicators"
          :key="ind.name"
          class="wt-ind"
          :style="{ backgroundImage: `linear-gradient(rgba(2, 18, 48, 0.45), rgba(2, 18, 48, 0.45)), url(${countBg})` }"
        >
          <span class="wt-ind-name">{{ ind.name }}</span>
          <span class="wt-ind-value font-youshehei">{{ ind.value }}</span>
        </div>
      </div>

      <!-- 折线图（自适应容器尺寸） -->
      <div class="wt-chart" ref="chartEl">
        <svg
          v-if="size.w > 0 && size.h > 0"
          :width="size.w"
          :height="size.h"
          :viewBox="`0 0 ${size.w} ${size.h}`"
        >
          <!-- 横向网格线 + Y 轴刻度 -->
          <template v-for="tick in yTicks" :key="tick">
            <line
              :x1="pad.l" :y1="yPos(tick)" :x2="size.w - pad.r" :y2="yPos(tick)"
              stroke="rgba(255,255,255,0.08)" stroke-width="1"
              :stroke-dasharray="tick === 0 ? '' : '3 3'"
            />
            <text :x="pad.l - 6" :y="yPos(tick) + 3" text-anchor="end" class="axis-label">{{ tick }}</text>
          </template>

          <!-- X 轴月份 -->
          <text
            v-for="(m, i) in waterTrend.months"
            :key="m"
            :x="xPos(i)" :y="size.h - 4" text-anchor="middle" class="axis-label"
          >{{ m }}</text>

          <!-- 数据折线 -->
          <template v-for="s in waterTrend.series" :key="s.name">
            <path :d="linePath(s.data)" fill="none" :stroke="s.color" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round" />
            <circle
              v-for="(v, i) in s.data" :key="i"
              :cx="xPos(i)" :cy="yPos(v)" r="2" :fill="s.color"
            />
          </template>
        </svg>
      </div>

      <!-- 图例 -->
      <div class="wt-legend">
        <span v-for="s in waterTrend.series" :key="s.name" class="wt-lg">
          <i class="wt-lg-dot" :style="{ background: s.color }" />
          {{ s.name }}
        </span>
      </div>
    </div>
  </Box>
</template>

<script setup>
import { computed } from 'vue'
import Box from '@/components/Box.vue'
import { waterTrend } from '@/data/vrMetrics'
import { useChartSize, smoothPath } from './chart'
import countBg from '@/assets/countBg.png'

const { el: chartEl, size } = useChartSize()

const MAX_Y = 400
const yTicks = [0, 100, 200, 300, 400]
const pad = { t: 8, r: 14, b: 16, l: 30 }

const pw = computed(() => Math.max(size.w - pad.l - pad.r, 10))
const ph = computed(() => Math.max(size.h - pad.t - pad.b, 10))

const xPos = (i) => {
  const n = waterTrend.months.length
  return pad.l + (n <= 1 ? 0 : (i * pw.value) / (n - 1))
}
const yPos = (v) => pad.t + ph.value * (1 - v / MAX_Y)

const linePath = (data) =>
  smoothPath(data.map((v, i) => [xPos(i), yPos(v)]))
</script>

<style scoped>
@import './panel.css';

/* 指定高度 240px（由 Box height 传入），下方污水处理率 flex:1 自适应 */
.water-trend {
  flex: none;
}

.wt-body {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 0;
}

/* 标题栏右侧更新时间 */
.wt-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 0.5px;
}

/* 四指标一行：背景图卡片 + flex 布局 */
.wt-indicators {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  flex: none;
}

.wt-ind {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding: 6px 2px;
  background-repeat: no-repeat;
  background-position: center;
  background-size: 100% 100%;
  border-radius: 4px;
}

.wt-ind-name {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.78);
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.wt-ind-value {
  font-size: 17px;
  color: rgb(44, 206, 255);
  line-height: 1;
  white-space: nowrap;
  text-shadow: 0 0 8px rgba(44, 206, 255, 0.4);
}

/* 图表区：占据剩余空间 */
.wt-chart {
  flex: 1;
  min-height: 40px;
  position: relative;
}

.wt-chart svg {
  display: block;
}

.axis-label {
  font-size: 9px;
  fill: rgba(255, 255, 255, 0.45);
}

/* 图例 */
.wt-legend {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex: none;
}

.wt-lg {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
  white-space: nowrap;
}

.wt-lg-dot {
  width: 12px;
  height: 3px;
  border-radius: 2px;
  display: inline-block;
}
</style>
