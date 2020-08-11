package com.de4bi.members.service.oauth;

import com.de4bi.common.data.ApiResult;

/**
 * OAuth 회원 가입을 위한 인터페이스 입니다.
 */
public interface IOAuthService {
    /**
     * <p>클라이언트가 플랫폼의 OAuth를 수행하기 위해 호출해야 할 로그인 URL을 생성합니다.</p>
     * @param extObj : 플랫폼 종속 파라미터를 전달할 객체입니다.
     * @return 성공 시, OAuth수행을 위한 URL을 문자열로 반환합니다.
     */    
    public ApiResult<String> makeLoginUrlForAuthCode(Object extObj);

    /**
     * <p>클라이언트에게 전달받은 코드값을 플랫폼으로부터 검증합니다.</p>
     * @param code : 플랫폼으로부터 클라이언트에게 내려준 토큰값.
     * @param extObj : 플랫폼 종속 데이터를 전달할 객체입니다.
     * @return 성공 시, 플랫폼으로부터 응답 문자열을 담은 ApiResult를 반환합니다.
     */
    public ApiResult<String> requestIdTokenUsingAuthCode(String code, Object extObj);
}