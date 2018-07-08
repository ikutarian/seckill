package com.okada.seckill.service;

import com.okada.seckill.dao.BaseTest;
import com.okada.seckill.dto.Exposer;
import com.okada.seckill.dto.SeckillExecution;
import com.okada.seckill.entity.Seckill;
import com.okada.seckill.exception.RepeatKillException;
import com.okada.seckill.exception.SeckillCloseException;
import com.okada.seckill.exception.SeckillException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SeckillServiceTest extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}", seckillList);
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillService.getById(1000);
        logger.info("seckill={}", seckill);
    }

    // 测试代码完整逻辑，注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        long id = 1001;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            logger.info("expose={}", exposer);

            long phone = 13812341234L;
            String md5 = exposer.getMd5();
            try {
                SeckillExecution seckillExecution = seckillService.excuteSeckill(id, phone, md5);
                logger.info("result={}", seckillExecution);
            } catch (RepeatKillException | SeckillCloseException e) {
                logger.error(e.getMessage());
            } catch (SeckillException e) {
                logger.error(e.getMessage());
            }
        } else {
            // 秒杀未开启
            logger.warn("expose={}", exposer);
        }
    }

    @Test
    public void excuteSeckillByProcedure() throws Exception {
        long seckillId = 1000;
        long phone = 13500000001L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.excuteSeckillByProcedure(seckillId, phone, md5);
            logger.info(execution.getStatusInfo());
        }
    }
}