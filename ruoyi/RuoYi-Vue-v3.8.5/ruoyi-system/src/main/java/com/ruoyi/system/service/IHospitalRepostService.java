package com.ruoyi.system.service;
import java.util.List; import com.ruoyi.system.domain.HospitalRepost;
public interface IHospitalRepostService { List<HospitalRepost> selectByAuthorAccount(String account); HospitalRepost selectByUserAndPost(Long userId,Long postId); int insert(HospitalRepost repost); }
