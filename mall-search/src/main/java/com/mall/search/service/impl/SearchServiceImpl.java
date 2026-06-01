package com.mall.search.service.impl;

import com.mall.common.exception.BusinessException;
import com.mall.search.client.ProductServiceClient;
import com.mall.search.document.ProductDocument;
import com.mall.search.dto.*;
import com.mall.search.repository.ProductSearchRepository;
import com.mall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ProductSearchRepository productSearchRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public SearchResult searchProducts(SearchDTO searchDTO) {
        log.info("搜索商品: {}", searchDTO);

        Query searchQuery = buildSearchQuery(searchDTO);

        SearchHits<ProductDocument> searchHits = elasticsearchTemplate.search(searchQuery, ProductDocument.class);

        return convertToSearchResult(searchHits, searchDTO);
    }

    @Override
    public List<String> getSuggestions(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        log.info("获取搜索建议: {}", keyword);
        return new ArrayList<>();
    }

    @Override
    public void syncProduct(Long productId) {
        log.info("同步商品到ES: productId={}", productId);
        try {
            var response = productServiceClient.getProductDetail(productId);
            if (response.getCode() == 200 && response.getData() != null) {
                ProductDTO productDTO = response.getData();
                ProductDocument document = convertToDocument(productDTO);
                productSearchRepository.save(document);
                log.info("商品同步成功: productId={}", productId);
            }
        } catch (Exception e) {
            log.error("同步商品异常: productId={}", productId, e);
        }
    }

    @Override
    public void syncAllProducts() {
        log.info("开始批量同步所有商品");
        try {
            var response = productServiceClient.getAllProductIds();
            if (response.getCode() == 200 && response.getData() != null) {
                List<Long> productIds = response.getData();
                for (Long productId : productIds) {
                    try {
                        syncProduct(productId);
                    } catch (Exception e) {
                        log.error("同步商品失败: productId={}", productId, e);
                    }
                }
                log.info("批量同步完成，共处理{}个商品", productIds.size());
            }
        } catch (Exception e) {
            log.error("批量同步异常", e);
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        log.info("从ES删除商品: productId={}", productId);
        productSearchRepository.deleteById(productId);
    }

    private Query buildSearchQuery(SearchDTO searchDTO) {
        Criteria criteria = new Criteria();
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            criteria = criteria.and("name").contains(searchDTO.getKeyword())
                    .or("description").contains(searchDTO.getKeyword());
        }
        if (searchDTO.getCategoryId() != null) {
            criteria = criteria.and("categoryId").is(searchDTO.getCategoryId());
        }

        Query query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize()));
        return query;
    }

    private SearchResult convertToSearchResult(SearchHits<ProductDocument> searchHits, SearchDTO searchDTO) {
        SearchResult result = new SearchResult();

        List<ProductDTO> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        result.setProducts(products);
        result.setTotal(searchHits.getTotalHits());
        result.setPage(searchDTO.getPage());
        result.setSize(searchDTO.getSize());
        result.setTotalPages((int) Math.ceil((double) searchHits.getTotalHits() / searchDTO.getSize()));
        result.setAggregations(new HashMap<>());

        return result;
    }

    private ProductDocument convertToDocument(ProductDTO productDTO) {
        if (productDTO == null) return null;
        ProductDocument document = new ProductDocument();
        document.setId(productDTO.getId());
        document.setName(productDTO.getName());
        document.setDescription(productDTO.getDescription());
        document.setImage(productDTO.getImage());
        document.setPrice(productDTO.getPrice());
        document.setStock(productDTO.getStock());
        document.setCategoryId(productDTO.getCategoryId());
        document.setCategoryName(productDTO.getCategoryName());
        document.setStatus(productDTO.getStatus());
        document.setSales(productDTO.getSales());
        document.setRating(productDTO.getRating());
        document.setReviewCount(productDTO.getReviewCount());
        document.setTags(productDTO.getTags());
        document.setCreateTime(productDTO.getCreateTime());
        document.setUpdateTime(productDTO.getUpdateTime());
        return document;
    }

    private ProductDTO convertToDTO(ProductDocument document) {
        if (document == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setDescription(document.getDescription());
        dto.setImage(document.getImage());
        dto.setPrice(document.getPrice());
        dto.setStock(document.getStock());
        dto.setCategoryId(document.getCategoryId());
        dto.setCategoryName(document.getCategoryName());
        dto.setStatus(document.getStatus());
        dto.setSales(document.getSales());
        dto.setRating(document.getRating());
        dto.setReviewCount(document.getReviewCount());
        dto.setTags(document.getTags());
        dto.setCreateTime(document.getCreateTime());
        dto.setUpdateTime(document.getUpdateTime());
        return dto;
    }
}