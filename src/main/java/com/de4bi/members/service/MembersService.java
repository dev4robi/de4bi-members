package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.CipherUtil;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.data.dto.PutMembersDto;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;

import org.apache.tomcat.util.buf.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * Members에 대한 서비스입니다.
 */
@AllArgsConstructor
@Service
public class MembersService {
 
    private static final Logger logger = LoggerFactory.getLogger(MembersService.class);
    
    private final MembersMapper membersMpr;
    private final SecureProperties secureProps;

    /**
     * <p>신규 맴버를 추가합니다.</p>
     * <p>password는 {@code passwordSecureHashing()}를 통해 해싱되어 저장됩니다.</p>
     * @param postMembersDto - 새로 추가될 맴버 정보
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> insert(final PostMembersDto postMembersDto) {
        Objects.requireNonNull(postMembersDto, "'postMembersDto' is null!");

        final MembersDao insertedMembersDao = MembersDao.builder()
            .id(postMembersDto.getId())
            .password(passwordSecureHashing(postMembersDto.getPassword(), secureProps.getMemberPasswordSalt()))
            .nickname(postMembersDto.getNickname())
            .name(postMembersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .level(1).exp(0)
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .build();

        membersMpr.insert(insertedMembersDao);
        return ApiResult.of(true);
    }

    /**
     * <p><strong>[RAW API]</strong> 맴버를 추가합니다.</p>
     * @param membersDao - 추가될 맴버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawInsert(final MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMpr.insert(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p><strong>[RAW API]</strong> 맴버를 조회합니다.</p>
     * @param seq - 조회할 맴버의 시퀀스 번호.
     * @return {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawSelect(final long seq) {
        if (seq < 0L) {
            throw new IllegalArgumentException("'seq' less then zero! (seq: " + seq + ")");
        }
        final MembersDao selectedMembersDao = membersMpr.select(seq);
        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * <p><strong>[RAW API]</strong> 맴버를 조회합니다.</p>
     * @param id - 조회할 맴버의 아이디.
     * @return {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawSelect(final String id) {
        Objects.requireNonNull(id, "'id' is null!");
        final MembersDao selectedMembersDao = membersMpr.selectById(id);
        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * @@ 일단 락 염두하지 않고 개발. 스프링 락과 DB제공 락중 무엇을 사용할지 반드시 결정할 것. @@
     * <p>맴버 정보를 수정합니다.</p>
     * @param putMembersDto - 수정할 맴버 정보
     * @return
     */
    public ApiResult<MembersDao> update(final PutMembersDto putMembersDto) {
        Objects.requireNonNull(putMembersDto, "'putMembersDto' is null!");

        final MembersDao seletedMembersDao = rawSelect(putMembersDto.getSeq()).getData();
        if (seletedMembersDao == null) {
            throw new ApiException("존재하지 않는 회원입니다.");
        }

        final MembersDao updatedMembersDao = MembersDao.builder()
            .password(passwordSecureHashing(putMembersDto.getPassword(), secureProps.getMemberPasswordSalt()))
            .nickname(putMembersDto.getNickname())
            .name(putMembersDto.getName())
            .build();

        return ApiResult.of(true, membersMpr.update(updatedMembersDao));
    }

    /**
     * <p>[RAW API] 맴버 정보를 수정합니다.
     * @param membersDao - 수정할 맴버 정보.
     * @return 수정된 정보{@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawUpdate(final MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        return ApiResult.of(true, membersMpr.update(membersDao));
    }

    public ApiResult<?> delete() {
        return null;
    }

    public ApiResult<?> rawDelete() {
        return null;
    }

    /**
     * {@code password + salt} SHA256 해싱된 비밀번호를 16진수로 반환합니다.
     * @param password - 해싱되기 전 비밀번호.
     * @param salt - SALT 문자열. (nullable)
     * @return 해싱된 64자리의 16진수 비밀번호를 반환합니다.
     */
    private String passwordSecureHashing(final String password, final String salt) {
        Objects.requireNonNull(password, "'password' is null!");
        final String saltedPassword = (salt == null ? password : password + salt);
        return HexUtils.toHexString(CipherUtil.hashing(CipherUtil.SHA256, saltedPassword));
    }
}