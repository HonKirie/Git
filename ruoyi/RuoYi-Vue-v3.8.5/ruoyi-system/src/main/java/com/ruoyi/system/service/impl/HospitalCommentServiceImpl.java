package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.HospitalComment;
import com.ruoyi.system.mapper.HospitalCommentMapper;
import com.ruoyi.system.service.IHospitalCommentService;

@Service
public class HospitalCommentServiceImpl implements IHospitalCommentService
{
    @Autowired
    private HospitalCommentMapper commentMapper;

    @Override
    public List<HospitalComment> selectCommentsByPostId(Long postId)
    {
        return commentMapper.selectCommentsByPostId(postId);
    }

    @Override
    public HospitalComment selectCommentById(Long commentId)
    {
        return commentMapper.selectCommentById(commentId);
    }

    @Override
    public int insertComment(HospitalComment comment)
    {
        return commentMapper.insertComment(comment);
    }

    @Override
    public int deleteCommentById(Long commentId)
    {
        return commentMapper.deleteCommentById(commentId);
    }
}
