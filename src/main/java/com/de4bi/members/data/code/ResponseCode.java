package com.de4bi.members.data.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ResponseCode {

    // A(API)
    public static final String A_SUCCESS                = "A0000"; // 정상 응답
    public static final String A_FAIL                   = "A0001"; // 실패 응답
    public static final String A_ERROR                  = "A9999"; // 오류 응답

    // M(회원)
    public static final String M_SUCCESS                = "M0000"; // 회원 관련 작업 성공
    public static final String M_FAIL                   = "M0001"; // 회원 관련 작업 실패
    public static final String M_DUPLICATED_MEMBER      = "M0002"; // 이미 가입된 회원
    public static final String M_NOT_EXIST_MEMBER       = "M0003"; // 존재하지 않는 회원
    public static final String M_WRONG_PW               = "M0004"; // 비밀번호 불일치
    public static final String M_NOT_EXIST_OR_WRONG_PW  = "M0005"; // 존재하지 않거나 비밀번호 불일치
    public static final String M_NO_PERMISSION          = "M0006"; // 권한 부족
    public static final String M_DUPLICATED_EMAIL       = "M0007"; // 이메일 중복
    public static final String M_DUPLICATED_NICKNAME    = "M0008"; // 닉네임 중복
    public static final String M_BANNED_MEMBER          = "M0009"; // 정책상 정지된 회원
    public static final String M_DEREGISTED_MEMBER      = "M0010"; // 탈퇴한 회원
    public static final String M_SLEEPING_MEMBER        = "M0011"; // 휴면중 회원
    public static final String M_CHAGNE_INFO_FAILED     = "M0012"; // 회원정보 수정 실패
    public static final String M_DEREGISTER_FAILED      = "M0013"; // 회원탈퇴 실패
    public static final String M_RECENTLY_DEREGISTERED  = "M0014"; // 최근 회원탈퇴
    public static final String M_REQUIRE_PW_CHANGE      = "M0015"; // 비밀번호 변경 필요
    public static final String M_REQUIRE_PW_REGISTER    = "M0016"; // 비밀번호 등록 필요
    public static final String M_ERROR                  = "M9999"; // 회원 관련 오류 발생

    // MA(회원 인증)
    public static final String MA_SUCCESS               = "MA000"; // 회원 인증 성공
    public static final String MA_FAIL                  = "MA001"; // 회원 인증 실패
    public static final String MA_JWT_ISSUE_FAIL        = "MA002"; // JWT발급 실패
    public static final String MA_JWT_VALIDATION_FAIL   = "MA003"; // JWT인증 실패
    public static final String MA_JWT_INVAILD_AUD       = "MA004"; // JWT사용처(audience) 오류
    public static final String MA_ERROR                 = "MA999"; // 회원 인증관련 오류 발생

    // MD(회원 DB)
    public static final String DB_SUCCESS               = "DB000"; // DB작업 성공
    public static final String DB_FAIL                  = "DB001"; // DB작업 실패
    public static final String DB_INSERT_FAIL           = "DB002"; // DB인서트 실패
    public static final String DB_SELECT_FAIL           = "DB003"; // DB셀렉트 실패
    public static final String DB_UPDATE_FAIL           = "DB004"; // DB업데이트 실패
    public static final String DB_DELETE_FAIL           = "DB005"; // DB딜리트 실패
    public static final String DB_ERROR                 = "DB999"; // DB관련 오류 발생
}