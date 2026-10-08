package com.ruoyi.web.controller.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.HospitalPost;
import com.ruoyi.system.service.IHospitalPostService;

@RestController
@RequestMapping("/mobile/posts")
public class HospitalPostController
{
    @Autowired
    private IHospitalPostService postService;

    @GetMapping
    public AjaxResult list()
    {
        return AjaxResult.success(postService.selectPostList());
    }

    @GetMapping("/{postId}")
    public AjaxResult get(@PathVariable Long postId)
    {
        return AjaxResult.success(postService.selectPostById(postId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody HospitalPost post)
    {
        if (StringUtils.isEmpty(post.getCrop()) || StringUtils.isEmpty(post.getPlantingTime())
                || StringUtils.isEmpty(post.getDescription()))
        {
            return AjaxResult.error("问题内容不完整");
        }
        if (post.getPostId() != null && postService.selectPostById(post.getPostId()) != null)
        {
            return AjaxResult.success(postService.selectPostById(post.getPostId()));
        }
        post.setUserId(SecurityUtils.getUserId());
        post.setAuthorId(SecurityUtils.getUsername());
        post.setDescription(StringUtils.substring(post.getDescription().trim(), 0, 1000));
        post.setMediaFiles(StringUtils.substring(post.getMediaFiles(), 0, 4000));
        postService.insertPost(post);
        return AjaxResult.success(post);
    }

    @DeleteMapping("/{postId}")
    public AjaxResult remove(@PathVariable Long postId)
    {
        HospitalPost post = postService.selectPostById(postId);
        if (post == null) return AjaxResult.error("问题不存在或已删除");
        if (!SecurityUtils.getUserId().equals(post.getUserId())) return AjaxResult.error("只能删除自己发布的问题");
        return postService.deletePostById(postId) == 1 ? AjaxResult.success() : AjaxResult.error("删除问题失败");
    }
}
