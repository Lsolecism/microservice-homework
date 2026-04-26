package com.yiyu.homework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/info")
    public String info() {
        return "order-service instance port=" + port;
    }
}
