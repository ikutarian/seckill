package com.okada.seckill.dao;

import com.okada.seckill.entity.SuccessKilled;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;


public class SuccessKilledDaoTest extends BaseTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() throws Exception {
        int insertCount = successKilledDao.insertSuccessKilled(1001L, 13812341234L);
        System.out.println("insertCount=" + insertCount);
    }

    @Test
    public void queryByIdWithSeckill() throws Exception {
        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(1001L, 13812341234L);
        System.out.println(successKilled);
    }

}