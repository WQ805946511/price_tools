package com.smarting.price.tools;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.smarting.price.tools.mapper")
@SpringBootApplication
public class PriceToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PriceToolsApplication.class, args);
	}

}
