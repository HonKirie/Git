
<template>
  <div class="vr-container">
    <!-- Five 渲染容器（透明渲染层 + 背景图） -->
    <div
      ref="containerRef"
      class="vr-scene"
      :style="{ backgroundImage: `url(${bgAr})` }"
    />

    <!-- ============ 大屏布局 ============ -->

    <!-- 顶部：系统名称 -->
    <header class="vr-top" >
      <h1 class="vr-top-title font-youshehei" :style="{ backgroundImage: `url(${bgTop})` }">泰中特种纸数字孪生系统</h1>
    </header>

    <!-- 左侧列：数据概览 / 水质指标趋势 / 污水处理率 -->
    <aside
      class="vr-left"
      v-show="!state.loading && !state.error"
    >
      <DataOverview />
      <WaterTrend />
      <SewageRate />
    </aside>

    <!-- 右侧列：告警处理状态 / 监控安防 / 告警动态 -->
    <aside
      class="vr-right"
      v-show="!state.loading && !state.error"
    >
      <AlarmStatus />
      <MonitoringVideo />
      <AlarmList />
    </aside>

    <!-- 底部：导览图轮播（参考附件 2），左联当前厂区切换、右带自动导览开关 -->
    <GuideCarousel
      v-if="!state.loading && !state.error"
      class="vr-carousel"
      :guides="guideList"
      :active-index="activeGuideIndex"
      :project-name="currentProject.label || currentProject.name"
      :factories="vrProList"
      :current-vr-id="currentVrID"
      :autoplay="autoPlay"
      @select="goToGuide"
      @select-factory="selectProject"
      @toggle-autoplay="toggleAutoPlay"
    />

    <!-- 加载遮罩 + 进度条 -->
    <div v-if="state.loading" class="loading-mask">
      <div class="loading-content">
        <h2 class="loading-title">{{ currentProject.name || 'VR 全景' }}</h2>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: state.progress + '%' }" />
        </div>
        <p class="progress-text">{{ state.progressText || '准备中...' }}</p>
      </div>
    </div>

    <!-- 错误遮罩 -->
    <div v-if="state.error" class="error-mask">
      <div class="error-content">
        <h2>加载失败</h2>
        <p>{{ state.error }}</p>
        <button class="retry-btn" @click="loadScene">重试</button>
      </div>
    </div>

    <!-- 模式切换（右上角）：3D 模型 / 全景漫游 单开关 -->
    <div class="mode-switch" v-if="!state.loading && !state.error">
      <span
        class="ms-item"
        :class="{ active: currentMode === 'Floorplan' }"
        @click="setMode('Floorplan')"
      >3D 模型</span>
      <span class="ms-slider" :class="{ right: currentMode === 'Panorama' }" />
      <span
        class="ms-item"
        :class="{ active: currentMode === 'Panorama' }"
        @click="setMode('Panorama')"
      >全景漫游</span>
    </div>

    <!-- 返回地球按钮（左上角） -->
    <button class="back-earth-btn" @click="backToEarth">
      <i class="bc bc-tl"></i><i class="bc bc-tr"></i><i class="bc bc-bl"></i><i class="bc bc-br"></i>
      <span class="bc-icon">
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
          <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.3" />
          <ellipse cx="12" cy="12" rx="4" ry="9" fill="none" stroke="currentColor" stroke-width="0.9" opacity="0.7" />
          <line x1="3" y1="12" x2="21" y2="12" stroke="currentColor" stroke-width="0.9" opacity="0.7" />
        </svg>
      </span>
      <span class="bc-text">返回地球</span>
      <span class="bc-scan"></span>
    </button>
    <!-- 热点标签悬浮窗 -->
    <Transition name="tag-popup">
      <div
        v-if="tagPopup.visible"
        ref="tagPopupRef"
        class="tag-popup"
        @pointerdown.stop
        @click.stop
        @wheel.stop
        :style="{ left: tagPopup.x + 'px', top: tagPopup.y + 'px' }"
      >
        <button class="tag-popup-close" @click="closeTagPopup">✕</button>
        <div class="tag-popup-header"
          title="拖动调整位置"
          @pointerdown.stop.prevent="startPopupDrag"
          @pointermove="movePopupDrag"
          @pointerup="endPopupDrag"
          @pointercancel="endPopupDrag"
          @lostpointercapture="endPopupDrag"
        >
          <span class="tag-popup-icon">📍</span>
          <div>
            <div class="tag-popup-label">节点名称</div>
            <div class="tag-popup-name">{{ tagPopup.card.name }}</div>
          </div>
        </div>
        <div class="tag-popup-id">设备编号：{{ tagPopup.deviceId }}</div>
        <div class="tag-popup-divider"></div>
        <div class="tag-popup-row">
          <span class="tag-popup-status-dot" :style="{ background: tagPopup.card.statusColor, boxShadow: `0 0 6px ${tagPopup.card.statusColor}` }"></span>
          <span class="tag-popup-status-text">设备状态：<strong :style="{ color: tagPopup.card.statusColor }">{{ tagPopup.card.status }}</strong></span>
        </div>
        <div class="tag-popup-divider"></div>
        <div class="tag-popup-metrics">
          <div v-for="metric in tagPopup.card.metrics" :key="metric.label" class="tag-popup-metric">
            <span class="tag-popup-metric-icon">{{ metric.icon }}</span>
            <span class="tag-popup-metric-label">{{ metric.label }}</span>
            <span class="tag-popup-metric-value">{{ metric.value }}</span>
            <span v-if="metric.note" class="tag-popup-metric-set">{{ metric.note }}</span>
          </div>
        </div>
        <div class="tag-popup-divider"></div>
        <div class="tag-popup-wash">
          <span>⏱️</span> {{ tagPopup.card.maintenanceLabel || '维护状态' }}：<strong>{{ tagPopup.card.maintenance }}</strong>
        </div>
        <div v-if="tagPopup.card.washStatus" class="tag-popup-wash">
          冲洗状态：{{ tagPopup.card.washStatus }}
        </div>
      </div>
    </Transition>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, onUnmounted, reactive, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Five, parseWork } from '@realsee/five'
import { PanoTagPlugin } from '@realsee/dnalogel'
import config from '@/config'
import { deviceCards } from '@/data/deviceCards'
import { syncTagPointStyle, getTagDeviceId } from '@/utils/vrTags'
import { ensureVrToken, fetchVrInfo, fetchVrHotTags, fetchVrObserverPictures } from '@/api/vr'
// 大屏素材
import bgAr from '@/assets/bgAr.jpg'
import bgTop from '@/assets/vrTop.png'
// 面板组件
import DataOverview from './components/DataOverview.vue'
import WaterTrend from './components/WaterTrend.vue'
import SewageRate from './components/SewageRate.vue'
import AlarmStatus from './components/AlarmStatus.vue'
import MonitoringVideo from './components/MonitoringVideo.vue'
import AlarmList from './components/AlarmList.vue'
import GuideCarousel from './components/GuideCarousel.vue'

const route = useRoute()
const router = useRouter()

const proList = config.proList || []

// VR 下拉只展示有 VR 场景的项目（无 vrID 的点位如东平县污水处理厂不进入下拉）
const vrProList = computed(() => proList.filter(item => item.vrID))

// 从路由参数获取 vrID
const currentVrID = computed(() => route.params.vrID || '')

// 当前选中的项目对象
const currentProject = computed(() => {
  return proList.find(item => item.vrID === currentVrID.value) || {}
})

// Five 渲染容器
const containerRef = ref(null)
let five = null

// Five 模式：Panorama=全景漫游 / Floorplan=空间总览
// 默认进入模式：空间总览（3D 模型），即进入 VR 后直接展示模型俯视视图
const DEFAULT_MODE = 'Floorplan'
const currentMode = ref(DEFAULT_MODE)

// 页面状态
const state = reactive({
  loading: true,
  progress: 0,       // 0-100 加载进度
  progressText: '',  // 进度文字
  error: '',
})

// 热点标签悬浮窗
const tagPopup = reactive({
  visible: false,
  deviceId: '',
  card: deviceCards['17D526QT01'],
  x: 0,
  y: 0,
})
const tagPopupRef = ref(null)
let popupDrag = null
let latestView = {}
let popupView = null

function constrainPopup(x = tagPopup.x, y = tagPopup.y) {
  const el = tagPopupRef.value
  if (!el) return
  const margin = 12
  tagPopup.x = Math.max(margin, Math.min(x, window.innerWidth - el.offsetWidth - margin))
  tagPopup.y = Math.max(margin, Math.min(y, window.innerHeight - el.offsetHeight - margin))
}

async function openTagPopup(deviceId, tag) {
  popupView = { ...latestView }
  const point = tag?.screenPosition
  const bounds = containerRef.value?.getBoundingClientRect()
  const anchor = point && bounds
    ? { x: bounds.left + point.leftPx, y: bounds.top + point.topPx }
    : { ..._mousePos }
  tagPopup.card = deviceCards[deviceId]
  tagPopup.deviceId = tagPopup.card.deviceId || deviceId
  tagPopup.visible = true
  await nextTick()
  if (!tagPopup.visible || !tagPopupRef.value) return
  const el = tagPopupRef.value
  // 以设备圆点为锚点展开，优先放在上方并约束到当前视口。
  const above = anchor.y - el.offsetHeight - 16
  constrainPopup(anchor.x - el.offsetWidth / 2, above >= 12 ? above : anchor.y + 16)
}

function startPopupDrag(event) {
  if (event.button !== 0 || !event.isPrimary) return
  popupDrag = { id: event.pointerId, x: event.clientX - tagPopup.x, y: event.clientY - tagPopup.y }
  event.currentTarget.setPointerCapture(event.pointerId)
}
function movePopupDrag(event) {
  if (popupDrag?.id !== event.pointerId) return
  constrainPopup(event.clientX - popupDrag.x, event.clientY - popupDrag.y)
}
function endPopupDrag(event) {
  if (popupDrag?.id !== event.pointerId) return
  popupDrag = null
  if (event.currentTarget.hasPointerCapture(event.pointerId)) {
    event.currentTarget.releasePointerCapture(event.pointerId)
  }
}

// 鼠标和触摸均使用视口坐标定位浮窗。
const _mousePos = { x: 0, y: 0 }
function _onMouseMove(e) {
  _mousePos.x = e.clientX
  _mousePos.y = e.clientY
}
function closeTagPopup() {
  popupView = null
  popupDrag = null
  tagPopup.visible = false
}

// 导览列表
const guideList = ref([])     // 当前 VR 的导览列表

// 当前 Five 点位索引（由 stateChange 同步）→ 底部轮播高亮
const currentPanoIndex = ref(0)
const activeGuideIndex = computed(() => {
  const i = guideList.value.findIndex(
    (g) => (g.pano_index ?? g.data?.pano_index) === currentPanoIndex.value
  )
  return i >= 0 ? i : 0
})

// 切换厂区/项目（通过路由跳转；底部轮播「当前厂区」下拉触发）
function selectProject(item) {
  closeTagPopup()
  stopAutoPlay()
  if (item.vrID === currentVrID.value) return
  currentMode.value = DEFAULT_MODE
  router.push(`/vr/${item.vrID}`)
}

// 返回地球
function backToEarth() {
  router.push('/')
}

// 切换 Five 模式（右上角 3D 模型 / 全景漫游 开关）
function setMode(mode) {
  closeTagPopup()
  if (five) five.setState({ mode })
}

/**
 * 加载 VR 场景
 * 流程：鉴权 → 拉取 VR 数据 → parseWork → five.load（带进度监听）
 */
async function loadScene() {
  if (!containerRef.value || !currentVrID.value) return

  closeTagPopup()
  stopAutoPlay()
  state.loading = true
  state.progress = 0
  state.progressText = ''
  state.error = ''

  try {
    // 1. 鉴权
    state.progressText = '正在鉴权...'
    await ensureVrToken()

    // 2. 拉取 VR 场景数据
    state.progressText = '正在获取场景数据...'
    const workData = await fetchVrInfo(currentVrID.value)

    // 切换 VR：清空旧导览列表与旧标签，再销毁旧 Five 实例
    guideList.value = []
    currentPanoIndex.value = 0
    five?.plugins?.panoTagPlugin?.clearTags()
    five?.dispose()

    // 4. 创建新 Five 实例并加载
    five = new Five({
      plugins: [
        [PanoTagPlugin, 'panoTagPlugin', {}],
      ],
      backgroundAlpha: 0, // 初始化即透明（配合 CSS 背景图）
      poweredByRealsee: false, // 关闭 SDK 自带的 "Powered by 如视" 水印
    })
    five.backgroundAlpha = 0 // 兜底：确保渲染层透明
    five.appendTo(containerRef.value)

    // 按后台标题精确匹配设备卡片。
    five.plugins.panoTagPlugin.hooks.on('click', (params) => {
      const tag = params?.tag
      console.log('[Tag] 点击标签:', {
        target: params?.target,
        id: tag?.id,
        contentType: tag?.contentType,
        pointType: tag?.pointType,
        dimensionType: tag?.dimensionType,
        data: tag?.data,
      })
      closeTagPopup()
      const deviceId = getTagDeviceId(tag)
      if (!Object.hasOwn(deviceCards, deviceId)) return
      openTagPopup(deviceId, tag)
    })

    const work = parseWork(workData)

    // 5. 在 Five 实例上监听加载进度
    const offProgress = five.on('progress', (loaded, total) => {
      const pct = total > 0 ? Math.round((loaded / total) * 100) : 0
      state.progress = Math.min(pct, 99)
      state.progressText = `加载中 ${state.progress}%`
    })

    // 6. 监听 Five stateChange（模式变化 → 同步 UI；点位变化 → 轮播高亮，贯穿整个会话不清理）
    latestView = {}
    five.on('stateChange', (s) => {
      // 比较打开时的视角，过滤浮点抖动；拖动浮窗本身不改变 Five 视角。
      latestView = { ...latestView, ...s }
      if (tagPopup.visible && popupView) {
        const moved = ['longitude', 'latitude', 'fov'].some((key) =>
          popupView[key] != null && latestView[key] != null &&
          Math.abs(latestView[key] - popupView[key]) > 0.0001
        )
        const switched = ['panoIndex', 'mode'].some((key) =>
          popupView[key] != null && latestView[key] !== popupView[key]
        )
        if (moved || switched) closeTagPopup()
      }
      if (s.mode) currentMode.value = s.mode
      if (s.panoIndex != null) currentPanoIndex.value = s.panoIndex
    })

    // 6.1 监听用户手势交互（拖拽/点击/缩放/滚轮）→ 自动关闭自动导览
    const gestureEvents = ['gesture.pan', 'gesture.tap', 'gesture.pinch', 'gesture.mousewheel']
    gestureEvents.forEach((evt) => {
      five.on(evt, () => {
        if (autoPlay.value) {
          console.log('[AutoPlay] 检测到手势交互，自动关闭导览')
          stopAutoPlay()
        }
      })
    })

    // 6.2 监听点位切换：非自动导览触发的（如用户点击走点箭头）→ 自动关闭导览
    five.on('pano.willChange', () => {
      if (autoPlay.value && !isAutoNavigating) {
        console.log('[AutoPlay] 检测到手动切换点位，自动关闭导览')
        stopAutoPlay()
      }
    })

    // 7. 加载完成
    five.load(work).then(async () => {
      state.progress = 100
      state.progressText = '加载完成'
      state.loading = false
      offProgress?.()
      // 8. 默认进入空间总览（3D 模型视图）
      five.setState({ mode: DEFAULT_MODE })
      // 9. 拉取热点标签与导览列表（并行）
      await Promise.all([loadTags(), loadGuides()])
    }).catch((err) => {
      state.error = err?.message || '场景加载失败'
      state.loading = false
      offProgress?.()
      console.error('[VR] five.load 失败:', err)
    })
  } catch (err) {
    state.error = err?.message || '场景加载失败'
    state.loading = false
    console.error('[VR] 加载失败:', err)
  }
}

const onResize = () => {
  five?.refresh()
  constrainPopup()
}

/**
 * 生成今日日期及星期几，如：2026年9月14日 星期一
 */
function formatToday() {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week}`
}

/**
 * 按业务需求调整标签：
 * 0. 统一：仅当前点位可见（visiblePanoIndex='current'）→ 切换点位自动隐藏，进入点位才显示对应标签
 * 1. 图片类标签（ImageText/Image/MediaPlane）→ 常展开直接显示
 * 2. Text 标签且 title=泰中特种纸业 → 内容替换为今日日期，并以浮窗（popover）展示
 */
function transformTags(tags) {
  const dateText = formatToday()
  const imageTypes = ['ImageText', 'Image', 'MediaPlane']
  const tagList = (tags?.tagList || []).map((sourceTag) => {
    const tag = syncTagPointStyle(sourceTag)
    // 强制按点位显示/隐藏（后台部分标签 visiblePanoIndex 为 'all'，会导致切换点位后标签叠加常显）
    const config = {
      ...tag.config,
      visibleConfig: {
        ...tag.config?.visibleConfig,
        visiblePanoIndex: 'current',
      },
      pointConfig: {
        ...tag.config?.pointConfig,
      },
    }
    // 1. 图片标签直接显示（保持展开，不收起为小圆点）
    if (imageTypes.includes(tag.contentType)) {
      return {
        ...tag,
        config: { ...config, unfoldedConfig: { keep: 'unfolded' } },
      }
    }
    // 2. 文本标签 title=泰中特种纸业 → 浮窗显示今日日期及星期几
    if (tag.contentType === 'Text' && tag.data?.title === '泰中特种纸业') {
      return {
        ...tag,
        data: { ...tag.data, title: dateText, text: dateText },
        config: { ...config, popoverConfig: { enabled: true, trigger: 'click' } },
      }
    }
    // 其余标签默认收起为圆点，不显示文字
    return { ...tag, config: { ...config, unfoldedConfig: { keep: 'folded' } } }
  })
  return { ...tags, tagList }
}

/**
 * 加载并渲染热点标签
 * hot/list 返回的 data（tagList/globalConfig/contentTypeConfig）可直接传给 PanoTagPlugin.load()
 * 标签失败不影响全景渲染，仅打印警告
 */
async function loadTags() {
  const vrID = currentVrID.value
  try {
    const tags = await fetchVrHotTags(vrID)
    const plugin = five?.plugins?.panoTagPlugin
    // 请求期间已切换厂区 → 丢弃本次结果，避免旧标签渲染到新场景
    if (!plugin || currentVrID.value !== vrID) return
    plugin.clearTags() // 重新创建前清空（与切换时的清理双保险）
    await plugin.load(transformTags(tags))
    console.log('[Tag] 热点标签已加载:', tags?.tagList?.length ?? 0, '个')
  } catch (err) {
    console.warn('[VR] 热点标签加载失败:', err)
  }
}

/**
 * 加载当前 VR 的点位导览列表
 * observerpicture/search 返回 data.list[]，切换 VR 时先清空再加载
 */
async function loadGuides() {
  const vrID = currentVrID.value
  try {
    const data = await fetchVrObserverPictures(vrID)
    // 请求期间已切换厂区 → 丢弃本次结果，避免旧导览渲染到新场景
    if (currentVrID.value !== vrID) return
    guideList.value = data?.list || []
    console.log('[Guide] 导览列表已加载:', guideList.value.length, '个')
  } catch (err) {
    console.warn('[VR] 导览列表加载失败:', err)
    guideList.value = []
  }
}

/**
 * 点击导览项：快速定位到目标点位（底部轮播复用）
 * 朝向使用导览项的 data.longitude / data.latitude（弧度），fov 可选
 * 注意：data.heading 恒为 0（无效字段），不可用作 longitude
 */
function goToGuide(item) {
  closeTagPopup()
  if (!five) return
  const panoIndex = item.pano_index ?? item.data?.pano_index
  if (panoIndex == null) return
  const { longitude, latitude, fov } = item.data || {}
  // 点击导览：先切换到全景漫游模式，再定位到对应点位
  five.setState({
    mode: 'Panorama',
    panoIndex,
    ...(longitude != null ? { longitude } : {}),
    ...(latitude != null ? { latitude } : {}),
    ...(fov ? { fov } : {}),
  })
  console.log('[Guide] 切换全景漫游并定位到导览项:', { name: item.name, panoIndex, longitude, latitude, fov })
}

// ============ 自动导览 ============
const autoPlay = ref(false)
let autoPlayTimer = null
let isAutoNavigating = false // 标记本次点位切换是否由自动导览触发
const AUTO_PLAY_INTERVAL = 5000 // 每个点位停留 5 秒

function clearAutoTimer() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

function toggleAutoPlay() {
  autoPlay.value = !autoPlay.value
  if (autoPlay.value) {
    startAutoPlay()
  } else {
    stopAutoPlay()
  }
}

function startAutoPlay() {
  clearAutoTimer()
  if (!guideList.value.length || guideList.value.length < 2) return
  autoPlayTimer = setInterval(() => {
    const next = (activeGuideIndex.value + 1) % guideList.value.length
    const item = guideList.value[next]
    if (item) {
      isAutoNavigating = true
      goToGuide(item)
      // 切换动画结束后重置标志位（1.5s 足够覆盖点位过渡）
      setTimeout(() => { isAutoNavigating = false }, 1500)
    }
  }, AUTO_PLAY_INTERVAL)
  console.log('[AutoPlay] 自动导览已启动，间隔', AUTO_PLAY_INTERVAL, 'ms')
}

function stopAutoPlay() {
  clearAutoTimer()
  isAutoNavigating = false
  autoPlay.value = false
}

// 路由参数变化 → 重新加载
watch(currentVrID, () => loadScene())

onMounted(() => {
  window.addEventListener('resize', onResize)
  window.addEventListener('pointerdown', _onMouseMove, true)
  loadScene()
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('pointerdown', _onMouseMove, true)
  popupDrag = null
  stopAutoPlay()
  // 离开 VR 页：清空标签后再销毁实例
  five?.plugins?.panoTagPlugin?.clearTags()
  five?.dispose()
  five = null
})
</script>


<style scoped>
.vr-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: #0b0f19;
}

.vr-scene {
  width: 100%;
  height: 100%;
  overflow: hidden;
  /* 背景图铺满（配合 five.backgroundAlpha = 0 的透明渲染层） */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* ============ 大屏布局 ============ */

/* 顶部系统名称 */
.vr-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 78px;
  z-index: 40;
  pointer-events: none;
  background: linear-gradient(180deg, #141c2c 36%, #12182399 76%, #141c2c00 98%);
}

.vr-top-bg {
  position: absolute;
  top: 0;
  width: 480px;
  height: 48px;
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-position: center;
  opacity: 0.92;
}

.vr-top-title {
  position: relative;
  margin: 10px 0 0;
  font-size: 28px;
  font-weight: 600;
  line-height: 78px;
  letter-spacing: 4px;
  color: #fff;
  text-align: center;
  background-position: bottom center;
  background-size: cover;
  background-repeat: no-repeat;
  text-shadow: 0 0 18px rgba(34, 192, 255, 0.75), 0 2px 6px rgba(0, 0, 0, 0.6);
}

/* 左右两列面板容器 */
.vr-left,
.vr-right {
  position: absolute;
  top: 88px;
  bottom: 20px;
  width: 340px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 30;
}

.vr-left {
  left: 24px;
}

.vr-right {
  right: 24px;
}

/* 底部导览轮播 */
.vr-carousel {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: 18px;
  z-index: 35;
}

/* 加载遮罩 */
.loading-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(11, 15, 25, 0.9);
  z-index: 100;
  transition: opacity 0.3s ease;
}

.loading-content {
  text-align: center;
  color: #f1f5f9;
  padding: 40px 60px;
}

.loading-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
}

.progress-bar {
  width: 280px;
  height: 6px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
  overflow: hidden;
  margin: 0 auto 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
  font-family: monospace;
}

/* 错误遮罩 */
.error-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(11, 15, 25, 0.92);
  z-index: 100;
}

.error-content {
  text-align: center;
  color: #f1f5f9;
  padding: 40px 60px;
}

.error-content h2 {
  margin: 0 0 12px 0;
  font-size: 22px;
  font-weight: 600;
  color: #ef4444;
}

.error-content p {
  margin: 0 0 20px 0;
  color: #94a3b8;
  font-size: 14px;
}

.retry-btn {
  padding: 8px 28px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 20px;
  border: 1px solid #3b82f6;
  background: transparent;
  color: #60a5fa;
  cursor: pointer;
  transition: all 0.2s ease;
}

.retry-btn:hover {
  background: #3b82f6;
  color: #fff;
}

/* 模式切换（右上角）：3D 模型 / 全景漫游 分段开关 */
.mode-switch {
  position: absolute;
  top: 12px;
  right: 24px;
  display: flex;
  align-items: center;
  padding: 3px;
  border: 1px solid rgba(34, 192, 255, 0.4);
  border-radius: 18px;
  background: rgba(6, 26, 51, 0.75);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 0 14px rgba(34, 192, 255, 0.18);
  z-index: 50;
}

.ms-item {
  position: relative;
  z-index: 2;
  min-width: 88px;
  padding: 6px 16px;
  font-size: 12px;
  letter-spacing: 1px;
  text-align: center;
  color: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  border-radius: 15px;
  transition: color 0.3s ease;
  user-select: none;
  white-space: nowrap;
}

.ms-item:hover {
  color: #cfeaff;
}

.ms-item.active {
  color: #ffffff;
  text-shadow: 0 0 8px rgba(34, 192, 255, 0.9);
}

.ms-slider {
  position: absolute;
  top: 3px;
  left: 3px;
  width: calc(50% - 3px);
  height: calc(100% - 6px);
  border-radius: 15px;
  background: linear-gradient(135deg, rgba(34, 192, 255, 0.38), rgba(34, 192, 255, 0.12));
  border: 1px solid rgba(34, 192, 255, 0.55);
  box-shadow: 0 0 12px rgba(34, 192, 255, 0.35);
  transition: transform 0.3s ease;
  z-index: 1;
}

.ms-slider.right {
  transform: translateX(100%);
}
/* 返回地球按钮（左上角，科技感样式） */
.back-earth-btn {
  position: absolute;
  top: 12px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 22px 9px 14px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 2px;
  color: #9fd9ff;
  background: linear-gradient(135deg, rgba(34, 192, 255, 0.16), rgba(6, 26, 51, 0.72) 45%, rgba(6, 26, 51, 0.72));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 0 18px rgba(34, 192, 255, 0.22), inset 0 0 16px rgba(34, 192, 255, 0.08);
  border: 1px solid rgba(34, 192, 255, 0.45);
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  z-index: 50;
  overflow: hidden;
}

.back-earth-btn:hover {
  color: #ffffff;
  border-color: #22c0ff;
  box-shadow: 0 0 26px rgba(34, 192, 255, 0.5), inset 0 0 20px rgba(34, 192, 255, 0.16);
  transform: translateY(-1px);
}

.back-earth-btn:active {
  transform: translateY(0);
}

/* 四角括号 */
.bc {
  position: absolute;
  width: 8px;
  height: 8px;
  pointer-events: none;
}

.bc-tl { top: -1px; left: -1px; border-top: 2px solid #22c0ff; border-left: 2px solid #22c0ff; }
.bc-tr { top: -1px; right: -1px; border-top: 2px solid #22c0ff; border-right: 2px solid #22c0ff; }
.bc-bl { bottom: -1px; left: -1px; border-bottom: 2px solid #22c0ff; border-left: 2px solid #22c0ff; }
.bc-br { bottom: -1px; right: -1px; border-bottom: 2px solid #22c0ff; border-right: 2px solid #22c0ff; }

/* 地球图标：缓慢自转光环 */
.bc-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  color: #22c0ff;
  flex: none;
  filter: drop-shadow(0 0 4px rgba(34, 192, 255, 0.8));
  animation: bc-spin 6s linear infinite;
}

.bc-text {
  position: relative;
  z-index: 1;
}

/* 底部流光扫描线 */
.bc-scan {
  position: absolute;
  left: -40%;
  bottom: 1px;
  width: 30%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #22c0ff, transparent);
  filter: drop-shadow(0 0 4px #22c0ff);
  animation: bc-scan 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes bc-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes bc-scan {
  0% { left: -40%; opacity: 0; }
  30% { opacity: 1; }
  100% { left: 110%; opacity: 0; }
}

/* 热点标签悬浮窗 */
.tag-popup {
  position: fixed;
  z-index: 200;
  width: 320px;
  max-width: calc(100vw - 24px);
  max-height: calc(100dvh - 24px);
  overflow-y: auto;
  background: rgba(6, 18, 38, 0.92);
  border: 1px solid rgba(34, 192, 255, 0.45);
  border-radius: 10px;
  padding: 16px 18px 14px;
  color: #d4eeff;
  font-size: 13px;
  line-height: 1.6;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: 0 0 28px rgba(34, 192, 255, 0.2), 0 8px 32px rgba(0, 0, 0, 0.55);
  pointer-events: auto;
}

.tag-popup-close {
  position: absolute;
  top: 8px;
  right: 10px;
  background: none;
  border: none;
  color: rgba(255,255,255,0.4);
  font-size: 13px;
  cursor: pointer;
  padding: 2px 6px;
  line-height: 1;
  transition: color 0.2s;
}
.tag-popup-close:hover { color: #fff; }

.tag-popup-header {
  cursor: grab;
  touch-action: none;
  user-select: none;
  padding-right: 20px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 4px;
}
.tag-popup-header:active { cursor: grabbing; }
.tag-popup-icon { font-size: 18px; line-height: 1.4; }
.tag-popup-label { font-size: 11px; color: rgba(34,192,255,0.7); letter-spacing: 1px; }
.tag-popup-name { font-size: 15px; font-weight: 600; color: #fff; letter-spacing: 0.5px; }
.tag-popup-id { font-size: 12px; color: rgba(212,238,255,0.6); margin: 2px 0 0 28px; }

.tag-popup-divider {
  height: 1px;
  background: rgba(34,192,255,0.18);
  margin: 10px 0;
}

.tag-popup-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.tag-popup-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 6px #22c55e;
  flex-shrink: 0;
}
.tag-popup-status-text { font-size: 13px; color: #d4eeff; }
.tag-popup-status-text strong { color: #4ade80; }

.tag-popup-metrics { display: flex; flex-direction: column; gap: 6px; }
.tag-popup-metric {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.tag-popup-metric-icon { font-size: 15px; width: 20px; text-align: center; flex-shrink: 0; }
.tag-popup-metric-label { color: rgba(212,238,255,0.65); min-width: 60px; }
.tag-popup-metric-value { color: #fff; font-weight: 600; font-variant-numeric: tabular-nums; }
.tag-popup-metric-set { font-size: 11px; color: rgba(212,238,255,0.45); margin-left: 4px; }

.tag-popup-wash {
  font-size: 12px;
  color: rgba(212,238,255,0.75);
}
.tag-popup-wash strong { color: #facc15; }

.tag-popup-tip {
  margin-top: 8px;
  font-size: 11px;
  color: rgba(212,238,255,0.45);
  border-top: 1px solid rgba(34,192,255,0.1);
  padding-top: 8px;
}

/* 入场动画 */
.tag-popup-enter-active { transition: opacity 0.18s ease, transform 0.18s ease; }
.tag-popup-leave-active { transition: opacity 0.14s ease, transform 0.14s ease; }
.tag-popup-enter-from,
.tag-popup-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
