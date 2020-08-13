const login_js = {
    // 전역 변수
    // ...
    
    // 페이지 초기화
    initPage : function() {
        // 이벤트: 플랫폼 로그인 버튼 클릭
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
$(document).ready(function(){login_js.initPage()});