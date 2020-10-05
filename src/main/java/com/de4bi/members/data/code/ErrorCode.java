package com.de4bi.members.data.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ErrorCode {

    // Members General (MG0)
    public static final String MG0_ERROR                = "MG0001"; // 회원 작업 오류 발생
    public static final String MG0_DUPLICATED           = "MG0002"; // 중복된 회원
    public static final String MG0_NO_SUCH_MEMBER       = "MG0003"; // 존재하지 않는 회원
    public static final String MG0_BAD_PASSWORD         = "MG0004"; // 비밀번호 불일치
    public static final String MG0_NOSMEM_OR_BADPW      = "MG0005"; // 존재하지 않는 회원이거나 비밀번호 불일치
    public static final String MG0_BANNED_MEMBER        = "MG0006"; // 정지된 회원
    public static final String MG0_NO_PERMISSIONS       = "MG0007"; // 권한 부족
    public static final String MG0_DUPLICATED_NICKNAME  = "MG0008"; // 닉네임 중복

    // Members Auth (MA0)
    public static final String MA0_ERROR                = "MA0001"; // 회원 인증 오류 발생
    public static final String MA0_JWT_ISSUE_FAIL       = "MA0002"; // JWT발급 실패
    public static final String MA0_JWT_AUD_ERROR        = "MA0003"; // JWT사용처 오류

    // Members DB (MD0)
    public static final String MD0_ERROR                = "MD0001"; // DB작업 오류 발생
    public static final String MD0_FAIL                 = "MD0002"; // DB쿼리 실패
}