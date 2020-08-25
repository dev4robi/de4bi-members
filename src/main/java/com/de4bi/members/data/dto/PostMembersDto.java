package com.de4bi.members.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PostMembersDto {
    
    private String id;
    private String password;
    private String nickname;
    private String name;
    private long authAgency;
}