package com.ruoyi.web.controller.mobile;
import org.springframework.beans.factory.annotation.Autowired; import org.springframework.web.bind.annotation.*; import com.ruoyi.common.core.domain.AjaxResult; import com.ruoyi.common.utils.SecurityUtils; import com.ruoyi.system.domain.HospitalFavorite; import com.ruoyi.system.service.IHospitalFavoriteService;
import com.ruoyi.system.service.IHospitalPostService;

@RestController @RequestMapping("/mobile/favorites") public class HospitalFavoriteController {
    @Autowired private IHospitalFavoriteService service;
    @Autowired private IHospitalPostService postService;
    @GetMapping("/user/{account}") public AjaxResult list(@PathVariable String account) { return AjaxResult.success(service.selectByAuthorAccount(account)); }
    @PostMapping public AjaxResult add(@RequestBody HospitalFavorite r) {
        if (r.getPostId() == null) return AjaxResult.error("帖子ID不能为空");
        if (postService.selectPostById(r.getPostId()) == null) return AjaxResult.error("帖子不存在或已被删除");
        Long u = SecurityUtils.getUserId();
        if (service.selectByUserAndPost(u, r.getPostId()) != null) return AjaxResult.success();
        r.setUserId(u);
        r.setAuthorAccount(SecurityUtils.getUsername());
        return service.insert(r) > 0 ? AjaxResult.success() : AjaxResult.error("帖子不存在或已被删除");
    }
}
