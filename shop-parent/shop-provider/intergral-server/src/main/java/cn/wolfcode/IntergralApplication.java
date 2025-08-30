package cn.wolfcode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = "cn.wolfcode.mapper")
public class IntergralApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntergralApplication.class,args);
    }
}
