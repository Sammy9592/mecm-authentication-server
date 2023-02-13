package com.sl.mecm.authentication.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sl.mecm.*")
@EnableAutoConfiguration(exclude = org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class)
public class AuthenticationServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServerApplication.class);

    public static void main(String[] args) {
        try {
            logger.debug("start cache service application debug");
            logger.info("start cache service application info");
            logger.error("start cache service application error");
            SpringApplication.run(AuthenticationServerApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}