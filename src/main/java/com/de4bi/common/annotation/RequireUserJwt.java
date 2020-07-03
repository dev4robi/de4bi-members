package com.de4bi.common.annotation;

/**
 * 컨트롤러 호출을 위해 UserJwt검증이 필요합니다.
 * Spring AOP에서 before를 사용하여 컨트롤러 호출 전에 검사를 수행합니다.
 * 권한이 없는 경우 예외를 응답합니다.
 */
public @interface RequireUserJwt {}