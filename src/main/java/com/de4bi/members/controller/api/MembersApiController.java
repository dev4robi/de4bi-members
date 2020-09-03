package com.de4bi.members.controller.api;

import java.util.Map;

import com.de4bi.common.annotation.RequireAdminJwt;
import com.de4bi.common.annotation.RequireMemberJwt;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.data.dto.PutMembersDto;
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
    
    private final MembersService membersSvc;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/members")
    public String postMembers(@RequestBody PostMembersDto postMembersDto) {
        return membersSvc.insert(postMembersDto).toString();
    }

    @RequireMemberJwt
    @GetMapping("/members/{seq}")
    public String getMembers(@PathVariable long seq) {
        return membersSvc.rawSelect(seq).toString();
    }

    @RequireMemberJwt
    @PutMapping("/members/{seq}")
    public String putMembers(
        @PathVariable long seq,
        @RequestBody PutMembersDto putMembersDto) {
        return membersSvc.updateMemberInfo(seq, putMembersDto).toString();
    }

    @RequireAdminJwt
    @DeleteMapping("/members/{seq}")
    public String deleteMembers(@PathVariable long seq) {
        return membersSvc.rawDelete(seq).toString();
    }
}