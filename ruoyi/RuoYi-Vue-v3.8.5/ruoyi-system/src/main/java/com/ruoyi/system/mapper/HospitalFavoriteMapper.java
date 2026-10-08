package com.ruoyi.system.mapper;
import java.util.List; import org.apache.ibatis.annotations.Param; import com.ruoyi.system.domain.HospitalFavorite;
public interface HospitalFavoriteMapper { List<HospitalFavorite> selectByAuthorAccount(@Param("account") String account); HospitalFavorite selectByUserAndPost(@Param("userId") Long userId,@Param("postId") Long postId); int insert(HospitalFavorite favorite); }
