package com.de4bi.members.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PutMembersDto {
    
    private String password;
    private String nickname;
    private String name;
}