import config from '@/config'
import { useTokenStore } from '@/stores/token'
import { VRPost, VRGet } from '@/utils/request'

/**
 * VR 认证：使用 appId / appSecret 换取 access_token
 * 通过 vrInstance（走 vite 代理）调用，避免直接暴露目标地址
 */
export async function fetchVrAccessToken() {
  const data = await VRPost('/auth/access_token', {
    app_key: config.appId,
    app_secret: config.appSecret,
  })
  const tokenStore = useTokenStore()
  tokenStore.setVrToken(data.access_token, data.expire_at)
  return data.access_token
}

/**
 * 保证 VR token 有效（过期或不存在则重新获取）
 * 在 VR 入口页面进入时调用
 */
export async function ensureVrToken() {
  const tokenStore = useTokenStore()
  if (!tokenStore.isVrTokenValid) {
    await fetchVrAccessToken()
  }
}

/**
 * 获取 VR 场景基础数据
 * GET /open/v3/vr/info?resource_code={vrID}
 * 返回数据可直接传给 parseWork 供 Five SDK 渲染
 * @param {string} resourceCode - VR 标识（vrID）
 * @returns {Promise<object>} VR 场景数据（data 字段）
 */
export async function fetchVrInfo(resourceCode) {
  return VRGet('/open/v3/vr/info', { resource_code: resourceCode })
}

/**
 * 获取 VR 场景热点标签
 * GET /open/v3/hot/list?resource_code={vrID}
 * 返回 data（{ tagList, globalConfig, contentTypeConfig }）可直接传给 PanoTagPlugin.load()
 * @param {string} resourceCode - VR 标识（vrID）
 * @returns {Promise<object>} 热点标签数据（data 字段）
 */
export async function fetchVrHotTags(resourceCode) {
  return VRGet('/open/v3/hot/list', { resource_code: resourceCode })
}

/**
 * 获取 VR 点位导览列表
 * GET /open/v3/observerpicture/search?resource_code={vrID}
 * 返回 data.list[]（含 name / pano_index / picture_url / data.heading / data.fov）
 * @param {string} resourceCode - VR 标识（vrID）
 * @returns {Promise<object>} 导览数据（data 字段，list 为导览项数组）
 */
export async function fetchVrObserverPictures(resourceCode) {
  return VRGet('/open/v3/observerpicture/search', { resource_code: resourceCode })
}
