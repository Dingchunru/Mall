package com.mall.order.feign;

import com.mall.common.response.Result;
import com.mall.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-product", path = "/product")
public interface ProductFeignClient {
    
    @GetMapping("/detail/{id}")
    Result<ProductDTO> getProduct(@PathVariable("id") Long id);
    
    @PostMapping("/stock/deduct")
    Result<Boolean> deductStock(@RequestBody List<ProductStockDTO> stocks);
    
    @PostMapping("/stock/restore")
    Result<Boolean> restoreStock(@RequestBody List<ProductStockDTO> stocks);
}