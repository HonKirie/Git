<template>
  <view class="page">
    <view class="profile-hero">
      <view class="header"><text class="back" @tap="goBack">‹</text><text class="title">用户主页</text><view class="space" /></view>
      <view class="profile-user"><view class="avatar">{{ avatarText }}</view><view class="user-copy"><text class="name">{{ displayName }}</text><text class="tag">{{ identityLabel }}</text></view></view>
      <view v-if="isOwner" class="profile-stats"><view><text class="stat-value">172.63</text><text class="stat-label">余额</text></view><view><text class="stat-value">200</text><text class="stat-label">田园币</text></view><view><text class="stat-value">2600</text><text class="stat-label">积分</text></view><view><text class="stat-value">LV.1</text><text class="stat-label">推广等级</text></view></view>
    </view>
    <view v-if="isOwner" class="order-section"><view class="section-heading"><text class="section-title">我的订单</text><text class="section-more" @tap="showTip('全部订单功能即将开放')">全部订单 ›</text></view><view class="order-actions"><view v-for="label in orderLabels" :key="label" class="order-action" @tap="showTip(label + '功能即将开放')"><text class="order-icon">{{ orderIcons[label] }}</text><text class="order-label">{{ label }}</text></view></view></view>
    <view class="tabs"><text :class="{ active: tab === 'reposts' }" @tap="tab = 'reposts'">转发</text><text :class="{ active: tab === 'favorites' }" @tap="tab = 'favorites'">收藏</text></view>
    <view v-if="loading" class="empty">正在加载内容...</view><view v-else-if="!items.length" class="empty">暂无{{ tab === 'reposts' ? '转发' : '收藏' }}内容</view>
    <view v-else class="list"><view v-for="item in items" :key="item.postId" class="card" @tap="openPost(item)"><view class="meta"><view class="small-avatar">农</view><view><text class="card-name">{{ item.authorId || item.authorAccount || '农友' }}</text><text class="date">{{ item.createdAt || '' }}</text></view></view><text class="description">{{ item.description || '内容' }}</text><text class="source">原内容：{{ item.crop || '田园动态' }}</text></view></view>
  </view>
</template>

<script>
export default {
  data() { return { account: '', displayName: '农友', identityLabel: '农友', tab: 'reposts', reposts: [], favorites: [], loading: false, isOwner: false, orderLabels: ['待付款', '待发货', '待收货', '待评价'], orderIcons: { 待付款: '￥', 待发货: '▣', 待收货: '▱', 待评价: '☆' } } },
  computed: { items() { return this.tab === 'reposts' ? this.reposts : this.favorites }, avatarText() { return (this.displayName || '农').slice(0, 1) } },
  onLoad(options) { this.account = decodeURIComponent((options && options.account) || ''); if (options && (options.tab === 'favorites' || options.tab === 'reposts')) this.tab = options.tab; this.loadProfileData() },
  onShow() { if (this.account) this.loadProfileData() },
  methods: {
    goBack() { uni.navigateBack() },
    showTip(title) { uni.showToast({ title, icon: 'none' }) },
    openPost(item) { if (item.postId) uni.navigateTo({ url: '/pages/hospital/detail?id=' + item.postId }) },
    loadProfileData() {
      const currentUser = uni.getStorageSync('currentUser') || ''
      this.isOwner = !!uni.getStorageSync('loginSession') && this.account === currentUser
      this.displayName = this.isOwner ? (uni.getStorageSync('profileDisplayName') || currentUser || '农友') : (this.account || '农友')
      uni.request({ url: 'http://123.57.255.171:8080/mobile/users/' + encodeURIComponent(this.account), method: 'GET', success: (res) => { const body = res.data || {}; if (body.code === 200 && body.data) this.displayName = body.data.nickName || body.data.userName || this.account } })
      this.reposts = []
      this.favorites = []
      uni.request({ url: 'http://123.57.255.171:8080/mobile/reposts/user/' + encodeURIComponent(this.account), method: 'GET', success: (res) => { const body = res.data || {}; if (body.code === 200 && Array.isArray(body.data)) this.reposts = body.data.map(item => ({ postId: item.postId, authorId: item.originalAuthorId, authorAccount: item.originalAuthorAccount, crop: item.crop, plantingTime: item.plantingTime, description: item.description, createdAt: item.createdAt })) } })
      this.loadFavorites()
    },
    loadFavorites() {
      this.loading = true
      uni.request({ url: 'http://123.57.255.171:8080/mobile/favorites/user/' + encodeURIComponent(this.account), method: 'GET', success: (res) => { const body = res.data || {}; if (body.code === 200 && Array.isArray(body.data)) this.favorites = body.data.map(item => ({ postId: item.postId, authorId: item.originalAuthorId, authorAccount: item.originalAuthorAccount, crop: item.crop, description: item.description, createdAt: item.createdAt })) }, complete: () => { this.loading = false } })
    }
  }
}
</script>

<style scoped>
.page{min-height:100vh;background:#f4f6f5}.profile-hero{padding-bottom:30rpx;background:linear-gradient(155deg,#66e3ac,#e9f9f0)}.header{height:100rpx;padding:0 30rpx;display:flex;align-items:center;justify-content:space-between}.back,.space{width:64rpx}.back{color:#14583d;font-size:70rpx}.title{color:#14583d;font-size:34rpx;font-weight:600}.profile-user{display:flex;align-items:center;padding:20rpx 32rpx}.avatar,.small-avatar{border-radius:50%;background:#a9edc9;color:#287c56;text-align:center}.avatar{width:128rpx;height:128rpx;font-size:48rpx;line-height:128rpx}.user-copy{margin-left:24rpx}.name{display:inline-block;color:#124d37;font-size:40rpx;font-weight:600}.tag{margin-left:14rpx;padding:5rpx 12rpx;border-radius:5rpx;background:#f0aa38;color:#fff;font-size:20rpx}.profile-stats{display:flex;justify-content:space-between;margin-top:18rpx;text-align:center}.profile-stats>view{flex:1}.stat-value,.stat-label{display:block}.stat-value{color:#173f30;font-size:32rpx}.stat-label{margin-top:10rpx;color:#3e715b;font-size:22rpx}.order-section{margin:18rpx 20rpx 24rpx;padding:26rpx 22rpx 24rpx;border-radius:18rpx;background:#fff}.section-heading{display:flex;justify-content:space-between}.section-title{font-size:30rpx;font-weight:600}.section-more{color:#7f9189;font-size:23rpx}.order-actions{display:flex;margin-top:26rpx}.order-action{flex:1;display:flex;flex-direction:column;align-items:center}.order-icon{width:58rpx;height:58rpx;border-radius:50%;background:#e9f8f0;color:#2bb77b;font-size:31rpx;line-height:58rpx;text-align:center}.order-label{margin-top:12rpx;color:#53655d;font-size:23rpx}.tabs{height:82rpx;display:flex;align-items:center;background:#fff}.tabs text{width:50%;color:#8c9891;font-size:27rpx;text-align:center}.tabs text.active{color:#168457;font-weight:600}.list{padding:18rpx 20rpx}.card{margin-bottom:18rpx;padding:24rpx;border-radius:12rpx;background:#fff}.meta{display:flex;align-items:center}.small-avatar{width:64rpx;height:64rpx;line-height:64rpx}.card-name{display:block;margin-left:14rpx;font-size:28rpx;font-weight:600}.date{display:block;margin:4rpx 0 0 14rpx;color:#9aa59f;font-size:21rpx}.description{display:block;margin-top:22rpx;color:#4f5d55;font-size:27rpx}.source{display:block;margin-top:14rpx;color:#8d9992;font-size:22rpx}.empty{padding-top:180rpx;text-align:center;color:#9aa59f;font-size:27rpx}
</style>
