<template>
  <Box title="污水处理率" class="vr-panel sewage-rate">
    <div class="sr-body">
      <!-- 柱状图 + 折线图（进水量/出水量柱、合格率折线，右轴 %） -->
      <div class="sr-chart" ref="chartEl">
        <svg
          v-if="size.w > 0 && size.h > 0"
          :width="size.w"
          :height="size.h"
          :viewBox="`0 0 ${size.w} ${size.h}`"
        >
          <!-- 左轴：水量网格线 + 刻度 -->
          <template v-for="tick in volTicks" :key="'v' + tick">
            <line
              :x1="pad.l" :y1="volY(tick)" :x2="size.w - pad.r" :y2="volY(tick)"
              stroke="rgba(255,255,255,0.08)" stroke-width="1"
              :stroke-dasharray="tick === 0 ? '' : '3 3'"
            />
            <text :x="pad.l - 6" :y="volY(tick) + 3" text-anchor="end" class="axis-label">{{ tick }}</text>
          </template>

          <!-- 右轴：合格率刻度 -->
          <template v-for="tick in rateTicks" :key="'r' + tick">
            <text :x="size.w - pad.r + 4" :y="rateY(tick) + 3" class="axis-label">{{ tick }}%</text>
          </template>

          <!-- X 轴月份 -->
          <text
            v-for="(m, i) in sewageRate.months"
            :key="m"
            :x="cx(i)" :y="size.h - 4" text-anchor="middle" class="axis-label"
          >{{ m }}</text>

          <!-- 进水量 / 出水量 柱状图 -->
          <template v-for="(_, i) in sewageRate.months" :key="'bar' + i">
            <rect
              :x="cx(i) - barW - 1.5" :y="volY(sewageRate.inlet[i])"
              :width="barW" :height="size.h - pad.b - volY(sewageRate.inlet[i])"
              fill="url(#srGradInlet)" rx="1.5"
            />
            <rect
              :x="cx(i) + 1.5" :y="volY(sewageRate.outlet[i])"
              :width="barW" :height="size.h - pad.b - volY(sewageRate.outlet[i])"
              fill="url(#srGradOutlet)" rx="1.5"
            />
          </template>

          <!-- 合格率 折线 -->
          <path :d="ratePath" fill="none" stroke="#ffb300" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round" />
          <circle
            v-for="(v, i) in sewageRate.rate" :key="'dot' + i"
            :cx="cx(i)" :cy="rateY(v)" r="2.2" fill="#ffb300"
          />

          <defs>
            <linearGradient id="srGradInlet" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#22c0ff" stop-opacity="0.95" />
              <stop offset="100%" stop-color="#22c0ff" stop-opacity="0.25" />
            </linearGradient>
            <linearGradient id="srGradOutlet" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.95" />
              <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.25" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      <!-- 图例 -->
      <div class="sr-legend">
        <span class="sr-lg"><i class="sr-lg-bar" style="background:#22c0ff" />进水量</span>
        <span class="sr-lg"><i class="sr-lg-bar" style="background:#3b82f6" />出水量</span>
        <span class="sr-lg"><i class="sr-lg-line" style="background:#ffb300" />合格率</span>
      </div>
    </div>
  </Box>
</template>

<script setup>
import { computed } from 'vue'
import Box from '@/components/Box.vue'
import { sewageRate } from '@/data/vrMetrics'
import { useChartSize, smoothPath } from './chart'

const { el: chartEl, size } = useChartSize()

const VOL_MAX = 800
const volTicks = [0, 200, 400, 600, 800]
const rateTicks = [0, 25, 50, 75, 100]
const pad = { t: 8, r: 28, b: 16, l: 30 }

const pw = computed(() => Math.max(size.w - pad.l - pad.r, 10))
const ph = computed(() => Math.max(size.h - pad.t - pad.b, 10))
const slotW = computed(() => pw.value / sewageRate.months.length)
const barW = computed(() => Math.min(slotW.value * 0.3, 14))

const cx = (i) => pad.l + i * slotW.value + slotW.value / 2
const volY = (v) => pad.t + ph.value * (1 - v / VOL_MAX)
const rateY = (v) => pad.t + ph.value * (1 - v / 100)

const ratePath = computed(() =>
  smoothPath(sewageRate.rate.map((v, i) => [cx(i), rateY(v)]))
)
</script>

<style scoped>
@import './panel.css';

.sewage-rate {
  flex: 1;
  min-height: 0;
}

.sr-body {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-height: 0;
}

.sr-chart {
  flex: 1;
  min-height: 40px;
  position: relative;
}

.sr-chart svg {
  display: block;
}

.axis-label {
  font-size: 9px;
  fill: rgba(255, 255, 255, 0.45);
}

/* 图例 */
.sr-legend {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.sr-lg {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
  white-space: nowrap;
}

.sr-lg-bar {
  width: 7px;
  height: 9px;
  border-radius: 1px;
  display: inline-block;
}

.sr-lg-line {
  width: 12px;
  height: 3px;
  border-radius: 2px;
  display: inline-block;
}
</style>
