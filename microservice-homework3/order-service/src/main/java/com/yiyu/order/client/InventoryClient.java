package com.yiyu.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/seata/inventory/deduct")
    String deduct(@RequestParam("productId") Long productId,
                  @RequestParam("count") Integer count);
}
