<template>
  <Box title="告警动态" class="vr-panel alarm-list">
    <div class="al-body">
      <!-- 表头（固定） -->
      <div class="al-head">
        <span class="al-c-time">告警时间</span>
        <span class="al-c-type">告警类型</span>
        <span class="al-c-detail">详情</span>
      </div>

      <!-- 告警列表：超出区域一行行向上无缝滚动（内容复制一份实现无缝循环） -->
      <div class="al-list">
        <div
          v-if="alarmList.length"
          class="al-track"
          :style="{ animationDuration: `${alarmList.length * 2.2}s` }"
        >
          <div v-for="(row, i) in alarmList" :key="'a-' + i" class="al-row">
            <span class="al-c-time">{{ row.time }}</span>
            <span class="al-c-type" :class="`lv-${row.level}`">{{ row.type }}</span>
            <span class="al-c-detail" :title="row.detail">{{ row.detail }}</span>
          </div>
          <!-- 复制一份用于无缝循环 -->
          <div v-for="(row, i) in alarmList" :key="'b-' + i" class="al-row" aria-hidden="true">
            <span class="al-c-time">{{ row.time }}</span>
            <span class="al-c-type" :class="`lv-${row.level}`">{{ row.type }}</span>
            <span class="al-c-detail" :title="row.detail">{{ row.detail }}</span>
          </div>
        </div>
        <div v-else class="al-empty">暂无告警</div>
      </div>
    </div>
  </Box>
</template>

<script setup>
import Box from '@/components/Box.vue'
import { alarmList } from '@/data/vrMetrics'
</script>

<style scoped>
@import './panel.css';

.alarm-list {
  flex: 1;
  min-height: 0;
}

.al-body {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 表头 */
.al-head,
.al-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 4px;
  min-height: 32px;
  box-sizing: border-box;
}

.al-head {
  flex: none;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  background: rgba(156, 197, 255, 0.08);
  border: 1px solid rgba(156, 197, 255, 0.12);
  border-radius: 3px;
}

/* 列宽：时间 / 类型 / 详情 */
.al-c-time {
  flex: none;
  width: 94px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  white-space: nowrap;
}

.al-c-type {
  flex: none;
  width: 72px;
  font-size: 12px;
  white-space: nowrap;
}

.al-c-detail {
  flex: 1;
  min-width: 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.72);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 告警类型配色 */
.lv-danger { color: #ff5252; }
.lv-warning { color: #ffb300; }
.lv-info { color: #22c0ff; }

/* 列表区：隐藏溢出，滚动轨道无缝上移 */
.al-list {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  margin-top: 4px;
  position: relative;
}

.al-track {
  display: flex;
  flex-direction: column;
  animation: al-scroll linear infinite;
}

.al-list:hover .al-track {
  animation-play-state: paused;
}

@keyframes al-scroll {
  from { transform: translateY(0); }
  to { transform: translateY(-50%); }
}

.al-row {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.2s;
}

.al-row:hover {
  background: rgba(34, 192, 255, 0.1);
}

.al-empty {
  padding: 20px 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
}
</style>
