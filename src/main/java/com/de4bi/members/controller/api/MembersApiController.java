package com.de4bi.members.controller.api;

import com.de4bi.common.annotation.RequireMemberJwt;
import com.de4bi.common.data.ApiResult;
import com.de4bi.members.controller.dto.PutMemberBasicInfoReqDto;
import com.de4bi.members.controller.dto.SelectMemberInfoResDto;
import com.de4bi.members.service.MembersService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1"})
@Api
public class MembersApiController {
    
    private final MembersService membersSvc;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequireMemberJwt
    @GetMapping("/members")
    @ApiOperation(value = "회원 기본정보 획득", notes = "회원 기본정보를 획득합니다.")
    public ApiResult<SelectMemberInfoResDto> getMemberBasicInfo(
        @ApiParam(required = true, value = "조회를 시도하는 회원 JWT")
        @RequestHeader(name = "member_jwt") String memberJwt
    ) {
        return membersSvc.selectMemberBasicInfo();
    }

    
    @GetMapping("/members/{seq}")
    @RequireMemberJwt
    @ApiOperation(value = "회원 기본정보 획득", notes = "회원 기본정보를 획득합니다.")
    public ApiResult<SelectMemberInfoResDto> getMemberBasicInfo(
        @ApiParam(required = true, value = "조회를 시도하는 회원 JWT")
        @RequestHeader(name = "member_jwt") String memberJwt,
        @ApiParam(required = true, value = "회원 시퀀스")
        @PathVariable long seq
    ) {
        return membersSvc.selectMemberBasicInfo(null, seq, null, null);
    }

    @RequireMemberJwt
    @PutMapping("/members/{seq}")
    @ApiOperation(value = "회원정보 수정", notes = "회원정보를 수정합니다.")
    public ApiResult<Void> putMemberBasicInfo(
        @ApiParam(required = true, value = "조회를 시도하는 회원 JWT")
        @RequestHeader(name = "member_jwt") String memberJwt,
        @ApiParam(required = true, value = "회원 시퀀스")
        @PathVariable long seq,
        @ApiParam(required = true, value = "수정할 회원 정보")
        @RequestBody PutMemberBasicInfoReqDto reqDto
    ) {
        return membersSvc.updateMemberInfo(
            seq, reqDto.getOldPassword(), reqDto.getNewPassword(), reqDto.getNickname(), reqDto.getName());
    }

    @RequireMemberJwt
    @DeleteMapping("/members/{seq}")
    @ApiOperation(value = "회원 탈퇴", notes = "회원 탈퇴를 수행합니다.")
    public ApiResult<Void> deregistMember(
        @ApiParam(required = true, value = "조회를 시도하는 회원 JWT")
        @RequestHeader(name = "member_jwt") String memberJwt,
        @ApiParam(required = true, value = "회원 시퀀스")
        @PathVariable long seq,
        @ApiParam(required = true, value = "계정 비밀번호")
        @RequestBody String password
    ) {
        return membersSvc.deregistMember(seq, password);
    }

    @PostMapping("/members/login")
    @ApiOperation(value = "일반 로그인", notes = "일반 로그인을 수행합니다.")
    public ApiResult<String> login(
        @ApiParam(required = true, value = "아이디(이메일)")
        @RequestBody String id,
        @ApiParam(required = true, value = "비밀번호")
        @RequestBody String password,
        @ApiParam(required = true, value = "사용처")
        @RequestBody String audience,
        @ApiParam(required = true, value = "로그인 유지 여부")
        @RequestBody boolean keepLoggedIn
    ) {
        return membersSvc.login(id, password, audience, keepLoggedIn);
    }
}