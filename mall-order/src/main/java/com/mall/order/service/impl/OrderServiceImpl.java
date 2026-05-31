package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BusinessException;
import com.mall.common.response.Result;
import com.mall.order.dto.*;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.feign.ProductFeignClient;
import com.mall.order.feign.ProductStockDTO;
import com.mall.order.feign.UserFeignClient;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.mq.OrderDelayProducer;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;
    private final OrderDelayProducer orderDelayProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO orderDTO) {
        log.info("创建订单: {}", orderDTO);
        
        // 1. 获取地址信息
        Result<AddressDTO> addressResult = userFeignClient.getAddress(orderDTO.getAddressId());
        if (addressResult.getCode() != 200 || addressResult.getData() == null) {
            throw new BusinessException(400, "地址信息获取失败");
        }
        AddressDTO address = addressResult.getData();
        
        // 2. 扣减库存
        List<ProductStockDTO> stockDTOList = orderDTO.getItems().stream()
                .map(item -> {
                    ProductStockDTO stockDTO = new ProductStockDTO();
                    stockDTO.setProductId(item.getProductId());
                    stockDTO.setQuantity(item.getQuantity());
                    return stockDTO;
                })
                .collect(Collectors.toList());
        
        Result<Boolean> stockResult = productFeignClient.deductStock(stockDTOList);
        if (stockResult.getCode() != 200 || !Boolean.TRUE.equals(stockResult.getData())) {
            throw new BusinessException(400, "库存不足");
        }
        
        // 3. 创建订单
        Order order = new Order();
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(orderDTO.getUserId());
        order.setStatus(0); // 待付款
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getFullAddress());
        order.setReceiverPostCode(address.getPostalCode());
        order.setRemark(orderDTO.getRemark());
        order.setFreightAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 4. 计算订单总金额并创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CreateOrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            // 获取商品信息
            Result<ProductDTO> productResult = productFeignClient.getProduct(itemDTO.getProductId());
            if (productResult.getCode() != 200 || productResult.getData() == null) {
                throw new BusinessException(400, "商品信息获取失败");
            }
            ProductDTO product = productResult.getData();
            
            // 计算小计
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setTotalAmount(itemTotal);
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            
            orderItems.add(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount.add(order.getFreightAmount()));
        
        // 5. 保存订单和订单项
        this.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }
        
        // 6. 发送延迟消息，30分钟后自动取消订单
        orderDelayProducer.sendDelayCancelOrder(orderNo, 30 * 60 * 1000);
        
        // 7. 构建返回结果
        return buildOrderVO(order, orderItems, address);
    }

    @Override
    public OrderVO getOrderDetail(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo)
                   .eq(Order::getUserId, userId);
        Order order = this.getOne(orderWrapper);

        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        // 修复这里：从订单中获取地址信息
        // 注意：这里需要根据实际情况调整，可能订单中保存了地址ID或完整地址
        AddressDTO address = new AddressDTO();
        address.setReceiverName(order.getReceiverName());
        address.setReceiverPhone(order.getReceiverPhone());
        address.setDetailAddress(order.getReceiverAddress());
        address.setPostalCode(order.getReceiverPostCode());

        return buildOrderVO(order, orderItems, address);
    }

    @Override
    public PageDTO<OrderVO> getUserOrders(Long userId, Integer page, Integer size, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> orderPage = this.page(pageParam, wrapper);
        
        List<OrderVO> orderVOS = orderPage.getRecords().stream()
                .map(order -> {
                    LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                    itemWrapper.eq(OrderItem::getOrderNo, order.getOrderNo());
                    List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
                    
                    OrderVO vo = new OrderVO();
                    vo.setOrder(order);
                    vo.setItems(items);
                    return vo;
                })
                .collect(Collectors.toList());
        
        PageDTO<OrderVO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(orderPage.getTotal());
        pageDTO.setPages((int) orderPage.getPages());
        pageDTO.setCurrent((int) orderPage.getCurrent());
        pageDTO.setSize((int) orderPage.getSize());
        pageDTO.setRecords(orderVOS);
        
        return pageDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long userId, String orderNo, String cancelReason) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 只有待付款的订单可以取消
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "当前订单状态不可取消");
        }
        
        order.setStatus(4); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(cancelReason);
        order.setUpdateTime(LocalDateTime.now());
        
        // 恢复库存
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);
        
        List<ProductStockDTO> stockDTOList = orderItems.stream()
                .map(item -> {
                    ProductStockDTO stockDTO = new ProductStockDTO();
                    stockDTO.setProductId(item.getProductId());
                    stockDTO.setQuantity(item.getQuantity());
                    return stockDTO;
                })
                .collect(Collectors.toList());
        
        productFeignClient.restoreStock(stockDTOList);
        
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long userId, String orderNo, Integer paymentMethod) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态异常");
        }
        
        order.setStatus(1); // 已付款
        order.setPaymentMethod(paymentMethod);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        if (order.getStatus() != 2) { // 必须是已发货状态
            throw new BusinessException(400, "订单状态异常");
        }
        
        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 只有已取消或已完成的订单可以删除
        if (order.getStatus() != 4 && order.getStatus() != 3) {
            throw new BusinessException(400, "当前订单状态不可删除");
        }
        
        return this.removeById(order.getId());
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        return this.getOne(wrapper);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 构建订单VO
     */
    private OrderVO buildOrderVO(Order order, List<OrderItem> items, AddressDTO address) {
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(items);
        vo.setAddress(address);
        
        List<ProductDTO> products = items.stream()
                .map(item -> {
                    Result<ProductDTO> result = productFeignClient.getProduct(item.getProductId());
                    return result.getData();
                })
                .collect(Collectors.toList());
        vo.setProducts(products);
        
        return vo;
    }
}