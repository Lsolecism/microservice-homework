package com.yiyu.consumer.client;

import com.yiyu.consumer.fallback.ProviderClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "provider-service", fallback = ProviderClientFallback.class)
public interface ProviderClient {

    @GetMapping("/provider/hello")
    String hello(@RequestParam("name") String name,
                 @RequestParam("sleepMs") int sleepMs);
}
