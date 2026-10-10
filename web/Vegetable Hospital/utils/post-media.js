export const MEDIA_SERVER = 'http://123.57.255.171:8080'

export function mediaUrl(path) {
	if (typeof path !== 'string') return ''
	return path.startsWith('/profile/') ? MEDIA_SERVER + path : path
}

export function uploadPostMedia(filePath, token) {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: MEDIA_SERVER + '/common/upload',
			filePath,
			name: 'file',
			header: { Authorization: 'Bearer ' + token },
			success: res => {
				if (res.statusCode === 413) return reject(new Error('文件太大，请压缩后重试'))
				let body
				try { body = JSON.parse(res.data) } catch (e) { return reject(new Error('上传响应异常，请重试')) }
				if (res.statusCode === 401 || body.code === 401) {
					const error = new Error('登录已过期，请重新登录')
					error.code = 401
					return reject(error)
				}
				if (res.statusCode !== 200 || body.code !== 200) return reject(new Error(body.msg || '文件上传失败'))
				// Store the public path rather than a URL derived from proxy headers.
				if (typeof body.fileName !== 'string' || !body.fileName.startsWith('/profile/')) {
					return reject(new Error('服务器未返回有效的文件地址'))
				}
				resolve(mediaUrl(body.fileName))
			},
			fail: () => reject(new Error('文件上传失败，请检查网络后重试'))
		})
	})
}
