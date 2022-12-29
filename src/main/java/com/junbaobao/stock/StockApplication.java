package com.junbaobao.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Classname StockApplication
 * @Description
 * @Date: Created in 2022/12/5 9:56
 * @Author Name:
 */
@EnableScheduling
@SpringBootApplication
public class StockApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
    }
}

