package com.netease.seckill.dto;

import com.netease.seckill.entity.SuccessKill;
import com.netease.seckill.enums.SeckillStatusEnum;

/**
 * 封装执行秒杀后的结果:是否秒杀成功
 * 成功就返回秒杀成功的所有信息(包括秒杀的商品id、秒杀成功状态、成功信息、用户明细)，
 * 失败就抛出一个我们允许的异常(重复秒杀异常、秒杀结束异常)
 * Created by Administrator on 2017/7/29.
 */
public class SeckillExcution {

    private long seckillId;

    //秒杀执行结果的状态
    private int status;

    //状态的明文标识
    private String statusInfo;

    //当秒杀成功时，需要传递秒杀成功的对象回去
    private SuccessKill successKill;


    //秒杀成功返回所有信息
    public SeckillExcution(long seckillId, SeckillStatusEnum seckillStatusEnum, SuccessKill successKill) {
        this.seckillId = seckillId;
        this.status = seckillStatusEnum.getStatus();
        this.statusInfo = seckillStatusEnum.getStatusInfo();
        this.successKill = successKill;
    }


    //秒杀失败返回信息
    public SeckillExcution(long seckillId, SeckillStatusEnum seckillStatusEnum) {
        this.seckillId = seckillId;
        this.status = seckillStatusEnum.getStatus();
        this.statusInfo = seckillStatusEnum.getStatusInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public SuccessKill getSuccessKill() {
        return successKill;
    }

    public void setSuccessKill(SuccessKill successKill) {
        this.successKill = successKill;
    }

    @Override public String toString() {
        final StringBuffer sb = new StringBuffer("SeckillExcution{");
        sb.append("seckillId=").append(seckillId);
        sb.append(", status=").append(status);
        sb.append(", statusInfo='").append(statusInfo).append('\'');
        sb.append(", successKill=").append(successKill);
        sb.append('}');
        return sb.toString();
    }



}
