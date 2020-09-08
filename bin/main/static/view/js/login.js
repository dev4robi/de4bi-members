const login_js = {
    // 전역 변수
    // ...
    
    // 페이지 초기화
    initPage : function() {
        // URL에서 member_jwt획득 시도
        const passed_member_jwt = new URLSearchParams(location.search).get('member_jwt');
        if (!!passed_member_jwt) {
            $.cookie('member_jwt', passed_member_jwt, { expires: 15 });
        }

        // GET파라미터를 URL에서 삭제
        history.replaceState({}, document.title, "." + location.pathname);

        // member_jwt쿠키 존재 시
        const member_jwt = $.cookie('mebmer_jwt');
        if (!!member_jwt) {
            // 리다이렉션 시도
            const after_url = $.cookie('after_url');
            if (!!after_url) {
                const user_param = $.cookie('user_param');
                const redirect_url = (!!user_param ? after_url : (after_url + '?user_param=' + user_param));
                location.replace(redirect_url);
            }
            else {
                console.log('Wrong "after_url"! (after_url: ' + after_url + ')');
            }

            // @@ 고민해볼 사항!
            // 최초 요청 시 쿠키에 저장
            // -> 로그인 완료전 다른 사이트에서 로그인 시도, 그 사이트에서 after_url전달을 안하면?
            // 토큰이 다른 서비스를 제공하는 사이트로 전송될 수 있음...?!
            // 세션이나 DB를 사용해야 할까...? 조금 더 고민해보자.

            // 리다이렉션 실패 시 토큰정보조회 수행
            if (true) {
                // 토근정보조회 성공
            }
            else {
                // 토큰정보조회 실패
                $.removeCookie('member_jwt');
            }
        }

        // 이벤트 부착: 소셜 로그인 버튼 클릭
        const platforms = ['google', 'naver', 'kakao', 'de4bi'];
        for (i = 0; i < platforms.length; ++i) {
            $('#btn_' + platforms[i] + '_login').click(function(){
                login_js.onclick_oauth_login($(this).attr('url'));
            });
        }
    },

    // 소셜 로그인하기 클릭
    onclick_oauth_login : function(oauthUrl) {
        if (!oauthUrl || oauthUrl == '#') {
            alert('Comming Soon!');
            return;
        }

        location.href = oauthUrl;
    },
}

// 페이지 초기화
$(document).ready(function(){try{login_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});