<template>
  <Box title="监控安防" height="auto" class="vr-panel monitoring">
    <div class="mv-body">
      <!-- 主画面（16:9，当前选中的摄像头） -->
      <div class="mv-video">
        <img class="mv-img" :src="activeCamera.img" :alt="activeCamera.name" />
        <div class="mv-tag">● LIVE</div>
        <div class="mv-cam">{{ activeCamera.name }}</div>
      </div>

      <!-- 摄像头列表（测试数据：厂区大门 / 配电房 / 污水池，点击切换主画面） -->
      <div class="mv-list">
        <div
          v-for="cam in cameras"
          :key="cam.name"
          class="mv-thumb"
          :class="{ active: cam.name === activeCamera.name }"
          @click="activeIndex = cameras.indexOf(cam)"
        >
          <img class="mv-thumb-img" :src="cam.img" :alt="cam.name" />
          <span class="mv-thumb-name">{{ cam.name }}</span>
        </div>
      </div>
    </div>
  </Box>
</template>

<script setup>
import { ref, computed } from 'vue'
import Box from '@/components/Box.vue'
import pic1 from '@/assets/test/pic1.png'
import pic2 from '@/assets/test/pic2.png'
import pic3 from '@/assets/test/pic3.png'

// 摄像头列表（测试数据，暂定三个机位；对接真实接口时替换为摄像头列表 + 流地址）
const cameras = [
  { name: '厂区大门', img: pic1 },
  { name: '配电房', img: pic2 },
  { name: '污水池', img: pic3 },
]

const activeIndex = ref(0)
const activeCamera = computed(() => cameras[activeIndex.value])
</script>

<style scoped>
@import './panel.css';

.monitoring {
  flex: none;
}

/* 高度随内容自适应，由 Box height="auto" 配合 aspect-ratio 撑开 */

.mv-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 16:9 主画面：宽度由面板决定，高度按 16:9 等比 */
.mv-video {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 3px;
  overflow: hidden;
  background: #000;
  border: 1px solid rgba(156, 197, 255, 0.18);
}

.mv-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 实时角标 */
.mv-tag {
  position: absolute;
  top: 6px;
  right: 8px;
  padding: 2px 8px;
  font-size: 10px;
  letter-spacing: 1px;
  color: #ff5d5d;
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 93, 93, 0.5);
  border-radius: 10px;
  animation: mv-blink 1.6s ease-in-out infinite;
}

/* 机位名称 */
.mv-cam {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 14px 8px 4px;
  font-size: 11px;
  color: #fff;
  text-align: center;
  letter-spacing: 1px;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.72));
}

/* 摄像头缩略图列表 */
.mv-list {
  display: flex;
  gap: 6px;
}

.mv-thumb {
  position: relative;
  flex: 1;
  min-width: 0;
  height: 34px;
  border-radius: 3px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(156, 197, 255, 0.16);
  background: #000;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.mv-thumb:hover {
  border-color: rgba(34, 192, 255, 0.6);
}

.mv-thumb.active {
  border-color: #22c0ff;
  box-shadow: 0 0 6px rgba(34, 192, 255, 0.5);
}

.mv-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0.75;
}

.mv-thumb.active .mv-thumb-img {
  opacity: 1;
}

.mv-thumb-name {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 6px 2px 2px;
  font-size: 9px;
  color: #fff;
  text-align: center;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.78));
  white-space: nowrap;
  overflow: hidden;
}

@keyframes mv-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.45; }
}
</style>
