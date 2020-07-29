package com.de4bi.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 컨트롤러 호출을 위해 AdminJwt검증이 필요합니다.
 * Spring AOP에서 around를 사용하여 컨트롤러 호출 전에 검사를
 * 수행합니다. 권한이 없는 경우 예외를 응답합니다.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdminJwt {}