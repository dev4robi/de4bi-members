package com.de4bi.members.controller.api;

import com.de4bi.common.annotation.RequireAdminJwt;
import com.de4bi.common.annotation.RequireMemberJwt;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.members.controller.dto.PostMembersDto;
import com.de4bi.members.controller.dto.PutMembersDto;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.service.MembersService;

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

    @RequireAdminJwt
    @PostMapping("/members")
    public String postMembers(@RequestBody PostMembersDto postMembersDto) {
        return membersSvc.insert(postMembersDto).toString();
    }

    @RequireMemberJwt
    @GetMapping("/members/{seq}")
    public String getMemberBasicInfo(@PathVariable long seq) {
        return membersSvc.selectMemberInfo(seq).toString();
    }

    @RequireMemberJwt
    @GetMapping("/members")
    public String getMemberBasicInfo() {
        return membersSvc.selectMemberInfo(
            (MembersDao) ThreadStorage.get(MembersService.TSKEY_JWT_MEMBERS_DAO)).toString();
    }

    @RequireMemberJwt
    @PutMapping("/members")
    public String putMembers(
        @RequestBody PutMembersDto putMembersDto) {
        return membersSvc.updateMemberInfo(putMembersDto).toString();
    }

    @RequireAdminJwt
    @DeleteMapping("/members/{seq}")
    public String deleteMembers(@PathVariable long seq) {
        return membersSvc.rawDelete(seq).toString();
    }
}