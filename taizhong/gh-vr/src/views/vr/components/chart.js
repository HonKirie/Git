import { reactive, ref, onMounted, onUnmounted } from 'vue'

/**
 * 图表自适应尺寸：监听容器尺寸变化，返回实际像素宽高
 * 配合 SVG 使用，避免 preserveAspectRatio 拉伸导致的文字/描边变形
 */
export function useChartSize() {
  const el = ref(null)
  const size = reactive({ w: 0, h: 0 })
  let ro = null

  onMounted(() => {
    if (!el.value) return
    const update = () => {
      size.w = el.value.clientWidth
      size.h = el.value.clientHeight
    }
    update()
    ro = new ResizeObserver(update)
    ro.observe(el.value)
  })

  onUnmounted(() => {
    if (ro) ro.disconnect()
  })

  return { el, size }
}

/**
 * Catmull-Rom 样条 → 平滑贝塞尔路径（首尾保持直线段）
 * @param {Array<[number, number]>} pts 折线点集
 * @returns {string} SVG path d
 */
export function smoothPath(pts) {
  if (pts.length < 2) return ''
  let d = `M ${pts[0][0]},${pts[0][1]}`
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[i - 1] || pts[i]
    const p1 = pts[i]
    const p2 = pts[i + 1]
    const p3 = pts[i + 2] || p2
    const c1x = p1[0] + (p2[0] - p0[0]) / 6
    const c1y = p1[1] + (p2[1] - p0[1]) / 6
    const c2x = p2[0] - (p3[0] - p1[0]) / 6
    const c2y = p2[1] - (p3[1] - p1[1]) / 6
    d += ` C ${c1x.toFixed(2)},${c1y.toFixed(2)} ${c2x.toFixed(2)},${c2y.toFixed(2)} ${p2[0].toFixed(2)},${p2[1].toFixed(2)}`
  }
  return d
}
