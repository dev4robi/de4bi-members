// 전역
const gb_pageurl_login = (location.origin + '/login');
const gb_apiurl_header = ('/api/v1');
const gb_apiurl_login = (location.origin + gb_apiurl_header + '/members/login');
const gb_apiurl_member_info = (location.origin + gb_apiurl_header + '/members');

const common_js = {
    page_js_name : 'common_js',

    initPage : function() {
        console.log(this.page_js_name + ': Begin PageInit...');

        // css 이펙터 초기화
        this.css_loading_spinner_grow();

        console.log(this.page_js_name + ': End PageInit...');
    },

    // 로딩 스피너(grow버전)
    css_loading_spinner_grow : function() {
        var tags = $('.loading-spinner-grow');
        for (i = 0; i < tags.length; ++i) {
            var tag = tags[i];
            for (j = 0; j < 5; ++j) {
                $(tag).append('<div class="spinner-grow text-info" style="animation-delay:' + (j * 0.12) +
                              's;><span class="sr-only">.</span></div>&nbsp;');
            }
        }
    }
}

// 페이지 초기화
$(document).ready(function(){try{common_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});