<template>
	<view class="page">
		<view class="hero">
			<view class="status-bar"><text>9:41</text><view class="status-symbols"><text>▮▮▮</text><text>◒</text><view class="battery"><view /></view></view></view>
			<view class="nav-row"><text class="back" @tap="goBack">‹</text><text class="nav-title">个人信息设置</text><view class="nav-space" /></view>
			<view class="avatar-block" @tap="chooseAvatar">
				<view class="avatar-wrap"><image class="avatar" :src="avatarUrl" mode="aspectFill" /><view class="camera"><uni-icons type="camera-filled" size="18" color="#fff" /></view></view>
				<text class="avatar-label">更换头像</text>
			</view>
		</view>

		<view class="info-card">
			<view class="info-row" @tap="editName"><text class="label">名字</text><text class="value">{{ displayName }}</text><text class="arrow">›</text></view>
			<view class="info-row" @tap="editLoginAccount"><text class="label">账号</text><text class="value">{{ loginAccount }}</text><text class="arrow">›</text></view>
			<view class="info-row intro-row" @tap="editIntro"><text class="label">简介</text><text class="value intro">{{ introduction || '介绍一下自己吧' }}</text><text class="arrow">›</text></view>
			<picker mode="date" :value="birthdayValue" :end="today" @change="changeBirthday">
				<view class="info-row"><text class="label">生日</text><text class="value" :class="{ muted: !birthday }">{{ birthday || '未设置' }}</text><text class="arrow">›</text></view>
			</picker>
			<view class="info-row" @tap="chooseGender"><text class="label">性别</text><text class="value" :class="{ muted: !gender }">{{ gender || '未设置' }}</text><text class="arrow">›</text></view>
			<view class="info-row account-row"><text class="label">田园号</text><text class="value account">{{ account }}</text></view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return { displayName: '', loginAccount: '', introduction: '', birthday: '', gender: '', account: '', avatarUrl: '/static/logo.png', today: '2026-09-29' }
	},
	computed: {
		birthdayValue() { return this.birthday || '2000-01-01' }
	},
	onShow() {
		this.account = uni.getStorageSync('tianyuanNo') || '未分配'
		this.loginAccount = uni.getStorageSync('currentUser') || ''
		this.avatarUrl = uni.getStorageSync('profileAvatar') || '/static/logo.png'
		this.displayName = uni.getStorageSync('profileDisplayName') || uni.getStorageSync('currentUser') || '农友'
		this.introduction = uni.getStorageSync('profileIntroduction') || ''
		this.birthday = uni.getStorageSync('profileBirthday') || ''
		this.gender = uni.getStorageSync('profileGender') || ''
		this.loadRemoteProfile()
	},
	methods: {
		goBack() { uni.navigateBack() },
		chooseAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album'],
				success: (res) => {
					const filePath = res.tempFilePaths && res.tempFilePaths[0]
					if (!filePath) return
					this.avatarUrl = filePath
					uni.setStorageSync('profileAvatar', filePath)
					uni.showToast({ title: '头像已更新', icon: 'success' })
				}
			})
		},
		loadRemoteProfile() {
			const token = uni.getStorageSync('token')
			if (!token) return
			const cachedName = uni.getStorageSync('profileDisplayName')
			uni.request({
				url: 'http://123.57.255.171:8080/system/user/profile',
				method: 'GET',
				header: { Authorization: 'Bearer ' + token },
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !body.data) return
					this.account = body.data.tianyuanNo || '未分配'
					this.loginAccount = body.data.userName || this.loginAccount
					uni.setStorageSync('currentUser', this.loginAccount)
					uni.setStorageSync('tianyuanNo', this.account)
					const remoteName = body.data.nickName || this.account
					this.birthday = body.data.birthday || ''
					this.gender = this.genderLabel(body.data.sex)
					uni.setStorageSync('profileBirthday', this.birthday)
					uni.setStorageSync('profileGender', this.gender)
					if (cachedName && cachedName !== remoteName) {
						this.saveDisplayName(cachedName, false)
						return
					}
					this.displayName = remoteName
					uni.setStorageSync('profileDisplayName', this.displayName)
				}
			})
		},
		genderCode(value) { return value === '男' ? '0' : value === '女' ? '1' : '2' },
		genderLabel(value) { return value === '0' ? '男' : value === '1' ? '女' : value === '2' ? '保密' : '' },
		saveProfile(data, successTitle) {
			uni.request({
				url: 'http://123.57.255.171:8080/system/user/profile', method: 'PUT',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' },
				data,
				success: response => {
					const body = response.data || {}
					if (body.code !== 200) return uni.showToast({ title: body.msg || '保存失败', icon: 'none' })
					if (successTitle) uni.showToast({ title: successTitle, icon: 'success' })
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' })
			})
		},
		saveDisplayName(nextName, showSuccess = true) {
			uni.request({
				url: 'http://123.57.255.171:8080/system/user/profile',
				method: 'PUT',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' },
				data: { nickName: nextName },
				success: response => {
					const body = response.data || {}
					if (body.code === 401) {
						uni.removeStorageSync('token')
						uni.removeStorageSync('loginSession')
						uni.removeStorageSync('currentUser')
						uni.removeStorageSync('profileDisplayName')
						return uni.reLaunch({ url: '/pages/profile/index' })
					}
					if (body.code !== 200) return uni.showToast({ title: body.msg || '名字修改失败', icon: 'none' })
					this.displayName = nextName
					uni.setStorageSync('profileDisplayName', nextName)
					if (showSuccess) uni.showToast({ title: '名字已更新', icon: 'success' })
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' })
			})
		},
		editLoginAccount() {
			uni.showModal({ title: '修改账号', editable: true, content: this.loginAccount, placeholderText: '请输入登录账号', success: res => {
				if (!res.confirm) return
				const value = (res.content || '').trim()
				if (value.length < 2 || value.length > 20) return uni.showToast({ title: '账号长度为2-20位', icon: 'none' })
				this.saveProfile({ userName: value }, '账号已更新')
				this.loginAccount = value
				uni.setStorageSync('currentUser', value)
				}})
		},
		editName() {
			uni.showModal({
				title: '修改名字',
				editable: true,
				placeholderText: '请输入名字',
				content: this.displayName,
				success: res => {
					if (!res.confirm) return
					const value = (res.content || '').trim()
					if (!value) return uni.showToast({ title: '名字不能为空', icon: 'none' })
					const nextName = value.slice(0, 20)
					this.saveDisplayName(nextName)
				}
			})
		},
		editIntro() {
			uni.showModal({
				title: '修改简介',
				editable: true,
				placeholderText: '介绍一下自己吧',
				content: this.introduction,
				success: res => {
					if (!res.confirm) return
					this.introduction = (res.content || '').trim().slice(0, 100)
					uni.setStorageSync('profileIntroduction', this.introduction)
				}
			})
		},
		changeBirthday(event) {
			this.birthday = event.detail.value
			uni.setStorageSync('profileBirthday', this.birthday)
			this.saveProfile({ birthday: this.birthday }, '生日已更新')
		},
		chooseGender() {
			const items = ['男', '女', '保密']
			uni.showActionSheet({
				itemList: items,
				success: res => {
					this.gender = items[res.tapIndex]
					uni.setStorageSync('profileGender', this.gender)
					this.saveProfile({ sex: this.genderCode(this.gender) }, '性别已更新')
				}
			})
		}
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f3f4f4; color: #303532; }
.hero { height: 420rpx; padding: 22rpx 28rpx 0; box-sizing: border-box; background: linear-gradient(145deg, #59e4ad 0%, #36c986 100%); }
.status-bar { height: 66rpx; padding: 0 42rpx; display: flex; align-items: center; justify-content: space-between; color: #123b2c; font-size: 28rpx; font-weight: 600; }.status-symbols { display: flex; align-items: center; gap: 12rpx; font-size: 20rpx; }.battery { width: 38rpx; height: 19rpx; padding: 3rpx; border: 2rpx solid #123b2c; border-radius: 5rpx; box-sizing: border-box; }.battery view { width: 24rpx; height: 9rpx; border-radius: 1rpx; background: #123b2c; }
.nav-row { height: 76rpx; display: flex; align-items: center; justify-content: space-between; }.back { width: 70rpx; color: #fff; font-size: 72rpx; line-height: 60rpx; }.nav-title { color: #14583d; font-size: 32rpx; font-weight: 600; }.nav-space { width: 70rpx; }
.avatar-block { display: flex; align-items: flex-end; justify-content: center; margin-top: 22rpx; }.avatar-wrap { position: relative; width: 180rpx; height: 180rpx; }.avatar { width: 180rpx; height: 180rpx; border: 6rpx solid rgba(255,255,255,.35); border-radius: 50%; background: #fff; box-sizing: border-box; }.camera { position: absolute; right: -4rpx; bottom: -2rpx; width: 52rpx; height: 52rpx; display: flex; align-items: center; justify-content: center; border: 5rpx solid #45d395; border-radius: 50%; background: #202624; box-sizing: border-box; }.avatar-label { margin: 0 0 13rpx 24rpx; color: #ecfff6; font-size: 26rpx; }
.info-card { position: relative; z-index: 2; min-height: 560rpx; margin: -30rpx 10rpx 0; padding: 28rpx 38rpx; border-radius: 24rpx 24rpx 0 0; background: #fff; box-sizing: border-box; }.info-row { min-height: 100rpx; display: flex; align-items: center; border-bottom: 1rpx solid #f1f2f2; }.info-row:last-child { border-bottom: 0; }.intro-row { min-height: 142rpx; align-items: flex-start; padding: 28rpx 0; box-sizing: border-box; }.label { width: 154rpx; flex: 0 0 154rpx; color: #333936; font-size: 31rpx; }.value { min-width: 0; flex: 1; color: #747c78; font-size: 27rpx; line-height: 40rpx; text-align: left; overflow-wrap: anywhere; }.intro { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }.muted { color: #adb3b0; }.arrow { width: 40rpx; color: #8b928e; font-size: 54rpx; line-height: 1; text-align: right; }.account-row { padding-right: 40rpx; }.account { color: #8b928e; }
</style>
