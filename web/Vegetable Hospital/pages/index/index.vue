<template>
	<view class="page">
		<view class="hero">
			<view class="system-status-inset" />
			<view class="top-bar">
				<view class="location" @click="notify('已定位到寿光市')">
					<image class="block_8" src="/static/location-pin.png" mode="aspectFit" />
					<text>寿光市</text><text class="chevron">›</text>
				</view>
				<text class="page-title">田园采风</text>
				<view class="top-actions">
					<image class="search" src="/static/search-icon.png" mode="aspectFit" @click="notify('搜索功能即将开放')" />
					<image class="box_8" src="/static/scan-icon.png" mode="aspectFit" @click="notify('扫一扫功能即将开放')" />
				</view>
			</view>
		</view>

		<view class="services-card">
			<view v-for="item in services" :key="item.name" class="service" @click="notify(item.name)">
				<image v-if="item.type === 'hospital'" class="service-icon hospital-icon" src="/static/vegetable-hospital.png" mode="aspectFit" />
				<image v-else-if="item.type === 'video'" class="service-icon service-image-icon" src="/static/service-video.png" mode="aspectFit" />
				<image v-else-if="item.type === 'expert'" class="service-icon service-image-icon" src="/static/service-expert.png" mode="aspectFit" />
				<image v-else-if="item.type === 'fruit'" class="service-icon service-image-icon" src="/static/service-fruit.png" mode="aspectFit" />
				<image v-else-if="item.type === 'study'" class="service-icon service-image-icon" src="/static/service-study.png" mode="aspectFit" />
				<image v-else-if="item.type === 'live'" class="service-icon service-image-icon" src="/static/service-live.png" mode="aspectFit" />
				<image v-else-if="item.type === 'class'" class="service-icon service-image-icon" src="/static/service-class.png" mode="aspectFit" />
				<image v-else-if="item.type === 'seed'" class="service-icon service-image-icon" src="/static/service-seed.png" mode="aspectFit" />
				<image v-else-if="item.type === 'price'" class="service-icon service-image-icon" src="/static/service-price.png" mode="aspectFit" />
				<image v-else-if="item.type === 'circle'" class="service-icon service-image-icon" src="/static/service-circle.png" mode="aspectFit" />
				<image v-else-if="item.type === 'shop'" class="service-icon service-image-icon" src="/static/service-shop.png" mode="aspectFit" />
				<image v-else-if="item.type === 'share'" class="service-icon service-image-icon" src="/static/service-share.png" mode="aspectFit" />
				<view v-else class="service-icon" :class="item.type">
					<view v-if="item.type === 'video'" class="play-mark">▶</view>
					<view v-else-if="item.type === 'expert'" class="expert-mark">医</view>
					<view v-else-if="item.type === 'fruit'" class="fruit-mark">●</view>
					<view v-else-if="item.type === 'study'" class="study-mark">书</view>
					<view v-else-if="item.type === 'live'" class="live-mark">◉</view>
					<view v-else-if="item.type === 'class'" class="class-mark">讲</view>
					<view v-else-if="item.type === 'seed'" class="seed-mark">✦</view>
					<view v-else-if="item.type === 'price'" class="price-mark">¥</view>
					<view v-else-if="item.type === 'circle'" class="circle-mark">◌</view>
					<view v-else-if="item.type === 'shop'" class="shop-mark">▰</view>
					<view v-else class="share-mark">↗</view>
				</view>
				<text class="service-name">{{ item.name }}</text>
			</view>
		</view>

		<image class="featured-banner" src="/static/featured-banner.png" mode="aspectFill" />
		<view class="content-section">
			<view class="content-tabs">
				<text
					v-for="section in contentTabs"
					:key="section"
					class="content-tab"
					:class="{ 'active-content-tab': activeContentTab === section }"
					@click="activeContentTab = section"
				>{{ section }}</text>
				<text class="more-icon">≡</text>
			</view>
			<uni-section v-if="activeContentTab === '防治病害'" title="病害信息" type="line" title-font-size="30rpx" title-color="#333" padding>
				<uni-card
					v-for="post in groupedHospitalPosts"
					:key="`disease-${post.id}`"
					padding="16rpx"
					margin="0 0 18rpx"
					shadow="0 1rpx 8rpx rgba(0, 0, 0, .04)"
					@click="openHospitalPost(post)"
				>
					<view slot="title" class="disease-card-title">
							<image class="disease-thumbnail" :src="post.thumbnail || '/static/featured-banner.png'" mode="aspectFill" />
						<view class="disease-card-meta">
							<view class="disease-card-name-row">
								<text class="disease-card-name">{{ post.crop }}</text>
									<text class="growth-tag">{{ post.plantingTime }}</text>
									<text class="disease-card-date">{{ post.createdAt }}</text>
									</view>
									<text class="card-summary">{{ post.description }}<text v-if="post.problemCount > 1" class="problem-count">（{{ post.problemCount }}个问题）</text></text>
						</view>
					</view>
					<view slot="actions" class="card-actions">
						<view class="card-actions-item" @click.stop="repostPost(post)">
							<view class="action-icon-box"><image class="card-action-icon" src="/static/card-share.png" mode="aspectFit" /></view>
							<text class="card-actions-item-text">分享</text>
						</view>
						<view class="card-actions-item" @click.stop="likeHospitalPost(post.id)">
							<view class="action-icon-box"><uni-icons type="heart" size="18" :color="hospitalLikes[post.id] ? '#e64340' : '#999'" /></view>
							<text class="card-actions-item-text" :class="{ liked: hospitalLikes[post.id] }">点赞</text>
						</view>
						<view class="card-actions-item" @click.stop="toggleHospitalFavorite(post.id)">
							<view class="example-body">
								<uni-fav
									:checked="hospitalFavorites[post.id]"
									class="favBtn"
									@click.stop="toggleHospitalFavorite(post.id)"
								/>
							</view>
						</view>
						<view class="card-actions-item" @click.stop="openHospitalPost(post)">
							<view class="action-icon-box"><uni-icons type="chatbubble" size="18" color="#999" /></view>
							<text class="card-actions-item-text">评论</text>
						</view>
					</view>
				</uni-card>
				<view v-if="!hospitalPosts.length" class="home-hospital-empty">暂无用户发布的问题</view>
			</uni-section>
			<view v-if="activeContentTab === '防治病害'" class="feed-section">
				<view v-for="post in hospitalPosts" :key="`hospital-${post.id}`" class="feed-card hospital-feed-card" @click="openHospitalPost(post)">
					<view class="feed-header"><view class="feed-avatar" @click.stop="openUser(post.authorAccount || post.authorId)">农</view><view class="feed-author-meta"><view class="feed-author-row"><text class="feed-author" @click.stop="openUser(post.authorAccount || post.authorId)">{{ post.authorId || '农友' }}</text><text class="friend-tag">农友</text></view><text class="feed-date">{{ post.createdAt }}</text></view><text class="feed-crop">{{ post.crop }} | {{ post.plantingTime }}</text></view>
					<text class="feed-content">{{ post.description }}</text>
					<view v-if="post.mediaFiles && post.mediaFiles.length" class="feed-images"><image v-for="(media, index) in post.mediaFiles" v-if="post.mediaType === 'image'" :key="`hospital-image-${post.id}-${index}`" class="feed-image" :src="media" mode="aspectFill" /><video v-else v-for="(media, index) in post.mediaFiles" :key="`hospital-video-${post.id}-${index}`" class="feed-image" :src="media" /></view>
					<view v-if="post.location" class="feed-location"><text>● {{ post.location }}</text></view>
					<view class="feed-actions" @click.stop><view class="feed-action" @click.stop="notify('用户评论')"><view class="action-icon-box"><uni-icons type="chatbubble" size="17" color="#999" /></view><text>用户评论（{{ (hospitalComments[post.id] || []).length }}）</text></view><view class="feed-action" @click.stop="toggleHospitalFavorite(post.id)"><view class="action-icon-box"><uni-icons type="star" size="18" :color="hospitalFavorites[post.id] ? '#ffad2f' : '#999'" /></view><text :class="{ favorited: hospitalFavorites[post.id] }">{{ hospitalFavorites[post.id] ? '已收藏' : '收藏' }}</text></view><view class="feed-action" @click.stop="repostPost(post)"><view class="action-icon-box"><image class="feed-share-icon" src="/static/card-share.png" mode="aspectFit" /></view><text>转发</text></view></view>
					<view v-if="(hospitalComments[post.id] || []).length" class="home-comments"><view v-for="comment in hospitalComments[post.id]" :key="comment.id" class="home-comment"><text class="home-comment-author">{{ comment.authorId || '农友' }}</text><text v-if="comment.replyToId" class="home-comment-reply">回复 {{ comment.replyToId }}</text><audio v-if="comment.audioUrl" :src="'http://123.57.255.171:8080' + comment.audioUrl" :name="'语音评论 · ' + comment.audioDuration + '秒'" controls /><text v-else class="home-comment-text">{{ comment.text }}</text></view></view>
				</view>
			</view>
			<view v-else class="empty-tab-content" />
		</view>
		<app-tabbar current="首页" />
	</view>
</template>

<script src="./index.js"></script>

<style scoped src="./index.css"></style>
