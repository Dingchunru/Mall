package com.mall.search.client;

import com.mall.common.response.Result;
import com.mall.search.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mall-product", path = "/product")
public interface ProductServiceClient {
    
    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    Result<ProductDTO> getProductDetail(@PathVariable("id") Long id);
    
    /**
     * 批量获取商品信息
     */
    @GetMapping("/list/batch")
    Result<List<ProductDTO>> getProductBatch(@RequestParam("ids") List<Long> ids);
    
    /**
     * 获取所有商品ID列表
     */
    @GetMapping("/ids/all")
    Result<List<Long>> getAllProductIds();
}