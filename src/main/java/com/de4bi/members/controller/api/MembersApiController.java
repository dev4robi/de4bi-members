package com.de4bi.members.controller.api;

import java.util.Map;

import com.de4bi.common.annotation.RequireUserJwt;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.service.MembersService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = {"/api", "/api/v1"})
public class MembersApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(MembersApiController.class);
    
    private static MembersService membersSvc;

    @PostMapping("/members")
    public String postMembers(@RequestBody PostMembersDto postMembersDto) {
        return membersSvc.insert(postMembersDto).getBody().toString();
    }

    @RequireUserJwt
    @GetMapping("/members/{id}")
    public String getMembers(@PathVariable String id) {
        return null;
    }

    @RequireUserJwt
    @PutMapping("/members/{id}")
    public String putMembers(@PathVariable String id) {
        return null;
    }

    @RequireUserJwt
    @DeleteMapping("/members/{id}")
    public String deleteMembers(@PathVariable String id) {
        return null;
    }
}