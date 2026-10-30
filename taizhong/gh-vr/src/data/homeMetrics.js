/**
 * 首页两侧指标卡片静态占位数据
 * 待对接业务系统真实 API 时可直接在此替换或接入接口
 */
export const leftMetrics = [
  {
    id: 'production',
    title: '生产运行',
    items: [
      { label: '今日产量', value: '286.5', unit: 't' },
      { label: '生产订单', value: '42', unit: '单' },
      { label: '进行中任务', value: '12', unit: '', status: 'success' },
      {
        label: '分切方案',
        value: '6',
        unit: '个',
        subtext: '准交率 96.2%',
      },
    ],
  },
  {
    id: 'equipment',
    title: '设备运行',
    items: [
      { label: '设备总数', value: '158', unit: '台' },
      { label: 'OEE 综合效率', value: '82.4%', unit: '', status: 'info' },
      {
        label: '运行中',
        value: '126',
        unit: '',
        status: 'success',
        extra: { label: '/ 故障', value: '3', status: 'danger' },
      },
      { label: '待办巡检', value: '15', unit: '', status: 'warning' },
      { label: '维修工单', value: '7', unit: '', status: 'danger' },
    ],
  },
  {
    id: 'energy',
    title: '能源能耗',
    items: [
      { label: '今日用电', value: '128,600', unit: 'kWh' },
      { label: '吨纸电耗', value: '486', unit: 'kWh/t' },
      { label: '今日耗水', value: '3,120', unit: 't' },
      { label: '碳排放(月)', value: '18.6', unit: 'tCO₂', status: 'warning' },
    ],
  },
]

export const rightMetrics = [
  {
    id: 'storage',
    title: '仓储库存',
    items: [
      { label: '原料库存', value: '8,642', unit: 't' },
      { label: '今日入库', value: '23', unit: '', status: 'success' },
      { label: '今日出库', value: '31', unit: '', status: 'danger' },
      { label: '库存预警', value: '4', unit: '项', status: 'warning' },
      { label: '周转率', value: '6.8', unit: '次/月' },
    ],
  },
  {
    id: 'safety',
    title: '安环隐患',
    items: [
      { label: '重大危险源', value: '2', unit: '处', status: 'danger' },
      { label: '风险点', value: '86', unit: '处' },
      { label: '待排查任务', value: '9', unit: '', status: 'warning' },
      { label: '今日新增隐患', value: '3', unit: '', status: 'danger' },
    ],
  },
  {
    id: 'water',
    title: '水质质量',
    items: [
      { label: '出水 COD', value: '28.5', unit: 'mg/L', status: 'success' },
      { label: '出水氨氮', value: '1.2', unit: 'mg/L', status: 'success' },
      { label: '日处理水量', value: '12,600', unit: 'm³' },
      { label: '综合健康度', value: '87', unit: '分', status: 'info' },
    ],
  },
]
