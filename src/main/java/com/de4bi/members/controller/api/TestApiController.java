package com.de4bi.members.controller.api;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.members.service.TestService;

import org.apache.commons.lang3.RandomStringUtils;
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

    private TestService testSvc;

    @GetMapping("/test")
    public String getTest() {
        ThreadStorage.put(ApiResult.KEY_TID, RandomStringUtils.randomAlphanumeric(16));
        return testSvc.insert().toString();
    }
}