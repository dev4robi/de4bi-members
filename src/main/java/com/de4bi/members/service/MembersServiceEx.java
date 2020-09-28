package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.MemberJwtUtil;
import com.de4bi.common.util.SecurityUtil;
import com.de4bi.common.util.StringUtil;
import com.de4bi.members.controller.dto.PostMembersDto;
import com.de4bi.members.controller.dto.PutMembersDto;
import com.de4bi.members.controller.dto.SelectMemberInfoResDto;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.AllArgsConstructor;

/**
 * Members에 대한 서비스입니다.
 */
@PropertySource("config.properties")
@AllArgsConstructor
@Service
public class MembersServiceEx {

    ////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////
    
    private final MembersMapper membersMapper;
    private final SecureProperties secureProps;
    private final Environment env;

    public static final String TSKEY_JWT_MEMBERS_DAO =
        "JWT_MEMBERS_DAO"; // ThreadStorage에서 인증된 MemberJwt의 MemberDao를 저장하기위한 키값

    private static final String ENVKEY_MEMBER_JWT_EXPIRED_IN_MS =
        "member.jwt.expired-hour"; // MemberJwt 기본 로그인 만료시간
    private static final String ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN =
        "member.jwt.expired-hour-keeploggedin"; // MemberJwt 로그인 유지옵션 시 만료시간

    ////////////////////////////////////////////////////////////////
    // Query methods
    ////////////////////////////////////////////////////////////////
    
    /**
     * <strong>[Query Method]</strong>
     * <p>회원을 조회합니다.</p>
     * @param seq : 조회할 회원의 시퀀스 (1이상)
     * @param id : 조회활 회원의 아이디
     * @param nickname : 조회할 회원의 닉네임
     * @return true: 회원이 존재하는 경우 {@link MembersDao}를 함께 반환
     * <li>false: 회원이 존재하지 않는 경우</li>
     * @apiNote 세 파라미터중 1개의 파라미터만 전달하면 나머지는 null(0L)을 허용합니다.
     * 조회 순서는 <code>seq, id, nickname</code>순서입니다.
     */
    public ApiResult<MembersDao> select(long seq, String id, String nickname) {
        MembersDao selDao = null;
        int paramSwitch = -1;
        
        if (seq > 0L) {
            selDao = membersMapper.select(seq);
            paramSwitch = 0;
        }
        else if (id != null) {
            selDao = membersMapper.selectById(id);
            paramSwitch = 1;
        }
        else if (nickname != null) {
            selDao = membersMapper.selectByNickname(nickname);
            paramSwitch = 2;
        }
        else {
            throw ApiException.of().setInternalMsg(
                "Illegal parameter! (seq: " + seq + ", id: " + id + "nickname: " + nickname + ")");
        }
        
        if (selDao == null) {
            String extMsg = "Fail to select member.";
            if (paramSwitch == 0) {
                extMsg = extMsg + " (seq: " + seq + ")";
            }
            else if (paramSwitch == 1) {
                extMsg = extMsg + " (id: " + id + ")";
            }
            else if (paramSwitch == 2) {
                extMsg = extMsg + " (nickname: " + nickname + ")";
            }
            
            return ApiResult.of(false, extMsg, null);
        }
        
        return ApiResult.of(true, null, selDao);
    }

    /**
     * <strong>[Query Method]</strong>
     * <p>회원을 추가합니다.</p>
     * @param membersDao : 추가할 회원 정보
     * @return true: 회원 추가 성공
     * <li>false: 회원 추가 실패</li>
     */
    public ApiResult<Void> insert(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        final boolean rtRst = (membersMapper.insert(membersDao) != 0);
        return ApiResult.of(
            rtRst, rtRst == false ? "Fail to insert member. (seq: " + membersDao.getSeq() + ")" : null);
    }

    /**
     * <strong>[Query Method]</strong>
     * <p>회원 정보를 수정합니다.</p>
     * @param membersDao : 수정할 회원 정보
     * @return true: 회원정보 수정 성공
     * <li>false: 회원정보 수정 실패</li>
     * @apiNote <code>membersDao.seq</code>값은 수정할 대상의 <code>seq</code>값을 의미합니다.
     */
    public ApiResult<Void> update(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        final boolean rtRst = (membersMapper.update(membersDao) != 0);
        return ApiResult.of(
            rtRst, rtRst == false ? "Fail to update member. (seq: " + membersDao.getSeq() + ")" : null);
    }

    /**
     * <strong>[Query Method]</strong>
     * <p>회원 정보를 삭제합니다.</p>
     * @param seq : 삭제할 회원 시퀀스
     * @return true: 회원정보 삭제 성공
     * <li>false: 회원정보 삭제 실패</li>
     */
    public ApiResult<Void> delete(long seq) {
        final boolean rtRst = (membersMapper.delete(seq) != 0);
        return ApiResult.of(
            rtRst, rtRst == false ? "Fail to delete member. (seq: " + rtRst + ")" : null);
    }

    ////////////////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    // Public methods
    ////////////////////////////////////////////////////////////////
}