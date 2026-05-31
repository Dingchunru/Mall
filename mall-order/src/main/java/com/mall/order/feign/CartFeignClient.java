// CartFeignClient.java
package com.mall.order.feign;

import com.mall.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "mall-cart", path = "/cart")
public interface CartFeignClient {
    
    @DeleteMapping("/items")
    Result<Boolean> removeItems(@RequestParam("userId") Long userId,
                                @RequestBody List<Long> productIds);
    
    @GetMapping("/count/{userId}")
    Result<Integer> getCartCount(@PathVariable("userId") Long userId);
}