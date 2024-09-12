package com.flashsell.flashsell.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class TestController {
    @ResponseBody
    @RequestMapping("hello")
    public String hello(){
        String result;
        // "HelloResource" is the resource name, it can take in any name, by convention, name it with function, interface names or any other unique String. 
        try (Entry entry = SphU.entry("HelloResource")){
            // what's rate limited 
            result  = "Hello Sentinel";
            return result;
        }catch (BlockException ex) {
            // Visiting been blocked, rate limit happened
            log.error(ex.toString());
            result = "The system is busy, please try again latter";
            return  result;
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
        //3.add rules to the collection.
        rules.add(rule);
        rules.add(rule2);
        //4.load rules
        FlowRuleManager.loadRules(rules);
    }
}
