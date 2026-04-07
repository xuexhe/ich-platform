// src/main/java/com/ich/controller/OrderController.java
package com.ich.controller;

import com.ich.dto.OrderDTO;
import com.ich.entity.Order;
import com.ich.service.OrderService;
import com.ich.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 创建订单
    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody OrderDTO orderDTO) {
        Order order = orderService.createOrder(orderDTO);
        return order != null ? Result.success(order) : Result.error("创建订单失败");
    }

    // 模拟支付
    @PostMapping("/pay")
    public Result<?> payOrder(@RequestBody Map<String, Long> params) {
        Long orderId = params.get("orderId");
        boolean success = orderService.payOrder(orderId);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    // 取消订单
    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody Map<String, Long> params) {
        Long orderId = params.get("orderId");
        boolean success = orderService.cancelOrder(orderId);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    // 确认收货
    @PostMapping("/confirm")
    public Result<?> confirmOrder(@RequestBody Map<String, Long> params) {
        Long orderId = params.get("orderId");
        boolean success = orderService.completeOrder(orderId);
        return success ? Result.success("确认收货成功") : Result.error("操作失败");
    }

    // 获取用户订单列表（支持状态筛选）
    @GetMapping("/list/{userId}")
    public Result<?> getUserOrders(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer status) {
        if (status == null) {
            return Result.success(orderService.getUserOrders(userId, pageNum, pageSize));
        } else {
            return Result.success(orderService.getUserOrdersByStatus(userId, status, pageNum, pageSize));
        }
    }

    // 获取订单详情
    @GetMapping("/detail/{orderId}")
    public Result<?> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @DeleteMapping("/delete/{orderId}")
    public Result<?> deleteOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        boolean success = orderService.deleteOrder(orderId, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}