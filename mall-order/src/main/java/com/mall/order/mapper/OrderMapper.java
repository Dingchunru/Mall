// OrderMapper.java
package com.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.order.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;

public interface OrderMapper extends BaseMapper<Order> {
    
    @Update("UPDATE `order` SET status = #{status}, pay_time = NOW() " +
            "WHERE order_no = #{orderNo} AND status = 0")
    int updateStatusToPaid(@Param("orderNo") String orderNo, @Param("status") Integer status);
    
    @Update("UPDATE `order` SET status = 4, cancel_time = NOW(), " +
            "cancel_reason = #{reason} WHERE order_no = #{orderNo} AND status = 0")
    int cancelOrder(@Param("orderNo") String orderNo, @Param("reason") String reason);
}

