const login_js = {
    // 전역 변수
    // ...
    
    // 페이지 초기화
    initPage : function() {
        alert('hj!');
        // URL에서 member_jwt획득 시도
        const passed_member_jwt = new URLSearchParams(location.search).get('member_jwt');
        if (!!passed_member_jwt) {
            $.cookie('member_jwt', passed_member_jwt, { expires: 15 });
        }

        // GET파라미터를 URL에서 삭제
        history.replaceState({}, document.title, "." + location.pathname);

        // member_jwt쿠키 존재 시
        const member_jwt = $.cookie('member_jwt');
        if (!!member_jwt) {
            alert('member_jwt: ' + member_jwt);
            // frame_type에 따라 분기
            const frame_type = $("#dp_frame_type").val();
            var return_url = $('#dp_return_url').val();
            var return_data = $('#dp_return_data').val();
            var return_func = $('#dp_return_func').val();
            switch (frame_type) {
                default:
                case 'page': {
                    alert('page! 리다이렉션 수행! (' + return_url + ')');
                    if (!!return_url) {
                        return_url += ('?member_jwt=' + member_jwt);
                        if (!!return_data) {
                            return_url += ('&return_data=' + return_data);
                        }
                        location.replace(return_url); // @@리다이렉션 완료... 근데 그놈의 뒤로가기가 문제... 다시 재현해보고 어떻게 할지 고민!
                    }
                    console.log('Fail to redirect! (return_url: ' + return_url + ')');
                    break;
                }
                case 'popup': {
                    alert('popup! 오프너 함수 수행! (' + return_func + ')');
                    if (!!return_func) {
                        window.opener.return_func(member_jwt, return_data);
                    }
                    else {
                        console.log('Fail to find opener\'s function! (return_func: ' + return_func + ')');
                    }
                    window.close();
                    break;
                }
                case 'iframe': {
                    alert('iframe! 부모창 함수 수행! (' + return_func + ')');
                    if (!!return_func) {
                        window.parnet.return_func(member_jwt, return_data);
                    }
                    else {
                        console.log('Fail to find parent\'s function! (return_func: ' + return_func + ')');
                    }
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