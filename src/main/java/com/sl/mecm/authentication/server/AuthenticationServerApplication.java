package com.sl.mecm.authentication.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "com.sl.mecm.*")
@EnableAutoConfiguration(exclude = org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class)
@Slf4j
public class AuthenticationServerApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(AuthenticationServerApplication.class, args);
        }catch (Exception e){
            log.error("error on startup", e);
            e.printStackTrace();
        }
    }
}