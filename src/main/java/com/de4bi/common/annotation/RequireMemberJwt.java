package com.de4bi.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 컨트롤러 호출을 위해 MemberJwt검증이 필요합니다.
 * Spring AOP에서 around 사용하여 컨트롤러 호출 전에 검사를 수행합니다.
 * 유효하지 않은 토큰의 경우 예외를 응답합니다.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireMemberJwt {}