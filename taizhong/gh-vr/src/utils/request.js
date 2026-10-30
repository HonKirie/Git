import axios from 'axios'
import config from '@/config'
import { useTokenStore } from '@/stores/token'

// VR 网关代理前缀（由 vite.config.js 中的 server.proxy 代理到 VITE_VR_SERVER）
const VR_BASE = '/vr-api'

/**
 * 获取 token store 实例
 * 注：在请求拦截器内获取，确保 pinia 已初始化
 */
function getTokenStore() {
  return useTokenStore()
}

/**
 * 静默换 token（用裸 axios 调鉴权接口，避免循环依赖）
 * @returns {Promise<boolean>} 是否成功
 */
async function silentRefreshVrToken() {
  try {
    const resp = await axios.post(`${VR_BASE}/auth/access_token`, {
      app_key: config.appId,
      app_secret: config.appSecret,
    })
    const body = resp.data || {}
    if (body.code === 0) {
      const data = body.data || {}
      const tokenStore = getTokenStore()
      tokenStore.setVrToken(data.access_token, data.expire_at)
      return true
    }
    return false
  } catch {
    return false
  }
}

/* ---------------- 基础实例（系统默认 token） ---------------- */
const baseInstance = axios.create({
  timeout: 15000,
})

baseInstance.interceptors.request.use((cfg) => {
  const tokenStore = getTokenStore()
  // 调用方显式传入 Authorization 时优先使用，否则使用系统默认 token
  if (!cfg.headers?.Authorization && tokenStore.defaultToken) {
    cfg.headers.Authorization = `Bearer ${tokenStore.defaultToken}`
  }
  return cfg
})

baseInstance.interceptors.response.use(
  (res) => res.data,
  (error) => Promise.reject(error)
)

/* ---------------- VR 实例（VRServer access_token） ---------------- */
const vrInstance = axios.create({
  baseURL: VR_BASE,
  timeout: 15000,
})

vrInstance.interceptors.request.use((cfg) => {
  const tokenStore = getTokenStore()
  // 仅注入已有的 VR access_token；鉴权由调用方（页面）主动 ensureVrToken
  // 调用方显式传入 Authorization 时优先使用
  if (!cfg.headers?.Authorization && tokenStore.vrAccessToken) {
    cfg.headers.Authorization = tokenStore.vrAccessToken
  }
  return cfg
})

vrInstance.interceptors.response.use(
  (res) => {
    // VRServer 规范：code === 0 为成功，数据在 res.data.data
    const body = res.data || {}
    if (body.code === 0) {
      return body.data
    }
    // token 失效：code === -3，静默换 token 并重试原请求
    if (body.code === -3 && !res.config.__retried) {
      const tokenStore = getTokenStore()
      tokenStore.clearVrToken()
      return silentRefreshVrToken().then((ok) => {
        if (ok) {
          const newToken = tokenStore.vrAccessToken
          const retryCfg = { ...res.config, __retried: true }
          if (newToken) retryCfg.headers.Authorization = newToken
          return vrInstance.request(retryCfg)
        }
        const err = new Error('VR token 刷新失败')
        err.code = -3
        err.raw = body
        return Promise.reject(err)
      })
    }
    // 业务失败：抛出包含 code/message 的错误
    const err = new Error(body.status || 'VR request failed')
    err.code = body.code
    err.raw = body
    err.requestId = body.request_id
    return Promise.reject(err)
  },
  (error) => {
    // 401 视为 VR token 失效，静默换 token 并重试
    if (error?.response?.status === 401 && !error.config?.__retried) {
      const tokenStore = getTokenStore()
      tokenStore.clearVrToken()
      return silentRefreshVrToken().then((ok) => {
        if (ok) {
          const newToken = tokenStore.vrAccessToken
          const retryCfg = { ...error.config, __retried: true }
          if (newToken) retryCfg.headers.Authorization = newToken
          return vrInstance.request(retryCfg)
        }
        return Promise.reject(error)
      })
    }
    return Promise.reject(error)
  }
)

/* ---------------- 暴露 API ---------------- */

// 基础请求（系统默认 token）
export function get(url, params, cfg = {}) {
  return baseInstance({ method: 'get', url, params, ...cfg })
}

export function post(url, data, cfg = {}) {
  return baseInstance({ method: 'post', url, data, ...cfg })
}

// VR 请求（VRServer access_token，由调用方确保已鉴权）
export function VRGet(url, params, cfg = {}) {
  return vrInstance({ method: 'get', url, params, ...cfg })
}

export function VRPost(url, data, cfg = {}) {
  return vrInstance({ method: 'post', url, data, ...cfg })
}

export default {
  get,
  post,
  VRGet,
  VRPost,
}
