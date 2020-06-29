package com.de4bi.members.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class TestApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestApiController.class);

    @GetMapping("/test")
    public String getTest() {
        return "Hello Test!";
    }
}