package com.studenthelpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudentHelpdeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentHelpdeskApplication.class, args);
    }
}
