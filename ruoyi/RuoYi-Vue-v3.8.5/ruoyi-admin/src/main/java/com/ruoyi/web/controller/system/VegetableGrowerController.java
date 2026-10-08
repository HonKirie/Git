package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.service.ISysUserService;

@RestController
@RequestMapping("/system/vegetable")
public class VegetableGrowerController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @PreAuthorize("@ss.hasPermi('system:vegetable:list')")
    @GetMapping("/growers")
    public TableDataInfo growers(SysUser query)
    {
        query.setStatus("0");
        startPage();
        List<SysUser> users = userService.selectUserList(query);
        return getDataTable(users);
    }
}
