package com.mall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationInfo {
    
    /**
     * 聚合项名称
     */
    private String name;
    
    /**
     * 聚合项值
     */
    private String value;
    
    /**
     * 文档数量
     */
    private Long count;
}