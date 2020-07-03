package com.de4bi.members.controller.api;

import java.util.Map;

import com.de4bi.common.annotation.RequireUserJwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = {"/api", "/api/v1"})
public class MembersApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(MembersApiController.class);

    @RequireUserJwt
    @GetMapping("/members/{id}")
    public Map<String, Object> getMembers(@PathVariable String id) {
        return null;
    }

    @PostMapping("/members")
    public Map<String, Object> postMembers() {
        return null;
    }

    @RequireUserJwt
    @PutMapping("/members/{id}")
    public Map<String, Object> putMembers(@PathVariable String id) {
        return null;
    }

    @RequireUserJwt
    @DeleteMapping("/members/{id}")
    public Map<String, Object> deleteMembers(@PathVariable String id) {
        return null;
    }
}