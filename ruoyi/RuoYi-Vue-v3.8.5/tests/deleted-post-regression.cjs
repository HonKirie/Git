const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const { execFileSync } = require('node:child_process')
const root = path.resolve(__dirname, '..')
const page = fs.readFileSync(path.resolve(root, '../../web/Vegetable Hospital/pages/profile/user.vue'), 'utf8')
const requests = []
const context = { uni: { getStorageSync: key => key === 'myReposts' ? [{ postId: 99 }] : 'tester', request: request => requests.push(request) } }
vm.createContext(context)
vm.runInContext(page.split('<script>')[1].split('</script>')[0].replace('export default', 'globalThis.component ='), context)
const state = { account: 'tester', reposts: [{ postId: 99 }], favorites: [{ postId: 99 }], ...context.component.methods }
state.loadProfileData()
assert.equal(state.reposts.length, 0)
assert.equal(state.favorites.length, 0)
for (const kind of ['reposts', 'favorites']) {
  const request = requests.find(item => item.url.includes('/' + kind + '/'))
  request.success({ data: { code: 200, data: [{ postId: 1 }] } })
  assert.equal(state[kind].length, 1)
  request.success({ data: { code: 200, data: [] } })
  assert.equal(state[kind].length, 0)
}
console.log('PASS: profile never restores deleted posts from local cache')
const statements = []
const expected = []
for (const kind of ['Favorite', 'Repost']) {
  const table = 'sys_hospital_' + kind.toLowerCase()
  const xml = fs.readFileSync(path.join(root, 'ruoyi-system/src/main/resources/mapper/system/Hospital' + kind + 'Mapper.xml'), 'utf8')
  const values = { account: "'tester'", userId: '1', authorAccount: "'tester'", postId: '1', createdAt: "'test'", originalAuthorId: "'test'", originalAuthorAccount: "'test'", crop: "'test'", plantingTime: "'test'", description: "'test'" }
  const sql = id => xml.match(new RegExp('<(?:select|insert) id="' + id + '"[^>]*>([\\s\\S]*?)</(?:select|insert)>'))[1].replace(/#\{(\w+)\}/g, (_, name) => values[name])
  statements.push(`CREATE TEMPORARY TABLE ${table} LIKE Vegetable.${table};`)
  if (kind === 'Favorite') statements.push('CREATE TEMPORARY TABLE sys_hospital_post LIKE Vegetable.sys_hospital_post;')
  statements.push('INSERT INTO sys_hospital_post(post_id,user_id,author_id,crop,planting_time,description,created_at) VALUES(1,1,\'test\',\'test\',\'test\',\'test\',\'test\');')
  statements.push(sql('insert') + '; SELECT ROW_COUNT();')
  statements.push('SELECT COUNT(*) FROM (' + sql('selectByAuthorAccount') + ') active_records;')
  statements.push('DELETE FROM sys_hospital_post WHERE post_id=1;')
  statements.push('SELECT COUNT(*) FROM (' + sql('selectByAuthorAccount') + ') active_records;')
  values.postId = '2'
  statements.push(sql('insert') + '; SELECT ROW_COUNT();')
  expected.push('1', '1', '0', '0')
}
const input = statements.join('\n').replace(/(?<!Vegetable\.)\bsys_hospital_(favorite|repost|post)\b/g, 'regression_$1')
const output = execFileSync('mysql', ['-h127.0.0.1', '-uroot', '-N', 'Vegetable'], { input, encoding: 'utf8' }).trim().split('\n')
assert.deepEqual(output, expected)
console.log('PASS: favorite and repost queries hide deleted posts and inserts reject missing posts (temporary tables only)')
