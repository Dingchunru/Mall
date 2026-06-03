package com.mall.product.controller;

import com.mall.common.response.Result;
import com.mall.product.dto.ProductDTO;
import com.mall.product.dto.ProductQueryDTO;
import com.mall.product.entity.Product;
import com.mall.product.service.ProductService;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 创建商品
     */
    @PostMapping
    public Result<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = productService.createProduct(productDTO);
        return Result.success(product);
    }

    /**
     * 更新商品
     */
    @PutMapping
    public Result<Product> updateProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = productService.updateProduct(productDTO);
        return Result.success(product);
    }

    /**
     * 批量更新商品状�?
     */
    @PutMapping("/status")
    public Result<Void> batchUpdateStatus(@RequestParam List<Long> ids, 
                                          @RequestParam Integer status) {
        productService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    /**
     * 搜索商品
     */
    @PostMapping("/search")
    public Result<Page<Product>> searchProducts(@RequestBody ProductQueryDTO queryDTO) {
        return Result.success(productService.searchProducts(queryDTO));
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    /**
     * 获取最新商�?
     */
    @GetMapping("/new")
    public Result<List<Product>> getNewProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getNewProducts(limit));
    }

    /**
     * 减少库存
     */
    @PostMapping("/stock/reduce")
    public Result<Void> reduceStock(@RequestParam Long productId, 
                                    @RequestParam Integer quantity) {
        productService.reduceStock(productId, quantity);
        return Result.success();
    }

    /**
     * 上架商品
     */
    @PutMapping("/{id}/onSale")
    public Result<Void> onSale(@PathVariable Long id) {
        productService.onSale(id);
        return Result.success();
    }

    /**
     * 下架商品
     */
    @PutMapping("/{id}/offSale")
    public Result<Void> offSale(@PathVariable Long id) {
        productService.offSale(id);
        return Result.success();
    }

    @GetMapping("/ids/all")
    public Result<List<Long>> getAllProductIds() {
        return Result.success(productService.getAllProductIds());
    }
}