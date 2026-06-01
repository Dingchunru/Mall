package com.mall.search.controller;

import com.mall.common.response.Result;
import com.mall.search.dto.SearchDTO;
import com.mall.search.dto.SearchResult;
import com.mall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/products")
    public Result<SearchResult> searchProducts(@Valid @RequestBody SearchDTO searchDTO) {
        log.info("搜索商品请求: {}", searchDTO);
        SearchResult result = searchService.searchProducts(searchDTO);
        return Result.success(result);
    }

    @GetMapping("/suggest")
    public Result<List<String>> getSuggestions(@RequestParam String keyword) {
        log.info("获取搜索建议: keyword={}", keyword);
        List<String> suggestions = searchService.getSuggestions(keyword);
        return Result.success(suggestions);
    }

    @PostMapping("/sync/{productId}")
    public Result<Void> syncProduct(@PathVariable Long productId) {
        log.info("手动同步商品: productId={}", productId);
        searchService.syncProduct(productId);
        return Result.success();
    }

    @PostMapping("/sync/all")
    public Result<Void> syncAllProducts() {
        log.info("手动批量同步所有商品");
        searchService.syncAllProducts();
        return Result.success();
    }

    @DeleteMapping("/product/{productId}")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        log.info("删除ES商品: productId={}", productId);
        searchService.deleteProduct(productId);
        return Result.success();
    }
}