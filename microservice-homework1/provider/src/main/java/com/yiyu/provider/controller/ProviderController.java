package com.yiyu.provider.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/provider/hello")
    @HystrixCommand(fallbackMethod = "helloFallback")
    public String hello(@RequestParam(defaultValue = "student") String name,
                        @RequestParam(defaultValue = "0") int sleepMs) throws InterruptedException {
        if (sleepMs > 0) {
            Thread.sleep(sleepMs);
        }
        if ("error".equalsIgnoreCase(name)) {
            throw new IllegalStateException("simulate provider exception");
        }
        return "Hello " + name + ", from provider port " + port;
    }

    public String helloFallback(String name, int sleepMs) {
        return "Provider fallback triggered. name=" + name + ", port=" + port;
    }
}
