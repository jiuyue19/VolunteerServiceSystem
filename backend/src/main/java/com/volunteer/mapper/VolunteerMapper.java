package com.volunteer.mapper;

import com.volunteer.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface VolunteerMapper {
    Volunteer selectByUsername(@Param("username") String username);
    Volunteer selectById(@Param("id") Long id);
    List<Volunteer> selectAll();
    List<Volunteer> selectByIds(@Param("ids") List<Long> ids);
    int insert(Volunteer volunteer);
    int update(Volunteer volunteer);
    int resetStats(@Param("id") Long id);
    int deleteById(@Param("id") Long id);
    Volunteer selectByPhone(@Param("phone") String phone);
    Volunteer selectByEmail(@Param("email") String email);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    // 区块链统计相关
    int countVolunteersWithWallet();  // 统计已绑定钱包的志愿者数量
    BigDecimal sumTotalHours();  // 统计所有志愿者的总时长
    List<Volunteer> selectVolunteersWithWallet();  // 查询所有已绑定钱包的志愿者
    Volunteer selectByWalletAddress(@Param("walletAddress") String walletAddress);  // 根据钱包地址查询志愿者
    
    // 积分同步相关
    int syncPoints(@Param("id") Long id, @Param("points") Integer points);  // 同步积分（直接设置，不是累加）
    int syncHours(@Param("id") Long id, @Param("hours") BigDecimal hours);  // 同步时长（直接设置，不是累加）
    List<Long> selectAllVolunteerIds();  // 获取所有志愿者ID
    
    // 统计方法
    Integer getTotalPoints();  // 获取所有志愿者的总积分
    Double getTotalHours();  // 获取所有志愿者的累计志愿时长总和
    List<Volunteer> getTopByPoints(@Param("limit") Integer limit);  // 获取积分排行榜
}