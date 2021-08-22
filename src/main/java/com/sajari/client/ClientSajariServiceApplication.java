package com.sajari.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ClientSajariServiceApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ClientSajariServiceApplication.class);
        springApplication.run(args);
    }

}
