package com.de4bi.members.service.oauth;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class OAuthDto {
    
    private String email;
    private String name;
    private Map<String, Object> etcMap;
}