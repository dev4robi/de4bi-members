package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;

import com.de4bi.common.data.ApiResult;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.db.mapper.MembersMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
public class TestService {

    private final static Logger logger = LoggerFactory.getLogger(TestService.class);

    @Autowired
    private MembersMapper membersMpr;
    
    public ResponseEntity<ApiResult> insert() {
        membersMpr.insert(MembersDao.builder()
            .id("de4bi@gmail.com").password("1234")
            .nickname("admin").name("de4bi")
            .authority(MembersCode.MEMBERS_AUTHORITY_BASIC.getValue()) // 맴버 코드에 문제가 좀 있다.
            .status(MembersCode.MEMBERS_STATUS_NORMAL.getValue())       // 코드는 db에서 정수로 관리하지만
            .joinDate(Date.from(Instant.now()))                            // 지금 내 코드에서는 문자열로 관리하기 때문.
            .build()                                                            // 각각 케이스의 장 단점을 생각해 보자... @@
        );
        return ResponseEntity.ok().body(ApiResult.of(true, "Hello World!"));
    }
}