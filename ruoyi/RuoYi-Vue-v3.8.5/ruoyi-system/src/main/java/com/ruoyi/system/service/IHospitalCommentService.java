package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.HospitalComment;

/**
 * 移动端病害问答评论服务。
 */
public interface IHospitalCommentService
{
    List<HospitalComment> selectCommentsByPostId(Long postId);

    HospitalComment selectCommentById(Long commentId);

    int insertComment(HospitalComment comment);

    int deleteCommentById(Long commentId);
}
