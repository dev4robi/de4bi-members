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
import com.de4bi.members.controller.dto.SigninMembersDto;
import com.de4bi.members.controller.dto.SocialSigninMembersDto;
import com.de4bi.members.data.code.ErrorCode;
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
import io.jsonwebtoken.JwtException;
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
    // Private methods
    ////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    // Public methods
    ////////////////////////////////////////////////////////////////

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
    public ApiResult<Void> checkMemberAuthority(MembersDao membersDao, MembersCode reqAuthOver) {
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
                .setCode(ErrorCode.MG0_NO_PERMISSION)
                .setMessage("No permission! (myAuthSeq: " + myAuthSeq + " / reqAuthSeq: " + reqAuthSeq + ")");
        }

        return ApiResult.of(true);
    }

    /**
     * <p>회원의 로그인 가능 여부를 검사합니다.<p>
     * @param membersDao : 검사할 회원 DAO
     * @return true: 로그인 가능<li>false: 로그인 불가능</li>
     */
    public ApiResult<Void> checkMemberLoginable(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");

        if (membersDao.getStatus() == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            return ApiResult.of(false)
                .setCode(ErrorCode.MG0_BANNED_MEMBER)
                .setMessage("Banned member! (id: " + membersDao.getId() + ")");
        }

        return ApiResult.of(true);
    }

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
                .setCode(ErrorCode.MA0_JWT_AUD_ERROR)
                .setMessage("No same audience! (audience: " + audience + " / " + " jwtAud: " + jwtAud + ")");
        }

        // 로그인 가능여부 조회
        final String jwtSub = jws.getBody().getSubject();
        final MembersDao loginMemberDao = select(0L, jwtSub, null).getData();
        ApiResult<Void> tempRst = null;
        if ((tempRst = checkMemberLoginable(loginMemberDao)).getResult() == false) {
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
        if (select(0L, newId, null).getResult()) {
            return ApiResult.of(false)
                .setCode(ErrorCode.MG0_DUPLICATED)
                .setMessage("Duplicated id! (newId: " + newId + ")");
        }

        final MembersDao newMembersDao = MembersDao.builder()
            .id(membersDto.getId())
            .password(null)
            .nickname(membersDto.getNickname())
            .name(membersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .authAgency(membersDto.getAuthAgency())
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .build();

        return insert(newMembersDao);
    }

    /**
     * <p>일반 회원가입을 시도합니다.</p>
     * @param membersDto : 신규 회원정보 DTO
     * @return true: 회원가입 성공<li>false: 회원가입 실패</li>
     */
    public ApiResult<Void> signin(SigninMembersDto membersDto) {
        Objects.requireNonNull(membersDto, "'membersDto' is null!");

        final String newId = membersDto.getId();
        if (select(0L, newId, null).getResult()) {
            return ApiResult.of(false)
                .setCode(ErrorCode.MG0_DUPLICATED)
                .setMessage("Duplicated id! (newId: " + newId + ")");
        }

        final MembersDao newMembersDao = MembersDao.builder()
            .id(membersDto.getId())
            .password(SecurityUtil.passwordSecureHashing(membersDto.getPassword(), secureProps.getMemberPasswordSalt()))
            .nickname(membersDto.getNickname())
            .name(membersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .authAgency(MembersCode.MEMBERS_AUTHAGENCY_DE4BI.getSeq())
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .build();

        return insert(newMembersDao);
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

        // 회원 존재여부 확인
        final MembersDao loginMemberDao = select(0L, id, null).getData();
        if (loginMemberDao == null) {
            return ApiResult.of(false, String.class)
                .setCode(ErrorCode.MG0_NO_SUCH_MEMBER)
                .setMessage("No such member! (id: " + id + ")");
        }

        // 로그인 가능여부 확인
        ApiResult<Void> tempRst = null;
        if ((tempRst = checkMemberLoginable(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class).setData(null);
        }

        // JWT 발급
        final long expSec = isKeepLoggedIn
            ? Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN))
            : Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS));
        final ApiResult<String> rtRst = issueMemberJwt(id, audience, expSec);
        if (rtRst.getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ErrorCode.MA0_JWT_ISSUE_FAIL);
        }

        // 로그인 일자 갱신
        loginMemberDao.setLastLoginDate(Date.from(Instant.now()));
        if ((tempRst = update(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ErrorCode.MA0_JWT_ISSUE_FAIL);
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

        // 회원 존재여부 확인
        final MembersDao loginMemberDao = select(0L, id, null).getData();
        if (loginMemberDao == null) {
            return ApiResult.of(false, String.class)
                .setCode(ErrorCode.MG0_NO_SUCH_MEMBER)
                .setMessage("No such member! (id: " + id + ")");
        }

        // 비밀번호 확인
        final String memberPw = loginMemberDao.getPassword();
        final String inputPw = SecurityUtil.passwordSecureHashing(password, secureProps.getMemberPasswordSalt());
        if (inputPw.equals(memberPw) == false) {
            return ApiResult.of(false, String.class)
                .setCode(ErrorCode.MG0_BAD_PASSWORD)
                .setMessage("Wrong password! (id: " + id + ")");
        }

        // 로그인 가능여부 확인
        ApiResult<Void> tempRst = null;
        if ((tempRst = checkMemberLoginable(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class).setData(null);
        }

        // JWT 발급
        final long expSec = isKeepLoggedIn
            ? Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN))
            : Long.parseLong(env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS));
        final ApiResult<String> rtRst = issueMemberJwt(id, audience, expSec);
        if (rtRst.getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ErrorCode.MA0_JWT_ISSUE_FAIL);
        }

        // 로그인 일자 갱신
        loginMemberDao.setLastLoginDate(Date.from(Instant.now()));
        if ((tempRst = update(loginMemberDao)).getResult() == false) {
            return ApiResult.of(tempRst, String.class)
                .setCode(ErrorCode.MA0_JWT_ISSUE_FAIL);
        }

        return rtRst;
    }
}

// JWT검증까지 작업 완료.
// 이후, 멤버 조회, 수정 작업 이어서 한 다음
// 컨트롤러단에서 이어붙여야 한다.
// Aop에서 ErrorCode랑 @ReqMemberJwt등 다루는 부분 수정은 덤! @@