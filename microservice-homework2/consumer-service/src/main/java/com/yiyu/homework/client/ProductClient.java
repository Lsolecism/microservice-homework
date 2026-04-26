package com.yiyu.homework.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service", contextId = "productClient", path = "/api/product")
public interface ProductClient {

    @GetMapping("/info")
    String info();
}
