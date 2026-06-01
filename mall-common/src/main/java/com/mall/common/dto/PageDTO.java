package com.mall.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页 */
    private Long pageNum;

    /** 每页数量 */
    private Long pageSize;

    /** 总条数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 数据列表 */
    private List<T> records;

    /**
     * 快速构建分页对象
     */
    public static <T> PageDTO<T> of(Long pageNum, Long pageSize, Long total, List<T> records) {
        long pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        return new PageDTO<>(pageNum, pageSize, total, pages, records);
    }
}