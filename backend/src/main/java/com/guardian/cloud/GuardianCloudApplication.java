package com.guardian.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GuardianCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                GuardianCloudApplication.class,
                args
        );
    }
}