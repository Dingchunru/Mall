// CreateOrderDTO.java
package com.mall.order.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class CreateOrderDTO {
    
    private Long userId;
    
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    
    @Size(min = 1, message = "至少选择一个商品")
    private List<OrderItemDTO> items;
    
    private String remark;
    
    private Integer paymentMethod;
    
    private Long couponId; // 使用的优惠券ID
    
    @Data
    public static class OrderItemDTO {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;
        
        private String skuId; // 商品规格ID
    }
}

