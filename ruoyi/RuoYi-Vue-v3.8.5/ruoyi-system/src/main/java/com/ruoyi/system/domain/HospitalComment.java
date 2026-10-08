package com.ruoyi.system.domain;

import java.util.Date;

/**
 * 移动端病害问答评论。
 */
public class HospitalComment
{
    private Long commentId;
    private Long postId;
    private Long userId;
    private String authorId;
    private String authorAccount;
    private String commentText;
    private String audioUrl;
    private Integer audioDuration;

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public Integer getAudioDuration() { return audioDuration; }
    public void setAudioDuration(Integer audioDuration) { this.audioDuration = audioDuration; }
    private String replyToId;
    private Date createTime;

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorAccount() { return authorAccount; }
    public void setAuthorAccount(String authorAccount) { this.authorAccount = authorAccount; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
