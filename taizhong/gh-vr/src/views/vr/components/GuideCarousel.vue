<template>
  <div class="gc">
    <!-- 左侧：当前厂区（点击展开厂区列表切换 VR） -->
    <div class="gc-left">
      <button
        class="gc-factory"
        :class="{ open: factoryOpen }"
        @click.stop="factoryOpen = !factoryOpen"
      >
        <span class="gc-factory-label">当前厂区</span>
        <span class="gc-factory-name font-youshehei">{{ projectName || 'VR 全景' }}</span>
        <span class="gc-factory-arrow" :class="{ up: factoryOpen }">▾</span>
      </button>

      <div class="gc-factory-drop" v-if="factoryOpen">
        <button
          v-for="f in factories"
          :key="f.vrID"
          class="gc-factory-item"
          :class="{ active: f.vrID === currentVrID }"
          @click="pickFactory(f)"
        >
          <span class="gc-factory-item-name">{{ f.name }}</span>
          <span class="gc-factory-tag">{{ f.label }}</span>
        </button>
      </div>
    </div>

    <!-- 中间：缩略图轮播条 -->
    <div class="gc-strip">
      <button class="gc-arrow" @click="step(-1)" aria-label="上一张">‹</button>
      <div class="gc-track" ref="trackEl">
        <div
          v-for="(g, i) in guides"
          :key="g.id ?? i"
          class="gc-thumb"
          :class="{ active: i === activeIndex }"
          @click="selectGuide(g)"
        >
          <img
            class="gc-thumb-img"
            :src="g.square_picture_url || g.picture_url"
            :alt="g.name"
            loading="lazy"
          />
          <span class="gc-thumb-name">{{ g.name || '导览点位 ' + (g.pano_index ?? i + 1) }}</span>
        </div>
        <div v-if="!guides.length" class="gc-empty">暂无导览点位</div>
      </div>
      <button class="gc-arrow" @click="step(1)" aria-label="下一张">›</button>
    </div>

    <!-- 右侧：计数 + 自动导览开关 -->
    <div class="gc-right">
      <span class="gc-count">
        <span class="gc-count-cur">{{ (activeIndex % Math.max(guides.length, 1)) + 1 }}</span>
        <span class="gc-count-total"> / {{ guides.length }}</span>
      </span>
      <button class="gc-auto" :class="{ on: autoplay }" @click="emit('toggle-autoplay')">
        <span class="gc-auto-text">自动导览</span>
        <span class="gc-auto-switch"><i /></span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  guides: { type: Array, default: () => [] },
  activeIndex: { type: Number, default: 0 },
  projectName: { type: String, default: '' },
  // 厂区列表（与厂区选择结合）
  factories: { type: Array, default: () => [] },
  currentVrID: { type: String, default: '' },
  // 自动导览开关状态（由父组件管理）
  autoplay: { type: Boolean, default: false },
})
const emit = defineEmits(['select', 'select-factory', 'toggle-autoplay'])

const factoryOpen = ref(false)
const trackEl = ref(null)

/** 手动切换上一个/下一个 */
function step(dir) {
  if (!props.guides.length) return
  const n = props.guides.length
  const next = (((props.activeIndex + dir) % n) + n) % n
  emit('select', props.guides[next])
}

/** 点击缩略图：定位点位并收起厂区下拉 */
function selectGuide(g) {
  factoryOpen.value = false
  emit('select', g)
}

/** 选择厂区：通知父页面切换 VR */
function pickFactory(f) {
  factoryOpen.value = false
  emit('select-factory', f)
}

/** 当前激活缩略图滚动到可视区中部 */
function scrollToActive() {
  const track = trackEl.value
  if (!track) return
  const el = track.children[props.activeIndex]
  if (!el) return
  track.scrollTo({
    left: el.offsetLeft - track.clientWidth / 2 + el.clientWidth / 2,
    behavior: 'smooth',
  })
}

// 点击组件外部时收起厂区下拉
function onDocClick() {
  factoryOpen.value = false
}

watch(() => props.activeIndex, scrollToActive)

onMounted(() => {
  scrollToActive()
  document.addEventListener('click', onDocClick)
})
onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
})
</script>

<style scoped>
/* 底部导览轮播：参考附件 2 的底部缩略图条样式 */
.gc {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 920px;
  max-width: 82vw;
  padding: 10px 14px;
  background: rgba(4, 30, 58, 0.82);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(156, 197, 255, 0.25);
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.45);
}

/* ===== 左侧：当前厂区切换 ===== */
.gc-left {
  flex: none;
  position: relative;
  min-width: 118px;
}

.gc-factory {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  width: 100%;
  padding: 4px 8px;
  border: 1px solid rgba(34, 192, 255, 0.35);
  border-radius: 6px;
  background: rgba(34, 192, 255, 0.08);
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: left;
}

.gc-factory:hover,
.gc-factory.open {
  border-color: #22c0ff;
  background: rgba(34, 192, 255, 0.18);
  box-shadow: 0 0 12px rgba(34, 192, 255, 0.3);
}

.gc-factory-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 2px;
}

.gc-factory-name {
  font-size: 15px;
  color: #fff;
  letter-spacing: 1px;
  max-width: 96px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.gc-factory-arrow {
  position: absolute;
  right: 8px;
  bottom: 10px;
  font-size: 10px;
  color: #22c0ff;
  transition: transform 0.25s ease;
}

.gc-factory-arrow.up {
  transform: rotate(180deg);
}

/* 厂区下拉 */
.gc-factory-drop {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 0;
  min-width: 220px;
  padding: 6px;
  background: rgba(4, 24, 48, 0.94);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(34, 192, 255, 0.4);
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  gap: 2px;
  z-index: 60;
}

.gc-factory-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 10px;
  font-size: 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: rgba(255, 255, 255, 0.75);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.gc-factory-item:hover {
  background: rgba(34, 192, 255, 0.15);
  color: #fff;
}

.gc-factory-item.active {
  background: rgba(34, 192, 255, 0.25);
  color: #fff;
  border: 1px solid rgba(34, 192, 255, 0.5);
}

.gc-factory-item-name {
  overflow: hidden;
  text-overflow: ellipsis;
}

.gc-factory-tag {
  flex: none;
  font-size: 10px;
  color: #22c0ff;
  border: 1px solid rgba(34, 192, 255, 0.4);
  border-radius: 8px;
  padding: 0 6px;
  line-height: 16px;
}

/* ===== 中间轮播条 ===== */
.gc-strip {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.gc-track {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
  padding: 4px 2px;
}

.gc-track::-webkit-scrollbar {
  display: none;
}

.gc-thumb {
  flex: none;
  position: relative;
  width: 72px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.15);
  opacity: 0.65;
  transition: all 0.25s ease;
}

.gc-thumb:hover {
  opacity: 1;
  border-color: rgba(34, 192, 255, 0.7);
}

.gc-thumb.active {
  opacity: 1;
  border-color: #22c0ff;
  box-shadow: 0 0 10px rgba(34, 192, 255, 0.55);
}

.gc-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.gc-thumb-name {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 8px 2px 1px;
  font-size: 8px;
  color: #fff;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.78));
}

.gc-empty {
  width: 100%;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  padding: 12px 0;
}

.gc-arrow {
  flex: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid rgba(156, 197, 255, 0.3);
  background: rgba(255, 255, 255, 0.06);
  color: #9cc5ff;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  padding: 0;
}

.gc-arrow:hover {
  background: rgba(34, 192, 255, 0.25);
  border-color: #22c0ff;
  color: #fff;
}

/* ===== 右侧：计数 + 自动导览开关 ===== */
.gc-right {
  flex: none;
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.gc-count {
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.gc-count-cur {
  font-size: 18px;
  color: #22c0ff;
  text-shadow: 0 0 8px rgba(34, 192, 255, 0.6);
}

.gc-count-total {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 自动导览开关 */
.gc-auto {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.gc-auto-text {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 1px;
  transition: color 0.25s ease;
}

.gc-auto.on .gc-auto-text {
  color: #22c0ff;
}

.gc-auto-switch {
  position: relative;
  width: 34px;
  height: 17px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.25s ease;
}

.gc-auto-switch i {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #8fa8c8;
  transition: all 0.25s ease;
}

.gc-auto.on .gc-auto-switch {
  background: rgba(34, 192, 255, 0.3);
  border-color: #22c0ff;
  box-shadow: 0 0 8px rgba(34, 192, 255, 0.5);
}

.gc-auto.on .gc-auto-switch i {
  left: 19px;
  background: #22c0ff;
  box-shadow: 0 0 6px rgba(34, 192, 255, 0.9);
}
</style>
