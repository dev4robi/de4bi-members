package com.de4bi.members.data.dao;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MembersDao {
    
    private long seq;           // 고유 시퀀스
    private String id;          // 아이디 (이메일/유니크)
    private String password;    // 비밀번호 (Salted + SHA256 Hashed)
    private String nickname;    // 별명 (닉네임/유니크)
    private String name;        // 이름
    private long authority;     // 권한
    private long status;        // 상태
    private int level;          // 레벨
    private int exp;            // 경험치
    private Date joinDate;      // (재)가입일자
    private Date lastLoginDate; // 마지막 로그인 일자
}