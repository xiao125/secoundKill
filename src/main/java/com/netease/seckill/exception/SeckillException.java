package com.netease.seckill.exception;

/**
 * 秒杀相关的所有业务异常
 * Created by Administrator on 2017/7/29.
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message){
        super(message);
    }

    public SeckillException(String message,Throwable cause){
        super(message,cause);
    }





}
