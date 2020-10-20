package com.de4bi.members.controller.api;

import com.de4bi.common.annotation.RequireMemberJwt;
import com.de4bi.common.data.ApiResult;
import com.de4bi.members.controller.dto.PutMemberBasicInfoReqDto;
import com.de4bi.members.controller.dto.SelectMemberInfoResDto;
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
    @GetMapping("/members")
    public ApiResult<SelectMemberInfoResDto> getMemberBasicInfo() {
        return membersSvc.selectMemberBasicInfo();
    }

    @RequireMemberJwt
    @GetMapping("/members/{seq}")
    public ApiResult<SelectMemberInfoResDto> getMemberBasicInfo(@PathVariable long seq) {
        return membersSvc.selectMemberBasicInfo(null, seq, null, null);
    }

    @RequireMemberJwt
    @PutMapping("/members/{seq}")
    public ApiResult<Void> putMemberBasicInfo(
        @PathVariable long seq,
        @RequestBody PutMemberBasicInfoReqDto reqDto
    ) {
        return membersSvc.updateMemberInfo(
            seq, reqDto.getOldPassword(), reqDto.getNewPassword(), reqDto.getNickname(), reqDto.getName());
    }

    @RequireMemberJwt
    @DeleteMapping("/members/{seq}")
    public ApiResult<Void> deregistMember(@PathVariable long seq, @RequestBody String password) {
        return membersSvc.deregistMember(seq, password);
    }
}