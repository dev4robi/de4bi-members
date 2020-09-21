const common_js = {
    page_js_name : 'common_js',

    initPage : function() {
        console.log(this.page_js_name + ': Begin PageInit...');

        // css 이펙터 초기화
        this.css_loading_spinner_grow(5);

        console.log(this.page_js_name + ': End PageInit...');
    },

    // 로딩 스피너(grow버전)
    css_loading_spinner_grow : function(i) {
        console.log(i);
        setTimeout(function (i) {
            if (i >= 0) {
                $('.loading-spinner-grow').append('<div class="spinner-grow text-info"><span class="sr-only">.</span></div>&nbsp;');
            }
        }, 2000, (i - 1)); // 여기부터 시작. js재귀호출이 1~2번에서 멈춘다. 이유는? @@
        console.log('!');
    }
}

// 페이지 초기화
$(document).ready(function(){try{common_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});