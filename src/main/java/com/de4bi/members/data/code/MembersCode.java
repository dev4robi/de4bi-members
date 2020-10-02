package com.de4bi.members.data.code;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum MembersCode {

    // Authority
    MEMBERS_AUTHORITY_BASIC     (1, "MEMBERS_AUTHORITY","AU00","준회원"),
    MEMBERS_AUTHORITY_STANDARD  (2, "MEMBERS_AUTHORITY","AU01","정회원"),
    MEMBERS_AUTHORITY_PREMIUM   (3, "MEMBERS_AUTHORITY","AU02","프리미엄"),
    MEMBERS_AUTHORITY_MANAGER   (4, "MEMBERS_AUTHORITY","AU98","운영진"),
    MEMBERS_AUTHORITY_ADMIN     (5, "MEMBERS_AUTHORITY","AU99","관리자"),

    // Status
    MEMBERS_STATUS_NORMAL       (101, "MEMBERS_STATUS","ST00","일반"),
    MEMBERS_STATUS_SLEEP        (102, "MEMBERS_STATUS","ST01","휴면"),
    MEMBERS_STATUS_BANNED       (103, "MEMBERS_STATUS","ST02","정지"),
    MEMBERS_STATUS_DEREGISTER   (104, "MEMBERS_STATUS","ST03","탈퇴"),

    // AuthAgency
    MEMBERS_AUTHAGENCY_DE4BI  (10001, "MEMBERS_AUTHAGENCY","DE4BI" ,"자체"),
    MEMBERS_AUTHAGENCY_GOOOLE (10002, "MEMBERS_AUTHAGENCY","GOOGLE","구글"),
    MEMBERS_AUTHAGENCY_KAKAO  (10003, "MEMBERS_AUTHAGENCY","KAKAO" ,"카카오"),
    MEMBERS_AUTHAGENCY_NAVER  (10004, "MEMBERS_AUTHAGENCY","NAVER" ,"네이버");

    private final long   seq;       // 시퀀스
    private final String groups;    // 그룹
    private final String value;     // 코드값(영문)
    private final String name;      // 코드명(한글)

    private static final Map<Long, MembersCode> MEMBERS_CODE_MAP = new HashMap<>();

    static {
        // 모든 코드데이터를 시퀀스(seq)를 키로 하여 전역 맵에 보관
        for (MembersCode elem : MembersCode.class.getEnumConstants()) {
            MEMBERS_CODE_MAP.put(elem.seq, elem);
        }
    }

    /**
     * <p>MembersCode에서 코드 seq에 해당하는 value를 반환합니다.</p>
     * @param seq : 코드의 고유 시퀀스
     * @return 영문 코드값
     */
    public static String getValueFromSeq(long seq) {
        return MEMBERS_CODE_MAP.get(seq).value;
    }

    /**
     * <p>MembersCode에서 코드 seq에 해당하는 name을 반환합니다.</p>
     * @param seq : 코드의 고유 시퀀스
     * @return 한글 코드명
     */
    public static String getNameFromSeq(long seq) {
        return MEMBERS_CODE_MAP.get(seq).name;
    }
}