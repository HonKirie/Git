<template>
  <div class="app-container vegetable-layout">
    <aside class="category-pane">
      <div class="pane-heading"><span>蔬菜分类</span><div><el-button type="text" icon="el-icon-plus" title="新增分类" v-hasPermi="['system:vegetable:add']" @click="openCategory()" /><el-button type="text" icon="el-icon-refresh" title="刷新分类" @click="loadCategories" /></div></div>
      <el-input v-model.trim="categoryFilter" size="small" clearable prefix-icon="el-icon-search" placeholder="搜索当前分类" />
      <el-tree ref="categoryTree" :data="categoryTree" node-key="categoryId" :props="treeProps" :filter-node-method="filterCategory" highlight-current default-expand-all :expand-on-click-node="false" @node-click="selectCategory">
        <span slot-scope="{ data }" class="tree-row"><span class="tree-label">{{ data.categoryName }}</span><span class="tree-actions"><el-button type="text" icon="el-icon-plus" title="新增下级" v-hasPermi="['system:vegetable:add']" @click.stop="openCategory(data)" /><el-button type="text" icon="el-icon-edit" title="修改分类" v-hasPermi="['system:vegetable:edit']" @click.stop="openCategory(data, true)" /><el-button type="text" icon="el-icon-delete" title="删除分类" v-hasPermi="['system:vegetable:remove']" @click.stop="removeCategory(data)" /></span></span>
      </el-tree>
    </aside>
    <main class="record-pane">
      <div class="pane-heading"><span>{{ selectedCategory ? selectedCategory.categoryName : '全部作物' }} · 种植记录</span><el-button type="primary" size="mini" icon="el-icon-plus" v-hasPermi="['system:vegetable:add']" @click="openRecord()">新增记录</el-button></div>
      <el-form :inline="true" size="small" :model="query">
        <el-form-item label="种植人"><el-select v-model="query.userId" filterable clearable remote :remote-method="searchGrowers" :loading="growerLoading" placeholder="输入用户名搜索" @change="refreshRecords"><el-option v-for="user in growers" :key="user.userId" :value="user.userId" :label="user.userName + (user.nickName ? ' (' + user.nickName + ')' : '')" /></el-select></el-form-item>
        <el-form-item><el-button icon="el-icon-refresh" @click="refreshRecords">刷新</el-button><el-button icon="el-icon-refresh-left" @click="resetCategoryQuery">重置</el-button></el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="categoryName" label="种植作物" min-width="110" />
        <el-table-column prop="userName" label="种植人" min-width="140"><template slot-scope="scope">{{ scope.row.userName }}<span v-if="scope.row.nickName" class="secondary"> ({{ scope.row.nickName }})</span></template></el-table-column>
        <el-table-column prop="plantingDays" label="种植天数" width="110"><template slot-scope="scope">{{ scope.row.sourceType === 'question' ? (scope.row.plantingDays === 99999 ? '20天以上' : scope.row.plantingDays + '天') : scope.row.plantingDays + ' 天' }}</template></el-table-column>
        <el-table-column prop="issue" label="出现的问题" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="记录时间" width="170"><template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="130" fixed="right"><template slot-scope="scope"><el-button v-if="scope.row.sourceType !== 'question'" type="text" icon="el-icon-edit" title="修改记录" v-hasPermi="['system:vegetable:edit']" @click="openRecord(scope.row)" /><el-button v-if="scope.row.sourceType !== 'question'" type="text" icon="el-icon-delete" title="删除记录" v-hasPermi="['system:vegetable:remove']" @click="removeRecord(scope.row)" /><span v-else class="question-source">前端问题</span></template></el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="loadRecords" />
    </main>
    <el-dialog :title="categoryForm.categoryId ? '修改分类' : '新增分类'" :visible.sync="categoryOpen" width="480px" append-to-body>
      <el-form ref="categoryForm" :model="categoryForm" :rules="categoryRules" label-width="90px">
        <el-form-item label="上级分类" prop="parentId"><el-cascader v-model="categoryForm.parentId" :options="parentOptions" :props="{ value: 'categoryId', label: 'categoryName', checkStrictly: true, emitPath: false }" clearable placeholder="无（顶级分类）" style="width:100%" /></el-form-item>
        <el-form-item label="分类名称" prop="categoryName"><el-input v-model.trim="categoryForm.categoryName" maxlength="30" /></el-form-item>
        <el-form-item label="显示排序" prop="orderNum"><el-input-number v-model="categoryForm.orderNum" :min="0" :precision="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="categoryForm.status"><el-radio label="0">启用</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item>
      </el-form><div slot="footer"><el-button @click="categoryOpen = false">取消</el-button><el-button type="primary" @click="submitCategory">确定</el-button></div>
    </el-dialog>
    <el-dialog :title="recordForm.recordId ? '修改种植记录' : '新增种植记录'" :visible.sync="recordOpen" width="560px" append-to-body>
      <el-form ref="recordForm" :model="recordForm" :rules="recordRules" label-width="90px">
        <el-form-item label="种植作物" prop="categoryId"><el-select v-model="recordForm.categoryId" filterable placeholder="选择作物" style="width:100%"><el-option v-for="item in availableCrops" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item>
        <el-form-item label="种植人" prop="userId"><el-select v-model="recordForm.userId" filterable remote :remote-method="searchGrowers" :loading="growerLoading" placeholder="输入用户名搜索" style="width:100%"><el-option v-for="user in growers" :key="user.userId" :value="user.userId" :label="user.userName + (user.nickName ? ' (' + user.nickName + ')' : '')" /></el-select></el-form-item>
        <el-form-item label="种植天数" prop="plantingDays"><el-input-number v-model="recordForm.plantingDays" :min="0" :max="36500" :precision="0" /></el-form-item>
        <el-form-item label="出现的问题" prop="issue"><el-input v-model.trim="recordForm.issue" type="textarea" :rows="4" maxlength="1000" show-word-limit /></el-form-item>
      </el-form><div slot="footer"><el-button @click="recordOpen = false">取消</el-button><el-button type="primary" @click="submitRecord">确定</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listCategories, addCategory, updateCategory, deleteCategory, listRecords, addRecord, updateRecord, deleteRecord, listGrowers } from '@/api/system/vegetable'

export default {
  name: 'Vegetable',
  data() {
    return {
      categories: [], selectedCategory: null, categoryFilter: '', treeProps: { label: 'categoryName', children: 'children' },
      categoryOpen: false, categoryForm: {}, recordOpen: false, recordForm: {}, growers: [], growerLoading: false,
      loading: false, records: [], total: 0, query: { pageNum: 1, pageSize: 10, userId: undefined },
      categoryRules: { categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }], orderNum: [{ required: true, message: '请输入排序', trigger: 'change' }] },
      recordRules: { categoryId: [{ required: true, message: '请选择作物', trigger: 'change' }], userId: [{ required: true, message: '请选择种植人', trigger: 'change' }], plantingDays: [{ required: true, message: '请输入天数', trigger: 'change' }], issue: [{ required: true, message: '请填写问题', trigger: 'blur' }] }
    }
  },
  computed: {
    categoryTree() { return this.handleTree(this.categories.map(item => ({ ...item, children: [] })), 'categoryId') },
    availableCrops() { return this.categories.filter(item => item.status === '0' && !this.categories.some(child => child.parentId === item.categoryId)) },
    parentOptions() { return this.handleTree(this.categories.filter(item => item.categoryId !== this.categoryForm.categoryId).map(item => ({ ...item, children: [] })), 'categoryId') }
  },
  watch: { categoryFilter(value) { this.$refs.categoryTree && this.$refs.categoryTree.filter(value) } },
  created() { this.loadCategories(); this.loadRecords(); this.searchGrowers('') },
  methods: {
    filterCategory(value, data) { return !value || data.categoryName.includes(value) },
    resetCategoryQuery() {
      this.categoryFilter = ''
      this.selectedCategory = null
      this.query = { pageNum: 1, pageSize: this.query.pageSize, userId: undefined }
      this.$nextTick(() => {
        if (this.$refs.categoryTree) {
          this.$refs.categoryTree.setCurrentKey(null)
          this.$refs.categoryTree.filter('')
        }
      })
      this.loadRecords()
    },
    loadCategories() {
      listCategories().then(response => {
        this.categories = response.data || []
        if (this.selectedCategory) this.selectedCategory = this.categories.find(item => item.categoryId === this.selectedCategory.categoryId) || null
        this.loadRecords()
      })
    },
    selectCategory(item) { this.selectedCategory = item; this.refreshRecords() },
    refreshRecords() { this.query.pageNum = 1; this.loadRecords() },
    loadRecords() {
      this.loading = true
      listRecords({ ...this.query, categoryId: this.selectedCategory ? this.selectedCategory.categoryId : undefined }).then(response => {
        this.records = response.rows || []; this.total = response.total || 0; this.loading = false
      }).catch(() => { this.loading = false })
    },
    searchGrowers(userName) {
      this.growerLoading = true
      listGrowers({ pageNum: 1, pageSize: 50, userName: userName || undefined }).then(response => {
        this.growers = response.rows || []; this.growerLoading = false
      }).catch(() => { this.growerLoading = false })
    },
    openCategory(item, editing = false) {
      this.categoryForm = editing ? { ...item } : { parentId: item ? item.categoryId : 0, categoryName: '', orderNum: 0, status: '0' }
      this.categoryOpen = true
      this.$nextTick(() => this.$refs.categoryForm && this.$refs.categoryForm.clearValidate())
    },
    submitCategory() {
      this.$refs.categoryForm.validate(valid => {
        if (!valid) return
        const data = { ...this.categoryForm, parentId: this.categoryForm.parentId || 0 }
        const action = data.categoryId ? updateCategory : addCategory
        action(data).then(() => { this.categoryOpen = false; this.loadCategories(); this.$modal.msgSuccess('保存成功') })
      })
    },
    removeCategory(item) {
      this.$modal.confirm('确认删除分类“' + item.categoryName + '”吗？').then(() => deleteCategory(item.categoryId)).then(() => { this.loadCategories(); this.$modal.msgSuccess('删除成功') }).catch(() => {})
    },
    openRecord(item) {
      this.recordForm = item ? { recordId: item.recordId, categoryId: item.categoryId, userId: item.userId, plantingDays: item.plantingDays, issue: item.issue } : { categoryId: this.selectedCategory && this.availableCrops.some(crop => crop.categoryId === this.selectedCategory.categoryId) ? this.selectedCategory.categoryId : undefined, userId: undefined, plantingDays: 0, issue: '' }
      if (item && !this.growers.some(user => user.userId === item.userId)) this.growers.push({ userId: item.userId, userName: item.userName, nickName: item.nickName })
      this.recordOpen = true
      this.$nextTick(() => this.$refs.recordForm && this.$refs.recordForm.clearValidate())
    },
    submitRecord() {
      this.$refs.recordForm.validate(valid => {
        if (!valid) return
        const action = this.recordForm.recordId ? updateRecord : addRecord
        action(this.recordForm).then(() => { this.recordOpen = false; this.loadRecords(); this.$modal.msgSuccess('保存成功') })
      })
    },
    removeRecord(item) {
      this.$modal.confirm('确认删除这条种植记录吗？').then(() => deleteRecord(item.recordId)).then(() => { this.loadRecords(); this.$modal.msgSuccess('删除成功') }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.vegetable-layout { display: grid; grid-template-columns: minmax(250px, 300px) minmax(0, 1fr); gap: 20px; align-items: start; }
.category-pane { min-height: 560px; border-right: 1px solid #e4e7ed; padding-right: 18px; }
.record-pane { min-width: 0; }
.pane-heading { height: 42px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; font-size: 16px; font-weight: 600; color: #303133; }
.tree-row { display: flex; align-items: center; justify-content: space-between; width: 100%; min-width: 0; }
.tree-label { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.tree-actions { display: none; flex: 0 0 auto; }
.tree-row:hover .tree-actions { display: flex; }
.tree-actions .el-button { margin: 0 2px; padding: 4px; }
.secondary { color: #909399; }.question-source { color: #67c23a; font-size: 12px; }
@media (max-width: 900px) { .vegetable-layout { grid-template-columns: minmax(0, 1fr); } .category-pane { min-height: 0; border-right: 0; border-bottom: 1px solid #e4e7ed; padding: 0 0 18px; } .tree-actions { display: flex; } }
</style>
