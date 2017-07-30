package com.netease.seckill.service.impl;

import com.netease.seckill.cache.RedisDao;
import com.netease.seckill.dao.SeckillDao;
import com.netease.seckill.dao.SuccessKillDao;
import com.netease.seckill.dto.Exposer;
import com.netease.seckill.dto.SeckillExcution;
import com.netease.seckill.entity.Seckill;
import com.netease.seckill.entity.SuccessKill;
import com.netease.seckill.enums.SeckillStatusEnum;
import com.netease.seckill.exception.RepeatKillException;
import com.netease.seckill.exception.SeckillCloseException;
import com.netease.seckill.exception.SeckillException;
import com.netease.seckill.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀Service接口的实现
 * Created by Administrator on 2017/7/29.
 */
@Service
@PropertySource(value = "classpath:sault.properties")
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SuccessKillDao successKillDao;

    //注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Value("${sault}")
    public String sault;

    @Autowired
    private RedisDao redisDao;

    //日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * expose seckill url when seckill start,else expose system time and kill time
     * @param seckillId
     */
    public Exposer exposeSeckillUrl(long seckillId) {
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(null == seckill) {
            seckill = getById(seckillId);
            if (seckill == null) { //说明查不到这个秒杀产品的记录
                return new Exposer(false, seckillId);
            }
            else{
                redisDao.putSeckill(seckill);
            }
        }

        //若是秒杀未开启
        Date createTime = seckill.getCreateTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date currentTime = new Date();
        //seckill success
        if(currentTime.after(createTime) && currentTime.before(endTime)){
            //秒杀开启，返回秒杀商品的id、用给接口加密的md5
            String md5 = getMd5(seckillId);
            return new Exposer(true,md5,seckillId);
        }
        else{
            return new Exposer(false,seckillId,currentTime.getTime(),createTime.getTime(),endTime.getTime());
        }
    }

    private String getMd5(long seckillId){
        String base = seckillId+"/"+sault;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    //秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
    @Transactional  //rollback when runtimeException happend
    public SeckillExcution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if(md5 == null || !md5.equals(getMd5(seckillId))){
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }

        //执行秒杀逻辑:减库存+增加购买明细
        Date currentTime = new Date();
        try {
            //否则更新了库存，秒杀成功,增加明细
            int insertCount = successKillDao.insertSuccessKill(seckillId, userPhone);
            //看是否该明细被重复插入，即用户是否重复秒杀
            if(insertCount<=0){
                //没有更新库存记录，说明秒杀结束
                throw new RepeatKillException("seckill repeated");
            }
            else{
                //execute seckill:1.reduce product 2.record purchase message    //热点商品竞争

                //减库存
                int updateCount = seckillDao.reduceNumber(seckillId,currentTime);

                if(updateCount<=0){

                    //没有更新库存记录，说明秒杀结束
                    throw new SeckillCloseException("seckill is close");
                }
                else{
                    //秒杀成功
                    SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExcution(seckillId, SeckillStatusEnum.SUCCESS,successKill);
                }

            }

        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            //rollback
            logger.error(e.getMessage(),e);
            //所以编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    public SeckillExcution executeSeckillProcedure(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5 == null || md5.equals(getMd5(seckillId))){
            return new SeckillExcution(seckillId, SeckillStatusEnum.DATA_REWRITE);
        }
        Date time = new Date();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",time);
        map.put("result",null);
        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExcution(seckillId,SeckillStatusEnum.SUCCESS,successKill);
            }
            else{
                return new SeckillExcution(seckillId,SeckillStatusEnum.statusOf(result));
            }
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExcution(seckillId,SeckillStatusEnum.INNER_ERROR);
        }
    }



}
