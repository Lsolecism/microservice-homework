package com.yiyu.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiyu.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("UPDATE inventory " +
            "SET used = used + #{count}, residue = residue - #{count} " +
            "WHERE product_id = #{productId} AND residue >= #{count}")
    int deduct(@Param("productId") Long productId, @Param("count") Integer count);
}
