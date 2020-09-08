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
public class MembersService {

    ////////////////////////////////////////////////////////////////
    // class fields
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
    // private methods
    ////////////////////////////////////////////////////////////////
    
    /**
     * <p>멤버의 로그인 가능상태를 반환합니다.</p>
     * @param selectedMembersDao : 선택된 멤버.
     * @param inputPassword : 입력한 비밀번호. (nullable)
     * @return 성공 시 true, 실패 시 실패 내용이 담긴 {@link ApiException}을 반환합니다.
     * @apiNote ※주의! 비밀번호에 null을 전달할 경우, 비밀번호 검사를 하지 않습니다. (member_jwt검증에 사용)
     */
    private boolean checkMemberLoginable(MembersDao selectedMembersDao, String inputPassword) {
        if (selectedMembersDao == null) {
            throw new ApiException("존재하지 않는 회원이거나 비밀번호가 틀립니다.")
                .setInternalMsg("'selectedMembersDao' is null!");
        }

        if (inputPassword != null) {
            final String securePassword = SecurityUtil.passwordSecureHashing(inputPassword, secureProps.getMemberPasswordSalt());
            if (selectedMembersDao.getPassword().equals(securePassword) == false) {
                throw new ApiException("존재하지 않는 회원이거나 비밀번호가 틀립니다.")
                            .setInternalMsg("Wrong password! (id: " + selectedMembersDao.getId() + ")");
            }
        }

        if (selectedMembersDao.getStatus() == MembersCode.MEMBERS_STATUS_BANNED.getSeq()) {
            throw new ApiException("사용 정지된 회원입니다. dev4robi@gmail.com으로 문의하십시오.")
                        .setInternalMsg("Banned member! (id: " + selectedMembersDao.getId() + ")");
        }

        return true;
    }

    /**
     * <p>기능 수행을 위한 멤버의 권한을 검사합니다.</p>
     * @param selectedMembersDao : 선택된 멤버.
     * @param requiredAuthOver : 최소로 필요한 권한.
     * @return 성공 시 true, 실패 시 실패 내용이 담긴 {@link ApiException}을 반환합니다.
     */
    private boolean checkMemberAuthority(MembersDao selectedMembersDao, MembersCode requiredAuthOver) {
        Objects.requireNonNull(selectedMembersDao, "'selectedMemberDao' is null!");
        Objects.requireNonNull(requiredAuthOver, "'requiredAuthOver' is null!");

        if (selectedMembersDao.getAuthority() < requiredAuthOver.getSeq()) {
            throw new ApiException("해당 기능을 수행할 권한이 없습니다.")
                        .setInternalMsg("No permission! (memberAuthority: " +
                            selectedMembersDao.getAuthority() + ", required: " + requiredAuthOver.getSeq() + ")");
        }

        return true;
    }

    /**
     * <p>기능 수행을 위한 멤버의 권한이 {@code MembersCode.MEMBERS_AUTHORITY_MANAGER}이상인지를 검사합니다.</p>
     * @param selectedMembersDao : 선택된 멤버.
     * @return 성공 시 true, 실패 시 실패 내용이 담긴 {@link ApiException}을 반환합니다.
     */
    private boolean checkManagerAuthority(MembersDao selectedMembersDao) {
        return checkMemberAuthority(selectedMembersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER);
    }

    ////////////////////////////////////////////////////////////////
    // public methods [RAW API]
    ////////////////////////////////////////////////////////////////

    /**
     * <p><strong>[RAW API]</strong> 멤버를 DB에 추가합니다.</p>
     * @param membersDao : 추가될 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawInsert(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMapper.insert(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p><strong>[RAW API]</strong> 멤버를 DB에서 조회합니다.</p>
     * @param id : 조회할 멤버의 아이디.
     * @return {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawSelect(String id) {
        Objects.requireNonNull(id, "'id' is null!");
        final MembersDao selectedMembersDao = membersMapper.selectById(id);
        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * <p>[RAW API] 멤버 정보를 DB에서 수정합니다.
     * @param membersDao : 수정할 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawUpdate(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMapper.update(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p>[RAW API] 멤버를 DB에서 삭제합니다.</p>
     * @param seq : 삭제할 멤버의 seq값.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawDelete(long seq) {
        membersMapper.delete(seq);
        return ApiResult.of(true);
    }

    ////////////////////////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////////////////////////

    /**
     * <p>신규 멤버를 DB에 추가합니다.</p>
     * <p>소셜로그인을 사용한 경우 비밀번호는 null로 저장됩니다.</p>
     * <p>de4bi자체 회원가입시, password는 {@code passwordSecureHashing()}를 통해 해싱되어 저장됩니다.</p>
     * @param postMembersDto : 새로 추가될 멤버 정보
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> insert(PostMembersDto postMembersDto) {
        Objects.requireNonNull(postMembersDto, "'postMembersDto' is null!");

        // [Note] 인증기관이 de4bi인 경우(자체가입) 비밀번호는 null
        final long authAgencySeq = postMembersDto.getAuthAgency();
        final String password = (MembersCode.MEMBERS_AUTHAGENCY_DE4BI.getSeq() == authAgencySeq
            ? SecurityUtil.passwordSecureHashing(postMembersDto.getPassword(), secureProps.getMemberPasswordSalt())
            : null);

        final MembersDao insertedMembersDao = MembersDao.builder()
            .id(postMembersDto.getId())
            .password(password)
            .nickname(postMembersDto.getNickname())
            .name(postMembersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .authAgency(authAgencySeq)
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .build();

        membersMapper.insert(insertedMembersDao);
        return ApiResult.of(true);
    }

    /**
     * <p><strong>[RAW API]</strong> 멤버를 DB에서 조회합니다.</p>
     * @param seq : 조회할 멤버의 시퀀스 번호.
     * @return {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawSelect(long seq) {
        if (seq < 0L) {
            throw new IllegalArgumentException("'seq' less then zero! (seq: " + seq + ")");
        }
        final MembersDao selectedMembersDao = membersMapper.select(seq);
        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * <p>멤버 정보를 조회하여 반환합니다.</p>
     * @param seq : 정보를 조회할 멤버의 고유 시퀀스
     * @return 성공 시 {@link ApiResult}<{@link SelectMemberInfoResDto}>를 반환합니다.
     */
    public ApiResult<SelectMemberInfoResDto> selectMemberInfo(long seq) {
        final MembersDao jwtMembersDao = (MembersDao) ThreadStorage.get(TSKEY_JWT_MEMBERS_DAO);
        final MembersDao selectedMemberDao = rawSelect(seq).getData();

        if (selectedMemberDao == null) {
            final String extMsg = checkManagerAuthority(jwtMembersDao)
                ? "존재하지 않는 회원입니다."
                : "해당 기능을 수행할 권한이 없습니다.";
            throw new ApiException(extMsg)
            .setInternalMsg("'selectedMemberDao' is null! (seq: " + seq + ")");
        }

        if (jwtMembersDao.getSeq() != selectedMemberDao.getSeq() && checkManagerAuthority(jwtMembersDao) == false) {
            // 관리자 권한이 아니면서, 타인의 정보를 조회하려고 할 경우
            throw new ApiException("해당 기능을 수행할 권한이 없습니다.")
                .setInternalMsg("No permission! (jwtMembersDao.authority: " +
                                jwtMembersDao.getAuthority() + ", jwtMembersDao.seq: " +
                                jwtMembersDao.getSeq() + "selectedMemberDao.seq: " + selectedMemberDao + ")");
        }

        final SelectMemberInfoResDto rtDto = SelectMemberInfoResDto.builder()
            .id(selectedMemberDao.getId())
            .name(selectedMemberDao.getName())
            .nickname(selectedMemberDao.getNickname())
            .status(MembersCode.getNameFromSeq(selectedMemberDao.getStatus()))
            .authority(MembersCode.getNameFromSeq(selectedMemberDao.getAuthority()))
            .joinDate(StringUtil.format(selectedMemberDao.getJoinDate()))
            .lastLoginDate(StringUtil.format(selectedMemberDao.getLastLoginDate()))
            .build();

        return ApiResult.of(true, rtDto);
    }

    /**
     * <p>멤버 존재 여부, 권한, 닉네임 중복검사를 수행 후 멤버 정보를 DB에서 수정합니다.</p>
     * {@code putMembersDto}내부 값 중, null을 전달받은 값은 기존 정보와 동일하게 수정합니다.
     * @param seq : 수정할 멤버의 seq값.
     * @param putMembersDto : 수정할 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> updateMemberInfo(long seq, PutMembersDto putMembersDto) {
        Objects.requireNonNull(putMembersDto, "'putMembersDto' is null!");

        // 존재여부 검사
        final MembersDao seletedMembersDao = rawSelect(seq).getData();
        if (seletedMembersDao == null) {
            throw new ApiException(HttpStatus.ACCEPTED, "존재하지 않는 회원입니다.");
        }

        // 권한 검사 (본인이 아닌 경우 매니저권한 이상 필요)
        if (seq != seletedMembersDao.getSeq()) {
            checkMemberAuthority(seletedMembersDao, MembersCode.MEMBERS_AUTHORITY_MANAGER);
        }

        // 닉네임 중복검사
        if (Objects.nonNull(putMembersDto.getNickname())) {
            final MembersDao duplicatedNicknameMembersDao = membersMapper.selectByNickname(putMembersDto.getNickname());
            if (duplicatedNicknameMembersDao != null) {
                throw new ApiException(HttpStatus.ACCEPTED, "'" + putMembersDto.getNickname() + "'은(는) 이미 존재하는 닉네임입니다.");
            }
        }

        // 업데이트 수행 (변경할 값으로 null을 전달받은 경우 기존값을 그대로 사용)
        final MembersDao updatedMembersDao = seletedMembersDao;
        final String newPassword = 
            Objects.isNull(putMembersDto.getPassword())
                ? seletedMembersDao.getPassword()
                : SecurityUtil.passwordSecureHashing(putMembersDto.getPassword(), secureProps.getMemberPasswordSalt());
        final String newNickname = Optional.ofNullable(putMembersDto.getNickname()).orElse(seletedMembersDao.getNickname());
        final String newName = Optional.ofNullable(putMembersDto.getName()).orElse(seletedMembersDao.getName());

        updatedMembersDao.setPassword(newPassword);
        updatedMembersDao.setNickname(newNickname);
        updatedMembersDao.setName(newName);
        membersMapper.update(updatedMembersDao);
        return ApiResult.of(true);
    }

    /**
     * <p>회원가입 및 로그인을 수행합니다.</p>
     * @param postMembersDto : 회원가입 정보.
     * @param isSocialLogin : 소셜로 회원가입 여부.
     * @return 성공 시, {@link ApiResult}에 MemberJwt문자열을 담아서 응답합니다.
     * @apiNote 내부적으로 {@code MembersService.login()}을 호출합니다.
     */
    public ApiResult<String> signin(PostMembersDto postMembersDto, boolean isSocialSignin) {
        Objects.requireNonNull(postMembersDto, "'postMembersDto' is null!");

        // 회원정보 검색
        final String id = postMembersDto.getId();
        if (this.rawSelect(id).getData() != null) {
            // 가입정보가 있는 경우 소셜로그인인지 확인, 아닌 경우(자체가입) 중복가입 거부
            if (postMembersDto.getAuthAgency() == MembersCode.MEMBERS_AUTHAGENCY_DE4BI.getSeq()) {
                throw new ApiException(StringUtil.quote(id) + "는 이미 가입된 이메일입니다.");
            }
        }
        else {
            // 가입정보가 없는 경우 신규회원 추가
            if (this.insert(postMembersDto).getResult() == false) {
                throw new ApiException("회원 가입 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            }
        }

        return login(id, postMembersDto.getPassword(), false, isSocialSignin); // 자동으로 로그인 결과 반환
    }

    /**
     * <p>로그인을 수행합니다.</p>
     * @param id : 로그인할 아이디.
     * @param password : 비밀번호.
     * @param isKeepLoggedIn : 로그인 유지여부.
     * @param isSocialLogin : 소셜 로그인 여부.
     * @return 성공 시, {@link ApiResult}에 MemberJwt문자열을 담아서 응답합니다.
     */
    public ApiResult<String> login(String id, String password, boolean isKeepLoggedIn, boolean isSocialLogin) {
        Objects.requireNonNull(id, "'id' is null!");
        
        // 멤버 조회
        final MembersDao selectedMembersDao = rawSelect(id).getData();

        // 로그인 가능여부 검사 (소셜 로그인시 생략)
        if (isSocialLogin == false) {
            checkMemberLoginable(selectedMembersDao, password);
        }
        
        // 토큰 발급
        final String rtMemberJwt = issueMemberJwt(id, env.getProperty(
            isKeepLoggedIn ? ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN : ENVKEY_MEMBER_JWT_EXPIRED_IN_MS, Long.class)
        ).getData();

        // 마지막으로 로그인한 시간 업데이트
        selectedMembersDao.setLastLoginDate(Date.from(Instant.now()));
        rawUpdate(selectedMembersDao);

        return ApiResult.of(true, null, rtMemberJwt);
    }

    /**
     * <p>토큰이 유효한지, 토큰에서 획득한 멤버가 유효한(활동 가능한)지를 검사합니다.</p>
     * @param memberJwt : 검사할 멤버JWT.
     * @return 성공 시 검증된 유저의 {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     * @throws 토큰 검증에 실패한 경우, {@link ApiException}을 반환합니다.
     * @apiNote 검증 성공 시 {@link ThreadStorage}에 {@code TSKEY_JWT_MEMBERS_DAO}를 키로 검증된 유저의 정보를 저장합니다.
     */
    public ApiResult<MembersDao> validateMemberJwt(String memberJwt) {
        Objects.requireNonNull(memberJwt, "'memberJwt' is null!");

        // JWT 검사
        final Jws<Claims> jws = MemberJwtUtil.validate(memberJwt, secureProps.getMemberJwtSecret(), null);

        // 멤버 조회
        final String id = jws.getBody().getSubject();
        final MembersDao selectedMembersDao = rawSelect(id).getData();
        checkMemberLoginable(selectedMembersDao, null);

        // 스레드 스토레지에 저장
        ThreadStorage.put(TSKEY_JWT_MEMBERS_DAO, selectedMembersDao);

        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * <p>토큰이 유효한지, 토큰에서 획득한 멤버가 유효한(활동 가능한)지, 관리자인지를 검사합니다.</p>
     * @param adminJwt : 검사할 멤버JWT.
     * @return {@link ApiResult}를 반환합니다.
     * @throws 토큰 검증에 실패한 경우, {@link ApiException}을 반환합니다.
     * @apiNote 검증 성공 시 {@link ThreadStorage}에 {@code TSKEY_JWT_MEMBERS_DAO}를 키로 검증된 유저의 정보를 저장합니다.
     */
    public ApiResult<?> validateAdminJwt(String adminJwt) {
        Objects.requireNonNull(adminJwt, "'adminJwt' is null!");

        // JWT 검사
        final Jws<Claims> jws = MemberJwtUtil.validate(adminJwt, secureProps.getMemberJwtSecret(), null);

        // 멤버 조회
        final String id = jws.getBody().getId();
        final MembersDao selectedMembersDao = rawSelect(id).getData();

        // 추가 검사
        if (selectedMembersDao.getAuthority() != MembersCode.MEMBERS_AUTHORITY_MANAGER.getSeq() &&
            selectedMembersDao.getAuthority() != MembersCode.MEMBERS_AUTHORITY_ADMIN.getSeq()) {
            throw new ApiException("해당 기능을 수행할 권한이 없습니다.").setInternalMsg("Unauthorized member. (id: " + id + ")");
        }

        // 스레드 스토레지에 저장
        ThreadStorage.put(TSKEY_JWT_MEMBERS_DAO, selectedMembersDao);

        return ApiResult.of(true);
    }

    /**
     * <p>전달받은 정보로 MemberJwt를 발급하고 마지막 로그인한 시간을 갱신합니다.</p>
     * @param id : 멤버 아이디.
     * @param expiredIn : 토큰 유지시간. (1000L -> 1초 후 만료, 초 단위로 입력)
     */
    public ApiResult<String> issueMemberJwt(String id, long expiredIn) {
        Objects.requireNonNull(id, "'id' is null!");

        // MemberJwt 생성
        final long curTime = System.currentTimeMillis() / 1000L;
        final MemberJwtUtil.JwtClaims jwtClaims = MemberJwtUtil.JwtClaims.builder()
            .id(ThreadStorage.getStr(ApiResult.KEY_TID))    // jid(JWT 식별자) = tid
            .subject(id)                                    // sub : 멤버 아이디
            .issuer("members.de4bi.com")                    // iss : 멤버서버
            .audience("*.de4bi.com")                        // aud : 모든 de4bi 플랫폼
            .issuedAt(curTime)                              // iat : 발급시간(초 단위)
            .expiration(curTime + (expiredIn / 1000L))      // exp : 만료시간(초 단위)
            .notBefore(curTime)                             // nbf : 시작시간(초 단위)
            .build();

        return ApiResult.of(true, null, MemberJwtUtil.issue(null, jwtClaims, secureProps.getMemberJwtSecret()));
    }
}