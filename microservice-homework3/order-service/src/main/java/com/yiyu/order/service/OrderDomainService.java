package com.yiyu.order.service;

import com.yiyu.order.client.InventoryClient;
import com.yiyu.order.entity.OrderRecord;
import com.yiyu.order.mapper.OrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderDomainService {

    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;

    public OrderDomainService(OrderMapper orderMapper, InventoryClient inventoryClient) {
        this.orderMapper = orderMapper;
        this.inventoryClient = inventoryClient;
    }

    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, Long productId, Integer count, boolean simulateFail) {
        OrderRecord orderRecord = new OrderRecord();
        orderRecord.setUserId(userId);
        orderRecord.setProductId(productId);
        orderRecord.setCount(count);
        orderRecord.setMoney(new BigDecimal("100.00"));
        orderRecord.setStatus(0);
        orderMapper.insert(orderRecord);

        inventoryClient.deduct(productId, count);

        if (simulateFail) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("模拟异常: 触发全局回滚");
        }

        orderRecord.setStatus(1);
        orderMapper.updateById(orderRecord);
        return "下单成功，事务提交";
    }
}
