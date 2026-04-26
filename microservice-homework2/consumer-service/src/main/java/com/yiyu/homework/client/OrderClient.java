package com.yiyu.homework.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "order-service", contextId = "orderClient", path = "/api/order")
public interface OrderClient {

    @GetMapping("/info")
    String info();
}
