# VR 模型附着到 Cesium 地球 —— 技术方案

> 目标：把 `@realsee/five` 的倾斜摄影 3D 模型，真正贴到 Cesium 地球上泰中厂区的真实经纬度位置上，
> 天地图影像底图替代 static-demo 里的 CARTO 平面贴图，实现"从太空钻进厂区"的无缝过渡。

---

## 1. 现状对比

| 维度         | VR数字工厂 现状                                                     | static-demo（参考）                      |
| ------------ | ------------------------------------------------------------------- | ---------------------------------------- |
| 渲染引擎     | Cesium（首页）+ Five（VR 页），**两个独立 WebGL context、两个路由** | 纯 Five 单引擎                           |
| 底图         | 天地图 img_w 卫星影像（已配 `/tianditu` 代理）                      | CARTO 瓦片拼接到 three.js 平面           |
| 模型位置     | Five 模型在自己的局部米制坐标系里，和地球无关联                     | 模型在 five.scene 里，底部贴一张平面地图 |
| 进入模型方式 | 地球 flyTo 5000m → `router.push('/vr/:vrID')` **硬切路由**          | 无地球，直接展示模型                     |
| 坐标系       | Cesium 用 ECEF/经纬度；Five 用局部米制                              | Five 局部米制                            |

**核心问题**：地球和模型是两张皮。用户从地球看不到模型，进了 VR 页看不到地球，两次加载、两套相机。

---

## 2. 技术路线选型

| 路线                                  | 做法                                                                                   | 可行性                                                                    | 结论        |
| ------------------------------------- | -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ----------- |
| A. 导出 glTF 到 Cesium                | 把 Five 的 at3d 倾斜摄影瓦片导出为 glTF，用 Cesium Model 加载                          | Five 模型是 CDN 流式瓦片（`.at3d`），前端无法提取完整 mesh                | ❌ 不可行   |
| B. three.js 对象挂进 Cesium scene     | 把 `five.scene` 的 Object3D 直接加到 Cesium scene                                      | 两个 WebGL context / Scene 不互通，Cesium 不接受 three.js 对象            | ❌ 不可行   |
| C. **双 Canvas 叠加 + 坐标/相机同步** | Cesium canvas（底层，地球）+ Five canvas（上层，透明），通过桥接器对齐坐标系和相机矩阵 | Five 已支持 `backgroundAlpha:0` 透明渲染；config 已有厂区经纬度；技术成熟 | ✅ **推荐** |

---

## 3. 整体架构

```
┌──────────────────────────────────────────────────────┐
│                 页面容器 (full screen)                 │
│                                                      │
│  ┌────────────────────────────────────────────────┐  │
│  │  Five canvas (z-index:1, 透明, backgroundAlpha:0)│  │
│  │  - 倾斜摄影模型 (at3d 流式加载)                 │  │
│  │  - 热点标签 / 导览轮播                         │  │
│  ├────────────────────────────────────────────────┤  │
│  │  Cesium canvas (z-index:0)                   │  │
│  │  - 地球 + 天地图 img_w 影像                    │  │
│  │  - 厂区圈地 / billboard / 发射线              │  │
│  ├────────────────────────────────────────────────┤  │
│  │  DOM UI 层 (z-index:10+)                      │  │
│  │  - 顶部标题 / 左右面板 / 底部轮播 / 模式开关   │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│            ▲                    ▲                     │
│            │  坐标对齐+相机同步  │                     │
│            └──── EarthVRBridge ┘                     │
└──────────────────────────────────────────────────────┘
```

**关键设计**：

- 两个 canvas 同屏叠加，不再路由跳转。Cesium 在下渲染地球，Five 在上透明渲染模型。
- `EarthVRBridge` 是唯一的桥接层：负责坐标变换和相机矩阵同步。
- 交互控制权按距离切换（见第 6 节状态机），任一时刻只有一个引擎接收鼠标事件。

---

## 4. 坐标对齐方案（核心）

### 4.1 两个坐标系

**Five / three.js 局部坐标**：

- X 右、Y 上、Z 前，单位米，原点在 Five 内部（采集基准点）。
- 模型加载完成后可通过 `five.modelScene.boundingBox` 读取包围盒。

**Cesium ENU 局部坐标**（以厂区经纬度为原点）：

- East(X)、North(Y)、Up(Z)，单位米。
- 用 `Cesium.Transforms.eastNorthUpToFixedFrame(Cartesian3.fromDegrees(lon, lat, 0))` 建立。

### 4.2 对齐需要两个标定参数

Five 模型的局部原点和朝向，不一定正好等于 config 里的 `longitude/latitude`。需要在 config 每个厂区下补两个参数：

```js
{
  name: '泰中特种纸有限公司',
  vrID: 'oLkzny3lwSX1eAEt2x',
  longitude: 116.448069,
  latitude: 35.915923,
  // ── 新增：模型锚点标定参数（一次性调好）──
  modelAnchor: {
    // Five 模型包围盒中心在 Five 局部坐标里，对应 (longitude, latitude)
    // 一般取 boundingBox 中心 (cx, cz) 和最低点 minY
    origin: { x: 0, y: 0, z: 0 },
    // 模型朝向与正北的夹角（弧度），0 = 模型"前"方向朝北
    heading: 0,
    // 模型底部相对地面的抬高（米），防 z-fighting，参考 static-demo 的 0.05
    lift: 0.5,
  },
}
```

### 4.3 坐标变换链

```
Five 局部坐标 (fx, fy, fz)
   │  ① 平移：减去 modelAnchor.origin
   │  ② 绕 Y 轴旋转 heading（对齐正北）
   │  ③ 轴映射：Five(X右,Y上,Z前) → ENU(E,N,U)
   ▼
ENU 局部坐标 (east, north, up)
   │  ④ eastNorthUpToFixedFrame
   ▼
ECEF 世界坐标 → Cesium 渲染
```

**轴映射说明**：

- Five Y（上）→ ENU U（上）
- Five X（右）→ ENU E（东）
- Five Z（前）→ ENU N（北）
- 旋转 heading 后，模型朝向即正北。

这个变换在 `EarthVRBridge` 里用一个 `THREE.Matrix4` 封装，Five→Cesium 用正矩阵，Cesium→Five 用逆矩阵。

---

## 5. 相机同步方案

两个引擎各有相机，每帧根据当前交互状态选择同步方向。

### 5.1 Cesium → Five（地球总览 / 接近厂区阶段）

用户用 Cesium 鼠标旋转地球、缩放，需要把相机位置换算成 Five 的 `setState`：

```
Cesium 相机位置 (ECEF)
  → ENU 局部坐标
  → 逆对齐变换（逆矩阵）
  → Five 局部坐标 (offset.x, offset.y, offset.z)
  → five.setState({ offset, distance, fov })
```

### 5.2 Five → Cesium（VR 漫游阶段）

用户进入 Five 的 Floorplan/Panorama 漫游，Cesium 相机跟随：

```
Five 相机 offset (Five 局部坐标)
  → 对齐变换（正矩阵）
  → ENU 坐标
  → ECEF 坐标
  → camera.setView({ destination, orientation: { heading, pitch, roll } })
```

### 5.3 同步时机

- 在 `viewer.scene.preRender.addEventListener()` 里做。
- 两个引擎视口大小必须一致（同一个容器，监听 resize 同步调 `five.refresh()` 和 Cesium canvas 尺寸）。
- fov 保持一致（Five 的 `defaultFov` 和 Cesium 的 `camera.frustum.fov`）。
- **深度缓冲不共享**：两个独立 WebGL context 无法共享 depth buffer。通过"相机高度 + 模型 lift"控制视觉贴合，不做像素级深度对齐。

---

## 6. 交互状态机

```
        ┌─────────────┐
        │  地球总览    │  Cesium 相机控制
        │ (Overview)  │  Five canvas 隐藏/暂停渲染
        └──────┬──────┘
               │ 点击厂区标记 / zoom in 到阈值高度（~500m）
               ▼
        ┌─────────────┐
        │  接近厂区    │  Cesium→Five 同步
        │ (Approach)  │  Five 模型淡入（opacity 0→1）
        └──────┬──────┘
               │ 高度降到阈值（~100m）/ 点击"进入"
               ▼
        ┌─────────────┐
        │  VR 漫游     │  Five 相机控制（Floorplan/Panorama）
        │ (VR)        │  Cesium 相机跟随同步
        │              │  底部轮播 / 标签面板显示
        └──────┬──────┘
               │ 点击"返回地球"
               ▼
        ┌─────────────┐
        │  地球总览    │  Five 模型淡出，Cesium 拉远
        └─────────────┘
```

**pointer-events 控制**：

- Overview：Five canvas `pointer-events: none`，事件穿透到 Cesium。
- Approach/VR：Cesium canvas `pointer-events: none`（或只留 Five 接收事件），Five 接收鼠标。
- 用一个 `z-index` + `pointer-events` 切换，不需要销毁实例。

---

## 7. 代码改造点

### 新增文件

| 文件                            | 职责                                                        |
| ------------------------------- | ----------------------------------------------------------- |
| `src/utils/earthVrBridge.js`    | 坐标变换矩阵封装（正/逆）、相机同步函数、模型淡入淡出       |
| `src/views/vr/EarthVRStage.vue` | 融合舞台组件：同屏叠加 Cesium canvas + Five canvas + 状态机 |

### 改造文件

| 文件                       | 改动                                                                                    |
| -------------------------- | --------------------------------------------------------------------------------------- |
| `src/views/home/index.vue` | 把 Cesium 初始化逻辑迁移到 `EarthVRStage.vue`；首页变成纯容器或直接被 EarthVRStage 取代 |
| `src/views/vr/index.vue`   | 不再独立路由页；Five 初始化、标签、导览、面板逻辑迁入 `EarthVRStage.vue`                |
| `src/router/index.js`      | `/vr/:vrID` 改为加载 `EarthVRStage`，由路由参数决定初始 vrID 和初始状态                 |
| `src/config/index.js`      | 每个 `proList` 项补 `modelAnchor: { origin, heading, lift }`（标定值）                  |
| `src/vite.config.js`       | 无需改动（Cesium 插件和 `/tianditu` 代理已配好）                                        |

### EarthVRBridge.js 核心接口（示意）

```js
// 输入：厂区配置（经纬度 + modelAnchor）、Five 实例、Cesium viewer
class EarthVRBridge {
  constructor({ item, five, viewer })
  // Five 局部坐标 → Cesium 世界坐标
  localToWorld(x, y, z) → Cartesian3
  // Cesium 相机位置 → Five offset
  cesiumCameraToFiveOffset() → { offset, distance, fov }
  // Five 相机 offset → Cesium setView
  fiveCameraToCesium() → void
  // 每帧同步（注册到 viewer.scene.preRender）
  syncFrame()
  // 模型淡入/淡出
  setModelOpacity(0~1)
}
```

---

## 8. 分阶段实施

### Phase 1：单厂标定（验证可行性）—— 约 1~2 天

1. 新建 `EarthVRStage.vue`，把 Cesium 初始化和 Five 初始化放进同页两个 canvas。
2. 先用南厂 `vrID: oLkzny3lwSX1eAEt2x` 试点。
3. Five 加载完成后读 `five.modelScene.boundingBox`，手动调 `modelAnchor.origin` 和 `heading`，让模型和 config 里的厂区圈地多边形在 Cesium 上对齐。
4. 验证：模型确实出现在天地图影像上对应厂区位置。

### Phase 2：相机同步与状态机 —— 约 2~3 天

1. 实现 `EarthVRBridge` 的正/逆变换矩阵。
2. 实现三状态机（Overview → Approach → VR）。
3. 实现 `pointer-events` 切换和模型淡入淡出。
4. 从地球点击标记 → 飞到厂区 → 模型淡入 → 进入 Five 漫游，全程不跳路由。

### Phase 3：多厂切换与现有功能迁移 —— 约 2~3 天

1. 5 个厂区分别标定 `modelAnchor`。
2. 迁移现有的热点标签（PanoTagPlugin）、导览轮播、左右面板到 `EarthVRStage`。
3. "返回地球"按钮改为拉远淡出，不再 `router.push('/')`。
4. 清理旧路由 `/vr/:vrID` 的独立页面逻辑。

---

## 9. 风险与注意

| 风险                    | 说明                                                            | 应对                                                                        |
| ----------------------- | --------------------------------------------------------------- | --------------------------------------------------------------------------- |
| 双 WebGL context 性能   | 同时跑 Cesium + Five，显存和帧率压力大                          | Overview 阶段暂停 Five 渲染（`five.onlyRenderIfNeeds`）；Approach 再 resume |
| 模型锚点不准            | Five 模型原点和 config 经纬度不一定重合                         | `modelAnchor` 参数化，每个厂区一次性标定；提供调试模式微调                  |
| 深度不共享              | 两个 WebGL context 无法共享 depth buffer                        | 不做像素级深度合成，靠相机高度 + lift 控制视觉贴合；模型和地球表面不穿插    |
| 模型朝向偏差            | Five 模型"前"方向不一定朝北                                     | `modelAnchor.heading` 参数旋转校正                                          |
| Five 透明 canvas 的事件 | 上层 canvas 会挡住 Cesium 的鼠标事件                            | 按状态切换 `pointer-events`，Overview 时 Five canvas 设 `none`              |
| 天地图层级              | img_w 最大 zoom 18，近距离看模型时底图分辨率够不够              | 模型本身分辨率足够，底图只作地理参考；必要时叠加更高层级影像                |
| three.js 版本           | static-demo 用 three 0.160，VR数字工厂 的 five 6.8.9 自带 three | 不直接引用 three，统一用 five 暴露的 three（`five.THREE`）                  |
| 部署兼容性              | 双 canvas + Cesium + Five 体积更大                              | `chunkSizeWarningLimit` 已设 5000；按需分包，Five 和 Cesium 分开 chunk      |

---

## 10. 与 static-demo 的差异对照

| 项         | static-demo                       | VR数字工厂 本方案                                               |
| ---------- | --------------------------------- | --------------------------------------------------------------- |
| 底图       | CARTO 瓦片拼 three.js 平面        | 天地图 img_w（Cesium 原生）                                     |
| 地图范围   | work.json 里的 `mapBbox` 手动指定 | config 里的 `longitude/latitude/points`，Cesium 原生            |
| 模型承载   | five.scene 一个渲染器             | Cesium + Five 双 canvas，桥接同步                               |
| 地理位置感 | 平面贴图，无地球曲率              | 真地球，可从太空钻到厂区                                        |
| 署名要求   | 需保留 "© OSM © CARTO"            | 天地图按 tk 服务条款署名（已在 creditContainer 隐藏，按需处理） |
