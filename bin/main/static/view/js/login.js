const login_js = {
    // 페이지 초기화
    initPage : function() {
        // 플랫폼 로그인 버튼 클릭
        const platforms = ['google', 'naver', 'kakao', 'de4bi'];
        for (i = 0; i < platforms.length; ++i) {
            var platform = platforms[i];
            $('#' + platform + '_login_url').on('click', login_js.onclick_oauth_login(platform));
        }
    },

    // 플랫폼으로 로그인하기 클릭
    onclick_oauth_login : function(platform) {
        var oauthUrl = null;
        if (platform == 'google' || platform == 'naver' || platform == 'kakao' || platform == 'de4bi') {
            oauthUrl = $('#' + platform + '_login_url').val();
        }

        if (!!oauthUrl) {
            alert('Comming Soon!');
            return;
        }

        location.href = oauthUrl;
    },
}

// 페이지 초기화
$(document).ready(login_js.initPage());