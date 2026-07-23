package com.volunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.volunteer.mapper")
public class VolunteerSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(VolunteerSystemApplication.class, args);
    }
}