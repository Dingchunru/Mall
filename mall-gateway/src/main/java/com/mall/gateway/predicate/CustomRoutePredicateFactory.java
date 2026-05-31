package com.mall.gateway.predicate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
public class CustomRoutePredicateFactory extends 
        AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {

    public CustomRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            LocalTime now = LocalTime.now();
            
            boolean timeValid = now.isAfter(config.getStartTime()) && 
                               now.isBefore(config.getEndTime());
            
            String version = exchange.getRequest().getHeaders().getFirst("API-Version");
            boolean versionValid = config.getVersion().equals(version);
            
            boolean result = timeValid && versionValid;
            
            log.info("Route predicate: timeValid={}, versionValid={}, result={}", 
                timeValid, versionValid, result);
            
            return result;
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("startTime", "endTime", "version");
    }

    public static class Config {
        private LocalTime startTime;
        private LocalTime endTime;
        private String version;
        
        public LocalTime getStartTime() {
            return startTime;
        }
        
        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }
        
        public LocalTime getEndTime() {
            return endTime;
        }
        
        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
    }
}