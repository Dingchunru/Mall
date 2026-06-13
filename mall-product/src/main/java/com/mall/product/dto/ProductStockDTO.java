package com.mall.product.dto;

import lombok.Data;

@Data
public class ProductStockDTO {
    private Long productId;
    private Integer quantity;
}