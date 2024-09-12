package com.flashsell.flashsell.web;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.flashsell.flashsell.db.dao.OrderDao;
import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.dao.SeckillCommodityDao;
import com.flashsell.flashsell.db.po.Order;
import com.flashsell.flashsell.db.po.SeckillActivity;
import com.flashsell.flashsell.db.po.SeckillCommodity;
import com.flashsell.flashsell.services.SeckillActivityService;
import com.flashsell.flashsell.util.RedisService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;


@Slf4j
@Controller
public class SeckillActivityController {
    
    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    @Autowired
    SeckillActivityService seckillActivityService;

    @Autowired
    OrderDao orderDao;

    @Resource
    private RedisService redisService;

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {

        try (Entry entry = SphU.entry("seckills")) {
            List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
            resultMap.put("seckillActivities", seckillActivities);
            return "seckill_activity"; 
        } catch (BlockException ex) {
            log.error("Checking flash sale items, rate limit activated " + ex.toString());
            return "wait";
        }

    }

        /**
     *  Define rate limiting rules
     *  1. Create a collection to store the rate limiting rules
     *  2. Create a rate limiting rule
     *  3. Add the rate limiting rule to the collection
     *  4. Load the rate limiting rules
     *  @PostConstruct is executed after the constructor of the current class has been completed
     */
    @PostConstruct
    public void seckillsFlow(){
        // 1. Create a collection to store the rate limiting rules
        List<FlowRule> rules = new ArrayList<>();
        //2.Create rate limit rule instance and set rule
        FlowRule rule = new FlowRule();
        //2.1.1 Define the targeted resources.
        rule.setResource("seckills");
        //2.1.2 Define rate limit QPS type.
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //2.1.3 Define QPS request number.
        rule.setCount(1);

        FlowRule rule2 = new FlowRule();
        // 2.2.1
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // 2.2.2
        rule2.setCount(2);
        // 2.2.3
        rule2.setResource("HelloResource");

        FlowRule rule3 = new FlowRule();
        rule3.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule3.setCount(3);
        rule3.setResource("flashSale_buy");
        //3.add rules to the collection.
        rules.add(rule);
        rules.add(rule2);
        rules.add(rule3);
        //4.load rules
        FlowRuleManager.loadRules(rules);
    }

    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) {
        SeckillActivity seckillActivity;
        SeckillCommodity seckillCommodity;
        
        // get seckill activity from redis cache, if not stored in redis then fetch from mysql
        String seckillActivityInfo = redisService.getValue("seckillActivity:" +
        seckillActivityId);
        if (StringUtils.isNotEmpty(seckillActivityInfo)) { 
            log.info("redis cache seckillActivityInfo data: " + seckillActivityInfo); 
            seckillActivity = JSON.parseObject(seckillActivityInfo, SeckillActivity.class);
        } else {
            seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        }

        // get commodity information from redis by commodity id, if not stored in redis, fetch from mysql.
        String seckillCommodityInfo = redisService.getValue("seckillCommodity:"
        + seckillActivity.getCommodityId());
        if (StringUtils.isNotEmpty(seckillCommodityInfo)) { 
            log.info("redis cache seckillCommodityInfo data: " + seckillCommodityInfo); seckillCommodity = JSON.parseObject(seckillActivityInfo, SeckillCommodity.class);
        } else {
            seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        }
        
        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap) throws ParseException {
        
            startTime = startTime.substring(0, 10) + startTime.substring(11);
            endTime = endTime.substring(0, 10) + endTime.substring(11);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
            SeckillActivity seckillActivity = new SeckillActivity();
            seckillActivity.setName(name);
            seckillActivity.setCommodityId(commodityId);
            seckillActivity.setSeckillPrice(seckillPrice);
            seckillActivity.setOldPrice(oldPrice);
            seckillActivity.setTotalStock(seckillNumber);
            seckillActivity.setAvailableStock((int) seckillNumber); // Corrected Integer construction
            seckillActivity.setLockStock(0L);
            seckillActivity.setActivityStatus(1);
            seckillActivity.setStartTime(format.parse(startTime));
            seckillActivity.setEndTime(format.parse(endTime));
            seckillActivityDao.insertSeckillActivity(seckillActivity);
            resultMap.put("seckillActivity", seckillActivity);
            return "add_success"; // returns the view "add_success"
    }

    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity"; // returns the view "add_activity"
    }

    /**
    * Handle flashsale request
    * @param userId
    * @param seckillActivityId
    * @return
    */
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId, 
                                        @PathVariable long seckillActivityId) {
        boolean stockValidateResult = false;
        ModelAndView modelAndView = new ModelAndView();
        try {
            /*
            * Make sure flash sale can be processed 
            */
            if (redisService.isInLimitMember(seckillActivityId, userId)) {
                // notify user there is a limit buy 
                modelAndView.addObject("resultInfo", "Sorry, you have already purchased the product."); 
                modelAndView.setViewName("seckill_result");
                return modelAndView;
            }
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if (stockValidateResult) {
                try (Entry entry = SphU.entry("flashSale_buy")) {
                    Order order = seckillActivityService.createOrder(seckillActivityId, userId);
                    modelAndView.addObject("resultInfo", "Flash sale successful, order is being created, Order ID: " + order.getOrderNo());
                    modelAndView.addObject("orderNo", order.getOrderNo());
                    // add user into buyers list in redis  
                    redisService.addLimitMember(seckillActivityId, userId);
                } catch (BlockException ex) {
                    log.error("Buying flash sale item, rate limit activated " + ex.toString());
                    modelAndView.setViewName("wait");
                }
            } else {
                modelAndView.addObject("resultInfo","Sorry, the selected product is out of stock");
            }
        } catch (Exception e) {
            log.error("Flashsale system error" + e.toString());
            modelAndView.addObject("resultInfo","Flashsale failed");
        }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    /*
     * Order status checking
     * @param orderNo
     * @return
     */
    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo) {
        log.info("Order Status, Order number: " + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (order != null) {
            log.info("Order found with status ======> " + order.getOrderStatus());

            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        } else {
            log.info("Order not found, redirecting to wait template.");
            modelAndView.setViewName("wait");
        }
        return modelAndView;
    }

    /**
     * Order payment
     * @return
     */
    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception{
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }

    /**
    * get current server time
    * @return
    */
    @ResponseBody
    @RequestMapping("/seckill/getSystemTime")
    public String getSystemTime() {
        // set up time format
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        // new Date(): to get the acurate time of the system at the moment.
        String date = df.format(new Date());
        return date;
    }
}