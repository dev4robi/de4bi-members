package com.de4bi.members.service;

import java.util.Objects;

import com.de4bi.common.data.ApiResult;
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
        final MembersDao selectedMembersDao = membersMapper.selectById(jws.getBody().getId());
        
        if (selectedMembersDao == null) {
            throw new ApiException("존재하지 않는 회원입니다.");
        }

        if (selectedMembersDao.getStatus() == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            throw new ApiException("사용 정지된 회원입니다. dev4robi@gmail.com으로 문의하십시오.");
        }        

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
        final MembersDao selectedMembersDao = membersMapper.selectById(jws.getBody().getId());
        
        if (selectedMembersDao == null) {
            throw new ApiException("존재하지 않는 회원입니다.");
        }

        if (selectedMembersDao.getStatus() == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            throw new ApiException("사용 정지된 회원입니다. dev4robi@gmail.com으로 문의하십시오.");
        }

        if (selectedMembersDao.getAuthority() != MembersCode.MEMBERS_AUTHORITY_MANAGER.getSeq() &&
            selectedMembersDao.getAuthority() != MembersCode.MEMBERS_AUTHORITY_ADMIN.getSeq()) {
            throw new ApiException("해당 기능을 수행할 권한이 없습니다.");
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
        Objects.requireNonNull(password, "'password' is null!");

        // Member 검사
        final MembersDao selectedMembersDao = membersMapper.selectById(id);
        
        if (selectedMembersDao == null) {
            throw new ApiException("존재하지 않는 회원이거나 비밀번호가 틀립니다.", "Member not exist! (id: " + id + ")");
        }

        if (selectedMembersDao.getPassword() != SecurityUtil.passwordSecureHashing(password, secureProps.getMemberPasswordSalt())) {
            throw new ApiException("존재하지 않는 회원이거나 비밀번호가 틀립니다.", "Wrong password! (id: " + id + ")");
        }

        if (selectedMembersDao.getStatus() == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            throw new ApiException("사용 정지된 회원입니다. dev4robi@gmail.com으로 문의하십시오.", "Banned member! (id: " + id + ")");
        }

        // 여기부터 계속... @@
        // 이제 어떤 포멧으로 MemberJwt를 생성할지 고민해 보자.


        return null;
    }
}