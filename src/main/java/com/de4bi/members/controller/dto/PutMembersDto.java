package com.de4bi.members.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PutMembersDto {
    
    private long seq;
    private String oldPassword;
    private String newPassword;
    private String nickname;
    private String name;
}