package com.mall.order.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String postalCode;
    private Integer isDefault;
    
    // 添加 getFullAddress 方法
    public String getFullAddress() {
        return (province != null ? province : "") +
               (city != null ? city : "") +
               (district != null ? district : "") +
               (detailAddress != null ? detailAddress : "");
    }
}