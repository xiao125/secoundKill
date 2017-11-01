package com.netease.seckill.service;

import com.netease.seckill.dto.Exposer;
import com.netease.seckill.dto.SeckillExcution;
import com.netease.seckill.entity.Seckill;
import com.netease.seckill.exception.RepeatKillException;
import com.netease.seckill.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/8/2.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

  //  private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {

        List<Seckill> seckills = seckillService.getSeckillList();
        System.out.println(seckills);

    }

    @Test
    public void getById() throws Exception {

        long seckillId =1000;
        Seckill seckill = seckillService.getById(seckillId);
        System.out.println(seckill);


    }

    @Test
    public void exposeSeckillUrl() throws Exception {

        long seckillId=1000;
        Exposer exposer = seckillService.exposeSeckillUrl(seckillId);
        System.out.println(exposer);

    }

    @Test
    public void executeSeckill() throws Exception{
        long seckillId =1000;
        long userPhone=13476191876L;
        String md5="bf204e2683e7452aa7db1a50b5713bae";



        try {

            SeckillExcution seckillExcution = seckillService.executeSeckill(seckillId,userPhone,md5);

            System.out.println(seckillExcution);

        }catch (RepeatKillException e){

            e.printStackTrace();
        }catch (SeckillCloseException e1){

            e1.printStackTrace();
        }


    }

}