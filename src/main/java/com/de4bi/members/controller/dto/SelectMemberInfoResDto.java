package com.de4bi.members.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class SelectMemberInfoResDto {
 
    String id;              // 아이디(메일)
    String name;            // 이름(실명)
    String nickname;        // 닉네임
    String status;          // 멤버 상태
    String authority;       // 권한
    String joinDate;        // yyyy-MM-dd HH:mm:ss
    String lastLoginDate;   // yyyy-MM-dd HH:mm:ss
}