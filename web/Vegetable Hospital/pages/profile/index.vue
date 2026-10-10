<template>
	<view class="page">
		<view v-if="!loggedIn" class="login-hero">
			<view class="system-status-inset" />
			<view class="login-body">
				<text class="welcome-title">欢迎登录田园采风</text>
				<view class="login-form">
					<view class="input-row"><text class="input-label">账号</text><input v-model.trim="account" class="login-input" placeholder="请输入账号" placeholder-class="input-placeholder" /></view>
					<view class="input-row"><text class="input-label">密码</text><input v-model="password" class="login-input" password placeholder="请输入密码" placeholder-class="input-placeholder" /></view>
					<button class="login-button" :class="{ disabled: !canLogin }" @click="login">登录</button>
					<view class="form-links single-link"><text @tap="goRegister">没有账号？去注册</text></view>
				</view>
			</view>
			<view class="agreement"><text class="agreement-mark" @click="agreed = !agreed">{{ agreed ? '✓' : '' }}</text><text>我已阅读并同意《用户服务协议》和《隐私政策》</text></view>
		</view>
		<view v-else class="profile-hero">
			<view class="profile-top"><text class="profile-back" @tap="logout">‹</text><text class="profile-title">我的</text><view class="profile-actions"><text>客服</text><text @tap="openSettingsDrawer">设置</text></view></view>
			<view class="profile-user">
				<view class="avatar-picker" @tap="chooseAvatar">
					<image class="avatar" :src="avatarUrl" mode="aspectFill" />
					<view class="avatar-edit"><text>相机</text></view>
				</view>
				<view class="user-copy"><view class="user-name-row"><text class="user-name">{{ username }}</text><text class="user-badge expert">{{ identityLabel }}</text></view><view class="user-links"><text>⌖ 收货地址</text><text @tap="openMyProfile">▢ 我的主页</text></view></view>
			</view>
			<view class="profile-stats"><view><text class="stat-value">172.63</text><text class="stat-label">余额</text></view><view><text class="stat-value">200</text><text class="stat-label">田园币</text></view><view><text class="stat-value">2600</text><text class="stat-label">积分</text></view><view><text class="stat-value">LV.1</text><text class="stat-label">推广等级</text></view></view>
		</view>
		<view v-if="loggedIn" class="order-section">
			<view class="section-heading"><text class="section-title">我的订单</text><text class="section-more" @tap="openOrder('全部订单')">全部订单 ›</text></view>
			<view class="order-actions">
				<view class="order-action" @tap="openOrder('待付款')"><view class="order-icon"><image class="order-icon-image" src="/static/icons/wallet.png" mode="aspectFit" /></view><text class="order-label">待付款</text></view>
				<view class="order-action" @tap="openOrder('待发货')"><view class="order-icon"><image class="order-icon-image" src="/static/icons/pending-shipment.png" mode="aspectFit" /></view><text class="order-label">待发货</text></view>
				<view class="order-action" @tap="openOrder('待收货')"><view class="order-icon"><image class="order-icon-image" src="/static/icons/pending-receipt.png" mode="aspectFit" /></view><text class="order-label">待收货</text></view>
				<view class="order-action" @tap="openOrder('待评价')"><view class="order-icon"><image class="order-icon-image" src="/static/icons/pending-review.png" mode="aspectFit" /></view><text class="order-label">待评价</text></view>
			</view>
		</view>
		<view v-if="loggedIn" class="profile-entry" @tap="openOrder('我的收藏')">
			<view class="profile-entry-main"><text class="profile-entry-icon">☆</text><text class="profile-entry-label">我的收藏</text></view>
			<text class="profile-entry-arrow">›</text>
		</view>
		<uni-drawer v-if="loggedIn" ref="settingsDrawer" mode="right" :width="260" :mask-click="false" @change="settingsDrawerChange">
			<view class="settings-drawer">
				<view class="settings-drawer-header"><text class="settings-drawer-close" @tap="closeSettingsDrawer">‹</text><text class="settings-drawer-title">设置</text><view class="settings-drawer-space" /></view>
				<scroll-view class="settings-scroll" scroll-y="true">
					<view v-for="item in settingItems" :key="item.label" class="settings-item" @tap="settingsAction(item)">
						<image v-if="item.label === '个人信息设置'" class="settings-icon-image" src="/static/icons/settings-gear.png" mode="aspectFit" /><image v-else-if="item.label === '观看历史'" class="settings-icon-image" src="/static/icons/history-clock.png" mode="aspectFit" /><image v-else-if="item.label === '我的客服'" class="settings-icon-image" src="/static/icons/customer-service.png" mode="aspectFit" /><image v-else-if="item.label === '我的二维码'" class="settings-icon-image" src="/static/icons/my-qr-code.png" mode="aspectFit" /><image v-else-if="item.label === '通讯录'" class="settings-icon-image" src="/static/icons/contacts.png" mode="aspectFit" /><image v-else-if="item.label === '我的钱包'" class="settings-icon-image" src="/static/icons/wallet.png" mode="aspectFit" /><text v-else class="settings-icon">{{ item.icon }}</text><text class="settings-label">{{ item.label }}</text><text class="settings-arrow">›</text>
					</view>
					<view class="logout-button" @tap="logout">退出登录</view>
				</scroll-view>
			</view>
		</uni-drawer>
		<app-tabbar current="我的" />
	</view>
</template>

<script>
export default {
	data() {
		return { account: '', password: '', agreed: false, loading: false, loggedIn: false, settingsOpen: false, username: '农友', identityLabel: '农友', avatarUrl: '/static/logo.png', settingItems: [{ icon: '⚙', label: '个人信息设置' }, { icon: '◷', label: '观看历史' }, { icon: '♧', label: '我的客服' }, { icon: '▦', label: '我的二维码' }, { icon: '▧', label: '通讯录' }, { icon: '▱', label: '我的钱包' }] }
	},
	computed: {
		canLogin() { return this.account.length > 0 && this.password.length > 0 }
	},
	onShow() {
		this.loadProfile()
	},
	methods: {
		showTip(title) { uni.showToast({ title, icon: 'none' }) },
		openSettingsDrawer() {
			this.settingsOpen = true
			this.$nextTick(() => this.$refs.settingsDrawer.open())
		},
		closeSettingsDrawer() {
			if (this.$refs.settingsDrawer) this.$refs.settingsDrawer.close()
			this.settingsOpen = false
		},
		settingsDrawerChange(open) { this.settingsOpen = open },
		openOrder(label) { if (label === '我的收藏') return this.openMyProfile('favorites'); this.showTip(label + '功能即将开放') },
		openMyProfile(tab) { const account = uni.getStorageSync('currentUser') || this.username; uni.navigateTo({ url: '/pages/profile/user?account=' + encodeURIComponent(account) + (tab ? '&tab=' + tab : '') }) },
		settingsAction(item) {
			if (item.label === '个人信息设置') {
				this.closeSettingsDrawer()
				setTimeout(() => uni.navigateTo({ url: '/pages/profile/edit' }), 320)
				return
			}
			this.showTip(`${item.label}功能即将开放`)
		},
		chooseAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					const filePath = res.tempFilePaths[0]
					if (!filePath) return
					this.avatarUrl = filePath
					uni.setStorageSync('profileAvatar', filePath)
				}
			})
		},
		loadProfile() {
			this.avatarUrl = uni.getStorageSync('profileAvatar') || '/static/logo.png'
			const token = uni.getStorageSync('token')
			const currentUser = uni.getStorageSync('currentUser')
			if (!token || !uni.getStorageSync('loginSession') || !currentUser) {
				this.clearLoginState()
				return
			}
			// 先用已验证过的本地会话渲染个人页，再后台校验 token。
			this.username = currentUser
			this.loggedIn = true
			this.loadIdentity(token)
			uni.request({
				url: 'http://123.57.255.171:8080/getInfo',
				method: 'GET',
				header: { Authorization: 'Bearer ' + token },
				success: (res) => {
					const body = res.data || {}
					if (body.code === 200 && body.user && uni.getStorageSync('token') === token && uni.getStorageSync('loginSession')) {
						this.username = body.user.nickName || body.user.userName || '农友'
						uni.setStorageSync('profileDisplayName', this.username)
						uni.setStorageSync('currentUser', body.user.userName || this.username)
						return
					}
					if (body.code === 401) this.clearLoginState()
				},
					fail: () => {}
				})
		},
		loadIdentity(token) {
			uni.request({
				url: 'http://123.57.255.171:8080/system/dict/data/type/user_identity',
				method: 'GET',
				header: { Authorization: 'Bearer ' + token },
				success: (res) => {
					const body = res.data || {}
					const item = Array.isArray(body.data) ? body.data.find((entry) => entry.isDefault === 'Y') || body.data[0] : null
					if (item && item.dictLabel) this.identityLabel = item.dictLabel
				}
			})
		},
		goRegister() { uni.navigateTo({ url: '/pages/register/index' }) },
		login() {
			if (!this.canLogin) return this.showTip('请输入账号和密码')
			if (!this.agreed) return this.showTip('请先同意服务协议和隐私政策')
			if (this.loading) return
			this.loading = true
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/login',
				method: 'POST',
				header: { 'content-type': 'application/json' },
				data: { username: this.account, password: this.password },
				success: (res) => {
					const body = res.data || {}
					if (body.code === 200 && body.token) {
						uni.setStorageSync('token', body.token)
						uni.setStorageSync('loginSession', true)
						uni.setStorageSync('currentUser', this.account)
						uni.removeStorageSync('profileDisplayName')
						// 直接切换当前页面状态，避免重新创建页面导致登录流程被重置。
						this.username = this.account
						this.loggedIn = true
						this.loadProfile()
						return
					}
					if (body.msg === '该用户未注册') {
						uni.showModal({ title: '登录失败', content: '该用户未注册', showCancel: false })
						return
					}
					this.showTip(body.msg || '账号或密码错误')
				},
				fail: () => this.showTip('无法连接服务器，请稍后重试'),
				complete: () => { this.loading = false }
			})
		},
		logout() {
			this.closeSettingsDrawer()
			this.clearLoginState()
		},
		clearLoginState() {
			uni.removeStorageSync('token')
			uni.removeStorageSync('loginSession')
			uni.removeStorageSync('currentUser')
			uni.removeStorageSync('profileDisplayName')
			this.loggedIn = false
			this.account = ''
			this.password = ''
		},
	}
}
</script>

<style scoped>
.page { min-height: 100vh; padding-bottom: 120rpx; background: #f5f7f5; }
.profile-entry { margin: 0 20rpx 24rpx; padding: 0 22rpx; height: 88rpx; display: flex; align-items: center; justify-content: space-between; border-radius: 18rpx; background: #fff; box-shadow: 0 4rpx 18rpx rgba(27, 104, 72, .06); }.profile-entry-main { display: flex; align-items: center; }.profile-entry-icon { width: 52rpx; height: 52rpx; border-radius: 50%; background: #e9f8f0; color: #2bb77b; font-size: 30rpx; line-height: 52rpx; text-align: center; }.profile-entry-label { margin-left: 16rpx; color: #34473e; font-size: 27rpx; }.profile-entry-arrow { color: #8b9892; font-size: 48rpx; line-height: 1; }
.profile-hero { min-height: 430rpx; }
.order-section { margin: 18rpx 20rpx 24rpx; padding: 26rpx 22rpx 24rpx; border-radius: 18rpx; background: #fff; box-shadow: 0 4rpx 18rpx rgba(27, 104, 72, .06); }.section-heading { display: flex; align-items: center; justify-content: space-between; }.section-title { color: #243b31; font-size: 30rpx; font-weight: 600; }.section-more { color: #7f9189; font-size: 23rpx; }.order-actions { display: flex; margin-top: 26rpx; }.order-action { flex: 1; display: flex; flex-direction: column; align-items: center; }.order-icon { width: 58rpx; height: 58rpx; border-radius: 50%; background: #e9f8f0; color: #2bb77b; font-size: 31rpx; line-height: 58rpx; text-align: center; }.order-label { margin-top: 12rpx; color: #53655d; font-size: 23rpx; }
.order-icon-image { width: 31rpx; height: 31rpx; vertical-align: middle; }
.login-hero { position: relative; min-height: calc(100vh - 120rpx); overflow: hidden; background: linear-gradient(155deg, #9eefd1 0%, #d8f7e8 56%, #edf9f1 100%); }
.login-hero::after { content: ''; position: absolute; right: -120rpx; bottom: 180rpx; left: -120rpx; height: 300rpx; border-radius: 50% 50% 0 0; background: rgba(83, 209, 143, .14); transform: rotate(-7deg); }

.login-body { position: relative; z-index: 1; padding: 230rpx 58rpx 0; }.welcome-title { display: block; color: #176042; font-size: 46rpx; font-weight: 600; text-align: center; }.login-form { margin-top: 72rpx; }.input-row { height: 98rpx; padding: 0 26rpx; margin-top: 22rpx; display: flex; align-items: center; border-radius: 12rpx; background: #fff; }.input-label { width: 72rpx; color: #515c56; font-size: 28rpx; }.login-input { flex: 1; height: 98rpx; margin-left: 18rpx; color: #263a31; font-size: 28rpx; }.input-placeholder { color: #b7bdb9; }
.login-button { width: 360rpx; height: 92rpx; margin: 62rpx auto 0; border: 0; border-radius: 14rpx; background: #25cb88; color: #fff; font-size: 34rpx; line-height: 92rpx; }.login-button::after { border: 0; }.login-button.disabled { opacity: .58; }.form-links { display: flex; justify-content: flex-end; padding: 30rpx 16rpx; color: #318665; font-size: 25rpx; }.single-link { justify-content: center; }
.agreement { position: absolute; right: 48rpx; bottom: 48rpx; left: 48rpx; z-index: 1; display: flex; align-items: flex-start; color: #7c9387; font-size: 21rpx; line-height: 32rpx; }.agreement-mark { width: 25rpx; height: 25rpx; flex: 0 0 25rpx; margin: 3rpx 9rpx 0 0; border: 2rpx solid #76ad91; border-radius: 50%; color: #2ebc7e; font-size: 20rpx; line-height: 23rpx; text-align: center; }
.settings-drawer { width: 260px; height: 100vh; background: #fff; }.settings-drawer-header { height: 60px; padding: 0 14px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f1f2f2; box-sizing: border-box; }.settings-drawer-close { width: 30px; color: #333; font-size: 36px; line-height: 30px; }.settings-drawer-title { color: #222; font-size: 16px; font-weight: 600; }.settings-drawer-space { width: 30px; }.settings-scroll { height: calc(100vh - 60px); padding: 8px 12px; box-sizing: border-box; }.settings-item { min-height: 48px; padding: 0 8px; display: flex; align-items: center; border-bottom: 1px solid #f4f4f4; }.settings-icon { width: 26px; color: #333; font-size: 19px; }.settings-label { margin-left: 10px; color: #333; font-size: 14px; }.settings-arrow { margin-left: auto; color: #111; font-size: 28px; font-weight: 300; line-height: 1; }.logout-button { height: 36px; margin: 16px 8px 20px; border-radius: 5px; background: #e84b4b; color: #fff; font-size: 14px; font-weight: 600; line-height: 36px; text-align: center; }
.settings-icon-image { width: 18px; height: 18px; margin-left: 4px; flex: 0 0 18px; }
.history-clock-icon { position: relative; width: 18px; height: 18px; margin-left: 4px; flex: 0 0 18px; border: 2px solid #333; border-radius: 50%; box-sizing: border-box; }.history-clock-hour { position: absolute; top: 3px; left: 7px; width: 2px; height: 5px; border-radius: 1px; background: #333; transform-origin: bottom; }.history-clock-minute { position: absolute; top: 8px; left: 7px; width: 5px; height: 2px; border-radius: 1px; background: #333; }
.service-headset-icon { position: relative; width: 22px; height: 20px; margin-left: 2px; flex: 0 0 22px; }.service-headset-band { position: absolute; top: 1px; left: 2px; width: 18px; height: 13px; border: 2px solid #333; border-bottom: 0; border-radius: 12px 12px 0 0; box-sizing: border-box; }.service-headset-ear { position: absolute; top: 10px; width: 5px; height: 7px; border: 2px solid #333; border-radius: 3px; box-sizing: border-box; }.service-headset-ear-left { left: 0; }.service-headset-ear-right { right: 0; }.service-headset-mic { position: absolute; right: 1px; bottom: 0; width: 8px; height: 2px; border-radius: 1px; background: #333; }
.qr-code-icon { width: 20px; height: 20px; margin-left: 3px; display: flex; flex-wrap: wrap; gap: 3px; flex: 0 0 20px; box-sizing: border-box; }.qr-code-cell { width: 8px; height: 8px; border: 2px solid #333; box-sizing: border-box; }
.contact-card-icon { position: relative; width: 21px; height: 20px; margin-left: 3px; flex: 0 0 21px; border: 2px solid #333; border-radius: 2px; box-sizing: border-box; }.contact-avatar-icon { position: absolute; top: 4px; left: 4px; width: 4px; height: 4px; border: 1.5px solid #333; border-radius: 50%; box-sizing: border-box; }.contact-avatar-icon::after { content: ''; position: absolute; top: 5px; left: -2px; width: 5px; height: 3px; border: 1.5px solid #333; border-radius: 4px 4px 0 0; border-bottom: 0; }.contact-line { position: absolute; left: 12px; width: 5px; height: 2px; border-radius: 1px; background: #333; }.contact-line-first { top: 6px; }.contact-line-second { top: 11px; }
.service-headset-icon { position: relative; width: 20px; height: 20px; margin-left: 3px; flex: 0 0 20px; }.service-headset-band { position: absolute; top: 1px; left: 2px; width: 16px; height: 13px; border: 2px solid #333; border-bottom: 0; border-radius: 10px 10px 0 0; box-sizing: border-box; }.service-headset-ear { position: absolute; top: 10px; width: 4px; height: 7px; border: 2px solid #333; border-radius: 3px; box-sizing: border-box; }.service-headset-ear-left { left: 0; }.service-headset-ear-right { right: 0; }.service-headset-mic { position: absolute; right: 1px; bottom: 0; width: 7px; height: 2px; border-radius: 1px; background: #333; }
.profile-hero { min-height: calc(100vh - 120rpx); padding: 36rpx 24rpx 40rpx; box-sizing: border-box; background: linear-gradient(155deg, #66e3ac 0%, #9aefca 58%, #e9f9f0 100%); }.profile-top { display: flex; align-items: center; justify-content: space-between; height: 70rpx; }.profile-back { width: 70rpx; color: #14583d; font-size: 70rpx; line-height: 60rpx; }.profile-title { color: #14583d; font-size: 40rpx; font-weight: 600; }.profile-actions { display: flex; gap: 22rpx; color: #14583d; font-size: 22rpx; }.profile-user { display: flex; align-items: center; margin-top: 42rpx; }.avatar-picker { position: relative; width: 128rpx; height: 128rpx; flex: 0 0 128rpx; }.avatar { width: 128rpx; height: 128rpx; border-radius: 50%; background: #d2eee1; }.avatar-edit { position: absolute; right: 0; bottom: 0; width: 42rpx; height: 42rpx; display: flex; align-items: center; justify-content: center; border: 3rpx solid #9aefca; border-radius: 50%; background: #1f9c4a; color: #fff; font-size: 15rpx; }.user-copy { min-width: 0; margin-left: 24rpx; }.user-name-row { display: flex; align-items: center; flex-wrap: wrap; gap: 10rpx; }.user-name { color: #124d37; font-size: 40rpx; font-weight: 600; }.user-badge { padding: 5rpx 12rpx; border-radius: 5rpx; font-size: 20rpx; }.expert { background: #f0aa38; color: #fff; }.user-links { display: flex; gap: 22rpx; margin-top: 18rpx; color: #4b9d79; font-size: 22rpx; }.profile-stats { display: flex; justify-content: space-between; margin-top: 50rpx; text-align: center; }.profile-stats > view { flex: 1; }.stat-value { display: block; color: #173f30; font-size: 34rpx; }.stat-label { display: block; margin-top: 14rpx; color: #3e715b; font-size: 24rpx; }.settings-layer { position: fixed; inset: 0; z-index: 20; background: rgba(50, 215, 148, .88); }.settings-info { position: absolute; top: 96rpx; left: 92rpx; width: 34rpx; height: 34rpx; border: 3rpx solid #fff; border-radius: 50%; color: #fff; font-size: 25rpx; line-height: 29rpx; text-align: center; }.settings-card { position: absolute; top: 112rpx; right: 24rpx; left: 148rpx; padding: 16rpx 24rpx; border-radius: 18rpx; background: #fff; box-shadow: 0 8rpx 20rpx rgba(0, 95, 56, .08); }.settings-item { height: 82rpx; display: flex; align-items: center; border-bottom: 1rpx solid #f2f2f2; color: #444; }.settings-item:last-child { border-bottom: 0; }.settings-icon { width: 54rpx; color: #333; font-size: 34rpx; text-align: center; }.settings-label { flex: 1; margin-left: 14rpx; font-size: 27rpx; }.settings-arrow { color: #222; font-size: 48rpx; line-height: 1; }
.profile-hero { min-height: 430rpx; }
</style>
