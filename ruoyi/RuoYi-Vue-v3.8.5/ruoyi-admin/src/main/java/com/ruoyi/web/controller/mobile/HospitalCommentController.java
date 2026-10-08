package com.ruoyi.web.controller.mobile;

import java.util.List;
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
import com.ruoyi.system.domain.HospitalComment;
import com.ruoyi.system.service.IHospitalCommentService;

/**
 * 移动端病害问答评论接口。评论作者只从当前 token 读取，不能由客户端指定。
 */
@RestController
@RequestMapping("/mobile/comments")
public class HospitalCommentController
{
    @Autowired
    private IHospitalCommentService commentService;

    @Autowired
    private com.ruoyi.system.service.IHospitalPostService postService;

    @PostMapping("/voice")
    public AjaxResult voice(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam("postId") Long postId,
            @org.springframework.web.bind.annotation.RequestParam("duration") Integer duration,
            @org.springframework.web.bind.annotation.RequestParam(value = "replyToId", required = false) String replyToId) throws Exception
    {
        Long userId = SecurityUtils.getUserId();
        if (postService.selectPostById(postId) == null) return AjaxResult.error("问题不存在或已删除");
        if (duration < 1 || duration > 60 || file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
            return AjaxResult.error("语音需为1至60秒，文件不超过5MB");
        String url = com.ruoyi.common.utils.file.FileUploadUtils.upload(
            com.ruoyi.common.config.RuoYiConfig.getUploadPath(), file,
            new String[] { "webm", "ogg", "mp4", "m4a", "mp3", "wav", "aac" });
        HospitalComment comment = new HospitalComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setAuthorId(SecurityUtils.getUsername());
        comment.setCommentText("[语音评论]");
        comment.setAudioUrl(url);
        comment.setAudioDuration(duration);
        comment.setReplyToId(StringUtils.substring(replyToId, 0, 30));
        commentService.insertComment(comment);
        return AjaxResult.success(comment);
    }

    @GetMapping("/{postId}")
    public AjaxResult list(@PathVariable Long postId)
    {
        return AjaxResult.success(commentService.selectCommentsByPostId(postId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody HospitalComment comment)
    {
        if (comment.getPostId() == null || StringUtils.isEmpty(comment.getCommentText())
                || comment.getCommentText().trim().length() > 200)
        {
            return AjaxResult.error("评论内容不正确");
        }

        comment.setUserId(SecurityUtils.getUserId());
        comment.setAudioUrl(null);
        comment.setAudioDuration(null);
        comment.setAuthorId(SecurityUtils.getUsername());
        comment.setCommentText(comment.getCommentText().trim());
        comment.setReplyToId(StringUtils.substring(comment.getReplyToId(), 0, 30));
        commentService.insertComment(comment);
        return AjaxResult.success(comment);
    }

    @DeleteMapping("/{commentId}")
    public AjaxResult remove(@PathVariable Long commentId)
    {
        HospitalComment comment = commentService.selectCommentById(commentId);
        if (comment == null)
        {
            return AjaxResult.error("评论不存在或已删除");
        }
        if (!SecurityUtils.getUserId().equals(comment.getUserId()))
        {
            return AjaxResult.error("只能删除自己的评论");
        }
        return commentService.deleteCommentById(commentId) == 1 ? AjaxResult.success() : AjaxResult.error("删除评论失败");
    }
}
