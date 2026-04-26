package com.yiyu.homework;

import com.yiyu.homework.client.OrderClient;
import com.yiyu.homework.client.PaymentClient;
import com.yiyu.homework.client.ProductClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remote")
public class RemoteInvokeController {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    public RemoteInvokeController(OrderClient orderClient, ProductClient productClient, PaymentClient paymentClient) {
        this.orderClient = orderClient;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
    }

    @GetMapping("/order")
    public String orderByName() {
        return orderClient.info();
    }

    @GetMapping("/all")
    public Map<String, String> allProviders() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("order", orderClient.info());
        map.put("product", productClient.info());
        map.put("payment", paymentClient.info());
        return map;
    }

    @GetMapping("/order/lb-demo")
    public List<String> loadBalanceDemo() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            lines.add(orderClient.info());
        }
        return lines;
    }
}
