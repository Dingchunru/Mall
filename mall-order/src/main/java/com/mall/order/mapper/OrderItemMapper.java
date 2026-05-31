// OrderItemMapper.java
package com.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.order.entity.OrderItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);
    
    List<OrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
}