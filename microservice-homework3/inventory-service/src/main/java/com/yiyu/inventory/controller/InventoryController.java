package com.yiyu.inventory.controller;

import com.yiyu.inventory.service.InventoryDomainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class InventoryController {

    private final InventoryDomainService inventoryDomainService;

    @Value("${server.port}")
    private String port;

    public InventoryController(InventoryDomainService inventoryDomainService) {
        this.inventoryDomainService = inventoryDomainService;
    }

    @GetMapping("/lb/echo")
    public Map<String, Object> lbEcho() {
        return result("负载均衡测试命中 inventory-service", "lb");
    }

    @GetMapping("/predicate/path/check")
    public Map<String, Object> pathPredicate() {
        return result("Path 断言命中", "path");
    }

    @GetMapping("/predicate/header/check")
    public Map<String, Object> headerPredicate() {
        return result("Header 断言命中", "header");
    }

    @GetMapping("/predicate/query/check")
    public Map<String, Object> queryPredicate() {
        return result("Query 断言命中", "query");
    }

    @PostMapping("/predicate/method/check")
    public Map<String, Object> methodPredicate() {
        return result("Method 断言命中", "method");
    }

    @PostMapping("/seata/inventory/deduct")
    public String deduct(@RequestParam("productId") Long productId,
                         @RequestParam("count") Integer count) {
        inventoryDomainService.deduct(productId, count);
        return "扣减库存成功";
    }

    private Map<String, Object> result(String msg, String scene) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scene", scene);
        data.put("message", msg);
        data.put("instancePort", port);
        return data;
    }
}
