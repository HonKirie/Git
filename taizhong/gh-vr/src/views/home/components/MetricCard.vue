<template>
  <Box :title="card.title" class="metric-card">
    <!-- 标题栏右侧：在线状态呼吸点 -->
    <template #titleRight>
      <i class="mc-live" />
    </template>

    <div class="metric-items">
      <div
        v-for="(item, idx) in card.items"
        :key="idx"
        class="metric-row"
      >
        <!-- 指标名称与补充描述 -->
        <div class="metric-left">
          <span class="metric-label">{{ item.label }}</span>
          <span v-if="item.subtext" class="metric-subtext">{{ item.subtext }}</span>
        </div>

        <!-- 指标数值与单位 -->
        <div class="metric-right">
          <span
            class="metric-value font-youshehei"
            :class="`status-${item.status || 'normal'}`"
          >
            {{ item.value }}
          </span>
          <span v-if="item.unit" class="metric-unit">{{ item.unit }}</span>

          <!-- 行内额外指标（例如: / 故障 3） -->
          <template v-if="item.extra">
            <span class="metric-extra-label">{{ item.extra.label }}</span>
            <span
              class="metric-value font-youshehei"
              :class="`status-${item.extra.status || 'normal'}`"
            >
              {{ item.extra.value }}
            </span>
            <span v-if="item.extra.unit" class="metric-unit">{{ item.extra.unit }}</span>
          </template>
        </div>
      </div>
    </div>
  </Box>
</template>

<script setup>
import Box from '@/components/Box.vue'

defineProps({
  card: {
    type: Object,
    required: true,
  },
})
</script>

<style scoped>
.metric-card {
  width: 100%;
  flex: 1;
  min-height: 0;
}

/* 标题栏右侧呼吸状态点 */
.mc-live {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #00e676;
  box-shadow: 0 0 6px rgba(0, 230, 118, 0.9);
  animation: mc-pulse 2s ease-in-out infinite;
  flex: none;
}

@keyframes mc-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.35; transform: scale(0.7); }
}

.metric-items {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  gap: 1px;
}

.metric-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 2px 4px;
  border-radius: 3px;
  transition: background 0.25s;
}

.metric-row:hover {
  background: linear-gradient(90deg, rgba(34, 192, 255, 0.14), transparent);
}

.metric-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
}

.metric-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 0.3px;
  white-space: nowrap;
}

.metric-subtext {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 1px;
  transform: scale(0.92);
  transform-origin: left center;
  white-space: nowrap;
}

.metric-right {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  flex-shrink: 0;
}

.metric-value {
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.5px;
  margin-right: 2px;
  line-height: 1;
}

.metric-unit {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  font-family: sans-serif;
  margin-right: 1px;
}

.metric-extra-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 2px 0 4px;
}

/* 状态高亮色彩规范 */
.status-normal {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(34, 192, 255, 0.3);
}

.status-success {
  color: #00e676;
  text-shadow: 0 0 8px rgba(0, 230, 118, 0.4);
}

.status-warning {
  color: #ffb300;
  text-shadow: 0 0 8px rgba(255, 179, 0, 0.4);
}

.status-danger {
  color: #ff5252;
  text-shadow: 0 0 8px rgba(255, 82, 82, 0.4);
}

.status-info {
  color: #00e5ff;
  text-shadow: 0 0 8px rgba(0, 229, 255, 0.4);
}
</style>
