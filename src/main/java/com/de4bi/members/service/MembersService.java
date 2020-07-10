package com.de4bi.members.service;

import java.sql.Date;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.data.dto.PostMembersDto;
import com.de4bi.members.db.mapper.MembersMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MembersService {
 
    private static final Logger logger = LoggerFactory.getLogger(MembersService.class);

    private static MembersMapper membersMpr;

    /**
     * 
     * @param postMembersDto
     * @return
     */
    public ResponseEntity<ApiResult> insert(PostMembersDto postMembersDto) {
        Objects.requireNonNull(postMembersDto, "'postMembersDto' is null!");

        final MembersDao insertMembersDao = MembersDao.builder()
            .id(postMembersDto.getId())
            .password(postMembersDto.getPassword())
            .nickname(postMembersDto.getNickname())
            .name(postMembersDto.getName())
            .autority(MembersCode.MEMBERS_AUTHORITY_BASIC.getValue())
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getValue())
            .level(1)
            .exp(0)
            .joinDate((Date)ThreadStorage.get("REQUEST_TIME"))
            .lastLoginDate(null)
            .build();

            // Aop만들어서 로깅이랑 스레드로컬 테스트...
            // Mapper도 Aop로 예외관리...
            // 여기서부터 시작 @@


        return ResponseEntity.ok(ApiResult.of(true, "Hello World!"));
    }

    public void select() {

    }

    public void update() {

    }

    public void delete() {

    }
}