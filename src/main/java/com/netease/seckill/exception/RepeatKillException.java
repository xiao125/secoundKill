package com.netease.seckill.exception;

/**
 * 重复秒杀异常，是一个运行期异常，不需要我们手动try catch
 * mysql只支持运行期异常的回滚操作
 * Created by Administrator on 2017/7/29.
 */
public class RepeatKillException extends SeckillException{

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
