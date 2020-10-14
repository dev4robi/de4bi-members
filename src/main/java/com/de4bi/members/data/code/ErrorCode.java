package com.de4bi.members.data.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ErrorCode {

    public static final String GC_OK        = "G0000"; // 정상 응답
    public static final String GC_FAIL      = "G0001"; // 실패 응답


    // Common (CC0)
    public static final String CC0_OK                   = "CC0000"; // 정상 응답
    public static final String CC0_ERROR                = "CC9999"; // 오류 응답

    // Members General (MG0)
    public static final String MG0_ERROR                = "MG0001"; // 회원 작업 오류 발생
    public static final String MG0_DUPLICATED           = "MG0002"; // 중복된 회원
    public static final String MG0_NO_SUCH_MEMBER       = "MG0003"; // 존재하지 않는 회원
    public static final String MG0_BAD_PASSWORD         = "MG0004"; // 비밀번호 불일치
    public static final String MG0_NOSMEM_OR_BADPW      = "MG0005"; // 존재하지 않는 회원이거나 비밀번호 불일치
    public static final String MG0_NO_PERMISSIONS       = "MG0006"; // 권한 부족
    public static final String MG0_DUPLICATED_NICKNAME  = "MG0007"; // 닉네임 중복
    public static final String MG0_BANNED_MEMBER        = "MG0008"; // 정지된 회원
    public static final String MG0_DEREGISTERED_MEMBER  = "MG0009"; // 탈퇴한 회원
    public static final String MG0_SLEEPING_MEMBER      = "MG0010"; // 휴면중 회원

    // Members Auth (MA0)
    public static final String MA0_ERROR                = "MA0001"; // 회원 인증 오류 발생
    public static final String MA0_JWT_ISSUE_FAIL       = "MA0002"; // JWT발급 실패
    public static final String MA0_JWT_VALIDATION_FAIL  = "MA0003"; // JWT인증 실패
    public static final String MA0_JWT_AUD_ERROR        = "MA0004"; // JWT사용처 오류

    // Members DB (MD0)
    public static final String MD0_ERROR                = "MD0001"; // DB작업 오류 발생
    public static final String MD0_INSERT_ERROR         = "MD0002"; // DB인서트 실패
    public static final String MD0_SELECT_ERROR         = "MD0003"; // DB셀렉트 실패
    public static final String MD0_UPDATE_ERROR         = "MD0004"; // DB업데이트 실패
    public static final String MD0_DELETE_ERROR         = "MD0005"; // DB딜리트 실패
}