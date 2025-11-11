package com.taskmanagement;

import com.taskmanagement.security.JwtUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@SpringBootApplication
public class TaskManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApiApplication.class, args);
    }
}
