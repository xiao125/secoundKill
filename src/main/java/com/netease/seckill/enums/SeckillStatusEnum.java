package com.netease.seckill.enums;

/**
 * Created by Administrator on 2017/7/29.
 */
public enum SeckillStatusEnum {

    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATA_REWRITE(-3,"数据篡改");

    private int state;
    private String info;

   SeckillStatusEnum(int state,String info){

       this.state = state;
       this.info = info;
   }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static SeckillStatusEnum stateOf(int index){

       for (SeckillStatusEnum state : values()){

           if (state.getState() == index){
               return state;
           }
       }
       return null;
    }

}
