package com.okada.seckill.dao;

import com.okada.seckill.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledDao {

    /**
     * 插入购买明细，可过滤重复
     *
     * 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId,
                            @Param("userPhone")long userPhone);

    /**
     * 根据id查询SuccessKilled，并携带秒杀产品对象实体
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId,
                                       @Param("userPhone")long userPhone);
}
