package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDictDataService;

/** Manage vegetable names using the existing dictionary infrastructure. */
@RestController
@RequestMapping("/system/vegetable-tag")
public class VegetableTagController extends BaseController
{
    public static final String DICT_TYPE = "vegetable_tag";

    @Autowired
    private ISysDictDataService dictDataService;

    @PreAuthorize("@ss.hasPermi('system:vegetableTag:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDictData query)
    {
        query.setDictType(DICT_TYPE);
        startPage();
        return getDataTable(dictDataService.selectDictDataList(query));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetableTag:query')")
    @GetMapping("/{dictCode}")
    public AjaxResult getInfo(@PathVariable Long dictCode)
    {
        SysDictData data = dictDataService.selectDictDataById(dictCode);
        return isVegetableTag(data) ? success(data) : error("蔬菜标签不存在");
    }

    @PreAuthorize("@ss.hasPermi('system:vegetableTag:add')")
    @Log(title = "蔬菜标签", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysDictData data)
    {
        String problem = validateTag(data, null);
        if (problem != null) return error(problem);
        data.setDictType(DICT_TYPE);
        data.setDictValue(data.getDictLabel());
        data.setIsDefault("N");
        data.setCreateBy(getUsername());
        return toAjax(dictDataService.insertDictData(data));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetableTag:edit')")
    @Log(title = "蔬菜标签", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysDictData data)
    {
        SysDictData old = data.getDictCode() == null ? null : dictDataService.selectDictDataById(data.getDictCode());
        if (!isVegetableTag(old)) return error("蔬菜标签不存在");
        String problem = validateTag(data, old.getDictCode());
        if (problem != null) return error(problem);
        data.setDictType(DICT_TYPE);
        data.setDictValue(data.getDictLabel());
        data.setIsDefault(old.getIsDefault());
        data.setUpdateBy(getUsername());
        return toAjax(dictDataService.updateDictData(data));
    }

    @PreAuthorize("@ss.hasPermi('system:vegetableTag:remove')")
    @Log(title = "蔬菜标签", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    public AjaxResult remove(@PathVariable Long[] dictCodes)
    {
        if (dictCodes.length == 0) return error("请选择要删除的蔬菜标签");
        for (Long code : dictCodes)
        {
            if (!isVegetableTag(dictDataService.selectDictDataById(code))) return error("蔬菜标签不存在");
        }
        dictDataService.deleteDictDataByIds(dictCodes);
        return success();
    }

    private boolean isVegetableTag(SysDictData data)
    {
        return data != null && DICT_TYPE.equals(data.getDictType());
    }

    private String validateTag(SysDictData data, Long currentCode)
    {
        if (StringUtils.isBlank(data.getDictLabel())) return "蔬菜名称不能为空";
        String name = data.getDictLabel().trim();
        if (name.length() > 30) return "蔬菜名称不能超过30个字符";
        if (data.getDictSort() == null || data.getDictSort() < 0) return "显示排序必须为非负整数";
        if (!"0".equals(data.getStatus()) && !"1".equals(data.getStatus())) return "状态不正确";
        SysDictData query = new SysDictData();
        query.setDictType(DICT_TYPE);
        for (SysDictData existing : dictDataService.selectDictDataList(query))
        {
            if (name.equals(existing.getDictLabel()) && !existing.getDictCode().equals(currentCode))
            {
                return "蔬菜名称已存在";
            }
        }
        data.setDictLabel(name);
        return null;
    }
}
