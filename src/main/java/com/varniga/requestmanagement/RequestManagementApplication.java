package com.varniga.requestmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RequestManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestManagementApplication.class, args);
    }
}