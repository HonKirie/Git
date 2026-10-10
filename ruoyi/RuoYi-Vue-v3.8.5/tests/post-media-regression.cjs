const fs = require('fs')
const path = require('path')
const vm = require('vm')
const assert = require('assert')
const compiler = require('../ruoyi-ui/node_modules/@vue/compiler-sfc')
const app = path.resolve(__dirname, '../../../web/Vegetable Hospital')
let requests = [], uploads = [], failUpload = false, expireUpload = false
const uni = {
  getStorageSync: () => 'test-token', removeStorageSync() {},
  showLoading() {}, hideLoading() {}, showToast() {}, redirectTo() {},
  request(options) { requests.push(options) },
  uploadFile(options) {
    uploads.push(options)
    if (failUpload) return options.fail()
    options.success({ statusCode: 200, data: JSON.stringify(expireUpload ? { code: 401 } : { code: 200, fileName: '/profile/upload/photo.jpg' }) })
  }
}
const media = vm.runInNewContext(fs.readFileSync(path.join(app, 'utils/post-media.js'), 'utf8')
  .replace(/export /g, '') + '\n;({uploadPostMedia,mediaUrl})', { uni, Error })
const source = fs.readFileSync(path.join(app, 'pages/hospital/publish.vue'), 'utf8')
const parsed = compiler.parse(source)
assert.equal(parsed.errors.length, 0)
assert.equal(compiler.compileTemplate({ source: parsed.descriptor.template.content, filename: 'publish.vue', id: 'test' }).errors.length, 0)
const component = vm.runInNewContext(parsed.descriptor.script.content.replace(/^import .*$/m, '').replace('export default', 'module.exports ='), { module: { exports: {} }, uni, ...media, Date })
function page() {
  const instance = component.data()
  Object.entries(component.methods).forEach(([key, method]) => { instance[key] = method.bind(instance) })
  Object.assign(instance, { crop: '黄瓜', plantingTime: '1-7天', description: 'test', mediaType: 'image', mediaFiles: ['_doc/local.jpg'] })
  return instance
}
async function run() {
  assert.equal(media.mediaUrl('/profile/a.jpg'), 'http://123.57.255.171:8080/profile/a.jpg')
  const instance = page()
  const pending = instance.submitQuestion()
  await instance.submitQuestion()
  await pending
  assert.equal(uploads.length, 1)
  assert.equal(requests.length, 1)
  assert.equal(uploads[0].header.Authorization, 'Bearer test-token')
  assert.equal(JSON.parse(requests[0].data.mediaFiles)[0], 'http://123.57.255.171:8080/profile/upload/photo.jpg')
  requests[0].fail(); requests[0].complete()
  await instance.submitQuestion()
  assert.equal(uploads.length, 1, 'Retry reuses completed upload')
  requests[1].complete()
  requests = []; failUpload = true
  const failed = page(); await failed.submitQuestion()
  assert.equal(requests.length, 0); assert.equal(failed.submitting, false)
  failUpload = false; expireUpload = true
  await page().submitQuestion(); assert.equal(requests.length, 0)
  expireUpload = false
  const text = page(); text.mediaFiles = []
  await text.submitQuestion(); assert.equal(requests.length, 1)
  requests[0].complete()
  text.mediaFiles = ['local.jpg']; text.changeMediaType('video')
  assert.equal(text.mediaFiles.length, 0)
  console.log('PASS: upload-before-publish, URLs, token, duplicate clicks, retry, failure, expiry, text-only, type switch')
}
run().catch(error => { console.error(error); process.exitCode = 1 })
