const login_js = {
    // 페이지 초기화
    initPage : function() {
        console.log('login_js: Begin PageInit...');
        // URL에서 member_jwt획득 시도
        const member_jwt = new URLSearchParams(location.search).get('member_jwt');

        // GET파라미터를 URL에서 삭제
        history.replaceState({}, document.title, "." + location.pathname);

        // member_jwt존재 시
        if (!!member_jwt) {
            // frame_type에 따라 분기
            const frame_type = $("#dp_frame_type").val();
            var return_url = $('#dp_return_url').val();
            var return_data = $('#dp_return_data').val();
            switch (frame_type) {
                // [Note] 초기에는 page, popup, iframe방식에 따라 다른 제어를 하려고 했지만, (JS callback등)
                // 각종 브라우저 보안 제약사항 (CROSS-ORIGIN, X-FRAME-OPTIONS등...)으로 인해 
                // 모든 방식을 페이지 redirection후 사용자 페이지에서 전달받은 파라미터로 남은 처리를 완료하도록 통일했다.
                default: case 'page': case 'popup': case 'iframe': {
                    console.log(frame_type + ' Trying redirection! (' + return_url + ')');
                    if (!!return_url) {
                        return_url += ('?member_jwt=' + member_jwt);
                        if (!!return_data) {
                            return_url += ('&return_data=' + return_data);
                        }
                        location.replace(return_url);
                    }
                    console.log('Fail to redirect! (return_url: ' + return_url + ')');
                    break;
                }
            }
        }

        // 이벤트 부착: 소셜 로그인 버튼 클릭
        const platforms = ['google', 'naver', 'kakao', 'de4bi'];
        for (i = 0; i < platforms.length; ++i) {
            $('#btn_' + platforms[i] + '_login').click(function(){
                login_js.onclick_oauth_login($(this).attr('url'));
            });
        }

        console.log('login_js: PageInit done.');
    },

    // 소셜 로그인하기 클릭
    onclick_oauth_login : function(oauthUrl) {
        if (!oauthUrl || oauthUrl == '#') {
            alert('Comming Soon!');
            return;
        }

        // 로그인 state값에 keep_logged_in 설정
        const keep_logged_in_flag = !!$('#input_keep_login').is(':checked') ? true : false;
        oauthUrl = oauthUrl.replace('&state=', '&state=keep_logged_in=' + keep_logged_in_flag + '`');

        // 플랫폼별 로그인 페이지로 이동
        location.href = oauthUrl;
    },
}

// 페이지 초기화
$(document).ready(function(){try{login_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});