package com.de4bi.members.service;

import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.SecurityUtil;
import com.de4bi.common.util.MemberJwtUtil;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.AllArgsConstructor;

/**
 * 발급된 JWT의 유효성을 검사하거나 JWT발급에 관련된 서비스입니다.
 */
@AllArgsConstructor
@Service
public class MemberJwtService {

    private final SecureProperties secureProps;
    private final MembersMapper membersMapper;

    /**
     * <p>토큰이 유효한지, 토큰에서 획득한 멤버가 유효한(활동 가능한)지를 검사합니다.</p>
     * @param memberJwt - 검사할 멤버JWT.
     * @return {@link ApiResult}를 반환합니다.
     * @throws 토큰 검증에 실패한 경우, {@link ApiException}을 반환합니다.
     */
    public ApiResult<?> validateMemberJwt(String memberJwt) {
        Objects.requireNonNull(memberJwt, "'memberJwt' is null!");

        // JWT 검사
        final Jws<Claims> jws = MemberJwtUtil.validate(memberJwt, secureProps.getMemberJwtSecret(), null);

        // Member 검사
        final String id = jws.getBody().getId();
        checkMemberLoginable(id, null);

        return ApiResult.of(true);
    }

    /**
     * <p>토큰이 유효한지, 토큰에서 획득한 멤버가 유효한(활동 가능한)지, 관리자인지를 검사합니다.</p>
     * @param adminJwt - 검사할 멤버JWT.
     * @return {@link ApiResult}를 반환합니다.
     * @throws 토큰 검증에 실패한 경우, {@link ApiException}을 반환합니다.
     */
    public ApiResult<?> validateAdminJwt(String adminJwt) {
        Objects.requireNonNull(adminJwt, "'adminJwt' is null!");

        // JWT 검사
        final Jws<Claims> jws = MemberJwtUtil.validate(adminJwt, secureProps.getMemberJwtSecret(), null);

        // Member 검사
        final String id = jws.getBody().getId();
        final MembersDao selectedMembersDao = checkMemberLoginable(id, null);

        // 추가 검사
        if (selectedMembersDao.getAuthority() != MembersCode.MEMBERS_AUTHORITY_MANAGER.getSeq() &&
            selectedMembersDao.getAuthority() != MembersCode.MEMBERS_AUTHORITY_ADMIN.getSeq()) {
            throw new ApiException("해당 기능을 수행할 권한이 없습니다.").setInternalMsg("Unauthorized member. (id: " + id + ")");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>전달받은 정보로 MemberJwt를 발급합니다.</p>
     * @param id : 맴버 아이디.
     * @param password : 맴버 비밀번호. (nullable:소셜로그인의 경우)
     * @param expiredIn : 토큰 유지시간. (1000L -> 1초 후 만료)
     */
    public ApiResult<String> issueMemberJwt(String id, String password, long expiredIn) {
        Objects.requireNonNull(id, "'id' is null!");

        // Member 로그인 가능여부 검사
        final MembersDao selectedMembersDao = checkMemberLoginable(id, password);

        // MemberJwt 생성
        final long curTime = System.currentTimeMillis();
        final MemberJwtUtil.JwtClaims jwtClaims = MemberJwtUtil.JwtClaims.builder()
            .id(ThreadStorage.getStr(ApiResult.KEY_TID))    // jid(JWT 식별자) = tid
            .subject(selectedMembersDao.getId())            // sub : 맴버 아이디
            .issuer("members.de4bi.com")                    // iss : 맴버서버
            .audience("*.de4bi.com")                        // aud : 모든 de4bi 플랫폼
            .issuedAt(curTime)                              // iat : 발급시간
            .expiration(curTime + expiredIn)                // exp : 만료시간
            .notBefore(curTime)                             // nbf : 시작시간
            .build();

        return ApiResult.of(true, null, MemberJwtUtil.issue(null, jwtClaims, secureProps.getMemberJwtSecret()));
    }

    ////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////
    
    /**
     * <p>맴버의 로그인 가능상태를 반환합니다.</p>
     * @param membersDao : 검사할 맴버 Dao.
     * @param id : 맴버 아이디.
     * @param password : 맴버 비밀번호. (nullable:비밀번호 검사를 생략합니다)
     * @return 성공 시 해당 맴버의 정보가 담긴 {@link MembersDao}를 반환하고,
     * 실패 시 실패 내용이 담긴 {@link ApiException}을 반환합니다.
     * @apiNote DB에 접근하기 위해 {@link MembersMapper}의 {@code selectById()}를 사용합니다.
     */
    private MembersDao checkMemberLoginable(String id, String password) {
        final MembersDao selectedMembersDao = membersMapper.selectById(id);
        if (selectedMembersDao == null) {
            throw new ApiException("존재하지 않는 회원이거나 비밀번호가 틀립니다.").setInternalMsg("Member not exist! (id: " + id + ")");
        }

        if (password != null) {
            if (selectedMembersDao.getPassword() != SecurityUtil.passwordSecureHashing(password, secureProps.getMemberPasswordSalt())) {
                throw new ApiException("존재하지 않는 회원이거나 비밀번호가 틀립니다.").setInternalMsg("Wrong password! (id: " + id + ")");
            }
        }

        if (selectedMembersDao.getStatus() == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            throw new ApiException("사용 정지된 회원입니다. dev4robi@gmail.com으로 문의하십시오.").setInternalMsg("Banned member! (id: " + id + ")");
        }

        return selectedMembersDao;
    }
}