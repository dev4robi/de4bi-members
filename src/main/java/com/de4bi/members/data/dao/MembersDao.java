package com.de4bi.members.data.dao;

import java.sql.Date;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MembersDao {
    
    private long seq;           // 고유 시퀀스
    private String id;          // 아이디
    private String password;    // 비밀번호 (SALTED + SHA-256)
    private String nickname;    // 별명 (닉네임/유니크)
    private String name;        // 이름
    private String autority;    // 권한
    private String status;      // 레벨
    private int level;          // 경험치
    private int exp;            // 계정상태
    private Date joinDate;      // 가입일자 (재가입시 갱신)
    private Date lastLoginDate; // 마지막 로그인 일자
}