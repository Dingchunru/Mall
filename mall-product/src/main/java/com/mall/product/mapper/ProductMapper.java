package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据分类ID查询商品数量
     */
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId} AND deleted = 0")
    int countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 批量更新商品状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
    
    /**
     * 扣减库存
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 增加销量
     */
    @Update("UPDATE product SET sales = sales + #{quantity} WHERE id = #{productId}")
    int increaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 复杂查询（需要在XML中实现）
     */
    List<Product> selectByCondition(@Param("keyword") String keyword,
                                   @Param("categoryId") Long categoryId,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("status") Integer status);
}