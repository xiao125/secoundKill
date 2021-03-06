package com.netease.seckill.controller;

import com.netease.seckill.dto.Exposer;
import com.netease.seckill.dto.SeckillExcution;
import com.netease.seckill.dto.SeckillResult;
import com.netease.seckill.entity.Seckill;
import com.netease.seckill.enums.SeckillStatusEnum;
import com.netease.seckill.exception.RepeatKillException;
import com.netease.seckill.exception.SeckillCloseException;
import com.netease.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;


/**
 * Controller中的每一个方法都对应我们系统中的一个资源URL
 * Created by Administrator on 2017/7/29.
 */

@Controller
@RequestMapping("/seckill")  //url:模块/资源/{}/细分
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private SeckillService seckillService;

    //秒杀列表
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){

        //list.jsp+mode=ModelAndView
        //获取列表页
        List<Seckill> seckills = seckillService.getSeckillList();
        model.addAttribute("seckills",seckills);
        return "list";
    }


    //详情页
    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId,Model model){

        if (seckillId == null){
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);

        if (null == seckill){

            return "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";

    }


    //暴露秒杀接口
    @RequestMapping(value = "/{seckillId}/exposer",method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"} )
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId){

        SeckillResult<Exposer> result;

        try{

            Exposer exposer = seckillService.exposeSeckillUrl(seckillId); //秒杀开启输出秒杀接口地址
            result = new SeckillResult<Exposer>(true,exposer);

        }catch (Exception e){
            logger.error(e.getMessage());
            result = new SeckillResult<Exposer>(false,e.getMessage());
        }

        return result;
    }


    //执行秒杀
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method =  RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExcution> execute(@PathVariable("seckillId") Long seckillId,
                                                  @PathVariable("md5") String md5,
                                                  @CookieValue(value = "killPhone",required = false) Long phone) {

        if (phone == null) {
            return new SeckillResult<SeckillExcution>(false, "未注册");
        }

        try {

            SeckillExcution seckillExcution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);
        } catch (RepeatKillException e) {

            //异常状态
            SeckillExcution seckillExcution = new SeckillExcution(seckillId, SeckillStatusEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);
        } catch (SeckillCloseException e) {

            SeckillExcution seckillExcution = new SeckillExcution(seckillId, SeckillStatusEnum.END);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);

        } catch (Exception e) {

            logger.error(e.getMessage());
            SeckillExcution seckillExcution = new SeckillExcution(seckillId, SeckillStatusEnum.INNER_ERROR);

            return new SeckillResult<SeckillExcution>(true, seckillExcution);
        }

    }

    //系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Long> time(){

            Date date = new Date();

            return new SeckillResult<Long>(true,date.getTime());

        }


}
