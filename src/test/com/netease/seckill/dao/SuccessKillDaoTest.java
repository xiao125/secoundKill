package com.netease.seckill.dao;

import com.netease.seckill.entity.SuccessKill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/31.
 */

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKillDaoTest {

    @Resource
    private SuccessKillDao successKillDao;

    @Test
    public void insertSuccessKill() throws Exception {

        long seckillId = 1000L;
        long userPhone=13147009492L;
        int insertCount=successKillDao.insertSuccessKilled(seckillId,userPhone);
        System.out.println("insertCount="+insertCount);

    }

    @Test
    public void queryByIdWithSeckill() throws Exception {

        long seckillId =1000L;
        long userPhone = 12476191877L;
        SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId,userPhone);
        System.out.println(seckillId);
        System.out.println(successKill.getSeckill());

    }

}