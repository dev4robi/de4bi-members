package com.de4bi.members.data.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum MembersCode {

    // Authority
    MEMBERS_AUTHORITY_BASIC     ("MEMBERS_AUTHORITY","AU00","준회원"),
    MEMBERS_AUTHORITY_STANDARD  ("MEMBERS_AUTHORITY","AU01","일반"),
    MEMBERS_AUTHORITY_PREMIUM   ("MEMBERS_AUTHORITY","AU02","프리미엄"),
    MEMBERS_AUTHORITY_MANAGER   ("MEMBERS_AUTHORITY","AU98","운영진"),
    MEMBERS_AUTHORITY_ADMIN     ("MEMBERS_AUTHORITY","AU99","관리자"),

    // Status
    MEMBERS_STATUS_NORMAL       ("MEMBERS_STATUS","ST00","일반"),
    MEMBERS_STATUS_SLEEP        ("MEMBERS_STATUS","ST01","휴면"),
    MEMBERS_STATUS_BANNED       ("MEMBERS_STATUS","ST02","정지"),
    MEMBERS_STATUS_DEREGISTER   ("MEMBERS_STATUS","ST03","탈퇴");

    private final String groups;
    private final String value;
    private final String name;
}