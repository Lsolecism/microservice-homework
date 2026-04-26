package com.yiyu.order.controller;

import com.yiyu.order.service.OrderDomainService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderDomainService orderDomainService;

    public OrderController(OrderDomainService orderDomainService) {
        this.orderDomainService = orderDomainService;
    }

    @PostMapping("/seata/order/create")
    public String createOrder(@RequestParam("userId") Long userId,
                              @RequestParam("productId") Long productId,
                              @RequestParam("count") Integer count,
                              @RequestParam(value = "simulateFail", defaultValue = "false") boolean simulateFail) {
        return orderDomainService.createOrder(userId, productId, count, simulateFail);
    }
}
