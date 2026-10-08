package com.ruoyi.system.domain;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

public class VegetableCategory extends BaseEntity
{
    private Long categoryId;
    private Long parentId;
    private String ancestors;
    private String categoryName;
    private Integer orderNum;
    private String status;
    private List<VegetableCategory> children = new ArrayList<>();

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getAncestors() { return ancestors; }
    public void setAncestors(String ancestors) { this.ancestors = ancestors; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<VegetableCategory> getChildren() { return children; }
    public void setChildren(List<VegetableCategory> children) { this.children = children; }
}
