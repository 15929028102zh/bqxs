package com.biangqiang.freshdelivery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 边墙鲜送应用启动类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.biangqiang.freshdelivery.mapper")
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class FreshDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreshDeliveryApplication.class, args);
        System.out.println("  ____  _                   ___  _                   ");
        System.out.println(" | __ )(_) __ _ _ __   __ _ / _ \\(_) __ _ _ __   __ _   ");
        System.out.println(" |  _ \\| |/ _` | '_ \\ / _` | | | | |/ _` | '_ \\ / _` |  ");
        System.out.println(" | |_) | | (_| | | | | (_| | |_| | | (_| | | | | (_| |  ");
        System.out.println(" |____/|_|\\__,_|_| |_|\\__, |\\__\\_|\\__,_|_| |_|\\__, |  ");
        System.out.println("                      |___/                    |___/   ");
        System.out.println("边墙鲜送后端服务启动成功！");
        System.out.println("接口文档地址: http://localhost:8081/swagger-ui/index.html");
    }
}

