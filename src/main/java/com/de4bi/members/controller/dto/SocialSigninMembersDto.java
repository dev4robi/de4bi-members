package com.de4bi.members.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SocialSigninMembersDto {
    
    private String id;
    private String nickname;
    private String name;
    private long authAgency;
}