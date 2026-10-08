package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class VegetableRecord extends BaseEntity
{
    private Long recordId;
    private Long categoryId;
    private Long userId;
    private Integer plantingDays;
    private String issue;
    private String categoryName;
    private String userName;
    private String nickName;
    private String sourceType;
    private Long sourcePostId;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getPlantingDays() { return plantingDays; }
    public void setPlantingDays(Integer plantingDays) { this.plantingDays = plantingDays; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourcePostId() { return sourcePostId; }
    public void setSourcePostId(Long sourcePostId) { this.sourcePostId = sourcePostId; }
}
