// 以如视后台标签标题（设备编号）匹配设备卡片。
export const deviceCards = {
  '17D526QT01': {
    name: '化机浆段在线浓度变送器',
    status: '正常运行',
    statusColor: '#4ade80',
    metrics: [
      { icon: '📊', label: '实时浆浓', value: '4.24 %', note: '设定值: 4.00%' },
      { icon: '🌡️', label: '介质温度', value: '65.2 ℃' },
      { icon: '🌊', label: '管道压力', value: '0.28 MPa' },
    ],
    maintenance: '距下一次冲洗 25 分钟',
    tip: '点击可展开查看近24小时浓度趋势',
  },
}

// 按用户提供的 skill.md 与现场照片制作，名称和显示编号由用户指定。
// 下列监测值及冲洗信息由用户提供，当前为静态配置，尚未接入实时数据源。
export const photographedDeviceCard = {
  name: '碱回收蒸发工段检测单元',
  deviceId: '17D549QT01',
  status: '正常运行',
  statusColor: '#4ade80',
  metrics: [
    { icon: '📊', label: '实时浓度', value: '45.62 %', note: '目标: 45.00%' },
    { icon: '🌡️', label: '介质温度', value: '88.5 ℃', note: '范围: 80–95 ℃' },
    { icon: '🌊', label: '管道压力', value: '0.32 MPa', note: '范围: 0.2–0.5 MPa' },
  ],
  maintenance: '自动水冲洗：距下次冲洗还有 22 分钟',
  maintenanceLabel: '冲洗计划',
  washStatus: '待机中（上次冲洗正常）',
  tip: '当前为配置值；实时数据与历史趋势待接入',
}

// 兼容现有后台标题，以及后续将标题改成节点名称或设备编号的情况。
for (const title of ['碱回收', photographedDeviceCard.name, photographedDeviceCard.deviceId]) {
  deviceCards[title] = photographedDeviceCard
}
