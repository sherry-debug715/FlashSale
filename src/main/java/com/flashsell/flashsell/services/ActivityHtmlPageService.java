package com.flashsell.flashsell.services;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.flashsell.flashsell.db.dao.SeckillActivityDao;
import com.flashsell.flashsell.db.dao.SeckillCommodityDao;
import com.flashsell.flashsell.db.po.SeckillActivity;
import com.flashsell.flashsell.db.po.SeckillCommodity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ActivityHtmlPageService {

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    /**
* 创建html页面
*
* @throws Exception */
public void createActivityHtml(long seckillActivityId) {
    PrintWriter writer = null;
    try {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        // get the template data
        Map<String, Object> resultMap = new HashMap<>(); 
        resultMap.put("seckillActivity", seckillActivity); 
        resultMap.put("seckillCommodity", seckillCommodity); 
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice()); 
        resultMap.put("oldPrice", seckillActivity.getOldPrice()); 
        resultMap.put("commodityId", seckillActivity.getCommodityId()); 
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        // Create thymeleaf template object
        Context context = new Context(); 
        // insert resultMap into thymeleaf object
        context.setVariables(resultMap);
        // Create the output path
        File file = new File("src/main/resources/templates/" + "seckill_item_" + seckillActivityId + ".html");
        writer = new PrintWriter(file);
        // Action: build the static html file
        templateEngine.process("seckill_item", context, writer);
        } catch (Exception e) {
            log.error(e.toString()); log.error("Building static html file error: " + seckillActivityId);
        } finally {
            if (writer != null) {
                writer.close();
        } 
        }
    }
}
