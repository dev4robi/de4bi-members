package com.de4bi.members.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.members.aop.ControllerAop;
import com.de4bi.members.data.code.ResponseCode;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;

public class MembersUtil {
    
    /**
     * <p>기능 수행을 위한 회원 권한을 검사합니다.</p>
     * @param membersDao : 검사할 회원 DAO
     * @param reqAuthOver : 최소로 필요한 권한
     * @return true: 권한 있음<li>false: 권한 없음</li>
     * @apiNote 권한 등급표
     * <ol><li><code>MembersCode.MEMBERS_AUTHORITY_BASIC (준회원)</code></li>
     * <li><code>MembersCode.MEMBERS_AUTHORITY_STANDARD (정회원)</code></li>
     * <li><code>MembersCode.MEMBERS_AUTHORITY_PREMIUM (프리미엄)</code></li>
     * <li><code>MembersCode.MEMBERS_AUTHORITY_MANAGER (운영진)</code></li>
     * <li><code>MembersCode.MEMBERS_AUTHORITY_ADMIN (관리자)</code></li></ol>
     */
    public static ApiResult<Void> checkMemberAuthority(MembersDao membersDao, MembersCode reqAuthOver) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        Objects.requireNonNull(reqAuthOver, "'reqAuthOver' is null!");
        
        final long minAuthSeq = MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq();
        final long reqAuthSeq = reqAuthOver.getSeq();
        final long maxAuthSeq = MembersCode.MEMBERS_AUTHORITY_ADMIN.getSeq();
        if (reqAuthSeq < MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq() ||
            reqAuthSeq > MembersCode.MEMBERS_AUTHORITY_ADMIN.getSeq()) {
            throw new IllegalArgumentException("'reqAuthSeq' must between '" +
                minAuthSeq + "' and '" + maxAuthSeq + "'! (reqAuthSeq: " + reqAuthSeq + ")");
        }

        final long myAuthSeq = membersDao.getAuthority();
        if (myAuthSeq < reqAuthSeq) {
            return ApiResult.of(false)
                .setCode(ResponseCode.M_NO_PERMISSION)
                .setMessage("No permission! (myAuthSeq: " + myAuthSeq + " / reqAuthSeq: " + reqAuthSeq + ")");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원의 로그인 가능 여부를 검사합니다.</p>
     * @param membersDao : 검사할 회원 DAO
     * @return true: 로그인 가능<li>false: 로그인 불가능</li>
     */
    public static ApiResult<Void> checkMemberLoginable(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");

        final long memberStatusSeq = membersDao.getStatus();
        if (memberStatusSeq == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            return ApiResult.of(false)
                .setCode(ResponseCode.M_BANNED_MEMBER)
                .setMessage("Banned member! (id: " + membersDao.getId() + ")");
        }
        else if (membersDao.getDeregisterDate() != null) {
            return ApiResult.of(false)
                .setCode(ResponseCode.M_DEREGISTED_MEMBER)
                .setMessage("Deregistred member! (id: " + membersDao.getId() + ")");
        }
        else if (memberStatusSeq == MembersCode.MEMBERS_STATUS_SLEEP.getSeq()) {
            return ApiResult.of(false)
                .setCode(ResponseCode.M_SLEEPING_MEMBER)
                .setMessage("Sleeping member! (id: " + membersDao.getId() + ")");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원의 회원가입 가능 여부를 검사합니다.</p>
     * @param membersDao : 검사할 회원 DAO (nullable)
     * @return true: 회원가입 가능, false: 회원가입 불가능
     */
    public static ApiResult<Void> checkMemberSigninable(MembersDao membersDao) {
        // 최초 가입인 경우
        if (membersDao == null) {
            return ApiResult.of(true);
        }

        // 탈퇴 이후로 1달 이내인 경우
        if (membersDao.getDeregisterDate() != null) {
            final Instant joinableTime = membersDao.getDeregisterDate().toInstant().plus(30L, ChronoUnit.DAYS);
            if (Instant.now().isBefore(joinableTime)) {
                final String joinableTimeStr = joinableTime.toString().replaceAll("[TZ]", " ");
                return ApiResult.of(false).setCode(ResponseCode.M_RECENTLY_DEREGISTERED).addMsgParam(joinableTimeStr)
                    .setMessage("Deregistred recently. (id: " + membersDao.getId() + ", Joinable after: " + joinableTimeStr + ")");
            }
        }
        else {
            // 탈퇴한적이 없는 경우
            return ApiResult.of(false).setCode(ResponseCode.M_DUPLICATED_EMAIL)
                .setMessage("Member already sign-in! (id: " + membersDao.getId() + ")");
        }

        // 기존에 가입했던 경우, 정지된 회원인지 확인
        final long memberStatusSeq = membersDao.getStatus();
        if (memberStatusSeq == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            return ApiResult.of(false).setCode(ResponseCode.M_BANNED_MEMBER)
                .setMessage("Banned member. Signin rejected! (id: " + membersDao.getId() + ")");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원의 서비스 제공가능 여부를 검사합니다.<p>
     * @param membersDao : 검사할 회원 DAO
     * @return true: 서비스 가능<li>false: 서비스 불가능</li>
     */
    public static ApiResult<Void> checkMemberServiceable(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");

        // 매니저등급 이상이면 로그인 가능여부에 상관 없이 서비스 수행 가능
        if (checkMemberAuthority(membersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult()) {
            return ApiResult.of(true);
        }

        return checkMemberLoginable(membersDao);
    }

    /**
     * <p>회원의 존재 여부를 검사합니다.</p>
     * @param membersDao : 검사할 회원 DAO
     * @return true: 회원 존재<li>false: 회원 미존재</li>
     * @apiNote <code>ThreadStorage.get(TSKEY_JWT_MEMBERS_DAO)</code>값으로 조회자의 권한을 확인합니다.
     */
    public static ApiResult<Void> checkMemberExist(MembersDao membersDao) {
        final MembersDao jwtMembersDao = (MembersDao) ThreadStorage.get(ControllerAop.TSKEY_JWT_MEMBERS_DAO);
        final boolean atleastManager = (jwtMembersDao != null ?
            checkMemberAuthority(jwtMembersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult() : false);
        
        if (membersDao == null) {
            return ApiResult.of(false)
                // 매니저권한 이상인 경우 회원정보 없음을, 일반 권한인 경우 권한없음을 반환 (보안)
                .setCode(atleastManager ? ResponseCode.M_NOT_EXIST_MEMBER : ResponseCode.M_NO_PERMISSION)
                .setMessage("No such member!");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원 일치여부를 검사합니다.</p>
     * @param firstDao : 첫 번째 회원 DAO
     * @param secondDao : 비교할 회원 DAO
     * @return true: 회원 일치<li>false: 회원 불일치</li>
     */
    public static ApiResult<Void> checkMemberSameSeq(MembersDao firstDao, MembersDao secondDao) {
        Objects.requireNonNull(firstDao, "'firstDao' is null!");
        Objects.requireNonNull(secondDao, "'secondDao' is null!");

        if (firstDao.getSeq() != secondDao.getSeq()) {
            return ApiResult.of(false)
                .setCode(ResponseCode.M_NO_PERMISSION)
                .setMessage("No permissions! (firstDao.seq: " + firstDao.getSeq() + ", secondDao.seq: " + secondDao.getSeq());
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원 비밀번호를 검사합니다.</p>
     * @param originPw : 회원 비밀번호 (nullable)
     * @param inputPw : 검사할 비밀번호
     * @return true: 비밀번호 일치<li>false: 비밀번호 불일치</li>
     * @apiNote <code>originPw</code>가 null인 경우, 비밀번호 검사를 true로 통과합니다.
     */
    public static ApiResult<Void> checkMemberPassword(String originPw, String inputPw) {
        // [Note] 이 메서드 내에서 비밀번호 전달 혹은 로깅으로 인한
        // 외부 노출이 발생하지 않도록 각별히 주의해야 합니다.
        if (originPw == null) {
            return ApiResult.of(true);
        }

        Objects.requireNonNull(inputPw, "'inputPw' is null!");

        if (originPw.equals(inputPw)) {
            return ApiResult.of(false)
                .setCode(ResponseCode.M_NOT_EXIST_OR_WRONG_PW)
                .setMessage("Wrong password!");
        }

        return ApiResult.of(true);
    }
}
