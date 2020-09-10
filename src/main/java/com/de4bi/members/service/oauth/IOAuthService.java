package com.de4bi.members.service.oauth;

import com.de4bi.common.data.ApiResult;
import com.de4bi.members.controller.dto.PostMembersDto;

/**
 * OAuth로 멤버 정보를 획득하기 위한 인터페이스 입니다.
 */
public interface IOAuthService {

    /**
     * <p>클라이언트가 플랫폼의 OAuth를 수행하기 위해 호출해야 할 로그인 URL을 생성합니다.</p>
     * @param returnParam : 로그인 후 이동할 페이지에 전달할 고유 데이터입니다.
     * @param extObj : 그 외 플랫폼 종속 파라미터를 전달할 객체입니다.
     * @return 성공 시, OAuth수행을 위한 URL을 문자열로 반환합니다.
     */    
    public ApiResult<String> makeLoginUrlForAuthCode(String returnParam, Object extObj);

    /**
     * <p>클라이언트에게 전달받은 코드값을 플랫폼으로부터 검증합니다.</p>
     * @param code : 플랫폼으로부터 클라이언트에게 내려준 인증코드값.
     * @param extObj : 플랫폼 종속 데이터를 전달할 객체입니다.
     * @return 성공 시, 플랫폼으로부터 응답 문자열을 담은 {@link ApiResult}를 반환합니다.
     */
    public ApiResult<String> requestIdTokenUsingAuthCode(String code, Object extObj);


    /**
     * <p>플랫폼으로부터 받은 식별토큰을 처리하여 클라이언트의 정보를 획득합니다.</p>
     * @param idToken : 플랫폼으로부터 전달받은 토큰값.
     * @param state : 플랫폼에 Code를위한 URL생성 시 넘겨주었던 고유 식별값.
     * @param extObj : 플랫폼 종속 파라미터를 전달할 객체입니다.
     * @return 성공 시, {@link ApiResult}<{@link OAuthDto}>를 반환합니다.
     */
    public ApiResult<OAuthDto> getMemberInfoFromIdToken(String idToken, String state, Object extObj);


    /**
     * <p>OAuth를 사용하여 회원 정보를 획득합니다.</p>
     * <strong>※ 일반적인 경우 이 메서드만 사용하면 됩니다.</strong>
     * @param code : 플랫폼으로부터 클라이언트에게 내려준 인증코드값.
     * @param state : 플랫폼에 Code를위한 URL생성 시 넘겨주었던 고유 식별값.
     * @param extObj : 플랫폼 종속 파라미터를 전달할 객체입니다.
     * @return 성공 시, {@link ApiResult}<{@link PostMembersDto}>를 반환합니다.
     * @apiNote 내부적으로 {@link IOAuthService}인터페이스의 메서드인
     * {@code requestIdTokenUsingAuthCode()}, {@code getMemberInfoFromIdToken}를 호출할 것입니다. 
     */
    public ApiResult<PostMembersDto> getMemberInfoWithOAuth(String code, String state, Object extObj);
}