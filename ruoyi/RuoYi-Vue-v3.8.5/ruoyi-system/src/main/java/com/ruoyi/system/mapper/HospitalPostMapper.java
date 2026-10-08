package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.HospitalPost;

public interface HospitalPostMapper
{
    List<HospitalPost> selectPostList();
    HospitalPost selectPostById(Long postId);
    int insertPost(HospitalPost post);
    int deletePostById(Long postId);
}
