package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;
import java.util.Base64;
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
     * 신규 맴버를 추가합니다.
     * <p>password는 {@code Salted + SHA256}되어 저장됩니다.</p>
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
     * <strong>[RAW API]</strong> 맴버를 추가합니다.
     * @param membersDao - 추가될 맴버 정보.
     * @return 성공 시 {@link ResponseEntity}<{@link ApiResult}>.ok()를 반환합니다.
     */
    public ResponseEntity<ApiResult> insert(final MembersDao membersDao) {
        Objects.requireNonNull(membersDao, "'membersDao' is null!");
        membersMpr.insert(membersDao);
        return ResponseEntity.ok(ApiResult.of(true));
    }

    public void select() {

    }

    public void update() {

    }

    public void delete() {

    }

    /**
     * {@code Salted + SHA256} 해싱된 비밀번호를 16진수로 반환합니다.
     * @param password - 해싱되기 전 비밀번호.
     * @return 보안 해싱된 64자리의 비밀번호를 반환합니다.
     */
    private String passwordSecureHashing(String password) {
        Objects.requireNonNull(password, "'password' is null!");
        return HexUtils.toHexString( // sha256이 32byte길이를 반환한다. 뭔가 이상한데...? @@
            CipherUtil.hashing(CipherUtil.SHA256, password.getBytes() + secureProps.getMemberPasswordSalt()));
    }
}