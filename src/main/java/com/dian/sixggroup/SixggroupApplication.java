package com.dian.sixggroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class SixggroupApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.out.println(LocalDateTime.now());
        SpringApplication.run(SixggroupApplication.class, args);
    }

}
