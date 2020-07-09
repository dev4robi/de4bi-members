package com.de4bi.members.service;

import com.de4bi.common.data.ApiResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TestService {

    private final static Logger logger = LoggerFactory.getLogger(TestService.class);
    
    public ResponseEntity<ApiResult> insert() {
        return ResponseEntity.ok().body(ApiResult.of(true, "Hello World!"));
    }
}