package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.product.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    
    /**
     * 获取分类树
     */
    List<Category> getCategoryTree();
    
    /**
     * 获取子分类列表
     */
    List<Category> getChildren(Long parentId);
    
    /**
     * 检查分类是否存在
     */
    boolean checkCategoryExists(Long categoryId);
}