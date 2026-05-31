package com.mall.search.task;

import com.mall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ProductSyncTask {

    private final SearchService searchService;

    /**
     * 每小时同步一次商品数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncProducts() {
        log.info("开始定时同步商品数据");
        try {
            searchService.syncAllProducts();
            log.info("定时同步商品数据完成");
        } catch (Exception e) {
            log.error("定时同步商品数据失败", e);
        }
    }
}