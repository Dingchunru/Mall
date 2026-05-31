package com.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.product.dto.ProductDTO;
import com.mall.product.dto.ProductQueryDTO;
import com.mall.product.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {
    
    /**
     * 创建商品
     */
    Product createProduct(ProductDTO productDTO);
    
    /**
     * 更新商品
     */
    Product updateProduct(ProductDTO productDTO);
    
    /**
     * 批量更新商品状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);
    
    /**
     * 搜索商品
     */
    Page<Product> searchProducts(ProductQueryDTO queryDTO);
    
    /**
     * 获取商品详情
     */
    Product getProductDetail(Long id);
    
    /**
     * 获取热门商品
     */
    List<Product> getHotProducts(Integer limit);
    
    /**
     * 获取最新商品
     */
    List<Product> getNewProducts(Integer limit);
    
    /**
     * 减少库存
     */
    boolean reduceStock(Long productId, Integer quantity);
    
    /**
     * 刷新商品缓存
     */
    void refreshProductCache(Long productId);
    
    /**
     * 从缓存获取商品
     */
    Product getProductFromCache(Long productId);
    
    /**
     * 上架商品
     */
    boolean onSale(Long productId);
    
    /**
     * 下架商品
     */
    boolean offSale(Long productId);
}