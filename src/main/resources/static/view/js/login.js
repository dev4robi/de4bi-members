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

        // member_jwt쿠키 존재 시 검증요청
        const member_jwt = $.cookie('mebmer_jwt');
        if (!!member_jwt) {
            if (true) {
                // 만료안됨
                // ...
                // @@ 로그인 유지 보관방법,
                // @@ 로그인 후 member_jwt 전달 방법
                // 1. 리디렉션
                // 2. js_call
                // 어떻게 인터페이스로 제공할건지 고민해 볼 것. @@
            }
            else {
                // 만료됨
                $.removeCookie('member_jwt');
            }
        }

        // 이벤트 부착: 플랫폼 로그인 버튼 클릭
        const platforms = ['google', 'naver', 'kakao', 'de4bi'];
        for (i = 0; i < platforms.length; ++i) {
            $('#btn_' + platforms[i] + '_login').click(function(){
                login_js.onclick_oauth_login($(this).attr('url'));
            });
        }
    },

    // 플랫폼으로 로그인하기 클릭
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