package com.de4bi.members.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PostMembersDto {
    
    private String id;
    private String password;
    private String nickname;
    private String name;
}