package com.mall.product.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Integer sales;
    private LocalDateTime createTime;
}