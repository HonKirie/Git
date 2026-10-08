package com.ruoyi.system.service;
import java.util.List; import com.ruoyi.system.domain.HospitalFavorite;
public interface IHospitalFavoriteService { List<HospitalFavorite> selectByAuthorAccount(String account); HospitalFavorite selectByUserAndPost(Long userId,Long postId); int insert(HospitalFavorite favorite); }
