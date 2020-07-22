package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.util.CipherUtil;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.db.mapper.MembersMapper;
import com.de4bi.members.spring.SecureProperties;

import org.apache.tomcat.util.buf.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.lang.Maps;
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
     * @return 성공 시 {@link ResponseEntity}<{@link ApiResult}>.ok()를 반환합니다.
     */
    public ResponseEntity<ApiResult> insert(final PostMembersDto postMembersDto) {
        Objects.requireNonNull(postMembersDto, "'postMembersDto' is null!");

        final MembersDao insertMembersDao = MembersDao.builder()
            .id(postMembersDto.getId())
            .password(passwordSecureHashing(postMembersDto.getPassword()))
            .nickname(postMembersDto.getNickname())
            .name(postMembersDto.getName())
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
            .level(1).exp(0)
            .joinDate(Date.from(Instant.now()))
            .lastLoginDate(null)
            .build();

        membersMpr.insert(insertMembersDao);
        return ResponseEntity.ok(ApiResult.of(true));
    }

    /**
     * <p><strong>[RAW API]</strong> 맴버를 추가합니다.</p>
     * @param membersDao - 추가될 맴버 정보.
     * @return 성공 시 {@link ResponseEntity}<{@link ApiResult}>.ok()를 반환합니다.
     */
    public ResponseEntity<ApiResult> insert(final MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMpr.insert(membersDao);
        return ResponseEntity.ok(ApiResult.of(true));
    }

    /**
     * <p><strong>[RAW API]</strong> 맴버를 조회합니다.</p>
     * @param seq - 조회할 맴버의 시퀀스 번호.
     * @return 성공 시 {@link ResponseEntity}<{@link ApiResult}>.ok()를 반환합니다.
     */
    public ResponseEntity<ApiResult> select(long seq) {
        if (seq < 0L) {
            throw new IllegalArgumentException("'seq' less then zero! (seq: " + seq + ")");
        }
        final MembersDao selectedMembersDao = membersMpr.select(seq);
        return ResponseEntity.ok().body(ApiResult.of(true, ));
    }

    public ResponseEntity<ApiResult> update() {
        return null;
    }

    public ResponseEntity<ApiResult> delete() {
        return null;
    }

    /**
     * {@code secureProps.getMemberPasswordSalt() + password} SHA256 해싱된 비밀번호를 16진수로 반환합니다.
     * @param password - 해싱되기 전 비밀번호.
     * @return 해싱된 64자리의 16진수 비밀번호를 반환합니다.
     */
    private String passwordSecureHashing(String password) {
        Objects.requireNonNull(password, "'password' is null!");
        return HexUtils.toHexString(
            CipherUtil.hashing(CipherUtil.SHA256, password + secureProps.getMemberPasswordSalt()));
    }
}