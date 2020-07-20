package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;

import com.de4bi.common.data.ApiResult;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.db.mapper.MembersMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TestService {

    private final static Logger logger = LoggerFactory.getLogger(TestService.class);

    private MembersMapper membersMpr;
    
    public ResponseEntity<ApiResult> insert() {
        membersMpr.insert(
            MembersDao.builder().id("de4bi@gmail.com").password("1234")
                .nickname("admin").name("de4bi")
                .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getSeq())
                .status(MembersCode.MEMBERS_STATUS_NORMAL.getSeq())
                .level(1).exp(0)
                .joinDate(Date.from(Instant.now()))
                .lastLoginDate(null)
                .build()
        );
        return ResponseEntity.ok().body(ApiResult.of(true, "Hello World!"));
    }
}