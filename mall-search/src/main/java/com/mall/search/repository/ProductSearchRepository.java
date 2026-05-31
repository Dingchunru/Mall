package com.mall.search.repository;

import com.mall.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    /**
     * 根据分类ID查询商品数量
     */
    long countByCategoryId(Long categoryId);
}