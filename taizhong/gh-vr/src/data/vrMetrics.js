/**
 * VR 大屏面板静态占位数据
 * 内容参考设计稿（泰中特种纸数字孪生系统，附件 1）
 * 待对接业务系统真实 API 时，可在对应组件中替换为接口数据
 */

/** 数据概览 —— 四宫格（icon 对应 src/assets/data-*.png|svg） */
export const overviewItems = [
  { name: '监测点', value: '12', icon: 'data-1' },
  { name: '污水池', value: '4', icon: 'data-2' },
  { name: '设备', value: '8', icon: 'data-3' },
  { name: '处理量', value: '123', icon: 'data-4' },
]

/** 水质指标趋势 */
export const waterTrend = {
  updateTime: '2025-09-10 09:40:07',
  indicators: [
    { name: 'COD', value: '80%', color: '#22c0ff' },
    { name: '总磷', value: '80%', color: '#00e676' },
    { name: '氨氮', value: '80%', color: '#ffb300' },
    { name: '总氮', value: '80%', color: '#ff5252' },
  ],
  months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月'],
  // 图例顺序与设计稿一致：COD / 氨氮 / 总氮 / 总磷
  series: [
    { name: 'COD', color: '#22c0ff', data: [322, 296, 271, 253, 240, 228, 219, 205, 192, 178] },
    { name: '氨氮', color: '#00e676', data: [275, 262, 251, 244, 233, 221, 210, 198, 185, 170] },
    { name: '总氮', color: '#ffb300', data: [352, 331, 308, 287, 269, 255, 241, 230, 215, 198] },
    { name: '总磷', color: '#ff5252', data: [236, 225, 218, 209, 201, 190, 183, 174, 166, 155] },
  ],
}

/** 污水处理率：进水量/出水量柱状图 + 合格率折线图（右轴 %） */
export const sewageRate = {
  months: ['01月', '02月', '03月', '04月', '05月', '06月', '07月', '08月'],
  inlet: [620, 580, 645, 602, 668, 625, 590, 632], // 进水量（占位，单位 m³）
  outlet: [556, 522, 578, 540, 602, 563, 532, 571], // 出水量（占位，单位 m³）
  rate: [92, 94, 95, 93, 96, 95, 94, 96], // 合格率（%）
}

/** 告警处理状态 —— 环形图 */
export const alarmStatus = {
  total: 548,
  items: [
    { name: '待处理', count: 2, pct: 4, color: '#ff5252' },
    { name: '已处理', count: 20, pct: 38, color: '#00e676' },
    { name: '处理中', count: 30, pct: 58, color: '#ffb300' },
  ],
}

/** 告警动态（超出容器高度滚动显示） */
export const alarmList = [
  { time: '2023-01-16 09:41', type: '水质告警', detail: '1号污水池 总磷超标(20000)', level: 'danger' },
  { time: '2023-01-16 09:37', type: '能耗告警-电', detail: '3号污水池-调节池 用电量异常', level: 'warning' },
  { time: '2023-01-16 09:30', type: '能耗告警-电', detail: '3号污水池-调节池 耗电量超标', level: 'warning' },
  { time: '2023-01-16 09:21', type: '水质告警', detail: '1号污水池 总磷超标(20000)', level: 'danger' },
  { time: '2023-01-16 09:08', type: '能耗告警-气', detail: '3号污水池-调节池 用气量异常', level: 'warning' },
  { time: '2023-01-16 08:55', type: '水质告警', detail: '2号污水池 氨氮超标(15.2)', level: 'danger' },
  { time: '2023-01-16 08:40', type: '设备告警', detail: '2号提升泵 电流波动异常', level: 'info' },
  { time: '2023-01-16 08:12', type: '水质告警', detail: '1号污水池 总磷超标(20000)', level: 'danger' },
  { time: '2023-01-16 07:58', type: '能耗告警-水', detail: '南厂区 用水量超阈值', level: 'warning' },
  { time: '2023-01-16 07:30', type: '设备告警', detail: '3号鼓风机 温度偏高', level: 'info' },
]
