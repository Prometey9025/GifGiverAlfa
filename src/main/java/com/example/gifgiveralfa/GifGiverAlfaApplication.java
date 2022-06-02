package com.example.gifgiveralfa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GifGiverAlfaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GifGiverAlfaApplication.class, args);
    }

}
