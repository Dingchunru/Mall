package com.mall.order.feign;

import com.mall.common.response.Result;
import com.mall.order.dto.AddressDTO;
import com.mall.order.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mall-user", path = "/user")
public interface UserFeignClient {
    
    @GetMapping("/address/{id}")
    Result<AddressDTO> getAddress(@PathVariable("id") Long id);
    
    @GetMapping("/info/{id}")
    Result<UserDTO> getUserInfo(@PathVariable("id") Long id);
}