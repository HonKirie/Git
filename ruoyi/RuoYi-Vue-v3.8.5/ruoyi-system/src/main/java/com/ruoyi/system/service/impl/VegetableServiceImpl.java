package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.VegetableCategory;
import com.ruoyi.system.domain.VegetableRecord;
import com.ruoyi.system.mapper.VegetableMapper;
import com.ruoyi.system.service.IVegetableService;

@Service
public class VegetableServiceImpl implements IVegetableService
{
    @Autowired
    private VegetableMapper mapper;

    @Override public List<VegetableCategory> selectCategories() { return mapper.selectCategories(); }
    @Override public VegetableCategory selectCategory(Long id) { return mapper.selectCategory(id); }

    @Override
    @Transactional
    public int insertCategory(VegetableCategory category)
    {
        Long parentId = category.getParentId() == null ? 0L : category.getParentId();
        category.setParentId(parentId);
        VegetableCategory parent = parentId == 0L ? null : mapper.selectCategory(parentId);
        category.setAncestors(parent == null || parent.getAncestors() == null ? "0" : parent.getAncestors() + "," + parent.getCategoryId());
        return mapper.insertCategory(category);
    }

    @Override
    @Transactional
    public int updateCategory(VegetableCategory category)
    {
        VegetableCategory old = mapper.selectCategory(category.getCategoryId());
        if (old == null) return 0;
        Long parentId = category.getParentId() == null ? 0L : category.getParentId();
        category.setParentId(parentId);
        VegetableCategory parent = parentId == 0L ? null : mapper.selectCategory(parentId);
        String newAncestors = parent == null || parent.getAncestors() == null ? "0" : parent.getAncestors() + "," + parent.getCategoryId();
        category.setAncestors(newAncestors);
        int result = mapper.updateCategory(category);
        if (result > 0 && old.getAncestors() != null && !old.getAncestors().equals(newAncestors))
        {
            mapper.updateChildrenAncestors(old.getAncestors() + "," + old.getCategoryId(), newAncestors + "," + category.getCategoryId(), category.getCategoryId());
        }
        return result;
    }

    @Override public int deleteCategory(Long id) { return mapper.deleteCategory(id); }
    @Override public int countChildren(Long id) { return mapper.countChildren(id); }
    @Override public int countRecords(Long id) { return mapper.countRecords(id); }
    @Override public VegetableRecord selectRecord(Long id) { return mapper.selectRecord(id); }
    @Override public int insertRecord(VegetableRecord record) { return mapper.insertRecord(record); }
    @Override public int updateRecord(VegetableRecord record) { return mapper.updateRecord(record); }
    @Override public int deleteRecord(Long id) { return mapper.deleteRecord(id); }

    @Override
    public List<VegetableRecord> selectRecords(Long categoryId, Long userId)
    {
        List<VegetableCategory> all = mapper.selectCategories();
        List<Long> ids = new ArrayList<>();
        if (categoryId == null) ids = all.stream().map(VegetableCategory::getCategoryId).collect(Collectors.toList());
        else { ids.add(categoryId); collectChildren(all, categoryId, ids); }
        return ids.isEmpty() ? new ArrayList<>() : mapper.selectRecords(ids, userId);
    }

    private void collectChildren(List<VegetableCategory> all, Long parentId, List<Long> ids)
    {
        for (VegetableCategory item : all)
        {
            if (parentId.equals(item.getParentId()))
            {
                ids.add(item.getCategoryId());
                collectChildren(all, item.getCategoryId(), ids);
            }
        }
    }
}
