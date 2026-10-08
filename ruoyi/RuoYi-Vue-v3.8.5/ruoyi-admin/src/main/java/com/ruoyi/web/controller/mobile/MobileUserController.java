package com.ruoyi.web.controller.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;

@RestController
@RequestMapping("/mobile/users")
public class MobileUserController {
    @Autowired private ISysUserService userService;

    @GetMapping("/{account}")
    public AjaxResult get(@PathVariable String account) {
        SysUser user = userService.selectUserByUserName(account);
        if (user == null) return AjaxResult.error("用户不存在");
        return AjaxResult.success(user);
    }
}
