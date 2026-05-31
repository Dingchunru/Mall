package com.mall.search.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResult {
    
    /**
     * 商品列表
     */
    private List<ProductDTO> products;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 聚合信息（分类聚合、品牌聚合、价格区间等）
     */
    private Map<String, List<AggregationInfo>> aggregations;
}