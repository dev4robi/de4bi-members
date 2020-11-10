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
import com.de4bi.members.aop.ControllerAop;
import com.de4bi.members.controller.dto.SelectMemberInfoResDto;
import com.de4bi.members.controller.dto.SigninMembersDto;
import com.de4bi.members.controller.dto.SocialSigninMembersDto;
import com.de4bi.members.data.code.ResponseCode;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;
import com.de4bi.members.util.MembersUtil;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
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
public class MembersService {

    ////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////
    
    private final MembersMapper membersMapper;
    private final SecureProperties secureProps;
    private final Environment env;

    private static final String ENVKEY_MEMBER_JWT_DEFAULT_AUDIENCE =
        "member.jwt.default-aud"; // MemberJwt 기본 audience
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
     * @return true: 회원이 존재하는 경우 {@link MembersDao}를 함께 반환<li>false: 회원이 존재하지 않는 경우</li>
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
            
            return ApiResult.of(false, MembersDao.class).setMessage(extMsg);
        }
        
        return ApiResult.of(true, MembersDao.class).setData(selDao);
    }

    /**
     * <strong>[Query Method]</strong>
     * <p>회원을 추가합니다.</p>
     * @param membersDao : 추가할 회원 정보
     * @return true: 회원 추가 성공<li>false: 회원 추가 실패</li>
     */
    public ApiResult<Void> insert(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        final boolean rtRst = (membersMapper.insert(membersDao) != 0);
        return ApiResult.of(rtRst)
            .setMessage(rtRst == false ? "Fail to insert member. (seq: " + membersDao.getSeq() + ")" : null);
    }

    /**
     * <strong>[Query Method]</strong>
     * <p>회원 정보를 수정합니다.</p>
     * @param membersDao : 수정할 회원 정보
     * @return true: 회원정보 수정 성공<li>false: 회원정보 수정 실패</li>
     * @apiNote <code>membersDao.seq</code>값은 수정할 대상의 <code>seq</code>값을 의미합니다.
     */
    public ApiResult<Void> update(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        final boolean rtRst = (membersMapper.update(membersDao) != 0);
        return ApiResult.of(rtRst)
            .setMessage(rtRst == false ? "Fail to update member. (seq: " + membersDao.getSeq() + ")" : null);
    }

    /**
     * <strong>[Query Method]</strong>
     * <p>회원 정보를 삭제합니다.</p>
     * @param seq : 삭제할 회원 시퀀스
     * @return true: 회원정보 삭제 성공<li>false: 회원정보 삭제 실패</li>
     */
    public ApiResult<Void> delete(long seq) {
        final boolean rtRst = (membersMapper.delete(seq) != 0);
        return ApiResult.of(rtRst)
            .setMessage(rtRst == false ? "Fail to delete member. (seq: " + rtRst + ")" : null);
    }

    ////////////////////////////////////////////////////////////////
    // Public methods
    ////////////////////////////////////////////////////////////////

    /**
     * <p>MemberJwt를 발급합니다.</p>
     * @param id : 회원 아이디
     * @param audience : 사용 가능한 기관 <code>(null -> "*.de4bi.com")</code>
     * @param expSec : 초 단위 만료 시간 <code>(10L -> 10초 후 만료)</code>
     * @return true: 발급 성공 <code>(data: member_jwt)</code><li>false: 발급 실패</li>
     * @throws {@link io.jsonwebtoken.JwtException.JwtException} : 토큰 발급 중 예외 발생 시
     */
    public ApiResult<String> issueMemberJwt(String id, String audience, long expSec) {
        Objects.requireNonNull(id, "'id' is null!");

        if (StringUtil.isEmpty(audience)) {
            audience = env.getProperty(ENVKEY_MEMBER_JWT_DEFAULT_AUDIENCE);
        }

        final long curTime = System.currentTimeMillis() / 1000L;
        final MemberJwtUtil.JwtClaims jwtClaims = MemberJwtUtil.JwtClaims.builder()
            .id(ThreadStorage.getStr(ApiResult.KEY_TID))    // jid(JWT 식별자) = tid
            .subject(id)                                    // sub : 멤버 아이디
            .issuer("members.de4bi.com")                    // iss : 멤버서버
            .audience(audience)                             // aud : 사용 가능한 서버 도메인
            .issuedAt(curTime)                              // iat : 발급시간(초 단위)
            .expiration(curTime + expSec)                   // exp : 만료시간(초 단위)
            .notBefore(curTime)                             // nbf : 시작시간(초 단위)
            .build();
        final String memberJwt = MemberJwtUtil.issue(null, jwtClaims, secureProps.getMemberJwtSecret());
        
        return ApiResult.of(true, String.class).setData(memberJwt);
    }

    /**
     * <p>MemberJwt를 검증합니다.</p>
     * @param memberJwt : 검사할 <code>member_jwt</code>문자열
     * @param audience : 사용처 도메인 <code>(null -> "*.de4bi.com")</code>
     * @return true: 검증 성공 시 검증된 유저의 정보<code>(data: member_dao)</code>
     * <li>false: 검증 실패</li>
     * @apiNote 반환 데이터를 위해 내부적으로 <code>select(seq, id, nickname)메서드를 사용합니다.</code>
     */
    public ApiResult<MembersDao> validateMemberJwt(String memberJwt, String audience) {
        Objects.requireNonNull(memberJwt, "'memberJwt' is null!");

        if (StringUtil.isEmpty(audience)) {
            audience = env.getProperty(ENVKEY_MEMBER_JWT_DEFAULT_AUDIENCE);
        }

        // JWT 검증
        final Jws<Claims> jws = MemberJwtUtil.validate(memberJwt, secureProps.getMemberJwtSecret(), null);

        // 사용처(aud) 비교
        boolean isSameAud = true;
        final String jwtAud = jws.getBody().getAudience();
        final String[] jwtAudSplit = jwtAud.split(".");
        final String[] audenceSplit = audience.split(".");
        if (jwtAudSplit.length != audenceSplit.length) {
            isSameAud = false;
        }
        else {
            String firstStr = null;
            String secondStr = null;
            for (int i = 0; i < jwtAudSplit.length; ++i) {
                if ((firstStr = audenceSplit[i]).equals("*") || // '*' : 와일드카드
                    (secondStr = jwtAudSplit[i]).equals("*")) {
                    continue;
                }
                else if (firstStr.equals(secondStr) == false) {
                    isSameAud = false;
                    break;
                }
            }
        }
        
        if (isSameAud == false) {
            return ApiResult.of(false, MembersDao.class)
                .setCode(ResponseCode.MA_JWT_INVAILD_AUD)
                .setMessage("No same audience! (audience: " + audience + " / " + " jwtAud: " + jwtAud + ")");
        }

        // 로그인 가능여부 조회
        final String jwtSub = jws.getBody().getSubject();
        final MembersDao loginMemberDao = select(0L, jwtSub, null).getData();
        ApiResult<Void> tempRst = null;
        if ((tempRst = MembersUtil.checkMemberLoginable(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, MembersDao.class);
        }

        return ApiResult.of(true, MembersDao.class).setData(loginMemberDao);
    }

    /**
     * <p>소셜로 회원가입을 시도합니다.</p>
     * @param membersDto : 신규 회원정보 DTO
     * @return true: 회원가입 성공<li>false: 회원가입 실패</li>
     */
    public ApiResult<Void> socialSignin(SocialSigninMembersDto membersDto) {
        Objects.requireNonNull(membersDto, "'membersDto' is null!");

        final String newId = membersDto.getId();
        final MembersDao selMembersDao = select(0L, newId, null).getData();

        ApiResult<Void> tempRst = MembersUtil.checkMemberExist(selMembersDao);
        if ((tempRst = MembersUtil.checkMemberSigninable(selMembersDao)).getResult() == false) {
            return tempRst;
        }

        final boolean isRejoinMember = (tempRst.getResult() ? true : false);
        final MembersDao singinMembersDao = MembersDao.builder()
            .seq(isRejoinMember ? selMembersDao.getSeq() : 0L)
            .id(membersDto.getId())
            .password(null)
            .nickname(membersDto.getNickname())
            .name(membersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .authAgency(membersDto.getAuthAgency())
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .deregisterDate(null)
            .build();

        return isRejoinMember ? update(singinMembersDao) : insert(singinMembersDao);
    }

    /**
     * <p>일반 회원가입을 시도합니다.</p>
     * @param membersDto : 신규 회원정보 DTO
     * @return true: 회원가입 성공<li>false: 회원가입 실패</li>
     */
    public ApiResult<Void> signin(SigninMembersDto membersDto) {
        Objects.requireNonNull(membersDto, "'membersDto' is null!");

        final String newId = membersDto.getId();
        final MembersDao selMembersDao = select(0L, newId, null).getData();
        
        ApiResult<Void> tempRst = MembersUtil.checkMemberExist(selMembersDao);
        final boolean isRejoinMember = (tempRst.getResult() ? true : false);
        
        if ((tempRst = MembersUtil.checkMemberSigninable(selMembersDao)).getResult() == false) {
            return tempRst;
        }

        final MembersDao singinMembersDao = MembersDao.builder()
            .seq(isRejoinMember ? selMembersDao.getSeq() : 0L)    
            .id(membersDto.getId())
            .password(null)
            .nickname(membersDto.getNickname())
            .name(membersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .authAgency(MembersCode.MEMBERS_AUTHAGENCY_DE4BI.getSeq())
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .deregisterDate(null)
            .build();

        return isRejoinMember ? update(singinMembersDao) : insert(singinMembersDao);
    }

    /**
     * <p>소셜 로그인을 시도합니다.</p>
     * @param id : 아이디
     * @param audience : 사용처 도메인 (nullable)
     * @param isKeepLoggedIn : 로그인 유지 여부
     * @return true: 로그인 성공 <code>(data: member_jwt)</code><li>false: 로그인 실패</li>
     */
    public ApiResult<String> socialLogin(String id, String audience, boolean isKeepLoggedIn) {
        Objects.requireNonNull(id, "'id' is null!");

        final MembersDao loginMemberDao = select(0L, id, null).getData();

        // 회원 존재여부 검사
        ApiResult<?> tempRst = MembersUtil.checkMemberExist(loginMemberDao);
        if (tempRst.getResult() == false) {
            return ApiResult.of(tempRst, String.class);
        }

        // 로그인 가능여부 확인
        if ((tempRst = MembersUtil.checkMemberLoginable(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class).setData(null);
        }

        // JWT 발급
        final long expSec = isKeepLoggedIn
            ? Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN))
            : Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS));
        final ApiResult<String> rtRst = issueMemberJwt(id, audience, expSec);
        if (rtRst.getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ResponseCode.MA_JWT_ISSUE_FAIL);
        }

        // 로그인 일자 갱신
        loginMemberDao.setLastLoginDate(Date.from(Instant.now()));
        if ((tempRst = update(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ResponseCode.MA_JWT_ISSUE_FAIL);
        }

        return rtRst;
    }

    /**
     * <p>일반 로그인을 시도합니다.</p>
     * @param id : 아이디
     * @param password : 비밀번호
     * @param audience : 사용처 도메인 (nullable)
     * @param isKeepLoggedIn : 로그인 유지 여부
     * @return true: 로그인 성공 <code>(data: member_jwt)</code><li>false: 로그인 실패</li>
     */
    public ApiResult<String> login(String id, String password, String audience, boolean isKeepLoggedIn) {
        Objects.requireNonNull(id, "'id' is null!");
        Objects.requireNonNull(password, "'password' is null!");

        final MembersDao loginMemberDao = select(0L, id, null).getData();

        // 회원 존재여부 검사
        ApiResult<?> tempRst = MembersUtil.checkMemberExist(loginMemberDao);
        if (tempRst.getResult() == false) {
            return ApiResult.of(tempRst, String.class);
        }

        // 비밀번호 확인
        final String memberPw = loginMemberDao.getPassword();
        final String saltedPw = SecurityUtil.passwordSecureHashing(password, secureProps.getMemberPasswordSalt());
        if ((tempRst = MembersUtil.checkMemberPassword(memberPw, saltedPw)).getResult() == false) {
            return ApiResult.of(tempRst, String.class);
        }

        // 로그인 가능여부 확인
        if ((tempRst = MembersUtil.checkMemberLoginable(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class).setData(null);
        }

        // JWT 발급
        final long expSec = isKeepLoggedIn
            ? Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN))
            : Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS));
        final ApiResult<String> rtRst = issueMemberJwt(id, audience, expSec);
        if (rtRst.getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ResponseCode.MA_JWT_ISSUE_FAIL);
        }

        // 로그인 일자 갱신
        loginMemberDao.setLastLoginDate(Date.from(Instant.now()));
        if ((tempRst = update(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ResponseCode.MA_JWT_ISSUE_FAIL);
        }

        return rtRst;
    }

    /**
     * <p>멤버 정보를 조회하여 반환합니다.</p>
     * @return true: 조회 성공 (data: {@link SelectMemberInfoResDto})<li>false: 조회 실패</li>
     * @apiNote <code>ThreadStorage.get(TSKEY_JWT_MEMBERS_DAO)</code>값의 맴버를 조회합니다.
     */
    public ApiResult<SelectMemberInfoResDto> selectMemberBasicInfo() {
        final MembersDao jwtMembersDao = (MembersDao) ThreadStorage.get(ControllerAop.TSKEY_JWT_MEMBERS_DAO);
        return selectMemberBasicInfo(jwtMembersDao, 0L, null, null);
    }

    /**
     * <p>멤버 정보를 조회하여 반환합니다.</p>
     * @param membersDao : 조회 대상 멤버 DAO <code>(null -> select(seq, id, nickname)수행)</code>
     * @param seq : 조회대상 시퀀스 (optional)
     * @param id : 조회대상 아이디 (optional)
     * @param nickname : 조회대상 닉네임 (optional)
     * @return true: 조회 성공 (data: {@link SelectMemberInfoResDto})<li>false: 조회 실패</li>
     */
    public ApiResult<SelectMemberInfoResDto> selectMemberBasicInfo(MembersDao membersDao, long seq, String id, String nickname) {
        final MembersDao jwtMembersDao = (MembersDao) ThreadStorage.get(ControllerAop.TSKEY_JWT_MEMBERS_DAO);
        final MembersDao selMembersDao = (membersDao == null ? select(seq, id, nickname).getData() : membersDao);
        final boolean isAdminAuthority = MembersUtil.checkMemberAuthority(jwtMembersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult();

        // 회원 존재여부 검사
        ApiResult<?> tempRst = MembersUtil.checkMemberExist(selMembersDao);
        if (tempRst.getResult() == false) {
            return ApiResult.of(tempRst, SelectMemberInfoResDto.class);
        }

        // 관리자 권한이 아닌 경우 추가 검사
        if (isAdminAuthority == false) {
            // 요청자 일치여부 검사
            tempRst = MembersUtil.checkMemberSameSeq(jwtMembersDao, selMembersDao);
            if (isAdminAuthority == false && tempRst.getResult() == false) {
                return ApiResult.of(tempRst, SelectMemberInfoResDto.class);
            }
        }

        // 조회결과 생성
        final SelectMemberInfoResDto rtDto = SelectMemberInfoResDto.builder()
            .seq(selMembersDao.getSeq())
            .id(selMembersDao.getId())
            .name(selMembersDao.getName())
            .nickname(selMembersDao.getNickname())
            .status(MembersCode.getNameFromSeq(selMembersDao.getStatus()))
            .authority(MembersCode.getNameFromSeq(selMembersDao.getAuthority()))
            .authAgency(MembersCode.getNameFromSeq(selMembersDao.getAuthAgency()))
            .joinDate(StringUtil.format(selMembersDao.getJoinDate()))
            .lastLoginDate(StringUtil.format(selMembersDao.getLastLoginDate()))
            .build();

        return ApiResult.of(true, SelectMemberInfoResDto.class).setData(rtDto);
    }

    /**
     * <p>회원 정보를 수정합니다.</p>
     * @param seq : 수정할 회원의 시퀀스
     * @param oldPassword : 기존 비밀번호 (nullable)
     * @param newPassword : 신규 비밀번호 (nullable)
     * @param nickname : 신규 닉네임
     * @param name : 신규 이름
     * @return true: 수정 성공<li>false: 수정 실패</li>
     */
    public ApiResult<Void> updateMemberInfo(long seq, String oldPassword, String newPassword, String nickname, String name) {
        final MembersDao jwtMembersDao = (MembersDao) ThreadStorage.get(ControllerAop.TSKEY_JWT_MEMBERS_DAO);
        final MembersDao selMembersDao = select(seq, null, null).getData();
        final boolean isAdminAuthority = MembersUtil.checkMemberAuthority(jwtMembersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult();

        // 회원 존재여부 검사
        ApiResult<Void> tempRst = MembersUtil.checkMemberExist(selMembersDao);
        if (tempRst.getResult() == false) {
            return tempRst;
        }

        // 관리자 권한이 아닌 경우 추가 검사
        if (isAdminAuthority == false) {
            // 요청자 일치여부 검사
            tempRst = MembersUtil.checkMemberSameSeq(jwtMembersDao, selMembersDao);
            if (tempRst.getResult() == false) {
                return tempRst;
            }

            // 비밀번호 일치 검사
            final String selPw = selMembersDao.getPassword();
            final String inputPw = SecurityUtil.passwordSecureHashing(oldPassword, secureProps.getMemberPasswordSalt());
            if ((tempRst = MembersUtil.checkMemberPassword(selPw, inputPw)).getResult() == false) {
                return tempRst;
            }
        }

        // 닉네임 중복검사
        if (nickname != null) {
            final MembersDao dupChkNicknameMembersDao = select(0L, null, nickname).getData();
            if (dupChkNicknameMembersDao != null && selMembersDao.getSeq() != dupChkNicknameMembersDao.getSeq()) {
                return ApiResult.of(false)
                    .setCode(ResponseCode.M_DUPLICATED_NICKNAME)
                    .setMessage("Duplicated nickname! (nickname: " + nickname + ")");
            }
        }

        // 업데이트 수행 (변경할 값으로 null을 전달받은 경우 기존값을 그대로 사용)
        final MembersDao updMembersDao = selMembersDao;
        final String updPassword = 
            (newPassword == null 
                ? selMembersDao.getPassword()
                : SecurityUtil.passwordSecureHashing(newPassword, secureProps.getMemberPasswordSalt()));
        final String updNickname = Optional.ofNullable(nickname).orElse(selMembersDao.getNickname());
        final String updName = Optional.ofNullable(name).orElse(selMembersDao.getName());

        updMembersDao.setPassword(updPassword);
        updMembersDao.setNickname(updNickname);
        updMembersDao.setName(updName);
        
        if (membersMapper.update(updMembersDao) != 1) {
            return ApiResult.of(false).setCode(ResponseCode.M_CHAGNE_INFO_FAILED)
                .setMessage("Fail to update! (seq: " + seq + ")");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원 탈퇴를 수행합니다.</p>
     * @param seq : 회원 시퀀스
     * @param password : 계정 비밀번호
     * @return true: 탈퇴 성공<li>false: 탈퇴 실패</li>
     * @apiNote 회원 탈퇴 시 DB에서 바로 삭제되지 않고, 탈퇴 상태<code>(MembersCode.MEMBERS_STATUS_DEREGISTER)</code>로 변경됩니다.
     */
    public ApiResult<Void> deregistMember(long seq, String password) {
        Objects.requireNonNull(password, "'password' is null!");
        
        final MembersDao jwtMembersDao = (MembersDao) ThreadStorage.get(ControllerAop.TSKEY_JWT_MEMBERS_DAO);
        final MembersDao selMembersDao = select(seq, null, null).getData();
        final boolean isAdminAuthority = MembersUtil.checkMemberAuthority(jwtMembersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult();

        // 회원 존재여부 검사
        ApiResult<Void> tempRst = MembersUtil.checkMemberExist(selMembersDao);
        if (tempRst.getResult() == false) {
            return tempRst;
        }

        // 관리자 권한이 아닌 경우 추가 검사
        if (isAdminAuthority == false) {
            // 요청자 일치여부 검사
            tempRst = MembersUtil.checkMemberSameSeq(jwtMembersDao, selMembersDao);
            if (tempRst.getResult() == false) {
                return tempRst;
            }

            // 비밀번호 일치 검사
            final String selPw = selMembersDao.getPassword();
            final String inputPw = SecurityUtil.passwordSecureHashing(password, secureProps.getMemberPasswordSalt());
            if ((tempRst = MembersUtil.checkMemberPassword(selPw, inputPw)).getResult() == false) {
                return tempRst;
            }
        }

        // 업데이트 수행
        selMembersDao.setDeregisterDate(Date.from(Instant.now()));

        if (membersMapper.update(selMembersDao) != 1) {
            return ApiResult.of(false).setCode(ResponseCode.M_DEREGISTER_FAILED)
                .setMessage("Fail to update! (seq: " + seq + ")");
        }

        return ApiResult.of(true);
    }
}