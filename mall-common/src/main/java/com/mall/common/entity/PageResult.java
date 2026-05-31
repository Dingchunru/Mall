package com.mall.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long total;
    private Long page;
    private Long size;
    private List<T> records;
    
    public PageResult() {
        this.total = 0L;
        this.records = Collections.emptyList();
    }
    
    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
    
    public PageResult(Long total, Long page, Long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }
}