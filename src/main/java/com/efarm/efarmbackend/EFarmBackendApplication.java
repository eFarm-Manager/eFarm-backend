package com.efarm.efarmbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EFarmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EFarmBackendApplication.class, args);
    }

}
