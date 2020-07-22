package com.de4bi.members.controller.api;

import java.util.Map;

import com.de4bi.common.annotation.RequireUserJwt;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.data.dto.PutMembersDto;
import com.de4bi.members.service.MembersService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = {"/api", "/api/v1"})
public class MembersApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(MembersApiController.class);
    
    private final MembersService membersSvc;

    @PostMapping("/members")
    public String postMembers(@RequestBody PostMembersDto postMembersDto) {
        return membersSvc.insert(postMembersDto).toString();
    }

    @RequireUserJwt
    @GetMapping("/members/{seq}")
    public String getMembers(@PathVariable long seq) {
        return membersSvc.rawSelect(seq).toString();
    }

    @RequireUserJwt
    @PutMapping("/members/{seq}")
    public String putMembers(
        @PathVariable long seq,
        @RequestBody PutMembersDto putMembersDto) {
            @@ 여기부터 시작 : requestbody에 받는 방법은?
        return membersSvc.update(putMembersDto).toString();
    }

    @RequireUserJwt
    @DeleteMapping("/members/{seq}")
    public String deleteMembers(@PathVariable long seq) {
        return null;
    }
}