import { mediaUrl } from '@/utils/post-media.js'
export default {
	data() {
		return {
			activeContentTab: '防治病害',
			hospitalPosts: [],
			hospitalFavorites: {},
			hospitalLikes: {},
			hospitalComments: {},
			contentTabs: ['防治病害', '最新视频', '农资优选', '附近商铺', '配方'],
			services: [
				{ name: '蔬菜医院', type: 'hospital' }, { name: '视频随拍', type: 'video' }, { name: '专家诊室', type: 'expert' }, { name: '果树医院', type: 'fruit' },
				{ name: '农技学习', type: 'study' }, { name: '在线直播', type: 'live' }, { name: '培训课堂', type: 'class' }, { name: '农资种苗', type: 'seed' },
				{ name: '菜价评选', type: 'price' }, { name: '农友圈', type: 'circle' }, { name: '农资商城', type: 'shop' }, { name: '业务共享', type: 'share' }
			],
		}
	},
		computed: {
			groupedHospitalPosts() {
				const groups = {}
				this.hospitalPosts.forEach(post => {
					const crop = post.crop || '其他作物'
					if (!groups[crop]) groups[crop] = { ...post, crop, problemCount: 1 }
					else groups[crop].problemCount += 1
				})
				return Object.values(groups).sort((a, b) => String(b.id).localeCompare(String(a.id), undefined, { numeric: true }))
			}
		},
		methods: {
			notify(name) {
				if (name === '蔬菜医院') return uni.navigateTo({ url: '/pages/hospital/index' })
				uni.showToast({ title: name, icon: 'none' })
			},
		openHospitalPost(post) { uni.navigateTo({ url: `/pages/hospital/detail?id=${post.id}` }) },
		openUser(account) { if (account) uni.navigateTo({ url: '/pages/profile/user?account=' + encodeURIComponent(account) }) },
		repostPost(post) {
			if (!uni.getStorageSync('token')) return uni.showToast({ title: '请先登录', icon: 'none' })
			const repost = { postId: post.id, createdAt: new Date().toISOString(), repostOwnerAccount: uni.getStorageSync('currentUser'), originalAuthorId: post.authorId, originalAuthorAccount: post.authorAccount, authorId: post.authorId, authorAccount: post.authorAccount, crop: post.crop, plantingTime: post.plantingTime, description: post.description }; const local = uni.getStorageSync('myReposts') || []; if (!local.some(item => String(item.postId) === String(post.id) && item.repostOwnerAccount === repost.repostOwnerAccount)) { local.unshift(repost); uni.setStorageSync('myReposts', local) }
			uni.request({ url: 'http://123.57.255.171:8080/mobile/reposts', method: 'POST', header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' }, data: repost, success: (res) => { if ((res.data || {}).code === 200) uni.showToast({ title: '已转发', icon: 'success' }); else uni.showToast({ title: (res.data || {}).msg || '转发失败', icon: 'none' }) }, fail: () => uni.showToast({ title: '已保存，服务器同步失败', icon: 'none' }) })
		},
		mapHospitalPost(post) {
			let mediaFiles = []
			try { mediaFiles = JSON.parse(post.mediaFiles || '[]') } catch (e) {}
			mediaFiles = Array.isArray(mediaFiles) ? mediaFiles.map(mediaUrl) : []
			return {
				id: post.postId,
				authorId: post.authorId,
				authorAccount: post.authorAccount,
				crop: post.crop,
				plantingTime: post.plantingTime,
				description: post.description,
				mediaType: post.mediaType,
				mediaFiles,
				location: post.location,
				createdAt: post.createdAt
			}
		},
		loadHospitalPosts() {
			uni.request({
				url: 'http://123.57.255.171:8080/mobile/posts',
				method: 'GET',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					this.hospitalPosts = body.data.map(this.mapHospitalPost)
					this.hospitalComments = {}
					this.hospitalPosts.forEach(post => this.loadHospitalComments(post.id))
				}
			})
		},
		loadHospitalComments(postId) {
			uni.request({
				url: `http://123.57.255.171:8080/mobile/comments/${postId}`,
				method: 'GET',
				success: res => {
					const body = res.data || {}
					if (body.code !== 200 || !Array.isArray(body.data)) return
					this.hospitalComments = {
						...this.hospitalComments,
						[postId]: body.data.map(comment => ({
							id: comment.commentId,
							authorId: comment.authorId,
							authorAccount: comment.authorAccount,
							text: comment.commentText, audioUrl: comment.audioUrl, audioDuration: comment.audioDuration,
							replyToId: comment.replyToId || ''
						}))
					}
				}
			})
		},
		toggleHospitalFavorite(postId) {
			const next = !this.hospitalFavorites[postId]
			const favorites = { ...this.hospitalFavorites, [postId]: next }
			this.hospitalFavorites = favorites
			uni.setStorageSync('hospitalPostFavorites', favorites)
			if (next) { const post = this.hospitalPosts.find(item => String(item.id) === String(postId)); if (post) uni.request({ url: 'http://123.57.255.171:8080/mobile/favorites', method: 'POST', header: { Authorization: 'Bearer ' + uni.getStorageSync('token'), 'content-type': 'application/json' }, data: { postId: post.id, createdAt: new Date().toISOString(), originalAuthorId: post.authorId, originalAuthorAccount: post.authorAccount, crop: post.crop, plantingTime: post.plantingTime, description: post.description } }) }
		},
		likeHospitalPost(postId) {
			const nextLikes = { ...this.hospitalLikes, [postId]: !this.hospitalLikes[postId] }
			this.hospitalLikes = nextLikes
			uni.setStorageSync('hospitalPostLikes', nextLikes)
		},
	},
	onLoad() {},
		onShow() {
		this.hospitalFavorites = uni.getStorageSync('hospitalPostFavorites') || {}
		this.hospitalLikes = uni.getStorageSync('hospitalPostLikes') || {}
		this.loadHospitalPosts()
	}
}
