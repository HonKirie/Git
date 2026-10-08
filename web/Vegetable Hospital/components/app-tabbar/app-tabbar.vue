<template>
	<view class="tabbar">
		<view v-for="tab in tabs" :key="tab.name" class="tab" :class="{ active: selectedTab === tab.name }" @tap="switchPage(tab)">
			<image class="tab-icon" :src="tabIcon(tab)" mode="aspectFit" />
			<text class="tab-name">{{ tab.name }}</text>
		</view>
	</view>
</template>

<script>
export default {
	props: {
		current: {
			type: String,
			required: true
		}
	},
	data() {
		return {
			selectedTab: this.current,
			tabs: [
				{ name: '首页', path: '/pages/index/index', icon: '/static/tab-home.png' },
				{ name: '地方特色', path: '/pages/local/index', icon: '/static/tab-local.png', activeIcon: '/static/tab-local-active.png' },
				{ name: '附近农资', path: '/pages/nearby/index', icon: '/static/tab-nearby.png', activeIcon: '/static/tab-nearby-active.png' },
				{ name: '消息', path: '/pages/message/index', icon: '/static/tab-message.png', activeIcon: '/static/tab-message-active.png' },
				{ name: '我的', path: '/pages/profile/index', icon: '/static/tab-profile.png', activeIcon: '/static/tab-profile-active.png' }
			]
		}
	},
	methods: {
		tabIcon(tab) {
			if (tab.name === '首页' && this.selectedTab === '首页') {
				return '/static/tab-home-active.png'
			}
			return tab.name === this.selectedTab && tab.activeIcon ? tab.activeIcon : tab.icon
		},
		switchPage(tab) {
			if (tab.name !== this.selectedTab) {
				this.selectedTab = tab.name
				uni.navigateTo({
					url: tab.path,
					fail: () => {
						this.selectedTab = this.current
						uni.showToast({ title: '页面跳转失败', icon: 'none' })
					}
				})
			}
		}
	}
}
</script>

<style scoped>
.tabbar { position: fixed; right: 0; bottom: 0; left: 0; z-index: 10; height: 112rpx; padding-bottom: env(safe-area-inset-bottom); display: flex; background: #fff; box-shadow: 0 -1rpx 8rpx rgba(0, 0, 0, .02); }
.tab { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2rpx; }
.tab-icon { width: 38rpx; height: 38rpx; }
.tab-name { color: #111; font-size: 23rpx; }
.active .tab-name { color: #40c997; }
</style>
