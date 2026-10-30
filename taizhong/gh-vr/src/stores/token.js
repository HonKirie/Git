import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTokenStore = defineStore('token', () => {
  // 系统默认 token（预留，暂为空）
  const defaultToken = ref('')
  // VR 网关 access_token
  const vrAccessToken = ref('')
  // VR token 过期时间（秒级时间戳）
  const vrExpireAt = ref(0)

  function setDefaultToken(value) {
    defaultToken.value = value
  }

  function setVrToken(accessToken, expireAt) {
    vrAccessToken.value = accessToken
    vrExpireAt.value = expireAt
  }

  function clearVrToken() {
    vrAccessToken.value = ''
    vrExpireAt.value = 0
  }

  // VR token 是否有效（提前 60s 视为过期）
  const isVrTokenValid = computed(() =>
    !!vrAccessToken.value &&
    vrExpireAt.value - Math.floor(Date.now() / 1000) > 60
  )

  return {
    defaultToken,
    vrAccessToken,
    vrExpireAt,
    isVrTokenValid,
    setDefaultToken,
    setVrToken,
    clearVrToken,
  }
}, {
  // 持久化到 localStorage
  persist: true,
})
