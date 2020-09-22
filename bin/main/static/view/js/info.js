const info_js = {
    page_js_name : "info_js",

    // 페이지 초기화
    initPage : function() {
        console.log(this.page_js_name + ': Begin PageInit...');

        // URL에서 member_jwt획득 시도
        const member_jwt = new URLSearchParams(location.search).get('member_jwt');

        // GET파라미터를 URL에서 삭제
        history.replaceState({}, document.title, "." + location.pathname);

        // member_jwt존재 시
        if (!!member_jwt) {
            var api_url = (location.origin + gb_apiurl_member_info);
            var header = {'member_jwt' : member_jwt};
            de4bi_api.apiCall('GET', api_url, header, null, 
                function() {
                    // Always
                    console.log('de4bi_apiCall(' + api_url + ') Call!');
                },
                function(api_result, status, jq_XHR) {
                    // Success
                    console.log('de4bi_apiCall(' + api_url + ') Success!');
                    console.log('api_result:' + api_result);
                    info_js.updateMemberInfo(api_result);
                },
                function(jq_XHR, status, error) {
                    // Fail
                    console.log('de4bi_apiCall(' + api_url + ') Fail!');
                    alert('서버와 통신에 실패했습니다. (' + status + '/' + error + ')');
                    location.replace(location.origin + gb_pageurl_login);
                }
            );
        }

        console.log(this.page_js_name + ': End PageInit...');
    },

    // 멤버정보 UI업데이트
    updateMemberInfo : function(api_result) {
        if (de4bi_api.isResultSuccess(api_result) == false) {
            alert('회원정보 획득에 실패했습니다.\n(' + de4bi_api.getResultMsg(api_result) + ')');
            location.replace(location.origin + gb_pageurl_login);
        }

        $('#input_id').val(de4bi_api.getResultData(api_result, 'id'));
        $('#input_state').val(de4bi_api.getResultData(api_result, 'status'));
        $('#input_authority').val(de4bi_api.getResultData(api_result, 'authority'));

        var auth_agency_img_url = '';
        var auth_agency_alt_str = '';
        switch (de4bi_api.getResultData(api_result, 'auth_agency')) {
            default: {
                auth_agency_img_url = '/img/icon-unknown.png';
                auth_agency_alt_str = '??';
                break;
            }
            case 'de4bi': {
                auth_agency_img_url = '/img/icon-de4bi.png';
                auth_agency_alt_str = 'D4';
                break;
            }
            case '구글': {
                auth_agency_img_url = '/img/icon-google.png';
                auth_agency_alt_str = 'GG';
                break;
            }
            case '네이버': {
                auth_agency_img_url = '/img/icon-naver.png';
                auth_agency_alt_str = 'NV';
                break;
            }
            case '카카오': {
                auth_agency_img_url = '/img/icon-kakao.png';
                auth_agency_alt_str = 'KA';
                break;
            }
        }
        $('#img_auth_agency').attr('src', auth_agency_img_url);
        $('#img_auth_agency').attr('alt', auth_agency_alt_str);
        $('#input_name').val(de4bi_api.getResultData(api_result, 'name'));
        $('#input_nickname').val(de4bi_api.getResultData(api_result, 'nickname'));
        $('#input_join_date').val(de4bi_api.getResultData(api_result, 'join_date'));
        $('#input_last_login_date').val(de4bi_api.getResultData(api_result, 'last_login_date'));

        // 로딩 UI숨기고 정보 UI표시
        $('#div_loading').addClass('d-none');
        $('#div_info').removeClass('d-none');
    }
}

// 페이지 초기화
$(document).ready(function(){try{info_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});

// 멤버정보 수정부터 시작하면 됨.
// API부터 만들까? @@