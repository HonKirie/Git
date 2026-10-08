package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VegetableCategory;
import com.ruoyi.system.domain.VegetableRecord;

public interface IVegetableService
{
    List<VegetableCategory> selectCategories();
    VegetableCategory selectCategory(Long id);
    int insertCategory(VegetableCategory category);
    int updateCategory(VegetableCategory category);
    int deleteCategory(Long id);
    int countChildren(Long id);
    int countRecords(Long id);
    List<VegetableRecord> selectRecords(Long categoryId, Long userId);
    VegetableRecord selectRecord(Long id);
    int insertRecord(VegetableRecord record);
    int updateRecord(VegetableRecord record);
    int deleteRecord(Long id);
}
