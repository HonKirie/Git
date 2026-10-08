package com.ruoyi.system.mapper;
import java.util.List; import org.apache.ibatis.annotations.Param; import com.ruoyi.system.domain.HospitalRepost;
public interface HospitalRepostMapper { List<HospitalRepost> selectByAuthorAccount(@Param("account") String account); HospitalRepost selectByUserAndPost(@Param("userId") Long userId,@Param("postId") Long postId); int insert(HospitalRepost repost); }
