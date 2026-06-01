package com.mall.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 * 配合 BaseEntity 中的 @TableField(fill = ...) 使用
 */
@Slf4j
@Component
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.handlers.MetaObjectHandler")
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始自动填充插入字段...");
        LocalDateTime now = LocalDateTime.now();

        // 创建时间（如果字段存在且为null才填充）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        // 逻辑删除默认值
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始自动填充更新字段...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}