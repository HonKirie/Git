package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.VegetableCategory;
import com.ruoyi.system.domain.VegetableRecord;

public interface VegetableMapper
{
    List<VegetableCategory> selectCategories();
    VegetableCategory selectCategory(Long categoryId);
    int countSibling(@Param("parentId") Long parentId, @Param("name") String name, @Param("excludeId") Long excludeId);
    int countChildren(Long categoryId);
    int countDescendants(@Param("categoryId") Long categoryId, @Param("candidateParentId") Long candidateParentId);
    int countRecords(Long categoryId);
    int insertCategory(VegetableCategory category);
    int updateCategory(VegetableCategory category);
    int updateChildrenAncestors(@Param("oldAncestors") String oldAncestors, @Param("newAncestors") String newAncestors, @Param("categoryId") Long categoryId);
    int deleteCategory(Long categoryId);
    List<VegetableRecord> selectRecords(@Param("categoryIds") List<Long> categoryIds, @Param("userId") Long userId);
    VegetableRecord selectRecord(Long recordId);
    int insertRecord(VegetableRecord record);
    int updateRecord(VegetableRecord record);
    int deleteRecord(Long recordId);
}
