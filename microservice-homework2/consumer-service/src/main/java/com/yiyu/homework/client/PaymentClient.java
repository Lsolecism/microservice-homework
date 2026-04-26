package com.yiyu.homework.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "payment-service", contextId = "paymentClient", path = "/api/payment")
public interface PaymentClient {

    @GetMapping("/info")
    String info();
}
