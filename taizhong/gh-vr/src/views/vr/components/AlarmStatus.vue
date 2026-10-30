<template>
  <Box title="告警处理状态" height="182px" class="vr-panel alarm-status">
    <div class="as-body">
      <!-- 环形图（左侧） -->
      <div class="as-ring">
        <img class="as-ring-deco" :src="circleAni" alt="" />
        <svg :width="RING" :height="RING" :viewBox="`0 0 ${RING} ${RING}`">
          <!-- 底色圆环 -->
          <circle :cx="c" :cy="c" :r="r" fill="none" stroke="rgba(255,255,255,0.08)" :stroke-width="stroke" />
          <!-- 各状态分段 -->
          <circle
            v-for="seg in segments" :key="seg.name"
            :cx="c" :cy="c" :r="r" fill="none"
            :stroke="seg.color" :stroke-width="stroke" stroke-linecap="butt"
            :stroke-dasharray="`${seg.dash} ${circ - seg.dash}`"
            :stroke-dashoffset="seg.offset"
            transform="rotate(-90 55 55)"
          />
        </svg>
        <div class="as-ring-center">
          <span class="as-total font-youshehei">{{ alarmStatus.total }}</span>
          <span class="as-total-label">告警总数</span>
        </div>
      </div>

      <!-- 图例（右侧） -->
      <div class="as-legend">
        <div v-for="item in alarmStatus.items" :key="item.name" class="as-item">
          <i class="as-dot" :style="{ background: item.color, boxShadow: `0 0 6px ${item.color}` }" />
          <span class="as-name">{{ item.name }}</span>
          <span class="as-count font-youshehei">{{ item.count }}</span>
          <span class="as-pct">{{ item.pct }}%</span>
        </div>
      </div>
    </div>
  </Box>
</template>

<script setup>
import { computed } from 'vue'
import Box from '@/components/Box.vue'
import { alarmStatus } from '@/data/vrMetrics'
import circleAni from '@/assets/circleAni.png'

// 环形图尺寸（固定像素，保持无拉伸）
const RING = 110
const c = RING / 2
const r = 42
const stroke = 11
const circ = 2 * Math.PI * r

// 各分段 dash/offset：按占比分配圆周，从 12 点方向开始顺时针
const segments = computed(() => {
  let acc = 0
  return alarmStatus.items.map((item) => {
    const dash = circ * (item.pct / 100)
    const seg = { name: item.name, color: item.color, dash, offset: -acc }
    acc += dash
    return seg
  })
})
</script>

<style scoped>
@import './panel.css';

/* 面板固定高度 182px 由 Box height 属性控制 */
.alarm-status {
  flex: none;
}

.as-body {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

/* 环形图 */
.as-ring {
  position: relative;
  flex: none;
  width: 110px;
  height: 110px;
}

.as-ring-deco {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  opacity: 0.55;
  mix-blend-mode: screen;
  pointer-events: none;
  /* 背景动画：呼吸缩放 + 透明度脉动 */
  animation: as-breathe 3.2s ease-in-out infinite;
}

@keyframes as-breathe {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.75; transform: scale(1.1); }
}

.as-ring svg {
  position: relative;
  display: block;
}

.as-ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.as-total-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.as-total {
  font-size: 20px;
  line-height: 1;
  color: #fff;
  text-shadow: 0 0 10px rgba(34, 192, 255, 0.7);
}

/* 图例 */
.as-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.as-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.66);
}

.as-dot {
  flex: none;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.as-name {
  white-space: nowrap;
}

.as-count {
  margin-left: auto;
  font-size: 15px;
  color: #fff;
  line-height: 1;
}

.as-pct {
  width: 34px;
  text-align: right;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.45);
  white-space: nowrap;
}
</style>
