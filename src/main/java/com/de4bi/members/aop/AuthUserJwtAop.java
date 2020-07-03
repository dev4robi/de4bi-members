package com.de4bi.members.aop;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
@Aspect
public class AuthUserJwtAop {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthUserJwtAop.class);

    
}