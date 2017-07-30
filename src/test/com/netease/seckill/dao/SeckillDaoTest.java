package com.netease.seckill.dao;

import com.netease.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器
 * Created by Administrator on 2017/7/30.
 */

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    //注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;


    @Test
    public void reduceNumber() throws Exception {
    }

    @Test
    public void queryById() throws Exception {

        long seckillId=1000;
        Seckill seckill = seckillDao.queryById(seckillId);
        System.out.println(seckill.getName());
        System.out.println(seckill);

    }

    @Test
    public void queryAll() throws Exception {

        List<Seckill> seckills = seckillDao.queryAll(0,100);
        for (Seckill seckill : seckills){

            System.out.println(seckill);
        }
    }

}