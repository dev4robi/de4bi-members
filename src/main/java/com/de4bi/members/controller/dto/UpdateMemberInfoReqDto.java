package com.de4bi.members.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UpdateMemberInfoReqDto {
 
    long seq;               // 시퀀스
    String oldPassword;     // 이전 비밀번호
    String newPassword;     // 신규 비밀번호
    String nickname;        // 닉네임
    String name;            // 이름(실명)
}
