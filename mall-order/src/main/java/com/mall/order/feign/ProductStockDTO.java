package com.mall.order.feign;

import lombok.Data;

@Data
public class ProductStockDTO {
    private Long productId;
    private Integer quantity;
}