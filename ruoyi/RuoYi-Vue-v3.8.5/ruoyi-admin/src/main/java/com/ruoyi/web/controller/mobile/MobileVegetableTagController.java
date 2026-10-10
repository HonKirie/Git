package com.ruoyi.web.controller.mobile;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.system.service.IVegetableService;
import com.ruoyi.system.domain.VegetableCategory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.system.service.ISysDictTypeService;

@RestController
@RequestMapping("/mobile/vegetable-tags")
public class MobileVegetableTagController
{
    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private ISysDictDataService dictDataService;

    @Autowired
    private IVegetableService vegetableService;

    @PostMapping
    @Transactional
    public synchronized AjaxResult add(@RequestBody SysDictData request)
    {
        String username = SecurityUtils.getUsername();
        String name = request.getDictLabel();
        if (StringUtils.isBlank(name)) return AjaxResult.error("请输入作物名称");
        name = name.trim();
        if (name.length() > 20 || "全部".equals(name)) return AjaxResult.error("作物名称不合法，最多20个字符");
        if (name.matches("[\\p{N}\\s]+")) return AjaxResult.error("作物名称不能是纯数字");
        SysDictData query = new SysDictData();
        query.setDictType("vegetable_tag");
        for (SysDictData existing : dictDataService.selectDictDataList(query))
        {
            if (name.equals(existing.getDictLabel()))
                return AjaxResult.error("该作物已存在");
        }
        SysDictData tag = new SysDictData();
        tag.setDictType("vegetable_tag");
        tag.setDictLabel(name);
        tag.setDictValue(name);
        tag.setDictSort(0L);
        tag.setStatus("0");
        tag.setIsDefault("N");
        tag.setCreateBy(username);
        if (dictDataService.insertDictData(tag) <= 0) throw new IllegalStateException("添加作物失败");
        final String categoryName = name;
        boolean categoryExists = vegetableService.selectCategories().stream()
            .anyMatch(category -> categoryName.equals(category.getCategoryName()));
        if (!categoryExists)
        {
            VegetableCategory category = new VegetableCategory();
            category.setParentId(0L);
            category.setCategoryName(name);
            category.setOrderNum(0);
            category.setStatus("0");
            category.setCreateBy(username);
            if (vegetableService.insertCategory(category) <= 0) throw new IllegalStateException("添加蔬菜分类失败");
        }
        return AjaxResult.success(tag);
    }

    @GetMapping
    public AjaxResult list()
    {
        List<SysDictData> tags = dictTypeService.selectDictDataByType("vegetable_tag");
        return AjaxResult.success(tags == null ? Collections.emptyList() : tags);
    }
}
