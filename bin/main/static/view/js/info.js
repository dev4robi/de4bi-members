const info_js = {
    page_js_name : "info_js",

    // 페이지 초기화
    initPage : function() {
        console.log(this.page_js_name + ': Begin PageInit...');

        console.log(this.page_js_name + ': End PageInit...');
    }        
}

// 페이지 초기화
$(document).ready(function(){try{info_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});