package com.ruoyi.system.domain;

/**
 * 移动端病害问答帖子。
 */
public class HospitalPost
{
    private Long postId;
    private Long userId;
    private String authorId;
    private String authorAccount;
    private String crop;
    private String plantingTime;
    private String description;
    private String mediaType;
    private String mediaFiles;
    private String location;
    private String createdAt;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorAccount() { return authorAccount; }
    public void setAuthorAccount(String authorAccount) { this.authorAccount = authorAccount; }
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    public String getPlantingTime() { return plantingTime; }
    public void setPlantingTime(String plantingTime) { this.plantingTime = plantingTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getMediaFiles() { return mediaFiles; }
    public void setMediaFiles(String mediaFiles) { this.mediaFiles = mediaFiles; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
