const error_js = {
    // 전역 변수
    // ...
    
    // 페이지 초기화
    initPage : function() {
        // 이벤트 부착: 플랫폼 로그인 버튼 클릭
        const platforms = ['google', 'naver', 'kakao', 'de4bi'];
        for (i = 0; i < platforms.length; ++i) {
            $('#button_goto_main').click(function(){
                error_js.onclick_button_main();
            });
        }
        // 경고 메시지 출력
        const error_msg = $('#input_error_msg').val();
        alert(error_msg);
    },

    // 메인으로 이동 버튼 클릭
    onclick_button_main : function() {
        location.replace('/login');
    },
}

// 페이지 초기화
$(document).ready(function(){try{error_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});