package com.aidev.workflowmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.aidev.workflowmanager.**.mapper")
public class WorkflowManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowManagerApplication.class, args);
    }
}
