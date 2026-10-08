package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.HospitalPost;

public interface IHospitalPostService
{
    List<HospitalPost> selectPostList();
    HospitalPost selectPostById(Long postId);
    int insertPost(HospitalPost post);
    int deletePostById(Long postId);
}
