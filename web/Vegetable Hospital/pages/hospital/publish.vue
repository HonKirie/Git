<template>
	<view class="page">
		<view class="green-header">
			<view class="status-bar"><text>9:41</text><view class="status-icons"><text>▮▮▮</text><text>◒</text><view class="battery"><view /></view></view></view>
			<view class="nav-row"><text class="back" @tap="goBack">‹</text><text class="nav-title">发布问题</text><text class="help" @tap="showHelp">查看说明</text></view>
			<view class="field-label"><text>作物种类:</text><text class="supplement" @tap="showTip('补充种类功能暂未开放')">补充种类</text></view>
			<view class="select-field" @tap="chooseCrop"><text :class="{ placeholder: !crop }">{{ crop || '请选择作物类型' }}</text><text class="chevron">›</text></view>
			<view class="field-label planting-label">定植时间:</view>
			<view class="select-field" @tap="choosePlantingTime"><text :class="{ placeholder: !plantingTime }">{{ plantingTime || '请选择种植时间' }}</text><text class="chevron">›</text></view>
			<view class="description-label">详细描述:</view>
		</view>

		<view class="form-body">
			<textarea v-model="description" class="description" maxlength="1000" placeholder="请详细描述你的问题" placeholder-class="placeholder" />
			<view class="publish-type"><text class="type-label">发布类型:</text><view class="type-option" @tap="mediaType = 'image'"><view class="radio" :class="{ checked: mediaType === 'image' }"><view v-if="mediaType === 'image'" /></view><text :class="{ selected: mediaType === 'image' }">图片</text></view><view class="type-option" @tap="mediaType = 'video'"><view class="radio" :class="{ checked: mediaType === 'video' }"><view v-if="mediaType === 'video'" /></view><text :class="{ selected: mediaType === 'video' }">短视频</text></view></view>
			<view class="media-list">
				<view v-for="(item, index) in mediaFiles" :key="item" class="media-item">
					<image v-if="mediaType === 'image'" :src="item" mode="aspectFill" />
					<video v-else :src="item" class="media-video" />
					<text class="remove-media" @tap="removeMedia(index)">×</text>
				</view>
				<view v-if="mediaFiles.length < mediaLimit" class="media-add" @tap="chooseMedia"><view class="media-plus"><i /><i /></view></view>
			</view>
			<view class="location" @tap="chooseLocation"><text class="location-pin">●</text><text>{{ location || '添加问题发生的位置' }}</text><text class="location-arrow">›</text></view>
			<button class="submit-button" @tap="submitQuestion">发布</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return { crop: '', plantingTime: '', description: '', mediaType: 'video', mediaFiles: [], location: '', cropOptions: ['黄瓜', '茄子', '辣椒', '番茄', '丝瓜'], timeOptions: ['刚定植', '1-7天', '8-20天', '20天以上'] }
	},
	computed: {
		mediaLimit() { return this.mediaType === 'image' ? 9 : 1 }
	},
	onLoad() { this.loadCrops() },
	methods: {
		loadCrops() {
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/vegetable-tags',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					const names = [...new Set(body.data.filter(tag => tag.status === '0').map(tag => tag.dictLabel).filter(Boolean))]
					if (!names.length) return
					this.cropOptions = names
					if (this.crop && !this.cropOptions.includes(this.crop)) this.crop = ''
				}
			})
		},
		goBack() { uni.navigateBack() },
		showTip(title) { uni.showToast({ title, icon: 'none' }) },
		showHelp() { uni.showModal({ title: '发布说明', content: '请描述作物情况并添加相关图片或视频，方便农技人员判断。', showCancel: false }) },
		chooseCrop() {
			if (!this.cropOptions.length) return this.showTip('暂无可选作物')
			uni.showActionSheet({ itemList: this.cropOptions, success: res => { this.crop = this.cropOptions[res.tapIndex] } })
		},
		choosePlantingTime() {
			uni.showActionSheet({ itemList: this.timeOptions, success: res => { this.plantingTime = this.timeOptions[res.tapIndex] } })
		},
		chooseMedia() {
			if (this.mediaType === 'image') {
				uni.chooseImage({ count: this.mediaLimit - this.mediaFiles.length, sizeType: ['compressed'], sourceType: ['album', 'camera'], success: res => { this.mediaFiles = [...this.mediaFiles, ...res.tempFilePaths] } })
				return
			}
			uni.chooseVideo({ sourceType: ['album', 'camera'], compressed: true, success: res => { this.mediaFiles = [res.tempFilePath] } })
		},
		removeMedia(index) { this.mediaFiles.splice(index, 1) },
		chooseLocation() {
			uni.chooseLocation({ success: res => { this.location = res.address || res.name } })
		},
		submitQuestion() {
			if (!this.crop) return this.showTip('请选择作物类型')
			if (!this.plantingTime) return this.showTip('请选择定植时间')
			if (!this.description.trim()) return this.showTip('请填写问题描述')
			const token = uni.getStorageSync('token')
			if (!token || !uni.getStorageSync('loginSession')) {
				uni.showToast({ title: '请先登录后再发布', icon: 'none' })
				return
			}
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/posts',
				method: 'POST',
				header: { Authorization: 'Bearer ' + token, 'content-type': 'application/json' },
				data: {
					crop: this.crop,
					plantingTime: this.plantingTime,
					description: this.description.trim(),
					mediaType: this.mediaType,
					mediaFiles: JSON.stringify(this.mediaFiles),
					location: this.location,
					createdAt: this.formatDate(new Date())
				},
				success: res => {
					const body = res.data || {}
					if (body.code === 401) {
						uni.removeStorageSync('token')
						uni.removeStorageSync('loginSession')
						uni.removeStorageSync('currentUser')
						return uni.reLaunch({ url: '/pages/profile/index' })
					}
					if (body.code !== 200) return this.showTip(body.msg || '发布失败')
					uni.redirectTo({ url: '/pages/hospital/index' })
				},
				fail: () => this.showTip('无法连接服务器，请稍后重试')
			})
		},
		formatDate(date) {
			const pad = value => String(value).padStart(2, '0')
			return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
		}
	}
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f4f5f5; color: #333; }
.green-header { position: relative; padding: 24rpx 48rpx 0; background: linear-gradient(135deg, #54dfa5, #32bb7b); }
.green-header::after { position: absolute; right: 0; bottom: -34rpx; left: 0; height: 70rpx; border-radius: 0 0 50% 50% / 0 0 35% 35%; background: linear-gradient(135deg, #32bb7b, #32bb7b); content: ''; }
.status-bar { height: 70rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 48rpx; color: #102d21; font-size: 28rpx; font-weight: 600; }.status-icons { display: flex; align-items: center; gap: 12rpx; font-size: 20rpx; }.battery { width: 38rpx; height: 19rpx; padding: 3rpx; border: 2rpx solid #102d21; border-radius: 5rpx; box-sizing: border-box; }.battery view { width: 24rpx; height: 9rpx; background: #102d21; border-radius: 1rpx; }
.nav-row { height: 76rpx; display: flex; align-items: center; justify-content: space-between; color: #173f30; }.back { width: 60rpx; font-size: 66rpx; line-height: 60rpx; }.nav-title { font-size: 34rpx; }.help { width: 120rpx; font-size: 23rpx; text-align: right; }
.field-label { position: relative; z-index: 1; display: flex; justify-content: space-between; margin-top: 28rpx; color: #174c35; font-size: 25rpx; }.supplement { color: #278c60; }.select-field { position: relative; z-index: 1; height: 72rpx; margin-top: 14rpx; padding: 0 24rpx; display: flex; align-items: center; justify-content: space-between; border-radius: 17rpx; background: #fff; color: #444; font-size: 24rpx; }.placeholder { color: #aaa; }.chevron { color: #777; font-size: 48rpx; line-height: 1; }.planting-label { margin-top: 22rpx; }.description-label { position: relative; z-index: 1; margin: 20rpx 16rpx 0; color: #174c35; font-size: 25rpx; }
.form-body { position: relative; z-index: 2; padding: 0 48rpx 50rpx; }.description { width: 100%; height: 280rpx; padding: 22rpx 24rpx; border-radius: 18rpx; background: #fff; color: #333; font-size: 25rpx; line-height: 1.6; }.placeholder { color: #aaa; }
.publish-type { height: 88rpx; display: flex; align-items: center; gap: 28rpx; color: #777; font-size: 24rpx; }.type-label { margin-right: 8rpx; color: #555; }.type-option { display: flex; align-items: center; gap: 10rpx; }.radio { width: 30rpx; height: 30rpx; display: flex; align-items: center; justify-content: center; border: 2rpx solid #9a9a9a; border-radius: 50%; }.radio.checked { border-color: #444; }.radio.checked view { width: 16rpx; height: 16rpx; border-radius: 50%; background: #444; }.selected { color: #555; }
.media-list { display: flex; flex-wrap: wrap; gap: 18rpx; }.media-add, .media-item { position: relative; width: 132rpx; height: 132rpx; }.media-add { display: flex; align-items: center; justify-content: center; border: 2rpx dashed #999; }.media-plus { position: relative; width: 58rpx; height: 58rpx; }.media-plus i { position: absolute; top: 50%; left: 50%; width: 44rpx; height: 6rpx; margin: -3rpx 0 0 -22rpx; background: #888; }.media-plus i + i { transform: rotate(90deg); }.media-item image, .media-video { width: 100%; height: 100%; }.remove-media { position: absolute; top: -12rpx; right: -12rpx; width: 34rpx; height: 34rpx; border-radius: 50%; background: rgba(0, 0, 0, .58); color: #fff; font-size: 28rpx; line-height: 32rpx; text-align: center; }
.location { min-height: 86rpx; display: flex; align-items: center; gap: 12rpx; color: #46b889; font-size: 23rpx; }.location-pin { font-size: 26rpx; }.location-arrow { margin-left: auto; font-size: 46rpx; }.submit-button { height: 82rpx; margin-top: 28rpx; border: 0; border-radius: 42rpx; background: #35c886; color: #fff; font-size: 29rpx; line-height: 82rpx; }.submit-button::after { border: 0; }
</style>
