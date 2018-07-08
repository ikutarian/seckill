package com.okada.seckill.service.impl;

import com.okada.seckill.cache.RedisDao;
import com.okada.seckill.dao.SeckillDao;
import com.okada.seckill.dao.SuccessKilledDao;
import com.okada.seckill.dto.Exposer;
import com.okada.seckill.dto.SeckillExecution;
import com.okada.seckill.entity.Seckill;
import com.okada.seckill.entity.SuccessKilled;
import com.okada.seckill.enums.SeckillStatusEnum;
import com.okada.seckill.exception.RepeatKillException;
import com.okada.seckill.exception.SeckillCloseException;
import com.okada.seckill.exception.SeckillException;
import com.okada.seckill.service.SeckillService;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;

    /**
     * MD5加盐，用于混淆MD5
     */
    private static final String SALT = "a^&*UHIHJU&*oioij4230909^&*^)(RT^U";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        // 优化点：缓存优化，在超时的基础上维护一致性
        // 1. 访问redis，拿到对象
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            // 2. 访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                // 3. 放入redis
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        // 当前系统时间
        Date nowTime = new Date();
        if (nowTime.getTime()< startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        // 转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + SALT;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1. 开发团队达成一致约定，明确标注事务方法的编程风格
     * 2. 保证事务方法的执行时间尽可能短，不要穿插其他的网络操作（RPC/HTTP请求），把这些网络操作剥离到事务的外部（也就是写一个上层方法，把网络请求和事务操作写在不同代码块里面）
     * 3. 不是所有的方法都需要事务，比如只有一条修改操作，或者只有读操作这样的，不需要事务控制
     */
    public SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }

        // 执行秒杀逻辑：减库存 + 记录购买行为
        Date nowTime = new Date();
        try {
            // 记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            // 唯一验证 seckillId, userPhone 同一个商品，一个用户只能秒杀一次
            if (insertCount <= 0) {
                // 重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                // 减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    // 没有更新成功记录，因为秒杀已结束了，rollback
                    throw new SeckillCloseException("seckill closed");
                } else {
                    // 秒杀成功，commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatusEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException | RepeatKillException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 所有编译时异常转化为运行时异常
            throw new SeckillException("seckill inner error:" + e.getMessage(), e);
        }
    }

    @Override
    public SeckillExecution excuteSeckillByProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatusEnum.DATE_REWRITE);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", new Date());
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            seckillDao.killByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                // 秒杀成功
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatusEnum.SUCCESS, sk);
            } else {
                return new SeckillExecution(seckillId, SeckillStatusEnum.statusOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatusEnum.INNER_ERROR);
        }
    }
}
