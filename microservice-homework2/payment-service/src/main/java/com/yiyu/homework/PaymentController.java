package com.yiyu.homework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/info")
    public String info() {
        return "payment-service instance port=" + port;
    }
}
