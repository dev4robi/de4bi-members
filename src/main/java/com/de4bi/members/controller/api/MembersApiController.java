package com.de4bi.members.controller.api;

import com.de4bi.common.annotation.RequireMemberJwt;
import com.de4bi.members.service.MembersService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = {"/api", "/api/v1"})
public class MembersApiController {
    
    private final MembersService membersSvc;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequireMemberJwt
    @GetMapping("/members/{seq}")
    public String getMemberBasicInfo(@PathVariable long seq) {
        return membersSvc.selectMemberInfo(null, seq, null, null).toString();
    }

    @RequireMemberJwt
    @PutMapping("/members/{seq}")
    public String putMemberBasicInfo(
        @PathVariable long seq,
        @RequestBody(required = false) String oldPassword,
        @RequestBody(required = false) String newPassword,
        @RequestBody(required = false) String nickname,
        @RequestBody(required = false) String name
    ) {
        return membersSvc.updateMemberInfo(seq, oldPassword, newPassword, nickname, name).toString();
    }

    @RequireMemberJwt
    @DeleteMapping("/members/{seq}")
    public String deregistMember(@PathVariable long seq, @RequestBody String password) {
        return membersSvc.deregistMember(seq, password).toString();
    }
}