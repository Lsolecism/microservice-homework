package com.yiyu.inventory.service;

import com.yiyu.inventory.mapper.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryDomainService {

    private final InventoryMapper inventoryMapper;

    public InventoryDomainService(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long productId, Integer count) {
        int changedRows = inventoryMapper.deduct(productId, count);
        if (changedRows == 0) {
            throw new IllegalStateException("库存不足，扣减失败");
        }
    }
}
