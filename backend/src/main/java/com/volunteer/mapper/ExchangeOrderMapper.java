package com.volunteer.mapper;

import com.volunteer.entity.ExchangeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExchangeOrderMapper {
    List<ExchangeOrder> selectAll();
    ExchangeOrder selectById(@Param("id") Long id);
    List<ExchangeOrder> selectByVolunteerId(@Param("volunteerId") Long volunteerId);
    void insert(ExchangeOrder order);
    void update(ExchangeOrder order);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    void deleteById(Long id);
    void deleteByGoodsId(Long goodsId);
    
    // 统计方法
    // 获取志愿者兑换成功的订单数量
    Integer countSuccessfulOrdersByVolunteer(@Param("volunteerId") Long volunteerId);
    
    // 获取所有志愿者兑换成功的订单总数
    Integer countTotalSuccessfulOrders();
}