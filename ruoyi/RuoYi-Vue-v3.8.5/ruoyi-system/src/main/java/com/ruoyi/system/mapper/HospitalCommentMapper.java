package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.HospitalComment;

/**
 * 移动端病害问答评论数据层。
 */
public interface HospitalCommentMapper
{
    List<HospitalComment> selectCommentsByPostId(Long postId);

    HospitalComment selectCommentById(Long commentId);

    int insertComment(HospitalComment comment);

    int deleteCommentById(Long commentId);
}
