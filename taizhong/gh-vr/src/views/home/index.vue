<template>
  <div class="home-container">
    <!-- Cesium 容器 -->
    <div ref="containerRef" class="cesium-container" />
    <div class="cnt-wrapper">
    <!-- 顶部标题容器（高度 90px，bgTop 做背景） -->
    <div class="top-title" >
      <div class="top-title-cnt" :style="{ backgroundImage: `url(${bgTop})` }">
        <!-- 左侧宣传语：泰中特纸，专造好纸 -->
        <div class="slogan slogan-left">
          <span class="slogan-txt">泰中特纸，专造好纸</span>
          <span class="slogan-line" />
          <span class="slogan-gem" />
        </div>

        <h1 class="font-youshehei">泰中特种纸数字孪生系统</h1>

        <!-- 右侧宣传语：特质非凡，纸耀未来 -->
        <div class="slogan slogan-right">
          <span class="slogan-gem" />
          <span class="slogan-line" />
          <span class="slogan-txt">特质非凡，纸耀未来</span>
        </div>
      </div>
    </div>

    <!-- 左侧指标面板（生产运行、设备运行、能源能耗） -->
    <div class="side-panel side-panel-left cnt">
      <div class="cntBody">
        <MetricCard
          v-for="card in leftMetrics"
          :key="card.id"
          :card="card"
        />
      </div>
    </div>

    <!-- 右侧指标面板（仓储库存、安环隐患、水质质量） -->
    <div class="side-panel side-panel-right cnt">
      <div class="cntBody">
        <MetricCard
          v-for="card in rightMetrics"
          :key="card.id"
          :card="card"
        />
      </div>
    </div>

    <!-- 底部容器（38px 渐变 + 左中右三图） -->
    <div class="bottom-bar">
      <img class="bottom-left" :src="bgBottomAniLeft" alt="" />
      <img class="bottom-center" :src="bgBottom" alt="" />
      <img class="bottom-right" :src="bgBottomAniRight" alt="" />
      <!-- 底部中间导航框：420 × 112，水平居中，贴底 -->
      <div class="bottom-nav">
        <!-- 左侧图标盒子 50×50：距左 128px，距顶 38px，距底 24px -->
        <div class="nav-box nav-left">
          <img :src="bottomLeftIcon" alt="" />
        </div>
        <!-- 地球 36×36，水平居中 -->
        <div class="nav-box nav-earth">
          <img :src="earthIcon" alt="" />
        </div>
        <!-- 右侧图标盒子 30×30：距地球 27px（图片待提供） -->
        <!-- <div class="nav-box nav-right"><img :src="bottomRightIcon" alt="" /></div> -->
      </div>
    </div>

    <!-- 左侧垂直居中动画 -->
    <img class="side-ani-left" :src="bgLeftAni" alt="" />

    <!-- 右侧垂直居中动画 -->
    <img class="side-ani-right" :src="bgRightAni" alt="" />

    <!-- 厂区快捷按钮 -->
    <!-- <div class="marker-buttons">
      <button
        v-for="item in config.proList"
        :key="item.vrID"
        class="marker-btn"
        @click="handleMarkerSelect(item)"
      >
        {{ item.name }}
      </button>
    </div> -->
    </div> 

    <!-- 进入缓冲 -->
    <div class="entering-mask" v-if="state.entering">
      <div class="loader"></div>
      <span>正在进入 {{ state.targetName }}...</span>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as Cesium from 'cesium'
import config from '@/config'
import MetricCard from './components/MetricCard.vue'
import { leftMetrics, rightMetrics } from '@/data/homeMetrics'
// 页面装饰图：顶部/底部横幅 + 底部左右动画
import bgTop from '@/assets/vrTop.png'
import bgBottom from '@/assets/bgBottomAni.png'
import earthIcon from '@/assets/earthIcon.png'
import bottomLeftIcon from '@/assets/bottomLeftIcon.png'
import bgBottomAniLeft from '@/assets/bgBottomAniLeft.gif'
import bgBottomAniRight from '@/assets/bgBottomAniRight.gif'
// 左右两侧垂直居中的动画素材
import bgLeftAni from '@/assets/bgLeftAni.gif'
import bgRightAni from '@/assets/bgRightAni.gif'
// 点位背景素材：主点位（开场）与子点位（放大）
import mapMainPointBg from '@/assets/mapMainPointBg.png'
import mapSubPointBg from '@/assets/mapSubPointBg.png'

// 不使用 Cesium Ion 服务
Cesium.Ion.defaultAccessToken = ''

const router = useRouter()
const containerRef = ref(null)

const state = reactive({
  entering: false,
  targetName: '',
})

let viewer = null
let handler = null
let areaHoverHandler = null
let rotateTimer = null
let rotateStop = null
const areaPolygons = [] // 按 config.points 渲染的区域实体
let initialMarkerEntity = null
let subMarkerEntities = []

const mainPointImg = new Image()
mainPointImg.src = mapMainPointBg

const subPointImg = new Image()
subPointImg.src = mapSubPointBg

function ensureImageLoaded(img) {
  if (img.complete && img.naturalWidth > 0) return Promise.resolve()
  return new Promise((resolve) => {
    img.onload = () => resolve()
    img.onerror = () => resolve()
  })
}

const TIANDITU_TOKEN = import.meta.env.VITE_TIANDITU_TOKEN
// 视角与发射线以北厂为中心（北厂 longitude/latitude 已回写为 points 质心）
const NORTH_ITEM = config.proList.find((i) => i.name === '泰中特种纸有限公司北厂')
const MAP_CENTER = {
  longitude: NORTH_ITEM ? NORTH_ITEM.longitude : 116.449114,
  // 纬度取全厂区南北边界中点（南厂南界 35.915103 ~ 算力中心北界 35.971679），
  // 保证南厂及其圈地区域完整落入可视区域（10km 高度垂直视野约 11.5km）
  latitude: 35.943391,
}
// 起始经度：目标经度（北厂）的背面（相差 180°）
// 起始时目标点位在地球背面不可见；rotateRight 向西转满 180° 后，目标转到正面中心
const START_LNG = MAP_CENTER.longitude - 180
const START_LAT = MAP_CENTER.latitude
// 开场动画高度序列（单位：米）
const INTRO_HEIGHT_1 = 26000000  // 首次加载：地球较小（远景）
const INTRO_HEIGHT_2 = 30000000  // 拉远变小：地球更小
const OVERVIEW_HEIGHT = 14000    // 放大显示目标区域（较之前 20000m 更近，圈定区域更明显）

// ===== 初始化 Cesium =====

function initCesium() {
  const container = containerRef.value
  // 确保容器有尺寸，否则 WebGL canvas 会是 0x0，导致 maximumTextureSize 异常
  if (!container || container.clientWidth === 0 || container.clientHeight === 0) {
    requestAnimationFrame(initCesium)
    return
  }

  // 幂等保护：若已有实例（HMR 热更新/组件重复挂载导致），先销毁再重建，避免重复 add 报错
  if (viewer && !viewer.isDestroyed()) {
    stopAutoRotate()
    if (handler) {
      handler.destroy()
      handler = null
    }
    if (areaHoverHandler) {
      areaHoverHandler.destroy()
      areaHoverHandler = null
    }
    if (initialMarkerEntity) {
      viewer.entities.remove(initialMarkerEntity)
      initialMarkerEntity = null
    }
    subMarkerEntities.forEach((e) => viewer.entities.remove(e))
    subMarkerEntities = []
    viewer.destroy()
    viewer = null
  }

  viewer = new Cesium.Viewer(container, {
    geocoder: false,
    sceneModePicker: false,
    baseLayerPicker: false,
    navigationHelpButton: false,
    homeButton: false,
    animation: false,
    timeline: false,
    fullscreenButton: false,
    selectionIndicator: false,
    infoBox: false,
    imageryProvider: false,
    scene: { msaaSamples: 8 },
    contextOptions: { webgl: { antialias: true } }, // 双保险抗锯齿
  })

  // 影像底图（天地图卫星）— 通过 /tianditu 代理避免跨域
  // 注意：仅加载无注记的 img_w，不叠加 cia_w（地名注记层），因此不显示地区文本
  viewer.imageryLayers.addImageryProvider(
    new Cesium.UrlTemplateImageryProvider({
      // 天地图 DataServer 使用 l 表示缩放级别，Cesium 的模板变量仍使用 z。
      url: `/tianditu/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=${TIANDITU_TOKEN}`,
      maximumLevel: 18,
    })
  )

  // 不带地形（平地球）
  viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider()

  // 隐藏版权
  viewer.cesiumWidget.creditContainer.style.display = 'none'

  // 缩放范围
  viewer.scene.screenSpaceCameraController.minimumZoomDistance = 1000
  viewer.scene.screenSpaceCameraController.maximumZoomDistance = 20000000

  // 初始视角：首次加载显示较小地球（远景，阿拉伯半岛上空）
  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(START_LNG, START_LAT, INTRO_HEIGHT_1),
    orientation: {
      heading: 0,
      pitch: -Cesium.Math.PI / 2,
      roll: 0,
    },
  })

  // 开场阶段仅显示第一个标记（打点距离近，远景下重叠成团），放大完成后再补充
  addInitialMarker()

  // 按 config.points 圈定区域（默认黄色边框，悬停变黄）
  addAreaPolygons()

  // 以北厂为中心，向其余四个点位添加动画发射线
  addEmissionLines()

  // 点击事件
  setupClickHandler()

  // 区域悬停高亮
  setupAreaHover()

  // 开场动画：拉远变小 → 旋转定位 → 放大显示
  playIntro()
}

// ===== 开场动画 =====

/**
 * 开场动画序列：
 * 1. 拉远变小：相机从首次加载的小视角继续拉远（地球占屏更小）
 * 2. 旋转定位：保持远景自动旋转，把目标区域（济南/厂区）转到画面中心
 * 3. 放大显示：旋转结束后推近放大，展示厂区标记（flyToOverview）
 */
function playIntro() {
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(START_LNG, START_LAT, INTRO_HEIGHT_2),
    orientation: {
      heading: 0,
      pitch: -Cesium.Math.PI / 2,
      roll: 0,
    },
    duration: 2.0,
    complete: () => startAutoRotate(), // 拉远完成后开始旋转定位
  })
}

// ===== 添加标记点 =====

// ===== 添加标记点（带引导线与背景图） =====

/**
 * 生成带圆点、引导折线与背景图片标签的高清 Canvas 标记
 * @param {string} text 点位名称
 * @param {HTMLImageElement} bgImg 背景图 (mapMainPointBg.png 或 mapSubPointBg.png)
 * @param {boolean} isMain 是否为主点位
 * @param {'right-up' | 'left-up'} direction 引导线延伸方向（支持左右错开）
 */
function createMarkerBillboardCanvas(text, bgImg, isMain = false, direction = 'right-up') {
  const dpr = 2 // 高清屏 Retina 2x 渲染

  // 尺寸设定 (CSS 逻辑像素)
  const cardW = isMain ? 180 : 155
  const cardH = isMain ? 44 : 26
  // 放大后的点大小缩小一倍（主点位半径 5.5px / 光晕 10px，放大后子点位半径 2.75px / 光晕 5.5px）
  const dotRadius = isMain ? 5.5 : 2.75
  const haloRadius = isMain ? 10 : 5.5
  const leaderDx = isMain ? 56 : 48
  const leaderDy = isMain ? 48 : 42

  const pad = 14
  const width = cardW + leaderDx + pad * 2
  const height = cardH + leaderDy + pad * 2

  const canvas = document.createElement('canvas')
  canvas.width = width * dpr
  canvas.height = height * dpr
  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)

  const isLeft = direction === 'left-up'

  // 圆点中心位置
  const dotX = isLeft ? width - pad : pad
  const dotY = height - pad

  // 卡片左上角位置
  const cardX = isLeft ? pad : dotX + leaderDx
  const cardY = dotY - leaderDy - cardH / 2

  // 引导线与主题色：主点位绿色，放大后子点位使用 #1a5fa4
  const themeColor = isMain ? 'rgb(60, 223, 131)' : '#1a5fa4'

  // 1. 绘制引导折线 (从圆点中心连接到卡片对应边缘中央)
  ctx.beginPath()
  ctx.moveTo(dotX, dotY)
  if (isLeft) {
    // 向左上方引出折线，连接卡片右侧
    const midX = dotX - leaderDx * 0.65
    const midY = cardY + cardH / 2
    ctx.lineTo(midX, midY)
    ctx.lineTo(cardX + cardW, cardY + cardH / 2)
  } else {
    // 向右上方引出折线，连接卡片左侧
    const midX = dotX + leaderDx * 0.65
    const midY = cardY + cardH / 2
    ctx.lineTo(midX, midY)
    ctx.lineTo(cardX, cardY + cardH / 2)
  }
  ctx.strokeStyle = themeColor
  ctx.lineWidth = isMain ? 1.5 : 1.2
  ctx.stroke()

  // 2. 绘制卡片背景图
  if (bgImg.complete && bgImg.naturalWidth > 0) {
    ctx.drawImage(bgImg, cardX, cardY, cardW, cardH)
  } else {
    // 降级兜底深色背景
    ctx.fillStyle = 'rgba(10, 28, 52, 0.88)'
    ctx.strokeStyle = themeColor
    ctx.lineWidth = 1
    ctx.fillRect(cardX, cardY, cardW, cardH)
    ctx.strokeRect(cardX, cardY, cardW, cardH)
  }

  // 3. 绘制卡片内文字（精确校准背景框内的水平与垂直居中）
  ctx.fillStyle = '#ffffff'
  ctx.font = isMain ? 'bold 13px sans-serif' : '12px sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.shadowColor = 'rgba(0, 0, 0, 0.9)'
  ctx.shadowBlur = 4

  // 主点位背景图（420x104）内部有效框居中微调：
  // 底部发光与中文基准线影响，Y 轴向下修正 +3.5px；右侧装饰条影响，X 轴向右修正 +6px 达到绝对居中
  const textOffsetX = isMain ? 6 : (isLeft ? -2 : 2)
  const textOffsetY = isMain ? 3.5 : 1.2
  ctx.fillText(text, cardX + cardW / 2 + textOffsetX, cardY + cardH / 2 + textOffsetY)
  ctx.shadowBlur = 0

  // 4. 绘制地面打点
  if (isMain) {
    // 绿色主打点：中心是绿色，然后是白色边框，半透明的白色光晕
    // 外层半透明白色光晕
    ctx.beginPath()
    ctx.arc(dotX, dotY, haloRadius, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255, 255, 255, 0.28)'
    ctx.fill()
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.75)'
    ctx.lineWidth = 1.2
    ctx.stroke()

    // 核心绿色圆点 + 白色边框
    ctx.beginPath()
    ctx.arc(dotX, dotY, dotRadius, 0, Math.PI * 2)
    ctx.fillStyle = 'rgb(60, 223, 131)'
    ctx.fill()
    ctx.strokeStyle = '#ffffff'
    ctx.lineWidth = 1.8
    ctx.stroke()
  } else {
    // 放大后的 5 个子点位：缩小一倍，点及引导线使用 #1a5fa4
    // 外层半透明 #1a5fa4 蓝色光晕
    ctx.beginPath()
    ctx.arc(dotX, dotY, haloRadius, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(26, 95, 164, 0.35)'
    ctx.fill()
    ctx.strokeStyle = 'rgba(26, 95, 164, 0.8)'
    ctx.lineWidth = 1
    ctx.stroke()

    // 核心 #1a5fa4 实体点 + 白色微边框
    ctx.beginPath()
    ctx.arc(dotX, dotY, dotRadius, 0, Math.PI * 2)
    ctx.fillStyle = '#1a5fa4'
    ctx.fill()
    ctx.strokeStyle = '#ffffff'
    ctx.lineWidth = 1
    ctx.stroke()
  }

  return {
    canvas,
    width,
    height,
    dotOffsetX: -dotX,
    dotOffsetY: height - dotY,
  }
}

/**
 * 开场阶段：只显示第一个主标记（远景下打点，使用 mapMainPointBg.png）
 */
async function addInitialMarker() {
  await ensureImageLoaded(mainPointImg)
  if (initialMarkerEntity && viewer && !viewer.isDestroyed()) {
    viewer.entities.remove(initialMarkerEntity)
    initialMarkerEntity = null
  }
  const item = NORTH_ITEM || config.proList[0]
  if (!item || !viewer || viewer.isDestroyed()) return

  const markerData = createMarkerBillboardCanvas(item.name, mainPointImg, true, 'right-up')
  initialMarkerEntity = viewer.entities.add({
    name: item.name,
    position: Cesium.Cartesian3.fromDegrees(item.longitude, item.latitude, 100),
    billboard: {
      image: markerData.canvas,
      width: markerData.width,
      height: markerData.height,
      horizontalOrigin: Cesium.HorizontalOrigin.LEFT,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      pixelOffset: new Cesium.Cartesian2(markerData.dotOffsetX, markerData.dotOffsetY),
      heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
    },
  })
}

/**
 * 进入放大模式后：重新对 5 个点位进行左右错开打点（使用 mapSubPointBg.png 背景）
 */
async function showRestMarkers() {
  await ensureImageLoaded(subPointImg)
  if (initialMarkerEntity && viewer && !viewer.isDestroyed()) {
    viewer.entities.remove(initialMarkerEntity)
    initialMarkerEntity = null
  }
  if (!viewer || viewer.isDestroyed()) return

  // 清理现有子标记点
  subMarkerEntities.forEach((entity) => viewer.entities.remove(entity))
  subMarkerEntities = []

  // 左右交错引导线配置（根据相对经纬度排布，避免重叠）
  const pointDirections = {
    '泰中特种纸有限公司北厂': 'left-up',    // 西侧偏南 -> 向左上延伸
    '泰中电力': 'left-up',                  // 西侧偏北 -> 向左上延伸
    '泰中污水处理厂': 'right-up',            // 东侧偏南 -> 向右上延伸
    '泰中算力中心': 'right-up',              // 东侧最北 -> 向右上延伸
    '泰中特种纸有限公司': 'right-up',        // 南侧独立 -> 向右上延伸
  }

  // 重新对 5 个点位进行打点
  config.proList.forEach((item, idx) => {
    const dir = pointDirections[item.name] || (idx % 2 === 0 ? 'left-up' : 'right-up')
    const markerData = createMarkerBillboardCanvas(item.name, subPointImg, false, dir)
    const entity = viewer.entities.add({
      name: item.name,
      position: Cesium.Cartesian3.fromDegrees(item.longitude, item.latitude, 100),
      billboard: {
        image: markerData.canvas,
        width: markerData.width,
        height: markerData.height,
        horizontalOrigin: Cesium.HorizontalOrigin.LEFT,
        verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
        pixelOffset: new Cesium.Cartesian2(markerData.dotOffsetX, markerData.dotOffsetY),
        heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
      },
    })
    subMarkerEntities.push(entity)
  })
}

// ===== 点击事件 =====

function setupClickHandler() {
  handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  handler.setInputAction((movement) => {
    const picked = viewer.scene.pick(movement.position)
    if (picked?.id) {
      const entity = picked.id
      // 实体 id 由 Cesium 自动生成，按 name 匹配项目
      const item = config.proList.find((i) => i.name === entity.name)
      if (item) {
        handleMarkerSelect(item)
      }
    }
    // 打印点击位置的经纬度（拾取球面坐标，命中实体或空白区域均可打印）
    // 输出格式与 config/index.js 中 points 一致：[经度, 纬度]
    const cartesian = viewer.camera.pickEllipsoid(movement.position, viewer.scene.globe.ellipsoid)
    if (cartesian) {
      const carto = Cesium.Cartographic.fromCartesian(cartesian)
      const lon = Cesium.Math.toDegrees(carto.longitude)
      const lat = Cesium.Math.toDegrees(carto.latitude)
      console.log(`[${lon.toFixed(6)}, ${lat.toFixed(6)}]`)
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
}

// ===== 按 config 圈定区域 =====

/**
 * 将 config.proList 中每个点位的 points（[[经度, 纬度], ...]，首尾闭合）
 * 渲染为「默认黄色边框 + 透明填充」的多边形，挂载到地球
 */
function addAreaPolygons() {
  areaPolygons.length = 0 // 幂等：HMR/重复挂载时清空旧引用
  config.proList.forEach((item) => {
    if (!Array.isArray(item.points) || item.points.length < 3) return
    const positions = item.points.map(([lon, lat]) =>
      Cesium.Cartesian3.fromDegrees(lon, lat)
    )
    areaPolygons.push(
      viewer.entities.add({
        id: `area-${item.name}`,
        polygon: {
          hierarchy: new Cesium.PolygonHierarchy(positions),
          material: Cesium.Color.fromCssColorString('#133460').withAlpha(0.7), // #125796 半透明填充
          outline: false,                               // polygon outline 用 GL 线不抗锯齿，改用 polyline
          height: 0,                                    // 贴地球表面
        },
        // 白色边框：贴地线（clampToGround 走矢量渲染路径）
        polyline: {
          positions: [...positions, positions[0]],
          width: 1,
          material: Cesium.Color.WHITE.withAlpha(0.6),
          clampToGround: true,
        },
      })
    )
  })
}

// ===== 北厂动画发射线 =====

let trailMaterialRegistered = false

/**
 * 注册科技虚线/点阵流动材质（幂等：重复挂载/HMR 时只注册一次）。
 * 呈现数据流包沿连线匀速穿梭的数字化科技效果。
 */
function registerTrailMaterial() {
  if (trailMaterialRegistered) return
  trailMaterialRegistered = true
  try {
    Cesium.Material._materialCache.addMaterial('PolylineTechDash', {
      fabric: {
        type: 'PolylineTechDash',
        uniforms: {
          color: new Cesium.Color(1.0, 1.0, 1.0, 1.0),       // 虚线流动高亮色（高亮纯白）
          bgColor: new Cesium.Color(1.0, 1.0, 1.0, 0.25),    // 线条底色背景（半透明白色）
          repeat: 6.0,                                       // 虚线段数量（调稀疏，原 20.0）
          dashRatio: 0.4,                                    // 虚线占空比（40% 实线，60% 空白间隔）
          speed: 0.008,                                      // 平稳科技流速
        },
        source: `
          czm_material czm_getMaterial(czm_materialInput materialInput) {
            czm_material material = czm_getDefaultMaterial(materialInput);
            vec2 st = materialInput.st;
            
            // 沿连线向前流动的虚线相位（北厂 0 -> 目标点 1）
            float s = fract(st.s * repeat - czm_frameNumber * speed);
            
            // 平滑抗锯齿的虚线段
            float edge = 0.04;
            float isDash = smoothstep(0.0, edge, s) * (1.0 - smoothstep(dashRatio - edge, dashRatio, s));
            
            // 虚线头部微光推进感
            float headGlow = pow(clamp(s / dashRatio, 0.0, 1.0), 1.6) * isDash;
            
            // 半透明白色底轨 + 纯白高亮流动虚线
            vec4 base = bgColor;
            vec4 flow = color;
            
            material.diffuse = mix(base.rgb, flow.rgb, isDash);
            material.alpha = clamp(base.a * 0.6 + isDash * flow.a * 0.9, 0.0, 1.0);
            material.emission = flow.rgb * (isDash * 0.35 + headGlow * 0.65);
            return material;
          }
        `,
      },
      translucent: true,
    })
  } catch (e) {
    // 忽略重复注册异常
  }
}

/**
 * 自定义 MaterialProperty：让 polyline.material 绑定 PolylineTechDash 材质
 */
class PolylineTechDashMaterialProperty {
  constructor(options = {}) {
    this._definitionChanged = new Cesium.Event()
    this._color = Cesium.Color.clone(options.color || Cesium.Color.WHITE)
    this._bgColor = Cesium.Color.clone(options.bgColor || new Cesium.Color(1.0, 1.0, 1.0, 0.25))
    this._repeat = options.repeat ?? 6.0
    this._dashRatio = options.dashRatio ?? 0.4
    this._speed = options.speed ?? 0.008
  }

  get isConstant() {
    return true
  }

  get definitionChanged() {
    return this._definitionChanged
  }

  getType() {
    return 'PolylineTechDash'
  }

  getValue(time, result) {
    if (!Cesium.defined(result)) result = {}
    result.color = Cesium.Color.clone(this._color, result.color)
    result.bgColor = Cesium.Color.clone(this._bgColor, result.bgColor)
    result.repeat = this._repeat
    result.dashRatio = this._dashRatio
    result.speed = this._speed
    return result
  }

  isTranslucent() {
    return true
  }

  equals(other) {
    return (
      this === other ||
      (other instanceof PolylineTechDashMaterialProperty &&
        this._color.equals(other._color) &&
        this._bgColor.equals(other._bgColor) &&
        this._repeat === other._repeat &&
        this._dashRatio === other._dashRatio &&
        this._speed === other._speed)
    )
  }
}

/**
 * 以北厂为中心，向其余四个点位添加动画发射线（科技虚线流动）
 * 起点/终点取各点 longitude/latitude（已回写为 points 质心）
 */
function addEmissionLines() {
  registerTrailMaterial()
  const north = config.proList.find((i) => i.name === '泰中特种纸有限公司北厂')
  if (!north) return
  const from = Cesium.Cartesian3.fromDegrees(north.longitude, north.latitude, 300)
  config.proList.forEach((item) => {
    if (item === north) return
    viewer.entities.add({
      polyline: {
        positions: [
          from,
          Cesium.Cartesian3.fromDegrees(item.longitude, item.latitude, 300),
        ],
        width: 3,
        material: new PolylineTechDashMaterialProperty({
          color: new Cesium.Color(1.0, 1.0, 1.0, 1.0),
          bgColor: new Cesium.Color(1.0, 1.0, 1.0, 0.25),
          repeat: 6.0,
          dashRatio: 0.4,
          speed: 0.008,
        }),
      },
    })
  })
}

/**
 * 悬停高亮：鼠标移入已圈定区域时填充变黄，移出恢复透明
 * 注意：多边形未设置 name，不会与现有标记点击逻辑（按 name 匹配）冲突
 */
function setupAreaHover() {
  areaHoverHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  areaHoverHandler.setInputAction((movement) => {
    const picked = viewer.scene.pick(movement.endPosition)
    areaPolygons.forEach((entity) => {
      entity.polygon.material = Cesium.Color.fromCssColorString('#133460').withAlpha(
        picked?.id === entity ? 0.9 : 0.8
      )
    })
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)
}

// ===== 自动旋转 =====

/**
 * 自动旋转定位：rotateRight 向西旋转半圈，把目标点位从地球背面转到正面中心
 * 起始经度 -63.55°（目标背面），累计转满 180° 即到达 116.45°（MAP_CENTER）
 * 用累计旋转量判断落点：与帧率无关，且规避跨 180° 经线的经度回绕
 */
function startAutoRotate() {
  let rotated = 0
  const STEP_DEG = 0.5 // 每 tick 0.5°（约 30°/s），转 180° 约 6s
  rotateStop = viewer.clock.onTick.addEventListener(() => {
    viewer.camera.rotateRight(Cesium.Math.toRadians(STEP_DEG))
    rotated += STEP_DEG
    if (rotated >= 180) {
      stopAutoRotate()
      alignAndZoom()
    }
  })
  viewer.clock.shouldAnimate = true
}

/**
 * 旋转到位后：先对正目标中心（远景），再放大显示，形成聚焦效果
 */
function alignAndZoom() {
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(
      MAP_CENTER.longitude,
      MAP_CENTER.latitude,
      INTRO_HEIGHT_2
    ),
    orientation: {
      heading: 0,
      pitch: -Cesium.Math.PI / 2,
      roll: 0,
    },
    duration: 0.8,
    complete: () => flyToOverview(),
  })
}

function stopAutoRotate() {
  if (rotateTimer) {
    clearTimeout(rotateTimer)
    rotateTimer = null
  }
  if (rotateStop) {
    rotateStop()
    rotateStop = null
  }
  if (viewer) viewer.clock.shouldAnimate = false
}

// ===== 缩放定位 =====

function flyToOverview() {
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(
      MAP_CENTER.longitude,
      MAP_CENTER.latitude,
      OVERVIEW_HEIGHT
    ),
    orientation: {
      heading: 0,
      pitch: -Cesium.Math.PI / 2,
      roll: 0,
    },
    duration: 1.5,
    // 放大到位后：补充其余标记点（避免远景下多点重叠）
    complete: () => showRestMarkers(),
  })
}

// ===== 进入 VR =====

function enterVR(item) {
  state.targetName = item.name
  state.entering = true
  setTimeout(() => router.push(`/vr/${item.vrID}`), 300)
}

// 统一入口：地球标记点击 / 底部快捷按钮共用
function handleMarkerSelect(item) {
  if (state.entering) return // 防重复进入
  stopAutoRotate()
  // 无 VR 场景的点位（如东平县污水处理厂）：仅聚焦展示，不进入 VR
  if (!item.vrID) {
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(item.longitude, item.latitude, 5000),
      orientation: {
        heading: 0,
        pitch: -Cesium.Math.PI / 2,
        roll: 0,
      },
      duration: 1.0,
    })
    return
  }
  // 有 VR：先飞过去再进入
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(item.longitude, item.latitude, 5000),
    orientation: {
      heading: 0,
      pitch: -Cesium.Math.PI / 2,
      roll: 0,
    },
    duration: 1.0,
    complete: () => enterVR(item),
  })
}

onMounted(() => {
  nextTick(() => initCesium())
})

onUnmounted(() => {
  stopAutoRotate()
  if (handler) {
    handler.destroy()
    handler = null
  }
  if (areaHoverHandler) {
    areaHoverHandler.destroy()
    areaHoverHandler = null
  }
  areaPolygons.length = 0
  if (initialMarkerEntity && viewer) {
    viewer.entities.remove(initialMarkerEntity)
    initialMarkerEntity = null
  }
  subMarkerEntities.forEach((e) => {
    if (viewer && !viewer.isDestroyed()) viewer.entities.remove(e)
  })
  subMarkerEntities = []
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
})
</script>
<style scoped>
.cnt-wrapper {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: url(../../assets/mask.png) no-repeat center center;
  background-size: cover;
  pointer-events: none;
}
.home-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
}

.cesium-container {
  width: 100%;
  height: 100%;
}

/* 顶部标题容器：高度 90px，bgTop 铺满背景 */
.top-title {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 90px;
  display: flex;
  justify-content: center;
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
  text-align: center;
  z-index: 1000;
  background: linear-gradient(180deg, #141c2c 36%, #12182399 76%, #141c2c00 98%);
  pointer-events: none;
}

.top-title-cnt{
    width: 100%;
    height: 100%;
    background-size: contain;
    background-position: center bottom;
    background-repeat: no-repeat;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10%;
    box-sizing: border-box;
}

/* 标题两侧宣传语：流光文字 + 渐变分隔线 + 发光菱形 */
.slogan {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.slogan-left {
  justify-content: flex-end;
}

.slogan-right {
  justify-content: flex-start;
}

/* 流光渐变文字（background-clip 裁到字形，光泽沿文字扫过） */
.slogan-txt {
  font-size: clamp(12px, 1.3vw, 16px);
  letter-spacing: 3px;
  white-space: nowrap;
  background: linear-gradient(90deg, #6db3f2 0%, #ffffff 50%, #6db3f2 100%);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 0 8px rgba(34, 192, 255, 0.45));
  animation: slogan-shine 5s linear infinite;
}

@keyframes slogan-shine {
  0% { background-position: 0% 0; }
  100% { background-position: 200% 0; }
}

/* 渐变分隔线：左侧由近到远淡出，右侧相反 */
.slogan-line {
  flex: none;
  width: clamp(16px, 3vw, 44px);
  height: 1px;
  background: linear-gradient(90deg, rgba(34, 192, 255, 0.05), rgba(34, 192, 255, 0.95));
}

.slogan-right .slogan-line {
  background: linear-gradient(90deg, rgba(34, 192, 255, 0.95), rgba(34, 192, 255, 0.05));
}

/* 发光菱形点缀：呼吸缩放 */
.slogan-gem {
  flex: none;
  width: 6px;
  height: 6px;
  transform: rotate(45deg);
  background: #22c0ff;
  box-shadow: 0 0 10px rgba(34, 192, 255, 0.9);
  animation: slogan-gem 2.4s ease-in-out infinite;
}

@keyframes slogan-gem {
  0%, 100% { opacity: 1; box-shadow: 0 0 10px rgba(34, 192, 255, 0.9); }
  50% { opacity: 0.35; box-shadow: 0 0 4px rgba(34, 192, 255, 0.4); }
}

.top-title h1 {
  margin: 0;
  font-size: clamp(24px, 2.4vw, 32px); /* 随屏宽缩放，为两侧宣传语让位 */
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.8);
  letter-spacing: 2px;
  flex: none;
  white-space: nowrap;
}

.top-title p {
  margin: 8px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

/* 底部容器：38px + 渐变背景，内部左中右三图 flex 布局 */
.bottom-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 38px;
  padding-bottom: 6px;
  box-sizing: border-box;
  display: flex;
  align-items: flex-end;
  background: linear-gradient(180deg, #141c2c00 18%, #141c2c);
  z-index: 10;
  pointer-events: none;
}

.bottom-bar img {
  height: 100%; /* 38px - 6px padding = 32px 内容高 */
  flex-shrink: 0;
  object-fit: contain;
}

/* 中间图宽度 100% 撑满（左右图固定宽度不收缩） */
.bottom-bar .bottom-center {
  width: 100%;
  flex: 1 1 100%;
  min-width: 0;
}

.bottom-bar .bottom-left {
  flex-shrink: 0;
}

.bottom-bar .bottom-right {
  flex-shrink: 0;
}

/* ===== 底部中间导航框（设计稿 420 × 112） ===== */
.bottom-bar .bottom-nav {
  position: absolute;
  left: 50%;
  bottom: -30px; /* 整体下移，让图标落在底栏内（数值越小越往下） */
  transform: translateX(-50%);
  width: 420px;
  height: 112px;
  pointer-events: none;
}

.bottom-nav .nav-box {
  position: absolute;
}

.bottom-nav .nav-box img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 左侧盒子：50×50，left 128，top 38，bottom 24 */
.bottom-nav .nav-left {
  left: 128px;
  top: 38px;
  width: 50px;
  height: 50px;
}

/* 地球：28×28，水平居中（中心 210px），整体在底栏亮线以下 */
.bottom-nav .nav-earth {
  left: 196px;  /* 210 - 28/2 */
  top: 47px;    /* 数值越大越往下 */
  width: 28px;
  height: 28px;
}

/* 右侧盒子：30×30，距地球右边缘 27px */
.bottom-nav .nav-right {
  left: 251px; /* 196 + 28 + 27 */
  top: 47px;
  width: 30px;
  height: 30px;
}

/* 左右两侧动画：原始尺寸 1248×150，贴边、垂直居中，不拦截鼠标事件 */
.side-ani-left,
.side-ani-right {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  z-index: 10;
  pointer-events: none;
}

/* 左侧动画：左边缘对齐（向右铺到中间区域） */
.side-ani-left {
  left: 10px;
}

/* 右侧动画：右边缘对齐（向左铺到中间区域） */
.side-ani-right {
  right: 10px;
}

/* 左右两侧指标面板容器：宽度 240px，自适应高度分布，允许鼠标事件 */
.side-panel {
  position: absolute;
  top: 196px;
  bottom: 146px;
  width: 240px;
  z-index: 20;
  pointer-events: auto;
  box-sizing: border-box;
}

.side-panel-left {
  left: 32px;
}

.side-panel-right {
  right: 32px;
}

/* 外部科技渐变边框 wrapper（.side-panel 自带 absolute 定位，此处不再声明 position） */
.cnt {
  background: linear-gradient(
    180deg,
    rgba(34, 192, 255, 0.9),
    rgba(34, 192, 255, 0.22) 45%,
    rgba(34, 192, 255, 0.6)
  );
  padding: 1px;
  border-radius: 6px;
  box-sizing: border-box;
  display: flex;
  overflow: hidden; /* 裁掉超出边框的旋转光层，仅保留边框一圈可见 */
  box-shadow:
    0 0 16px rgba(34, 192, 255, 0.22),
    inset 0 0 10px rgba(34, 192, 255, 0.12);
}

/* 绕边框流动的发光弧：conic 渐变旋转，沿 border 连续行走、拐角自然过渡 */
.cnt::before,
.cnt::after {
  content: '';
  position: absolute;
  inset: -50%;
  pointer-events: none;
  z-index: 1;
}

/* 蓝色光弧（顺时针） */
.cnt::before {
  background: conic-gradient(
    transparent 0deg,
    rgba(34, 192, 255, 0.9) 16deg,
    transparent 40deg
  );
  animation: border-spin 4s linear infinite;
}

/* 绿色光弧（错相 180° 跟随） */
.cnt::after {
  background: conic-gradient(
    transparent 180deg,
    rgba(0, 230, 118, 0.85) 196deg,
    transparent 220deg
  );
  animation: border-spin 4s linear infinite 2s;
}

@keyframes border-spin {
  to { transform: rotate(360deg); }
}

/* 内部深色毛玻璃容器：兼容黑色太空（默认）与浅色大陆（放大）两种背景 */
.cntBody {
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 2; /* 盖在旋转光层之上 */
  background-color: rgba(5, 22, 48, 0.8);
  background-image:
    linear-gradient(rgba(34, 192, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(34, 192, 255, 0.05) 1px, transparent 1px);
  background-size: 22px 22px;
  backdrop-filter: blur(14px) saturate(150%);
  border-radius: 5px;
  padding: 14px 10px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
  overflow: hidden;
}

.cntBody :deep(.box-title) {
  height: 24px;
}

.marker-buttons {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 16px;
  z-index: 1000;
  pointer-events: auto;
}

.marker-btn {
  padding: 10px 24px;
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  transition: all 0.2s;
}

.marker-btn:hover {
  background: rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
}

.entering-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  background: rgba(5, 10, 20, 0.92);
  backdrop-filter: blur(4px);
  z-index: 2000;
  animation: fadeIn 0.3s ease;
}

.entering-mask span {
  color: rgba(255, 255, 255, 0.85);
  font-size: 16px;
  letter-spacing: 1px;
}

.loader {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
