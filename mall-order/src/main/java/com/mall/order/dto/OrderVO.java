package com.mall.order.dto;

import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import lombok.Data;
import java.util.List;

@Data
public class OrderVO {
    private Order order;
    private List<OrderItem> items;
    private AddressDTO address;
    private List<ProductDTO> products;
}