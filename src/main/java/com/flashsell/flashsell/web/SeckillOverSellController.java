package com.flashsell.flashsell.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import com.flashsell.flashsell.services.SeckillActivityService;
import com.flashsell.flashsell.services.SeckillOverSellService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class SeckillOverSellController {
    
    @Autowired
    private SeckillOverSellService seckillOverSellService;

    @Autowired
    private SeckillActivityService seckillActivityService;
    /**
    * using lua
    * @param seckillActivityId
    * @return
    */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckil(@PathVariable long seckillActivityId) {
        boolean stockValidateRes = seckillActivityService.seckillStockValidator(seckillActivityId);
        return stockValidateRes ? "Congratulations, the flash sale was successful" : "Sorry, the item has been sold out";
    }
    
}
