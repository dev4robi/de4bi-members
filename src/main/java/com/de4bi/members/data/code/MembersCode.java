package com.de4bi.members.data.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum MembersCode {

    MEMBERS_STATUS_NORMAL("G_MEMBERS","S100","일반");

    private final String groups;
    private final String value;
    private final String name;

    
}