<template>
  <div class="box" :style="boxStyle">
    <!-- 标题栏：32px 高，vrTtile.png 做背景；左侧标题文字 + 右侧 titleRight 插槽 -->
    <div class="box-title" :style="{ backgroundImage: `url(${vtTitle})` }">
      <span v-if="title" class="box-title-text font-youshehei">{{ title }}</span>
      <slot v-else name="title" />
      <div v-if="$slots.titleRight" class="box-title-right">
        <slot name="titleRight" />
      </div>
    </div>

    <!-- body 区域：默认插槽，背景色可通过 bgColor 传入 -->
    <div class="box-body" :style="bgColor ? { backgroundColor: bgColor } : {}">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import vtTitle from '@/assets/vrTtile.png'

defineOptions({ name: 'Box' })

const props = defineProps({
  // 标题文字
  title: { type: String, default: '' },
  // 组件宽度，默认 100%（传数字时按 px 处理）
  width: { type: [String, Number], default: '100%' },
  // 组件总高度，默认 100%（传数字时按 px 处理）
  height: { type: [String, Number], default: '100%' },
  // body 区域背景色
  bgColor: { type: String, default: '#0330813d' },
})

const boxStyle = computed(() => {
  const w = typeof props.width === 'number' ? `${props.width}px` : props.width
  const h = typeof props.height === 'number' ? `${props.height}px` : props.height
  return { width: w, height: h }
})
</script>

<style scoped>
/* 整体：flex 纵向布局，无外部边框及背景色 */
.box {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
}

/* 标题栏：固定 32px 高，背景图拉伸铺满 */
.box-title {
  flex: none;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-sizing: border-box;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: left center;
}

.box-title-text {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
  padding-left: 20px;
}

.box-title-right {
  flex: none;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* body：与标题栏间距，内边距自适应排布 */
.box-body {
  flex: 1;
  min-height: 0;
  margin-top: 8px;
  padding: 4px 8px;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
}
</style>
