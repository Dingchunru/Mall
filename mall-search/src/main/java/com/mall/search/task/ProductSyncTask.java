package com.mall.search.task;

import com.mall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncTask {

    private final SearchService searchService;
    private volatile boolean syncing = false;

    /**
     * 每小时同步一次商品数据（防止重复执行）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncProducts() {
        if (syncing) {
            log.warn("上一次同步尚未完成，跳过本次");
            return;
        }
        syncing = true;
        log.info("开始定时同步商品数据");
        try {
            searchService.syncAllProducts();
            log.info("定时同步商品数据完成");
        } catch (Exception e) {
            log.error("定时同步商品数据失败", e);
        } finally {
            syncing = false;
        }
    }
}