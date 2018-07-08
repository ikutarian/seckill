package com.okada.seckill.web;

import com.okada.seckill.dto.Exposer;
import com.okada.seckill.dto.SeckillExecution;
import com.okada.seckill.dto.SeckillResult;
import com.okada.seckill.entity.Seckill;
import com.okada.seckill.enums.SeckillStatusEnum;
import com.okada.seckill.exception.RepeatKillException;
import com.okada.seckill.exception.SeckillCloseException;
import com.okada.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("seckill")
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String list(Model model) {
        // list.jsp + model = ModelAndView

        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value = "{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }

        model.addAttribute("seckill", seckill);
        return "detail";
    }

    @RequestMapping(value = "{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult exposer(@PathVariable("seckillId") Long seckillId) {
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            return new SeckillResult(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillResult(false, e.getMessage());
        }
    }

    @RequestMapping(value = "{seckillId}/{md5}/execution", method = RequestMethod.POST,
    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult execute(@PathVariable("seckillId") Long seckillId,
                                 @PathVariable("md5") String md5,
                                 @CookieValue(value = "killPhone", required = false) Long phone) {
        if (phone == null) {
            return new SeckillResult(false, "未注册");
        }

        try {
            // 调用存储过程
            SeckillExecution seckillExecution = seckillService.excuteSeckillByProcedure(seckillId, phone, md5);
            return new SeckillResult(true, seckillExecution);
        } catch (SeckillCloseException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatusEnum.END);
            return new SeckillResult(true, e.getMessage());
        } catch (RepeatKillException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatusEnum.REPEAT_KILL);
            return new SeckillResult(true, execution);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatusEnum.INNER_ERROR);
            return new SeckillResult(true, execution);
        }
    }

    @RequestMapping(value = "time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult time() {
        Date date = new Date();
        return new SeckillResult(true, date);
    }
}
