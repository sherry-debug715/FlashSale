package com.flashsell.flashsell;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.flashsell.flashsell.db.mappers")
@ComponentScan(basePackages = {"com.flashsell"})
public class FlashsellApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashsellApplication.class, args);
	}

}
