package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.VegetableCategory;
import com.ruoyi.system.domain.VegetableRecord;
import com.ruoyi.system.service.IVegetableService;

@RestController
@RequestMapping("/system/vegetable")
public class VegetableController extends BaseController
{
    @Autowired private IVegetableService vegetableService;

    @PreAuthorize("@ss.hasPermi('system:vegetable:list')")
    @GetMapping("/categories")
    public AjaxResult categories() { return success(vegetableService.selectCategories()); }

    @PreAuthorize("@ss.hasPermi('system:vegetable:query')")
    @GetMapping("/category/{categoryId}")
    public AjaxResult category(@PathVariable Long categoryId) { return success(vegetableService.selectCategory(categoryId)); }

    @PreAuthorize("@ss.hasPermi('system:vegetable:list')")
    @GetMapping("/records")
    public TableDataInfo records(Long categoryId, Long userId)
    {
        startPage();
        return getDataTable(vegetableService.selectRecords(categoryId, userId));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetable:query')")
    @GetMapping("/record/{recordId}")
    public AjaxResult record(@PathVariable Long recordId) { return success(vegetableService.selectRecord(recordId)); }

    @PreAuthorize("@ss.hasPermi('system:vegetable:add')")
    @Log(title = "蔬菜管理", businessType = BusinessType.INSERT)
    @PostMapping("/category")
    public AjaxResult addCategory(@Validated @RequestBody VegetableCategory category)
    {
        if (category.getParentId() == null) category.setParentId(0L);
        if (StringUtils.isEmpty(category.getStatus())) category.setStatus("0");
        if (vegetableService.selectCategories().stream().anyMatch(item -> item.getParentId().equals(category.getParentId()) && item.getCategoryName().equals(category.getCategoryName()))) return error("同级分类名称已存在");
        category.setCreateBy(getUsername());
        return toAjax(vegetableService.insertCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetable:edit')")
    @Log(title = "蔬菜管理", businessType = BusinessType.UPDATE)
    @PutMapping("/category")
    public AjaxResult editCategory(@Validated @RequestBody VegetableCategory category)
    {
        if (category.getCategoryId().equals(category.getParentId())) return error("上级分类不能是自己");
        if (category.getParentId() != null && vegetableService.selectCategories().stream().anyMatch(item ->
                category.getParentId().equals(item.getCategoryId()) && item.getAncestors() != null
                && (item.getAncestors().equals(category.getCategoryId().toString())
                || item.getAncestors().contains("," + category.getCategoryId() + ",")
                || item.getAncestors().endsWith("," + category.getCategoryId())))) return error("上级分类不能是当前分类的下级");
        if (vegetableService.selectCategories().stream().anyMatch(item -> item.getParentId().equals(category.getParentId()) && item.getCategoryName().equals(category.getCategoryName()) && !item.getCategoryId().equals(category.getCategoryId()))) return error("同级分类名称已存在");
        category.setUpdateBy(getUsername());
        return toAjax(vegetableService.updateCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetable:remove')")
    @Log(title = "蔬菜管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/category/{categoryId}")
    public AjaxResult removeCategory(@PathVariable Long categoryId)
    {
        if (vegetableService.selectCategories().stream().anyMatch(item -> categoryId.equals(item.getParentId()))) return warn("存在下级分类，不允许删除");
        if (vegetableService.countRecords(categoryId) > 0) return warn("当前分类存在种植记录，不允许删除");
        return toAjax(vegetableService.deleteCategory(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetable:add')")
    @Log(title = "种植记录", businessType = BusinessType.INSERT)
    @PostMapping("/record")
    public AjaxResult addRecord(@RequestBody VegetableRecord record)
    {
        if (record.getCategoryId() == null || record.getUserId() == null || record.getPlantingDays() == null || record.getPlantingDays() < 0 || StringUtils.isEmpty(record.getIssue())) return error("种植作物、种植人、种植天数和问题不能为空");
        record.setCreateBy(getUsername());
        return toAjax(vegetableService.insertRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetable:edit')")
    @Log(title = "种植记录", businessType = BusinessType.UPDATE)
    @PutMapping("/record")
    public AjaxResult editRecord(@RequestBody VegetableRecord record) { record.setUpdateBy(getUsername()); return toAjax(vegetableService.updateRecord(record)); }

    @PreAuthorize("@ss.hasPermi('system:vegetable:remove')")
    @Log(title = "种植记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/record/{recordId}")
    public AjaxResult removeRecord(@PathVariable Long recordId) { return toAjax(vegetableService.deleteRecord(recordId)); }
}
