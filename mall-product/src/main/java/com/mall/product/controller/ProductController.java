package com.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.response.Result;
import com.mall.product.dto.ProductDTO;
import com.mall.product.dto.ProductStockDTO;
import com.mall.product.dto.ProductQueryDTO;
import com.mall.product.entity.Product;
import com.mall.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        log.info("查询商品: page={}, size={}, keyword={}", page, size, keyword);

        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setKeyword(keyword);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setMinPrice(minPrice);
        queryDTO.setMaxPrice(maxPrice);

        Page<Product> result = productService.searchProducts(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable @NotNull Long id) {
        log.info("查询商品详情: id={}", id);
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @PostMapping
    public Result<Product> create(@Valid @RequestBody ProductDTO productDTO) {
        log.info("创建商品: {}", productDTO.getName());
        Product product = productService.createProduct(productDTO);
        return Result.success("创建成功", product);
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable @NotNull Long id,
                                  @Valid @RequestBody ProductDTO productDTO) {
        log.info("更新商品: id={}", id);
        productDTO.setId(id);
        Product product = productService.updateProduct(productDTO);
        return Result.success(product);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        log.info("删除商品: id={}", id);
        productService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    public Result<Void> reduceStock(@PathVariable @NotNull Long id,
                                    @RequestParam @Min(1) Integer quantity) {
        log.info("扣减库存: id={}, quantity={}", id, quantity);
        productService.reduceStock(id, quantity);
        return Result.success();
    }

    @PutMapping("/stock/deduct")
    public Result<Boolean> deductStock(@RequestBody List<ProductStockDTO> stockDTOList) {
        productService.deductStock(stockDTOList);
        return Result.success(true);
    }

    @PutMapping("/stock/restore")
    public Result<Boolean> restoreStock(@RequestBody List<ProductStockDTO> stockDTOList) {
        productService.restoreStock(stockDTOList);
        return Result.success(true);
    }

    @GetMapping("/ids/all")
    public Result<List<Long>> getAllProductIds() {
        return Result.success(productService.getAllProductIds());
    }

    @GetMapping("/list/batch")
    public Result<List<Product>> getProductBatch(@RequestParam List<Long> ids) {
        return Result.success(productService.listByIds(ids));
    }
}
