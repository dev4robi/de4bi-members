package com.de4bi.members.controller.api;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.service.TestService;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class TestApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestApiController.class);

    private TestService testSvc;

    @GetMapping("/test")
    public ApiResult<MembersDao> getTest() {
        ThreadStorage.put(ApiResult.KEY_TID, RandomStringUtils.randomAlphanumeric(16));
        return testSvc.insert();
    }

    @GetMapping("/testb")
    public ApiResult<MembersDao> getTestB() {
        return ApiResult.of(false, MembersDao.class)
            .setCode("A9999").setMessage("아이고...").addMsgParam("헬로")
            .setData(MembersDao.builder().id("test@gmail.com").nickname("닉넴이 뭐니?").build());
    }
}