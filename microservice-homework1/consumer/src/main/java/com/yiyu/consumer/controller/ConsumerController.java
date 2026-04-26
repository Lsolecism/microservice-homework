package com.yiyu.consumer.controller;

import com.yiyu.consumer.client.ProviderClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerController {

    private final ProviderClient providerClient;

    public ConsumerController(ProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    @GetMapping("/consumer/hello")
    public String hello(@RequestParam(defaultValue = "student") String name,
                        @RequestParam(defaultValue = "0") int sleepMs) {
        return providerClient.hello(name, sleepMs);
    }
}
