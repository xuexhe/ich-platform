// src/main/java/com/ich/service/OrderService.java
package com.ich.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ich.dto.OrderDTO;
import com.ich.dto.OrderItemDTO;
import com.ich.entity.Order;
import com.ich.entity.OrderItem;
import com.ich.entity.Product;
import com.ich.mapper.OrderItemMapper;
import com.ich.mapper.OrderMapper;
import com.ich.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        String orderNo = "ICH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(orderDTO.getUserId());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setAddress(orderDTO.getAddress());
        order.setPhone(orderDTO.getPhone());
        order.setReceiver(orderDTO.getReceiver());
        order.setStatus(0);  // 0:待支付
        order.setCreateTime(LocalDateTime.now());
        
        save(order);
        
        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setProductName(itemDTO.getProductName());
            item.setProductCover(itemDTO.getProductCover());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            item.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(item);
            
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product != null) {
                product.setStock(product.getStock() - itemDTO.getQuantity());
                productMapper.updateById(product);
            }
        }
        
        return order;
    }

    @Transactional
    public boolean payOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setStatus(1);  // 1:已支付
        order.setPayTime(LocalDateTime.now());
        return updateById(order);
    }

    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setStatus(2);  // 2:已取消
        updateById(order);
        
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
        return true;
    }
    
    @Transactional
    public boolean completeOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        order.setStatus(3);  // 3:已完成
        return updateById(order);
    }

    /**
     * 删除订单（只能删除已取消或已完成的订单）
     */
    @Transactional
    public boolean deleteOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            return false;
        }
        // 验证用户权限
        if (!order.getUserId().equals(userId)) {
            return false;
        }
        // 只能删除已取消(2)或已完成(3)的订单
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            return false;
        }
        return removeById(orderId);
    }

    public Page<Order> getUserOrders(Long userId, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
               .orderByDesc(Order::getCreateTime);
        return page(page, wrapper);
    }
    
    public Page<Order> getUserOrdersByStatus(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null && status != -1) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return page(page, wrapper);
    }

    public Map<String, Object> getOrderDetail(Long orderId) {
        Order order = getById(orderId);
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }
}