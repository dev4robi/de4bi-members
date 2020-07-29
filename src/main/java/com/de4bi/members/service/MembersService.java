package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.SecurityUtil;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.data.dto.PutMembersDto;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * @@ 일단 락 염두하지 않고 개발. 스프링 락과 DB제공 락중 무엇을 사용할지 반드시 결정할 것. @@
 * Members에 대한 서비스입니다.
 */
@AllArgsConstructor
@Service
public class MembersService {
 
    private static final Logger logger = LoggerFactory.getLogger(MembersService.class);
    
    private final MembersMapper membersMpr;
    private final SecureProperties secureProps;

    /**
     * <p>신규 멤버를 추가합니다.</p>
     * <p>password는 {@code passwordSecureHashing()}를 통해 해싱되어 저장됩니다.</p>
     * @param postMembersDto - 새로 추가될 멤버 정보
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> insert(final PostMembersDto postMembersDto) {
        Objects.requireNonNull(postMembersDto, "'postMembersDto' is null!");

        final MembersDao insertedMembersDao = MembersDao.builder()
            .id(postMembersDto.getId())
            .password(SecurityUtil.passwordSecureHashing(postMembersDto.getPassword(), secureProps.getMemberPasswordSalt()))
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
     * <p><strong>[RAW API]</strong> 멤버를 추가합니다.</p>
     * @param membersDao - 추가될 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawInsert(final MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMpr.insert(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p><strong>[RAW API]</strong> 멤버를 조회합니다.</p>
     * @param seq - 조회할 멤버의 시퀀스 번호.
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
     * <p><strong>[RAW API]</strong> 멤버를 조회합니다.</p>
     * @param id - 조회할 멤버의 아이디.
     * @return {@link ApiResult}<{@link MembersDao}>를 반환합니다.
     */
    public ApiResult<MembersDao> rawSelect(final String id) {
        Objects.requireNonNull(id, "'id' is null!");
        final MembersDao selectedMembersDao = membersMpr.selectById(id);
        return ApiResult.of(true, selectedMembersDao);
    }

    /**
     * <p>멤버 존재 여부와 닉네임 중복검사를 수행 후 멤버 정보를 수정합니다.</p>
     * {@code putMembersDto}내부 값 중, null을 전달받은 값은 기존 정보와 동일하게 수정합니다.
     * @param seq - 수정할 멤버의 seq값.
     * @param putMembersDto - 수정할 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> update(final long seq, final PutMembersDto putMembersDto) {
        Objects.requireNonNull(putMembersDto, "'putMembersDto' is null!");

        // 존재여부 검사
        final MembersDao seletedMembersDao = rawSelect(seq).getData();
        if (seletedMembersDao == null) {
            throw new ApiException(HttpStatus.ACCEPTED, "존재하지 않는 회원입니다.");
        }

        // 닉네임 중복검사
        if (Objects.nonNull(putMembersDto.getNickname())) {
            final MembersDao duplicatedNicknameMembersDao = membersMpr.selectByNickname(putMembersDto.getNickname());
            if (duplicatedNicknameMembersDao != null) {
                throw new ApiException(HttpStatus.ACCEPTED, "'" + putMembersDto.getNickname() + "'은(는) 이미 존재하는 닉네임입니다.");
            }
        }

        // 업데이트 수행 (변경할 값으로 null을 전달받은 경우 기존값을 그대로 사용)
        final MembersDao updatedMembersDao = seletedMembersDao;
        final String newPassword = 
            Objects.isNull(putMembersDto.getPassword()) ?
                seletedMembersDao.getPassword() :
                SecurityUtil.passwordSecureHashing(putMembersDto.getPassword(), secureProps.getMemberPasswordSalt());
        final String newNickname = Optional.ofNullable(putMembersDto.getNickname()).orElse(seletedMembersDao.getNickname());
        final String newName = Optional.ofNullable(putMembersDto.getName()).orElse(seletedMembersDao.getName());

        updatedMembersDao.setPassword(newPassword);
        updatedMembersDao.setNickname(newNickname);
        updatedMembersDao.setName(newName);
        membersMpr.update(updatedMembersDao);
        return ApiResult.of(true);
    }

    /**
     * <p>[RAW API] 멤버 정보를 수정합니다.
     * @param membersDao - 수정할 멤버 정보.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawUpdate(final MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMpr.update(membersDao);
        return ApiResult.of(true);
    }

    /**
     * <p>[RAW API] 멤버를 삭제합니다.</p>
     * @param seq - 삭제할 멤버의 seq값.
     * @return {@link ApiResult}를 반환합니다.
     */
    public ApiResult<?> rawDelete(final long seq) {
        membersMpr.delete(seq);
        return ApiResult.of(true);
    }
}