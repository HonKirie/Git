<template>
	<view class="page">
		<view class="register-hero">
			<view class="top-bar"><text class="back" @tap="goLogin">‹</text><text class="top-title">注册账号</text><view class="top-placeholder" /></view>
			<view class="register-body">
				<text class="welcome-title">创建田园采风账号</text>
				<text class="welcome-copy">注册后即可使用完整服务</text>
				<view class="register-form">
					<view class="input-row"><text class="input-label">账号</text><input v-model.trim="account" class="register-input" placeholder="4-20位账号" placeholder-class="input-placeholder" /></view>
					<view class="input-row"><text class="input-label">密码</text><input v-model="password" class="register-input" password placeholder="请输入密码" placeholder-class="input-placeholder" /></view>
					<view class="input-row"><text class="input-label">确认密码</text><input v-model="confirmPassword" class="register-input" password placeholder="再次输入密码" placeholder-class="input-placeholder" /></view>
						<view class="register-button" :class="{ disabled: !canRegister }" @tap="register">注册</view>
					<view class="login-link"><text>已有账号？</text><text class="login-link-action" @tap="goLogin">去登录</text></view>
				</view>
			</view>
			<view class="agreement"><text class="agreement-mark" @tap="agreed = !agreed">{{ agreed ? '✓' : '' }}</text><text>我已阅读并同意《用户服务协议》和《隐私政策》</text></view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return { account: '', password: '', confirmPassword: '', agreed: false, loading: false }
	},
	computed: {
		canRegister() { return this.account.length > 0 && this.password.length > 0 && this.confirmPassword.length > 0 }
	},
	methods: {
		showTip(title) { uni.showToast({ title, icon: 'none' }) },
		goLogin() {
			uni.redirectTo({ url: '/pages/profile/index' })
		},
		register() {
			if (!this.canRegister) return this.showTip('请填写完整注册信息')
			if (this.account.length < 4 || this.account.length > 20) return this.showTip('账号长度为4-20位')
			if (this.password.length < 6) return this.showTip('密码至少6位')
			if (this.password !== this.confirmPassword) return this.showTip('两次输入的密码不一致')
			if (this.loading) return
			this.loading = true
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/register',
				method: 'POST',
				header: { 'content-type': 'application/json' },
				data: { username: this.account, password: this.password },
				success: (res) => {
					const body = res.data || {}
					if (body.code === 200) {
						uni.showModal({ title: '注册成功', content: '账号已创建，请登录', showCancel: false, success: () => this.goLogin() })
						return
					}
					if (body.msg === '用户名已存在') {
						uni.showModal({ title: '注册失败', content: '用户名已存在', showCancel: false })
						return
					}
					this.showTip(body.msg || '注册失败，请稍后重试')
				},
				fail: () => this.showTip('无法连接服务器，请稍后重试'),
				complete: () => { this.loading = false }
			})
		}
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7f5; }.register-hero { position: relative; min-height: 100vh; overflow: hidden; padding-bottom: 48rpx; background: linear-gradient(155deg, #9eefd1 0%, #d8f7e8 56%, #edf9f1 100%); }.register-hero::after { content: ''; position: absolute; right: -120rpx; bottom: 160rpx; left: -120rpx; height: 300rpx; border-radius: 50% 50% 0 0; background: rgba(83, 209, 143, .14); transform: rotate(-7deg); }
.top-bar { position: relative; z-index: 1; height: 100rpx; padding: 0 34rpx; display: flex; align-items: center; justify-content: space-between; color: #176042; }.back { width: 60rpx; font-size: 62rpx; font-weight: 300; line-height: 70rpx; }.top-title { font-size: 32rpx; font-weight: 600; }.top-placeholder { width: 60rpx; }
.register-body { position: relative; z-index: 1; padding: 170rpx 58rpx 0; }.welcome-title { display: block; color: #176042; font-size: 46rpx; font-weight: 600; text-align: center; }.welcome-copy { display: block; margin-top: 18rpx; color: #5a9078; font-size: 27rpx; text-align: center; }.register-form { margin-top: 64rpx; }.input-row { height: 98rpx; padding: 0 26rpx; margin-top: 22rpx; display: flex; align-items: center; border-radius: 12rpx; background: #fff; }.input-label { width: 120rpx; flex: 0 0 120rpx; color: #515c56; font-size: 28rpx; white-space: nowrap; }.register-input { flex: 1; height: 98rpx; margin-left: 18rpx; color: #263a31; font-size: 28rpx; }.input-placeholder { color: #b7bdb9; }
.register-button { width: 360rpx; height: 92rpx; margin: 58rpx auto 0; display: flex; align-items: center; justify-content: center; box-sizing: border-box; border-radius: 14rpx; background: #25cb88; color: #fff; font-size: 34rpx; font-weight: 600; line-height: 1; text-align: center; }.register-button.disabled { opacity: .58; }.login-link { display: flex; justify-content: center; margin-top: 28rpx; color: #6a897b; font-size: 25rpx; }.login-link-action { margin-left: 8rpx; color: #258460; }
.agreement { position: relative; z-index: 2; margin: 36rpx 48rpx 0; display: flex; align-items: flex-start; color: #527967; font-size: 21rpx; line-height: 32rpx; }.agreement-mark { width: 25rpx; height: 25rpx; flex: 0 0 25rpx; margin: 3rpx 9rpx 0 0; border: 2rpx solid #76ad91; border-radius: 50%; color: #2ebc7e; font-size: 20rpx; line-height: 23rpx; text-align: center; }
</style>
