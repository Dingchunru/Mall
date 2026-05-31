package com.mall.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageDTO<T> {
    private Long total;
    private Integer pages;
    private Integer current;
    private Integer size;
    private List<T> records;
}