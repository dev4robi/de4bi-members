package com.de4bi.members.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PutMembersDto {
    
    private long seq;
    private String password;
    private String nickname;
    private String name;
}