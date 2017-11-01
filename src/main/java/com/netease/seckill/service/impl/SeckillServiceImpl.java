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
public class SeckillServiceImpl implements SeckillService {

    //日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //加入一个混淆字符串(秒杀接口)的salt，为了我避免用户猜出我们的md5值，值任意给，越复杂越好
    private final String salt="shsdssljdd";

    @Autowired
    private SuccessKillDao successKillDao;

    //注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Value("${sault}")
    public String sault;

    @Autowired
    private RedisDao redisDao;



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
            //2.访问数据库
            seckill = getById(seckillId);

            if (seckill == null) { //说明查不到这个秒杀产品的记录
                return new Exposer(false, seckillId);
            }
            else{
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }

        //若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();

        if(startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime()){

            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        //秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5 = getMd5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    //秒杀开启，返回秒杀商品的id、用给接口加密的md5
    private String getMd5(long seckillId){
        String base = seckillId+"/"+sault;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    //秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚

    /**  @Transactional
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     *
     * 秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
     */
    @Transactional  //rollback when runtimeException happend
    public SeckillExcution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if(md5 == null || !md5.equals(getMd5(seckillId))){
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }

        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        try {
            //减库存 （更新库存）
            int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
            if(updateCount<=0){
                //没有更新库存记录，说明秒杀结束
                throw new RepeatKillException("seckill repeated");
            }
            else{

                //否则更新了库存，秒杀成功,增加明细
                int insertCount = successKillDao.insertSuccessKilled(seckillId,userPhone);
                //看是否该明细被重复插入，即用户是否重复秒杀
                if(insertCount<=0){

                    throw new RepeatKillException("seckill repeated");
                }
                else{
                    //秒杀成功
                    SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId,userPhone); //根据秒杀商品的id查询明细
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
                //根据秒杀商品的id查询明细successKilled对象
                SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExcution(seckillId,SeckillStatusEnum.SUCCESS,successKill);
            }
            else{
                return new SeckillExcution(seckillId,SeckillStatusEnum.stateOf(result));
            }
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExcution(seckillId,SeckillStatusEnum.INNER_ERROR);
        }
    }



}
