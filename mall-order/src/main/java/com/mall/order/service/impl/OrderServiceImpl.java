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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
        if (!addressResult.isSuccess() || addressResult.getData() == null) {
            throw new BusinessException(400, "地址信息获取失败");
        }
        AddressDTO address = addressResult.getData();

        // 2. 计算订单总金额并创建订单项（先算金额，不扣库存）
        Order order = new Order();
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(orderDTO.getUserId());
        order.setStatus(0);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getFullAddress());
        order.setReceiverPostCode(address.getPostalCode());
        order.setRemark(orderDTO.getRemark());
        order.setFreightAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ProductStockDTO> stockDTOList = new ArrayList<>();

        for (CreateOrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            Result<ProductDTO> productResult = productFeignClient.getProduct(itemDTO.getProductId());
            if (!productResult.isSuccess() || productResult.getData() == null) {
                throw new BusinessException(400, "商品信息获取失败");
            }
            ProductDTO product = productResult.getData();

            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

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

            ProductStockDTO stockDTO = new ProductStockDTO();
            stockDTO.setProductId(itemDTO.getProductId());
            stockDTO.setQuantity(itemDTO.getQuantity());
            stockDTOList.add(stockDTO);
        }

        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount.add(order.getFreightAmount()));

        // 3. 先保存订单和订单项
        this.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        // 4. 再扣减库存（订单已落库，失败抛异常回滚订单）
        Result<Boolean> stockResult = productFeignClient.deductStock(stockDTOList);
        if (!stockResult.isSuccess() || !Boolean.TRUE.equals(stockResult.getData())) {
            throw new BusinessException(400, "库存不足");
        }

        // 5. 发送延迟消息，16 = 30分钟后自动取消
        orderDelayProducer.sendDelayCancelOrder(orderNo, 16);

        // 6. 构建返回结果
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

        // 批量查订单项，避免 N+1
        List<String> orderNos = orderPage.getRecords().stream()
                .map(Order::getOrderNo)
                .collect(Collectors.toList());

        List<OrderItem> allItems = Collections.emptyList();
        Map<String, List<OrderItem>> itemsMap = Collections.emptyMap();
        if (!orderNos.isEmpty()) {
            allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderNo, orderNos));
            itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderNo));
        }

        Map<String, List<OrderItem>> finalItemsMap = itemsMap;
        List<OrderVO> orderVOS = orderPage.getRecords().stream()
                .map(order -> {
                    OrderVO vo = new OrderVO();
                    vo.setOrder(order);
                    vo.setItems(finalItemsMap.getOrDefault(order.getOrderNo(), Collections.emptyList()));
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

        if (order.getStatus() != 0) {
            throw new BusinessException(400, "当前订单状态不可取消");
        }

        // 先恢复库存，再取消订单
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

        Result<Boolean> restoreResult = productFeignClient.restoreStock(stockDTOList);
        if (!restoreResult.isSuccess()) {
            log.error("恢复库存失败: orderNo={}", orderNo);
        }

        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(cancelReason);
        order.setUpdateTime(LocalDateTime.now());

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

        order.setStatus(1);
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

        if (order.getStatus() != 2) {
            throw new BusinessException(400, "订单状态异常");
        }

        order.setStatus(3);
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

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    private OrderVO buildOrderVO(Order order, List<OrderItem> items, AddressDTO address) {
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(items);
        vo.setAddress(address);
        return vo;
    }
}