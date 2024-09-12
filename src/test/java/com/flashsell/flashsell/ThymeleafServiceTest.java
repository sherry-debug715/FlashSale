package com.flashsell.flashsell;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.flashsell.flashsell.services.ActivityHtmlPageService;

@SpringBootTest
public class ThymeleafServiceTest {

    @Autowired
    private ActivityHtmlPageService activityHtmlPageService;

    @Test
    public void createHtmlTest() {
        activityHtmlPageService.createActivityHtml(12);
    }
    
}
