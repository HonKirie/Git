package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.HospitalPost;
import com.ruoyi.system.mapper.HospitalPostMapper;
import com.ruoyi.system.service.IHospitalPostService;

@Service
public class HospitalPostServiceImpl implements IHospitalPostService
{
    @Autowired
    private HospitalPostMapper postMapper;

    @Override public List<HospitalPost> selectPostList() { return postMapper.selectPostList(); }
    @Override public HospitalPost selectPostById(Long postId) { return postMapper.selectPostById(postId); }
    @Override public int insertPost(HospitalPost post) { return postMapper.insertPost(post); }
    @Override public int deletePostById(Long postId) { return postMapper.deletePostById(postId); }
}
