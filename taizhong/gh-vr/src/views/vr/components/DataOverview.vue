<template>
  <Box title="数据概览" height="182px" class="vr-panel data-overview">
    <div class="ov-body">
      <div v-for="item in overviewItems" :key="item.name" class="ov-cell">
        <img class="ov-icon" :src="icons[item.icon]" alt="" />
        <div class="ov-text">
          <span class="ov-name">{{ item.name }}</span>
          <span class="ov-value font-youshehei">{{ item.value }}</span>
        </div>
      </div>
    </div>
  </Box>
</template>

<script setup>
import Box from '@/components/Box.vue'
import { overviewItems } from '@/data/vrMetrics'
import icon1 from '@/assets/data-1.png'
import icon2 from '@/assets/data-2.svg'
import icon3 from '@/assets/data-3.svg'
import icon4 from '@/assets/data-4.svg'

const icons = {
  'data-1': icon1,
  'data-2': icon2,
  'data-3': icon3,
  'data-4': icon4,
}
</script>

<style scoped>
@import './panel.css';

/* 面板固定高度 182px 由 Box height 属性控制，此处保证不被压缩 */
.data-overview {
  flex: none;
}

/* 四宫格：2 行 2 列，左图右内容、文本上下布局 */
.ov-body {
  height: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 4px 8px;
}

.ov-cell {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
  padding: 0 6px;
  border-radius: 4px;
}

.ov-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  flex: none;
}

.ov-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
}

.ov-name {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* 数值：金色，无背景 */
.ov-value {
  font-size: 20px;
  color: rgb(255, 199, 33);
  text-shadow: 0 0 10px rgba(255, 199, 33, 0.35);
  letter-spacing: 1px;
  line-height: 1;
  white-space: nowrap;
}
</style>
