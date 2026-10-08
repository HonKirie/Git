package com.ruoyi.web.controller.mobile;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public AjaxResult list()
    {
        List<SysDictData> tags = dictTypeService.selectDictDataByType("vegetable_tag");
        return AjaxResult.success(tags == null ? Collections.emptyList() : tags);
    }
}
