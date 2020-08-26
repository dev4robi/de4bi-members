package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.SecurityUtil;
import com.de4bi.common.util.StringUtil;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.data.dto.PutMembersDto;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * Members에 대한 서비스입니다.
 */
@PropertySource("config.properties")
@AllArgsConstructor
@Service
public class MembersService {
    
    private final MemberJwtService memberJwtService;
    private final MembersMapper membersMapper;
    private final SecureProperties secureProps;
    private final Environment env;

    private static final String ENVKEY_MEMBER_JWT_EXPIRED_IN_MS =
        "member.jwt.expired-hour"; // MemberJwt 기본 로그인 만료시간
    private static final String ENVKEY_MEMBER_JWT_EXPIRED_IN_MS_KEEPLOGGEDIN =
        "members.jwt.expired-hour-keeploggedin"; // MemberJwt 로그인 유지옵션 시 만료시간

    /**360
     * <p>회원가입 및 로그인을 수행합니다.</p>
     * @param postMembersDto : 회원가입 정보.
     * @return 성공 시, {@link ApiResult}에 MemberJwt문자열을 담아서 응답합니다.
     * @apiNote 내부적으로 {@code MembersService.login()}을 호출합니다.
     */
    public ApiResult<String> signin(PostMembersDto postMembersDto) {
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

        return login(id, postMembersDto.getPassword()); // 자동으로 로그인 결과 반환
    }

    /**
     * <p>로그인을 수행합니다.</p>
     * @param id : 로그인할 아이디.
     * @param password : 비밀번호.
     * @return 성공 시, {@link ApiResult}에 MemberJwt문자열을 담아서 응답합니다.
     * @apiNote 내부적으로 {@code MembersMapper.select()}를 호출합니다.
     * {@code MembersService.rawSelect()}를 사용하지 않음에 유의해야 합니다.
     */
    public ApiResult<String> login(String id, String password) {
        // MemberJwt 발급
        return memberJwtService.issueMemberJwt(
            id, password, env.getProperty(ENVKEY_MEMBER_JWT_EXPIRED_IN_MS, Long.class));

        // 여기에 마지막 로그인일자 업데이트 추가해야 함... @@
         
    }

    /**
     * <p>신규 멤버를 DB에 추가합니다.</p>
     * <p>소셜로그인을 사용한 경우 비밀번호는 null로 저장됩니다.</p>
     * <p>de4bi자체 회원가입시, password는 {@code passwordSecureHashing()}를 통해 해싱되어 저장됩니다.</p>
     * @param postMembersDto - 새로 추가될 멤버 정보
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
     * <p><strong>[RAW API]</strong> 멤버를 DB에 추가합니다.</p>
     * @param membersDao - 추가될 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawInsert(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMapper.insert(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p><strong>[RAW API]</strong> 멤버를 DB에서 조회합니다.</p>
     * @param seq - 조회할 멤버의 시퀀스 번호.
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
     * <p><strong>[RAW API]</strong> 멤버를 DB에서 조회합니다.</p>
     * @param id - 조회할 멤버의 아이디.
     * @return {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawSelect(String id) {
        Objects.requireNonNull(id, "'id' is null!");
        final MembersDao selectedMembersDao = membersMapper.selectById(id);
        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * <p>멤버 존재 여부와 닉네임 중복검사를 수행 후 멤버 정보를 DB에서 수정합니다.</p>
     * {@code putMembersDto}내부 값 중, null을 전달받은 값은 기존 정보와 동일하게 수정합니다.
     * @param seq - 수정할 멤버의 seq값.
     * @param putMembersDto - 수정할 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> update(long seq, PutMembersDto putMembersDto) {
        Objects.requireNonNull(putMembersDto, "'putMembersDto' is null!");

        // 존재여부 검사
        final MembersDao seletedMembersDao = rawSelect(seq).getData();
        if (seletedMembersDao == null) {
            throw new ApiException(HttpStatus.ACCEPTED, "존재하지 않는 회원입니다.");
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
     * <p>[RAW API] 멤버 정보를 DB에서 수정합니다.
     * @param membersDao - 수정할 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawUpdate(MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMapper.update(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p>[RAW API] 멤버를 DB에서 삭제합니다.</p>
     * @param seq - 삭제할 멤버의 seq값.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawDelete(long seq) {
        membersMapper.delete(seq);
        return ApiResult.of(true);
    }
}