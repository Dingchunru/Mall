package com.mall.search.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SearchDTO {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 品牌ID
     */
    private Long brandId;
    
    /**
     * 价格区间-最低价
     */
    private BigDecimal minPrice;
    
    /**
     * 价格区间-最高价
     */
    private BigDecimal maxPrice;
    
    /**
     * 排序字段：price-价格，sales-销量，rating-评分，createTime-上架时间
     */
    private String sortBy;
    
    /**
     * 排序方式：asc-升序，desc-降序
     */
    private String sortOrder;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 当前页
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
}