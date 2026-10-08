<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="蔬菜名称" prop="dictLabel"><el-input v-model="queryParams.dictLabel" placeholder="请输入蔬菜名称" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="标签状态" clearable><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['system:vegetableTag:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['system:vegetableTag:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:vegetableTag:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>
    <el-table v-loading="loading" :data="tagList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="标签编号" prop="dictCode" align="center" width="110" />
      <el-table-column label="蔬菜名称" prop="dictLabel" align="center" />
      <el-table-column label="标签值" prop="dictValue" align="center" />
      <el-table-column label="显示排序" prop="dictSort" align="center" width="110" />
      <el-table-column label="状态" prop="status" align="center" width="100"><template slot-scope="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="备注" prop="remark" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" align="center" width="180"><template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template></el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="170"><template slot-scope="scope"><el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:vegetableTag:edit']">修改</el-button><el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['system:vegetableTag:remove']">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="蔬菜名称" prop="dictLabel"><el-input v-model.trim="form.dictLabel" maxlength="30" placeholder="例如：黄瓜" /></el-form-item>
        <el-form-item label="显示排序" prop="dictSort"><el-input-number v-model="form.dictSort" controls-position="right" :min="0" :precision="0" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" maxlength="500" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listVegetableTag, getVegetableTag, addVegetableTag, updateVegetableTag, delVegetableTag } from '@/api/system/vegetableTag'

export default {
  name: 'VegetableTag',
  data() {
    return {
      loading: true, showSearch: true, ids: [], single: true, multiple: true,
      tagList: [], total: 0, title: '', open: false,
      statusOptions: [{ label: '启用', value: '0' }, { label: '停用', value: '1' }],
      queryParams: { pageNum: 1, pageSize: 10, dictLabel: undefined, status: undefined },
      form: {},
      rules: {
        dictLabel: [{ required: true, message: '蔬菜名称不能为空', trigger: 'blur' }],
        dictSort: [{ required: true, message: '显示排序不能为空', trigger: 'change' }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listVegetableTag(this.queryParams).then(response => {
        this.tagList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    reset() {
      this.form = { dictCode: undefined, dictLabel: '', dictSort: 0, status: '0', remark: '' }
      this.resetForm('form')
    },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm('queryForm'); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.dictCode)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() { this.reset(); this.title = '新增蔬菜标签'; this.open = true },
    handleUpdate(row) {
      this.reset()
      getVegetableTag(row.dictCode || this.ids[0]).then(response => {
        this.form = response.data
        this.title = '修改蔬菜标签'
        this.open = true
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const action = this.form.dictCode == null ? addVegetableTag : updateVegetableTag
        action(this.form).then(() => {
          this.$modal.msgSuccess(this.form.dictCode == null ? '新增成功' : '修改成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const codes = row.dictCode || this.ids
      this.$modal.confirm('确认删除选中的蔬菜标签吗？历史贴文不会被删除。').then(() => delVegetableTag(codes)).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>
