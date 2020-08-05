package com.de4bi.members.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PutMembersDto {
    
    private String password;
    private String nickname;
    private String name;
}