package com.de4bi.members.service;

import java.util.Map;

import com.de4bi.common.data.ApiResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MembersService {
 
    private static final Logger logger = LoggerFactory.getLogger(MembersService.class);

    public ResponseEntity<ApiResult> insert() {
        return ResponseEntity.ok().body(ApiResult.of(true, "Hello World!"));
    }

    public void select() {

    }

    public void update() {

    }

    public void delete() {

    }
}