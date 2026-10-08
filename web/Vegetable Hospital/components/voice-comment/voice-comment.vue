<template>
  <view class="voice-composer">
    <button v-if="!path" class="record" :disabled="busy || requesting" @tap="toggleRecording">{{ requesting ? '正在开启麦克风' : recording ? '结束录音 ' + seconds + '秒' : '开始录音' }}</button>
    <template v-else>
      <button class="preview" :disabled="busy" @tap="preview"><uni-icons :type="playing ? 'pause' : 'sound'" size="20" /> {{ seconds }}秒</button>
      <button class="retry" :disabled="busy" @tap="clearDraft">重录</button>
      <button class="send" :disabled="busy" @tap="send">{{ busy ? '发送中' : '发送' }}</button>
    </template>
  </view>
</template>
<script>
export default {
  props: { postId: { type: Number, required: true }, replyToId: { type: String, default: '' } },
  data() { return { path: '', seconds: 0, recording: false, requesting: false, busy: false, playing: false } },
  beforeDestroy() { this.dispose() },
  beforeUnmount() { this.dispose() },
  methods: {
    tip(title) { uni.showToast({ title, icon: 'none' }) },
    async toggleRecording() {
      if (this.recording) return this.stopRecording()
      if (!uni.getStorageSync('token')) return this.tip('请先登录')
      this.requesting = true
      try {
        // #ifdef H5
        if (!navigator.mediaDevices || !window.MediaRecorder) throw new Error('当前浏览器不支持录音，请使用最新版浏览器和HTTPS或localhost')
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        if (this.disposed) { stream.getTracks().forEach(track => track.stop()); return }
        this.stream = stream
        this.chunks = []
        const mime = ['audio/webm', 'audio/mp4', 'audio/ogg'].find(type => MediaRecorder.isTypeSupported(type))
        this.recorder = new MediaRecorder(stream, mime ? { mimeType: mime } : undefined)
        this.recorder.ondataavailable = e => { if (e.data.size) this.chunks.push(e.data) }
        this.recorder.onstop = () => {
          stream.getTracks().forEach(track => track.stop())
          if (this.disposed) return
          this.blob = new Blob(this.chunks, { type: this.recorder.mimeType })
          if (!this.blob.size) return this.tip('未录到声音，请重试')
          this.path = URL.createObjectURL(this.blob)
        }
        this.recorder.start()
        // #endif
        // #ifndef H5
        this.recorder = uni.getRecorderManager()
        this.recorder.onStop(result => { if (!this.disposed) { this.finishTimer(); this.path = result.tempFilePath } })
        this.recorder.onError(() => { this.finishTimer(); this.tip('录音失败，请检查麦克风权限') })
        this.recorder.start({ duration: 60000, format: 'mp3' })
        // #endif
        this.seconds = 0
        this.recording = true
        this.startedAt = Date.now()
        this.timer = setInterval(() => { this.seconds = Math.min(60, Math.ceil((Date.now() - this.startedAt) / 1000)); if (this.seconds >= 60) this.stopRecording() }, 250)
      } catch (e) {
        if (this.stream) this.stream.getTracks().forEach(track => track.stop())
        this.tip(e.name === 'NotAllowedError' ? '请允许使用麦克风后重试' : e.message || '无法开启麦克风')
      } finally { this.requesting = false }
    },
    finishTimer() { clearInterval(this.timer); this.recording = false; this.seconds = Math.max(1, Math.min(60, Math.ceil((Date.now() - this.startedAt) / 1000))) },
    stopRecording() { this.finishTimer(); if (this.recorder) this.recorder.stop() },
    preview() {
      if (!this.player) {
        this.player = uni.createInnerAudioContext()
        this.player.onEnded(() => { this.playing = false })
        this.player.onError(() => { this.playing = false; this.tip('语音播放失败') })
      }
      if (this.playing) { this.player.stop(); this.playing = false; return }
      this.player.src = this.path; this.player.play(); this.playing = true
    },
    clearDraft() {
      if (this.player) this.player.stop()
      this.playing = false
      // #ifdef H5
      if (this.path) URL.revokeObjectURL(this.path)
      // #endif
      this.path = ''; this.blob = null; this.seconds = 0
    },
    async send() {
      if (this.busy || !this.path) return
      this.busy = true
      try {
        let body
        const url = 'http://123.57.255.171:8080/mobile/comments/voice'
        const header = { Authorization: 'Bearer ' + uni.getStorageSync('token') }
        // #ifdef H5
        const form = new FormData()
        const ext = this.blob.type.includes('mp4') ? 'm4a' : this.blob.type.includes('ogg') ? 'ogg' : 'webm'
        form.append('file', this.blob, 'comment.' + ext)
        form.append('postId', this.postId); form.append('duration', this.seconds); form.append('replyToId', this.replyToId)
        const response = await fetch(url, { method: 'POST', headers: header, body: form })
        body = await response.json()
        // #endif
        // #ifndef H5
        body = await new Promise((resolve, reject) => uni.uploadFile({ url, header, filePath: this.path, name: 'file', formData: { postId: this.postId, duration: this.seconds, replyToId: this.replyToId }, success: res => { try { resolve(JSON.parse(res.data)) } catch (e) { reject(e) } }, fail: reject }))
        // #endif
        if (body.code !== 200) throw new Error(body.msg || '发送失败')
        this.clearDraft(); this.$emit('sent')
      } catch (e) { this.tip(e.message || '发送失败，请重试') } finally { this.busy = false }
    },
    dispose() { this.disposed = true; if (this.recording) this.stopRecording(); clearInterval(this.timer); if (this.stream) this.stream.getTracks().forEach(track => track.stop()); this.clearDraft(); if (this.player) this.player.destroy() }
  }
}
</script>
<style scoped>
.voice-composer { display: flex; align-items: center; gap: 8px; flex: 1; min-width: 0; }
button { margin: 0; padding: 0 10px; font-size: 14px; line-height: 36px; white-space: nowrap; border-radius: 6px; }
.record, .preview { flex: 1; min-width: 0; background: white; color: #23744f; }
.send, .retry { flex: 0 0 auto; color: #167448; background: transparent; }
</style>
