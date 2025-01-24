package com.mtheory7.legoinventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LegoInventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LegoInventoryServiceApplication.class, args);
    }
}
