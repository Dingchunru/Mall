package com.mall.search.service;

import com.mall.search.dto.SearchDTO;
import com.mall.search.dto.SearchResult;

import java.util.List;

public interface SearchService {
    
    /**
     * 搜索商品
     */
    SearchResult searchProducts(SearchDTO searchDTO);
    
    /**
     * 获取搜索建议
     */
    List<String> getSuggestions(String keyword);
    
    /**
     * 同步商品到ES
     */
    void syncProduct(Long productId);
    
    /**
     * 批量同步商品
     */
    void syncAllProducts();
    
    /**
     * 从ES删除商品
     */
    void deleteProduct(Long productId);
}