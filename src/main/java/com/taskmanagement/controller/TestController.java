package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/profile")
    public String getProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "User is null!";
        }
        return "Hello " + user.getFullName() + "! Your email is: " + user.getEmail();
    }

    @GetMapping("/test")
    public String test() {
        return "Protected endpoint is working!";
    }
}