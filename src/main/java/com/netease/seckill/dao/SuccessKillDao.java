package com.netease.seckill.dao;

import com.netease.seckill.entity.SuccessKill;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Administrator on 2017/7/29.
 */
public interface SuccessKillDao {


    /**
     * 插入购买明细，可过滤重复
     * @param seckillId
     * @param userPhone
     * @return 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId")long seckillId,@Param("userPhone")long userPhone);


    /**
     * 根据秒杀商品的id查询明细successKill对象(该对象携带了Seckill秒杀产品对象)
     * @param seckillId
     * @param userPhone
     * @return
     */
    SuccessKill queryByIdWithSeckill(@Param("seckillId") long seckillId,@Param("userPhone")long userPhone);

}
