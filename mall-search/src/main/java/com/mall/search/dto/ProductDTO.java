package com.mall.search.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {
    
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
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}