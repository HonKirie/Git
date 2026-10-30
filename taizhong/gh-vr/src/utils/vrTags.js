/** 管理后台的可编辑圆点样式位于 extraData，SDK 只读取 style.point。 */
export function syncTagPointStyle(tag) {
  const point = tag.extraData?.style?.point
  if (!point || typeof point !== 'object') return tag
  return {
    ...tag,
    style: {
      ...tag.style,
      // 保留接口提供的图标 URL，同时同步颜色、透明度和尺寸。
      point: { ...tag.style?.point, ...point },
    },
  }
}

export function getTagDeviceId(tag) {
  return typeof tag?.data?.title === 'string' ? tag.data.title.trim() : ''
}
