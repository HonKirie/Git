<template>
	<view class="page">
		<view v-if="!categoryExpanded" class="hospital-header">
			<view class="system-status-inset" />
			<view class="search-row">
				<text class="back" @tap="goHome">‹</text>
				<view class="search-box"><text class="search-icon">⌕</text><text class="search-placeholder">请输入病害名称</text><text class="search-button">搜索</text></view>
				<text class="favorite" @tap="toggleFavorite">{{ favorited ? '已收藏' : '收藏' }}</text>
			</view>
		</view>
		<view v-else class="channel-header">
			<view class="system-status-inset" />
			<view class="channel-header-row"><text class="channel-back" @tap="closeChannelPanel">‹</text><text class="channel-help">找不到种植的作物？ <text class="help-icon">i</text></text></view>
		</view>

		<view v-if="!categoryExpanded">
			<view class="category-row">
				<scroll-view class="category-scroll" scroll-x scroll-with-animation :show-scrollbar="false">
					<view class="category-list" :style="categoryListStyle">
						<text v-for="(category, index) in selectedChannels" :key="`category-${category}-${index}`" class="category" :class="{ active: activeCategory === category }" @tap="activeCategory = category">{{ category }}</text>
					</view>
				</scroll-view>
				<view class="category-more" @tap="openChannelPanel"><view class="more-bars"><i /><i /><i /></view></view>
			</view>

			<view class="content-card">
				<view class="content-heading"><text>最新病害</text><text class="encyclopedia">病虫害百科</text></view>
				<view v-if="filteredHospitalPosts.length" class="post-list">
					<view v-for="post in filteredHospitalPosts" :key="post.id" class="post-card" @tap="openPost(post)">
						<view class="post-header"><view class="post-avatar">农</view><view class="post-author"><view><text class="post-name">{{ post.authorId || '农友' }}</text><text class="post-tag">农友</text></view><text class="post-date">{{ post.createdAt }}</text></view><text class="post-crop">{{ post.crop }}｜{{ post.plantingTime }}</text></view>
						<text class="post-description">{{ post.description }}</text>
						<view v-if="post.mediaFiles && post.mediaFiles.length" class="post-media"><image v-for="(media, index) in post.mediaFiles" v-if="post.mediaType === 'image'" :key="`${post.id}-${index}`" :src="media" mode="aspectFill" /><video v-else v-for="(media, index) in post.mediaFiles" :key="`${post.id}-${index}`" :src="media" class="post-video" /></view>
						<view v-if="post.location" class="post-location"><text>●</text><text>{{ post.location }}</text></view>
						<view class="post-actions" @tap.stop>
							<view class="post-action" @tap.stop="commentPost(post)"><view class="action-icon-box"><uni-icons type="chatbubble" size="17" color="#999" /></view><text>用户评论（{{ (postComments[post.id] || []).length }}）</text></view>
							<view class="post-action" @tap.stop="togglePostFavorite(post.id)"><view class="action-icon-box"><uni-icons type="star" size="18" :color="postFavorites[post.id] ? '#ffad2f' : '#999'" /></view><text :class="{ favorited: postFavorites[post.id] }">收藏</text></view>
							<view class="post-action" @tap.stop="sharePost(post)"><view class="action-icon-box"><image class="post-share-icon" src="/static/card-share.png" mode="aspectFit" /></view><text>转发</text></view>
						</view>
						<view v-if="commentPostId === post.id" class="comment-panel" @tap.stop>
							<view v-for="comment in postComments[post.id] || []" :key="comment.id" class="comment-item" @tap.stop><view class="comment-avatar">友</view><view class="comment-copy"><view class="comment-line"><text class="comment-name">{{ comment.authorId || '农友' }}</text><text v-if="comment.replyToId" class="comment-reply-target">回复 {{ comment.replyToId }}</text><audio v-if="comment.audioUrl" :src="'http://123.57.255.171:8080' + comment.audioUrl" :name="'语音评论 · ' + comment.audioDuration + '秒'" controls /><text v-else class="comment-text">{{ comment.text }}</text></view><view class="comment-tools"><text class="comment-reply" @tap.stop="replyComment(post.id, comment)">回复</text><text v-if="canDeleteComment(comment)" class="comment-delete" @tap.stop="deleteComment(post.id, comment.id)">删除</text></view></view></view>
							<view class="comment-input-row" @tap.stop><input v-model.trim="commentDrafts[post.id]" class="comment-input" maxlength="200" :placeholder="replyTargets[post.id] ? `回复 ${replyTargets[post.id]}` : '回复这个问题'" placeholder-class="comment-placeholder" confirm-type="send" @tap.stop @confirm="submitComment(post.id)" /><text v-if="replyTargets[post.id]" class="clear-reply" @tap.stop="clearReply(post.id)">×</text><text class="comment-submit" @tap.stop="submitComment(post.id)">发送</text></view>
						</view>
					</view>
				</view>
				<view v-else class="content-empty" />
			</view>
		</view>

		<view v-else class="channel-panel">
			<view class="channel-card">
				<view class="channel-title-row"><text>我的频道</text><text class="channel-tip" @tap="toggleEditMode">{{ editMode ? '完成' : '点击编辑' }}</text></view>
				<view class="channel-divider" />
				<view class="channel-tags">
					<view v-for="channel in selectedChannels" :key="`selected-${channel}`" class="channel-tag selected">
						<text>{{ channel }}</text><text v-if="editMode && channel !== '全部'" class="remove-mark" @tap.stop="removeChannel(channel)">×</text>
					</view>
				</view>
				<text class="group-title">隐藏频道</text>
				<view class="channel-divider" />
				<text class="group-title crop-title">蔬菜分类</text>
				<view class="custom-crop-row"><input v-model="customCropDraft" class="custom-crop-input" maxlength="20" placeholder="输入作物名称" confirm-type="done" @confirm="addCustomCrop" /><button class="custom-crop-add" @tap="addCustomCrop">添加</button></view>
				<view class="channel-tags">
					<view v-for="(channel, index) in hiddenChannels" :key="`hidden-${channel}-${index}`" class="channel-tag"><text>{{ channel }}</text><text class="add-mark" @tap.stop="addChannel(channel, index)">＋</text></view>
				</view>
			</view>
		</view>

		<view class="publish-button" @tap="publishQuestion"><view class="plus"><i /><i /></view><text class="publish-label">发布问题</text></view>
		<app-tabbar current="首页" />
	</view>
</template>

<script>
import { mediaUrl } from '@/utils/post-media.js'
export default {
	data() {
		return { customCropDraft: '', addingCrop: false, activeCategory: '全部', favorited: false, categoryExpanded: false, editMode: false, hospitalPosts: [], postFavorites: {}, postComments: {}, commentDrafts: {}, replyTargets: {}, commentPostId: null, selectedChannels: ['全部', '黄瓜', '茄子', '辣椒', '番茄'], hiddenChannels: ['丝瓜'] }
	},
	computed: {
		filteredHospitalPosts() {
			return this.activeCategory === '全部' ? this.hospitalPosts : this.hospitalPosts.filter(post => post.crop === this.activeCategory)
		},
		categoryListStyle() {
			return { width: `${Math.max(this.selectedChannels.length * 150 + 100, 750)}rpx` }
		}
	},
	methods: {
		loadVegetableTags() {
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/vegetable-tags',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					const names = [...new Set(body.data.filter(tag => tag.status === '0').map(tag => tag.dictLabel).filter(Boolean))]
					this.reconcileChannels(names)
				}
			})
		},
		reconcileChannels(names) {
			const saved = uni.getStorageSync('hospitalChannels') || {}
			const preferred = Array.isArray(saved.selected) ? saved.selected : this.selectedChannels
			const selected = [...new Set(preferred)].filter(name => name !== '全部' && names.includes(name))
			this.selectedChannels = ['全部', ...selected]
			this.hiddenChannels = names.filter(name => !selected.includes(name))
			if (!this.selectedChannels.includes(this.activeCategory)) this.activeCategory = '全部'
			this.saveChannels()
		},
		goHome() { uni.reLaunch({ url: '/pages/index/index' }) },
		addCustomCrop() {
			if (!this.isLoggedIn()) return this.requireLogin('登录后才能添加作物')
			const name = this.customCropDraft.trim()
			if (!name) return uni.showToast({ title: '请输入作物名称', icon: 'none' })
			if (name.length > 20) return uni.showToast({ title: '作物名称最多20个字', icon: 'none' })
			if (this.selectedChannels.includes(name)) return uni.showToast({ title: '该作物已添加', icon: 'none' })
			if (this.addingCrop) return
			this.addingCrop = true
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/vegetable-tags',
				method: 'POST',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' },
				data: { dictLabel: name },
				success: res => {
					const body = res.data || {}
					if (this.handleAuthFailure(body)) return
					if (body.code !== 200) return uni.showToast({ title: body.msg || '添加失败', icon: 'none' })
					this.hiddenChannels = this.hiddenChannels.filter(item => item !== name)
					if (!this.selectedChannels.includes(name)) this.selectedChannels.push(name)
					this.saveChannels()
					this.customCropDraft = ''
					this.loadVegetableTags()
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' }),
				complete: () => { this.addingCrop = false }
			})
		},
		openChannelPanel() { this.categoryExpanded = true; this.editMode = false },
		closeChannelPanel() { this.categoryExpanded = false; this.editMode = false },
		toggleFavorite() { this.favorited = !this.favorited },
		saveChannels() { uni.setStorageSync('hospitalChannels', { selected: this.selectedChannels, hidden: this.hiddenChannels }) },
		toggleEditMode() {
			this.editMode = !this.editMode
			this.saveChannels()
		},
		removeChannel(channel) {
			if (channel === '全部') return
			this.selectedChannels = this.selectedChannels.filter(item => item !== channel)
			if (!this.hiddenChannels.includes(channel)) this.hiddenChannels.push(channel)
			if (this.activeCategory === channel) this.activeCategory = '全部'
			this.saveChannels()
		},
		addChannel(channel, index) {
			if (this.selectedChannels.includes(channel)) {
				uni.showToast({ title: '该频道已添加', icon: 'none' })
				return
			}
			const nextHiddenChannels = [...this.hiddenChannels]
			nextHiddenChannels.splice(index, 1)
			this.hiddenChannels = nextHiddenChannels
			this.selectedChannels.push(channel)
			this.saveChannels()
		},
		publishQuestion() { uni.navigateTo({ url: '/pages/hospital/publish' }) },
		openPost(post) { uni.navigateTo({ url: `/pages/hospital/detail?id=${post.id}` }) },
		commentPost(post) {
			if (!this.isLoggedIn()) return this.requireLogin()
			this.commentPostId = this.commentPostId === post.id ? null : post.id
		},
		replyComment(postId, comment) {
			this.replyTargets = { ...this.replyTargets, [postId]: comment.authorId || '农友' }
			this.commentPostId = postId
		},
		clearReply(postId) {
			const nextTargets = { ...this.replyTargets }
			delete nextTargets[postId]
			this.replyTargets = nextTargets
		},
		submitComment(postId) {
			if (!this.isLoggedIn()) return this.requireLogin()
			const text = (this.commentDrafts[postId] || '').trim()
			if (!text) return uni.showToast({ title: '请输入回复内容', icon: 'none' })
			const replyToId = this.replyTargets[postId] || ''
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/comments',
				method: 'POST',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' },
				data: { postId, commentText: text, replyToId },
				success: res => {
					const body = res.data || {}
					if (this.handleAuthFailure(body)) return
					if (body.code !== 200) return uni.showToast({ title: body.msg || '评论发布失败', icon: 'none' })
					this.commentDrafts = { ...this.commentDrafts, [postId]: '' }
					this.clearReply(postId)
					this.loadComments(postId)
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' })
			})
		},
		requireLogin(content = '登录后才能发表评论') {
			uni.showModal({
				title: '请先登录',
				content,
				confirmText: '去登录',
				cancelText: '取消',
				success: res => {
					if (res.confirm) uni.navigateTo({ url: '/pages/profile/index' })
				}
			})
		},
		handleAuthFailure(body) {
			if (!body || body.code !== 401) return false
			uni.removeStorageSync('token')
			uni.removeStorageSync('loginSession')
			uni.removeStorageSync('currentUser')
			uni.showModal({
				title: '登录已失效',
				content: '登录状态已过期，请重新登录',
				showCancel: false,
				success: () => uni.reLaunch({ url: '/pages/profile/index' })
			})
			return true
		},
		isLoggedIn() { return !!(uni.getStorageSync('token') && uni.getStorageSync('loginSession') && uni.getStorageSync('currentUser')) },
		canDeleteComment(comment) {
			return this.isLoggedIn() && !!comment && comment.authorAccount === uni.getStorageSync('currentUser')
		},
		deleteComment(postId, commentId) {
			if (!this.isLoggedIn()) return this.requireLogin()
			const comments = this.postComments[postId] || []
			const comment = comments.find(item => item.id === commentId)
			if (!this.canDeleteComment(comment)) {
				uni.showToast({ title: '只能删除自己的评论', icon: 'none' })
				return
			}
			uni.request({
				url: `http://123.57.255.171:8080/mobile/comments/${commentId}`,
				method: 'DELETE',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token') },
				success: res => {
					const body = res.data || {}
					if (this.handleAuthFailure(body)) return
					if (body.code !== 200) return uni.showToast({ title: body.msg || '删除评论失败', icon: 'none' })
					this.loadComments(postId)
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' })
			})
		},
		loadComments(postId) {
			uni.request({
				url: `http://123.57.255.171:8080/mobile/comments/${postId}`,
				method: 'GET',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					this.postComments = { ...this.postComments, [postId]: body.data.map(comment => ({
						id: comment.commentId,
						authorId: comment.authorId,
						authorAccount: comment.authorAccount,
						text: comment.commentText, audioUrl: comment.audioUrl, audioDuration: comment.audioDuration,
						replyToId: comment.replyToId || ''
					})) }
				}
			})
		},
		mapPost(post) {
			let mediaFiles = []
			try { mediaFiles = JSON.parse(post.mediaFiles || '[]') } catch (e) {}
			mediaFiles = Array.isArray(mediaFiles) ? mediaFiles.map(mediaUrl) : []
			return { id: post.postId, authorId: post.authorId, authorAccount: post.authorAccount, crop: post.crop, plantingTime: post.plantingTime, description: post.description, mediaType: post.mediaType, mediaFiles, location: post.location, createdAt: post.createdAt }
		},
		loadPosts() {
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/posts',
				method: 'GET',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					this.hospitalPosts = body.data.map(this.mapPost)
					this.postComments = {}
					this.hospitalPosts.forEach(post => this.loadComments(post.id))
				}
			})
		},
		migrateLocalPosts() {
			const token = uni.getStorageSync('token')
			const posts = uni.getStorageSync('hospitalPosts') || []
			if (!token || !posts.length || uni.getStorageSync('hospitalPostsMigrated')) return this.loadPosts()
			let remaining = posts.length
			posts.forEach(post => {
				uni.request({
					url: 'http://123.57.255.171:8080/mobile/posts',
					method: 'POST',
					header: { Authorization: 'Bearer ' + token, 'content-type': 'application/json' },
					data: { postId: post.id, crop: post.crop, plantingTime: post.plantingTime, description: post.description, mediaType: post.mediaType, mediaFiles: JSON.stringify(post.mediaFiles || []), location: post.location, createdAt: post.createdAt },
					complete: () => {
						remaining -= 1
						if (!remaining) {
							uni.setStorageSync('hospitalPostsMigrated', true)
							this.loadPosts()
						}
					}
				})
			})
		},
		togglePostFavorite(postId) {
			const nextFavorites = { ...this.postFavorites }
			nextFavorites[postId] = !nextFavorites[postId]
			this.postFavorites = nextFavorites
			uni.setStorageSync('hospitalPostFavorites', nextFavorites)
			const post = this.hospitalPosts.find(item => String(item.id) === String(postId)); if (nextFavorites[postId]) { uni.request({ url: 'http://123.57.255.171:8080/mobile/favorites', method: 'POST', header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' }, data: { postId: post.id, createdAt: new Date().toISOString(), originalAuthorId: post.authorId, originalAuthorAccount: post.authorAccount, crop: post.crop, plantingTime: post.plantingTime, description: post.description } }) }
		},
		sharePost(post) {
			if (!this.isLoggedIn()) return this.requireLogin()
			const repost = { postId: post.id, createdAt: new Date().toISOString(), repostOwnerAccount: uni.getStorageSync('currentUser'), originalAuthorId: post.authorId, originalAuthorAccount: post.authorAccount, authorId: post.authorId, authorAccount: post.authorAccount, crop: post.crop, plantingTime: post.plantingTime, description: post.description }; const local = uni.getStorageSync('myReposts') || []; if (!local.some(item => String(item.postId) === String(post.id) && item.repostOwnerAccount === repost.repostOwnerAccount)) { local.unshift(repost); uni.setStorageSync('myReposts', local) }
			uni.request({ url: 'http://123.57.255.171:8080/mobile/reposts', method: 'POST', header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' }, data: repost, success: (res) => { if ((res.data || {}).code === 200) uni.showToast({ title: '已转发', icon: 'success' }); else uni.showToast({ title: (res.data || {}).msg || '转发失败', icon: 'none' }) }, fail: () => uni.showToast({ title: '已保存，服务器同步失败', icon: 'none' }) })
		},
	},
	onShow() {
		this.loadVegetableTags()
		this.postFavorites = uni.getStorageSync('hospitalPostFavorites') || {}
		this.migrateLocalPosts()
		const channels = uni.getStorageSync('hospitalChannels')
		if (!channels) return
		const selected = Array.isArray(channels.selected) ? channels.selected : this.selectedChannels
		const hidden = Array.isArray(channels.hidden) ? channels.hidden : this.hiddenChannels
		this.selectedChannels = ['全部', ...new Set(selected.filter(name => name && name !== '全部'))]
		this.hiddenChannels = [...new Set(hidden.filter(name => name && !this.selectedChannels.includes(name)))]
		if (!this.selectedChannels.includes(this.activeCategory)) this.activeCategory = '全部'
	},
	onLoad() {}
}
</script>

<style scoped>
.custom-crop-row { display: flex; align-items: center; gap: 16rpx; margin: 20rpx 0 28rpx; }.custom-crop-input { flex: 1; min-width: 0; height: 72rpx; padding: 0 20rpx; border: 1rpx solid #ddd; border-radius: 6rpx; font-size: 26rpx; box-sizing: border-box; }.custom-crop-add { flex: 0 0 112rpx; height: 72rpx; margin: 0; padding: 0; border-radius: 6rpx; background: #29b979; color: #fff; font-size: 26rpx; line-height: 72rpx; }
.comment-copy { min-width: 0; flex: 1; display: block; margin-left: 10rpx; }.comment-line { display: flex; flex-wrap: wrap; align-items: baseline; }.comment-reply-target { margin-right: 8rpx; color: #78a88f; }.comment-tools { display: flex; align-items: center; margin-top: 6rpx; }.comment-reply, .comment-delete { color: #999; font-size: 19rpx; }.comment-delete { margin-left: 18rpx; }.clear-reply { margin: 0 10rpx; color: #999; font-size: 26rpx; }
.action-icon-box { width: 30rpx; height: 30rpx; display: flex; align-items: center; justify-content: center; flex: 0 0 30rpx; }.post-share-icon { width: 24rpx; height: 24rpx; }.comment-panel { margin-top: 18rpx; padding: 16rpx 18rpx 12rpx; border-top: 1rpx solid #f0f0f0; background: #fafbfa; }.comment-item { display: flex; align-items: flex-start; padding: 10rpx 0; }.comment-avatar { width: 40rpx; height: 40rpx; flex: 0 0 40rpx; border-radius: 50%; background: #d9f3e5; color: #43a97c; font-size: 18rpx; line-height: 40rpx; text-align: center; }.comment-copy { display: flex; flex-wrap: wrap; align-items: baseline; margin-left: 10rpx; font-size: 20rpx; line-height: 30rpx; }.comment-name { margin-right: 10rpx; color: #69a88a; }.comment-text { color: #666; }.comment-input-row { display: flex; align-items: center; margin-top: 10rpx; padding-top: 12rpx; border-top: 1rpx solid #eee; }.comment-input { flex: 1; height: 54rpx; padding: 0 14rpx; border-radius: 27rpx; background: #fff; color: #555; font-size: 20rpx; }.comment-placeholder { color: #b6bdb9; }.comment-submit { margin-left: 12rpx; color: #33b77d; font-size: 20rpx; }
.page { min-height: 100vh; padding-bottom: 120rpx; background: #f1f3f4; color: #333; }
.hospital-header { padding: 24rpx 24rpx 28rpx; background: linear-gradient(135deg, #59dfa6, #43c68c); color: #fff; }

.search-row { display: flex; align-items: center; gap: 14rpx; }.back { width: 44rpx; font-size: 70rpx; line-height: 62rpx; }.search-box { height: 72rpx; flex: 1; display: flex; align-items: center; padding-left: 22rpx; border: 4rpx solid #fff; border-radius: 40rpx; background: #fff; color: #aaa; }.search-icon { margin-right: 14rpx; color: #999; font-size: 42rpx; line-height: 1; }.search-placeholder { flex: 1; font-size: 25rpx; }.search-button { height: 64rpx; padding: 0 28rpx; border-radius: 34rpx; background: #35d291; color: #fff; font-size: 25rpx; line-height: 64rpx; }.favorite { width: 62rpx; font-size: 24rpx; text-align: center; }
.category-row { position: relative; height: 84rpx; background: #fff; }.category-scroll { width: calc(100% - 70rpx); height: 84rpx; white-space: nowrap; }.category-list { height: 84rpx; display: flex; align-items: center; padding: 0 24rpx 0 14rpx; box-sizing: border-box; }.category { flex: 0 0 auto; min-width: 80rpx; padding: 10rpx 16rpx; margin-right: 10rpx; border-radius: 28rpx; color: #333; font-size: 27rpx; text-align: center; }.category.active { background: #29c77d; color: #fff; }.category-more { position: absolute; top: 0; right: 0; z-index: 2; width: 70rpx; height: 84rpx; display: flex; align-items: center; justify-content: center; background: #fff; box-shadow: -10rpx 0 14rpx rgba(255, 255, 255, .95); }.more-bars { width: 34rpx; height: 28rpx; display: flex; flex-direction: column; justify-content: space-around; }.more-bars i { display: block; height: 3rpx; border-radius: 2rpx; background: #777; }
.content-card { min-height: calc(100vh - 390rpx); margin: 0 16rpx; padding: 28rpx 24rpx; border-radius: 18rpx; background: #fff; }.content-heading { display: flex; align-items: center; justify-content: space-between; padding-bottom: 24rpx; border-bottom: 2rpx solid #f0f0f0; color: #666; font-size: 28rpx; }.encyclopedia { padding: 10rpx 18rpx; border-radius: 24rpx; background: #35cf8b; color: #fff; font-size: 22rpx; }.content-empty { min-height: 540rpx; }.post-list { padding-top: 4rpx; }.post-card { padding: 26rpx 0 22rpx; border-bottom: 2rpx solid #f0f0f0; }.post-header { display: flex; align-items: center; }.post-avatar { width: 62rpx; height: 62rpx; margin-right: 14rpx; border-radius: 50%; background: #c7f0dd; color: #20925d; font-size: 27rpx; line-height: 62rpx; text-align: center; }.post-author { min-width: 0; flex: 1; }.post-name { color: #333; font-size: 25rpx; }.post-tag { margin-left: 12rpx; padding: 4rpx 10rpx; border-radius: 16rpx; background: #f6ae35; color: #fff; font-size: 18rpx; }.post-date { display: block; margin-top: 6rpx; color: #999; font-size: 20rpx; }.post-crop { color: #777; font-size: 22rpx; }.post-description { display: block; margin: 18rpx 0; color: #444; font-size: 25rpx; line-height: 1.55; }.post-media { display: flex; flex-wrap: wrap; gap: 10rpx; }.post-media image, .post-video { width: 160rpx; height: 160rpx; background: #eef2ef; }.post-location { display: flex; align-items: center; gap: 8rpx; margin-top: 16rpx; color: #45b98a; font-size: 21rpx; }.post-actions { display: flex; align-items: center; justify-content: space-around; margin-top: 20rpx; color: #999; font-size: 21rpx; }.post-action { display: flex; align-items: center; gap: 8rpx; }.post-action text { font-size: 21rpx; }.post-action .uni-icons { line-height: 1; }.post-share-icon { width: 30rpx; height: 30rpx; }.favorited { color: #ffad2f; }
.channel-header { padding: 24rpx 24rpx 28rpx; background: linear-gradient(135deg, #59dfa6, #43c68c); color: #fff; }.channel-header-row { height: 74rpx; display: flex; align-items: center; justify-content: space-between; }.channel-panel { min-height: calc(100vh - 270rpx); padding: 0 24rpx 100rpx; background: #f3f4f4; }.channel-back { font-size: 70rpx; line-height: 60rpx; }.channel-help { font-size: 25rpx; }.help-icon { display: inline-flex; width: 28rpx; height: 28rpx; align-items: center; justify-content: center; border: 2rpx solid #fff; border-radius: 50%; font-size: 20rpx; }.channel-card { padding: 28rpx 24rpx 48rpx; border-radius: 16rpx; background: #fff; }.channel-title-row { display: flex; align-items: center; justify-content: space-between; color: #444; font-size: 27rpx; }.channel-tip { color: #777; font-size: 22rpx; }.channel-divider { height: 2rpx; margin: 22rpx 0; background: #f0f0f0; }.channel-tags { display: flex; flex-wrap: wrap; gap: 24rpx 28rpx; }.channel-tag { position: relative; min-width: 90rpx; height: 48rpx; padding: 0 24rpx; border: 2rpx solid #999; border-radius: 26rpx; color: #555; font-size: 23rpx; line-height: 44rpx; text-align: center; box-sizing: border-box; }.channel-tag.selected { border-color: #999; }.channel-input { width: 100%; height: 44rpx; padding: 0; border: 0; background: transparent; color: #555; font-size: 23rpx; line-height: 44rpx; text-align: center; }.remove-mark, .add-mark { position: absolute; top: -18rpx; right: -14rpx; width: 34rpx; height: 34rpx; border-radius: 50%; color: #fff; font-size: 30rpx; line-height: 31rpx; text-align: center; }.remove-mark { background: #31cc8b; }.add-mark { background: #e8343b; font-size: 27rpx; }.group-title { display: block; color: #444; font-size: 27rpx; }.crop-title { margin-top: 4rpx; }
.publish-button { position: fixed; right: 42rpx; bottom: 148rpx; z-index: 5; height: 78rpx; display: flex; align-items: center; padding: 0 26rpx 0 10rpx; border-radius: 42rpx; background: #2fbe7c; box-shadow: 0 10rpx 24rpx rgba(30, 143, 86, .22); color: #fff; }.plus { position: relative; width: 58rpx; height: 58rpx; margin-right: 10rpx; border-radius: 50%; background: #fff; }.plus i { position: absolute; top: 50%; left: 50%; display: block; width: 30rpx; height: 4rpx; margin: -2rpx 0 0 -15rpx; border-radius: 3rpx; background: #2fbe7c; }.plus i + i { transform: rotate(90deg); }.publish-label { font-size: 28rpx; font-weight: 600; }.post-share-icon { width: 24rpx; height: 24rpx; }.comment-copy { display: block; flex: 1; min-width: 0; margin-left: 10rpx; }.comment-line { display: block; }.comment-tools { display: flex; align-items: center; margin-top: 6rpx; }
</style>
