package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductDTO;
import com.mall.product.dto.ProductQueryDTO;
import com.mall.product.dto.ProductStockDTO;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.CategoryService;
import com.mall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryService categoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(ProductDTO productDTO) {
        // 检查分类是否存在
        if (productDTO.getCategoryId() != null) {
            if (!categoryService.checkCategoryExists(productDTO.getCategoryId())) {
                throw new BusinessException(400, "分类不存在");
            }
        }
        
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        
        // 设置默认值
        product.setSales(0);
        product.setStatus(1);
        
        this.save(product);
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product updateProduct(ProductDTO productDTO) {
        if (productDTO.getId() == null) {
            throw new BusinessException(400, "商品ID不能为空");
        }
        
        Product existProduct = this.getById(productDTO.getId());
        if (existProduct == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 检查分类是否存在
        if (productDTO.getCategoryId() != null) {
            if (!categoryService.checkCategoryExists(productDTO.getCategoryId())) {
                throw new BusinessException(400, "分类不存在");
            }
        }
        
        BeanUtils.copyProperties(productDTO, existProduct);
        this.updateById(existProduct);
        return existProduct;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "商品ID列表不能为空");
        }
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, ids);
        
        Product product = new Product();
        product.setStatus(status);
        
        return this.update(product, wrapper);
    }

    @Override
    public Page<Product> searchProducts(ProductQueryDTO queryDTO) {
        Page<Product> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(Product::getName, queryDTO.getKeyword())
                   .or()
                   .like(Product::getDescription, queryDTO.getKeyword());
        }
        
        // 分类过滤
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 价格范围过滤
        if (queryDTO.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, queryDTO.getMinPrice());
        }
        if (queryDTO.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, queryDTO.getMaxPrice());
        }
        
        // 只查询上架商品
        wrapper.eq(Product::getStatus, 1);
        
        // 排序
        if (StringUtils.hasText(queryDTO.getSortField())) {
            if ("asc".equalsIgnoreCase(queryDTO.getSortOrder())) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        
        return this.page(page, wrapper);
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getSales)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public List<Product> getNewProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getCreateTime)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reduceStock(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        if (product.getStock() < quantity) {
            throw new BusinessException(400, "库存不足");
        }
        
        product.setStock(product.getStock() - quantity);
        return this.updateById(product);
    }

    @Override
    public void refreshProductCache(Long productId) {
        log.info("刷新商品缓存: {}", productId);
    }

    @Override
    public Product getProductFromCache(Long productId) {
        return this.getById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean onSale(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        product.setStatus(1);
        return this.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offSale(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        product.setStatus(0);
        return this.updateById(product);
    }

    // ProductServiceImpl 实现
    @Override
    public List<Long> getAllProductIds() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Product::getId).eq(Product::getStatus, 1);
        return this.list(wrapper).stream().map(Product::getId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(List<ProductStockDTO> stockDTOList) {
        for (ProductStockDTO dto : stockDTOList) {
            reduceStock(dto.getProductId(), dto.getQuantity());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(List<ProductStockDTO> stockDTOList) {
        for (ProductStockDTO dto : stockDTOList) {
            Product product = this.getById(dto.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + dto.getQuantity());
                this.updateById(product);
            }
        }
    }
}