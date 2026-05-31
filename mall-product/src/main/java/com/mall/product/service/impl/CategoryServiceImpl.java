package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.product.entity.Category;
import com.mall.product.mapper.CategoryMapper;
import com.mall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getCategoryTree() {
        // 获取所有分类
        List<Category> allCategories = this.list();
        
        // 获取顶级分类（parentId为0）
        List<Category> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == 0)
                .collect(Collectors.toList());
        
        // 为每个顶级分类设置子分类
        rootCategories.forEach(category -> 
            category.setChildren(getChildren(category.getId(), allCategories))
        );
        
        return rootCategories;
    }

    @Override
    public List<Category> getChildren(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId)
               .orderByAsc(Category::getSort);
        return this.list(wrapper);
    }

    @Override
    public boolean checkCategoryExists(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        return this.getById(categoryId) != null;
    }

    /**
     * 递归获取子分类
     */
    private List<Category> getChildren(Long parentId, List<Category> allCategories) {
        return allCategories.stream()
                .filter(category -> parentId.equals(category.getParentId()))
                .peek(category -> category.setChildren(getChildren(category.getId(), allCategories)))
                .collect(Collectors.toList());
    }
}