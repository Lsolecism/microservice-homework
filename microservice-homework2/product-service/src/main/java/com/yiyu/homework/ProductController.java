package com.yiyu.homework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/info")
    public String info() {
        return "product-service instance port=" + port;
    }
}
