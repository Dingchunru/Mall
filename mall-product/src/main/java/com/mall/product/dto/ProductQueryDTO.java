package com.mall.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductQueryDTO {
    
    private String keyword;
    
    private Long categoryId;
    
    private BigDecimal minPrice;
    
    private BigDecimal maxPrice;
    
    private Integer page = 1;
    
    private Integer size = 10;
    
    private String sortField;
    
    private String sortOrder;
}