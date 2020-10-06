package com.de4bi.members.service;

import java.sql.Date;
import java.time.Instant;

import com.de4bi.common.data.ApiResult;
import com.de4bi.members.controller.dto.PostMembersDto;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.db.mapper.MembersMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * 각종 테스트를 위한 테스트 서비스입니다.
 */
@AllArgsConstructor
@Service
public class TestService {

    private MembersService membersSvc;
    
    public ApiResult<MembersDao> insert() {
        membersSvc.insert(MembersDao.builder()
            .id("de4bi@gmail.com").password("1234")
            .name("dev-lee").nickname("admin").build());
        return ApiResult.of(true, MembersDao.class);
    }
}