package com.okada.seckill.service;

import com.okada.seckill.dto.Exposer;
import com.okada.seckill.dto.SeckillExecution;
import com.okada.seckill.entity.Seckill;
import com.okada.seckill.exception.RepeatKillException;
import com.okada.seckill.exception.SeckillCloseException;
import com.okada.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口：站在“使用者”的角度设计接口
 * 设计依据三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 */
public interface SeckillService {

    /**
     * 查询所有秒杀
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     */
    SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException;

    /**
     * 执行秒杀操作 by 存储过程
     */
    SeckillExecution excuteSeckillByProcedure(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException;
}
