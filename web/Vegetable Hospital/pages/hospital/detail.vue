<template>
	<view class="page">
		<view class="header"><text class="back" @tap="goBack">‹</text><text class="title">问题详情</text><view class="header-space" /></view>
		<view v-if="post" class="post-card">
			<view class="post-header"><view class="avatar">农</view><view class="author"><view><text class="name">{{ post.authorId || '农友' }}</text><text class="tag">农友</text></view><text class="date">{{ post.createdAt }}</text></view><text class="crop">{{ post.crop }}｜{{ post.plantingTime }}</text></view>
			<text class="description">{{ post.description }}</text>
			<view v-if="post.mediaFiles && post.mediaFiles.length" class="media"><image v-for="(media, index) in post.mediaFiles" v-if="post.mediaType === 'image'" :key="`image-${index}`" :src="media" mode="aspectFill" /><video v-else v-for="(media, index) in post.mediaFiles" :key="`video-${index}`" :src="media" /></view>
			<view v-if="post.location" class="location">● {{ post.location }}</view>
			<view class="actions"><view @tap="toggleFavorite"><uni-icons type="star" size="18" :color="favorited ? '#ffad2f' : '#999'" /><text :class="{ active: favorited }">收藏</text></view><view @tap="sharePost"><image src="/static/card-share.png" mode="aspectFit" /><text>转发</text></view><view v-if="canDeletePost" class="delete-action" @tap="deletePost">删除</view></view>
		</view>
		<view v-else class="empty">帖子不存在或已被删除</view>
		<view v-if="post" class="comments"><text class="comments-title">评论（{{ comments.length }}）</text><view v-for="comment in comments" :key="comment.id" class="comment"><view class="comment-avatar">友</view><view class="comment-copy"><view class="comment-line"><text class="comment-author">{{ comment.authorId || '农友' }}</text><text v-if="comment.replyToId" class="reply-target">回复 {{ comment.replyToId }}</text><audio v-if="comment.audioUrl" :src="'http://123.57.255.171:8080' + comment.audioUrl" :name="'语音评论 · ' + comment.audioDuration + '秒'" controls /><text v-else class="comment-text">{{ comment.text }}</text></view><view class="detail-comment-actions"><text class="detail-reply" @tap.stop="replyComment(comment)">回复</text><text v-if="canDeleteComment(comment)" class="detail-delete" @tap.stop="deleteComment(comment.id)">删除</text></view></view></view></view>
		<view v-if="post" class="comment-bar" @tap="ensureCommentLogin"><view class="comment-user" @tap.stop="commentMode = commentMode === 'text' ? 'voice' : 'text'"><uni-icons :type="commentMode === 'text' ? 'mic' : 'compose'" size="24" color="#167448" /></view><voice-comment v-if="commentMode === 'voice'" ref="voice" :post-id="post.id" :reply-to-id="replyTarget" @sent="replyTarget = ''; loadComments()" /><input v-else v-model.trim="commentText" class="comment-input" maxlength="200" :placeholder="replyTarget ? `回复 ${replyTarget}` : '说点什么...'" placeholder-class="comment-placeholder" confirm-type="send" @confirm.stop="submitComment" /><text v-if="replyTarget" class="clear-reply" @tap.stop="replyTarget = ''">×</text><text v-if="commentMode === 'text'" class="comment-send" @tap.stop="submitComment">发送</text></view>
	</view>
</template>

<script>
import { mediaUrl } from '@/utils/post-media.js'
import VoiceComment from '../../components/voice-comment/voice-comment.vue'
export default {
	components: { VoiceComment },
	onHide() { if (this.$refs.voice) this.$refs.voice.dispose(); this.commentMode = 'text' },
	data() { return { commentMode: 'text', post: null, postId: null, comments: [], favorited: false, commentText: '', replyTarget: '' } },
	computed: {
		canDeletePost() { return this.isLoggedIn() && this.post && this.post.authorAccount === uni.getStorageSync('currentUser') }
	},
	onLoad(options) {
		this.postId = Number(options.id)
		this.loadPost()
		const favorites = uni.getStorageSync('hospitalPostFavorites') || {}
		this.favorited = !!favorites[this.postId]
	},
	onShow() {
		if (!this.post) return
		this.loadComments()
	},
	methods: {
		goBack() { uni.navigateBack() },
		toggleFavorite() {
			const favorites = { ...(uni.getStorageSync('hospitalPostFavorites') || {}) }
			favorites[this.post.id] = !favorites[this.post.id]
			this.favorited = favorites[this.post.id]
			uni.setStorageSync('hospitalPostFavorites', favorites)
			const post = this.post; if (favorites[this.post.id]) { uni.request({ url: 'http://123.57.255.171:8080/mobile/favorites', method: 'POST', header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' }, data: { postId: post.id, createdAt: new Date().toISOString(), originalAuthorId: post.authorId, originalAuthorAccount: post.authorAccount, crop: post.crop, plantingTime: post.plantingTime, description: post.description } }) }
		},
		sharePost() {
			if (!this.isLoggedIn()) return this.requireLogin()
			const repost = { postId: this.post.id, createdAt: new Date().toISOString(), repostOwnerAccount: uni.getStorageSync('currentUser'), originalAuthorId: this.post.authorId, originalAuthorAccount: this.post.authorAccount, authorId: this.post.authorId, authorAccount: this.post.authorAccount, crop: this.post.crop, plantingTime: this.post.plantingTime, description: this.post.description }; const local = uni.getStorageSync('myReposts') || []; if (!local.some(item => String(item.postId) === String(this.post.id) && item.repostOwnerAccount === repost.repostOwnerAccount)) { local.unshift(repost); uni.setStorageSync('myReposts', local) }
			uni.request({ url: 'http://123.57.255.171:8080/mobile/reposts', method: 'POST', header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' }, data: repost, success: (res) => { if ((res.data || {}).code === 200) uni.showToast({ title: '已转发', icon: 'success' }); else uni.showToast({ title: (res.data || {}).msg || '转发失败', icon: 'none' }) }, fail: () => uni.showToast({ title: '已保存，服务器同步失败', icon: 'none' }) })
		},
		replyComment(comment) { this.replyTarget = comment.authorId || '农友' },
		deleteComment(commentId) {
			if (!this.isLoggedIn()) return this.requireLogin()
			const comments = this.comments || []
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
					this.loadComments()
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' })
			})
		},
		deletePost() {
			if (!this.canDeletePost) return uni.showToast({ title: '只能删除自己发布的问题', icon: 'none' })
			uni.request({
				url: `http://123.57.255.171:8080/mobile/posts/${this.post.id}`,
				method: 'DELETE',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token') },
				success: res => {
					const body = res.data || {}
					if (this.handleAuthFailure(body)) return
					if (body.code !== 200) return uni.showToast({ title: body.msg || '删除问题失败', icon: 'none' })
					uni.navigateBack()
				}
			})
		},
		submitComment() {
			if (!this.isLoggedIn()) return this.requireLogin()
			const text = this.commentText.trim()
			if (!text) return uni.showToast({ title: '请输入评论内容', icon: 'none' })
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/comments',
				method: 'POST',
				header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' },
				data: { postId: this.post.id, commentText: text, replyToId: this.replyTarget },
				success: res => {
					const body = res.data || {}
					if (this.handleAuthFailure(body)) return
					if (body.code !== 200) return uni.showToast({ title: body.msg || '评论发布失败', icon: 'none' })
					this.commentText = ''
					this.replyTarget = ''
					this.loadComments()
				},
				fail: () => uni.showToast({ title: '无法连接服务器，请稍后重试', icon: 'none' })
			})
		},
		ensureCommentLogin() {
			if (!this.isLoggedIn()) this.requireLogin()
		},
		requireLogin() {
			uni.showModal({
				title: '请先登录',
				content: '登录后才能发表评论',
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
		loadPost() {
			uni.request({
				url: `http://123.57.255.171:8080/mobile/posts/${this.postId}`,
				method: 'GET',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !body.data) return
					let mediaFiles = []
					try { mediaFiles = JSON.parse(body.data.mediaFiles || '[]') } catch (e) {}
					mediaFiles = Array.isArray(mediaFiles) ? mediaFiles.map(mediaUrl) : []
					this.post = { id: body.data.postId, authorId: body.data.authorId, authorAccount: body.data.authorAccount, crop: body.data.crop, plantingTime: body.data.plantingTime, description: body.data.description, mediaType: body.data.mediaType, mediaFiles, location: body.data.location, createdAt: body.data.createdAt }
					this.loadComments()
				}
			})
		},
		loadComments() {
			if (!this.post) return
			uni.request({
				url: `http://123.57.255.171:8080/mobile/comments/${this.post.id}`,
				method: 'GET',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					this.comments = body.data.map(comment => ({
						id: comment.commentId,
						authorId: comment.authorId,
						authorAccount: comment.authorAccount,
						text: comment.commentText,
						audioUrl: comment.audioUrl, audioDuration: comment.audioDuration,
						replyToId: comment.replyToId || ''
					}))
				}
			})
		}
	}
}
</script>

<style scoped>
.comment-copy { display: block; min-width: 0; flex: 1; margin-left: 12rpx; }.comment-line { display: block; }.detail-comment-actions { display: flex; align-items: center; margin-top: 6rpx; }.detail-reply, .detail-delete { color: #999; font-size: 19rpx; }.detail-delete { margin-left: 18rpx; }
.page { min-height: 100vh; padding-bottom: 128rpx; background: #f4f5f5; color: #333; }.header { height: 130rpx; padding: 24rpx 32rpx 0; display: flex; align-items: center; justify-content: space-between; box-sizing: border-box; background: linear-gradient(135deg, #59dfa6, #43c68c); color: #173f30; }.back { width: 80rpx; font-size: 70rpx; line-height: 60rpx; }.title { font-size: 34rpx; }.header-space { width: 80rpx; }.post-card, .comments { margin: 24rpx 18rpx 0; padding: 28rpx 24rpx; border-radius: 18rpx; background: #fff; }.post-header { display: flex; align-items: center; }.avatar, .comment-avatar { flex: 0 0 62rpx; width: 62rpx; height: 62rpx; border-radius: 50%; background: #c7f0dd; color: #20925d; font-size: 27rpx; line-height: 62rpx; text-align: center; }.author { min-width: 0; flex: 1; margin-left: 14rpx; }.name { font-size: 26rpx; }.tag { margin-left: 12rpx; padding: 4rpx 10rpx; border-radius: 16rpx; background: #f6ae35; color: #fff; font-size: 18rpx; }.date { display: block; margin-top: 6rpx; color: #999; font-size: 20rpx; }.crop { color: #777; font-size: 22rpx; }.description { display: block; margin: 24rpx 0; color: #444; font-size: 28rpx; line-height: 1.6; }.media { display: flex; flex-wrap: wrap; gap: 12rpx; }.media image, .media video { width: 200rpx; height: 200rpx; background: #eef2ef; }.location { margin-top: 20rpx; color: #45b98a; font-size: 22rpx; }.actions { display: flex; align-items: center; justify-content: space-around; margin-top: 24rpx; padding-top: 20rpx; border-top: 1rpx solid #eee; color: #999; font-size: 22rpx; }.actions > view { display: flex; align-items: center; gap: 8rpx; }.actions image { width: 24rpx; height: 24rpx; }.actions .active { color: #ffad2f; }.delete-action { color: #e64340; }.comments-title { display: block; padding-bottom: 20rpx; border-bottom: 1rpx solid #eee; color: #555; font-size: 26rpx; }.comment { display: flex; align-items: flex-start; padding-top: 20rpx; }.comment-avatar { width: 44rpx; height: 44rpx; flex-basis: 44rpx; font-size: 18rpx; line-height: 44rpx; }.comment-copy { min-width: 0; flex: 1; margin-left: 12rpx; font-size: 21rpx; line-height: 32rpx; }.comment-line { display: block; }.comment-author { color: #5caa83; }.reply-target { margin-left: 8rpx; color: #78a88f; }.comment-text { margin-left: 8rpx; color: #666; }.comment-tools { display: flex; align-items: center; margin-top: 6rpx; }.comment-reply, .comment-delete { color: #999; font-size: 19rpx; }.comment-delete { margin-left: 18rpx; }.comment-bar { position: fixed; right: 0; bottom: 0; left: 0; z-index: 5; height: 106rpx; padding: 0 24rpx; display: flex; align-items: center; background: #5bddaa; box-sizing: border-box; }.comment-user { width: 54rpx; height: 54rpx; margin-right: 14rpx; border-radius: 50%; background: rgba(255, 255, 255, .18); color: #1d8b5a; font-size: 24rpx; line-height: 54rpx; text-align: center; }.comment-input { height: 62rpx; flex: 1; padding: 0 20rpx; border-radius: 14rpx; background: #fff; color: #444; font-size: 24rpx; }.comment-placeholder { color: #aaa; }.comment-send { margin-left: 14rpx; color: #167448; font-size: 23rpx; }.clear-reply { margin-left: 8rpx; color: #999; font-size: 26rpx; }.empty { margin: 80rpx 24rpx; color: #999; text-align: center; }
</style>
