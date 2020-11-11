const info_js = {
    page_js_name : "info_js",

    // 페이지 초기화
    initPage : function() {
        console.log(this.page_js_name + ': Begin PageInit...');

        // URL에서 member_jwt획득 시도
        var member_jwt = new URLSearchParams(location.search).get('member_jwt');
        if (!!member_jwt) {
            $.cookie('member_jwt', member_jwt, { expires:15, path:'/' }); // 쿠키에 저장
        }
        else {
            member_jwt = $.cookie('member_jwt'); // 쿠키에서 획득
        }
        
        // GET파라미터를 URL에서 삭제
        history.replaceState({}, document.title, "." + location.pathname);

        // 이벤트 부착
        $('#button_change').on('click', function(){info_js.api_changeMemberInfo()});
        $('#button_delete_account').on('click', function(){info_js.api_deleteAccount()});
        $('#button_logout').on('click', function(){info_js.onClickLogout()});

        // 회원정보 API호출
        if (!!member_jwt) {
            var method = 'GET';
            var api_url = gb_apiurl_member_info;
            var header = {'member_jwt' : member_jwt};
            de4bi_api.apiCall(method, api_url, header, null, 
                function() {
                    // Always
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Call!');
                },
                function(api_result, status, jq_XHR) {
                    // Success
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Success!');
                    console.log('api_result:' + api_result);
                    setTimeout(info_js.updateMemberInfoUI, 1000, api_result);
                },
                function(jq_XHR, status, error) {
                    // Fail
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Fail!');
                    alert('서버와 통신에 실패했습니다. (' + status + '/' + error + ')');
                    location.replace(gb_pageurl_login);
                }
            );
        }
        else {
            // member_jwt미존재 시 로그인 페이지로 이동
            location.replace(gb_pageurl_login);
        }

        console.log(this.page_js_name + ': End PageInit...');
    },

    // 회원정보 UI업데이트
    updateMemberInfoUI : function(api_result) {
        if (de4bi_api.isResultSuccess(api_result) == false) {
            alert('회원정보 획득에 실패했습니다.\n(' + de4bi_api.getResultMsg(api_result) + ')');
            location.replace(gb_pageurl_login);
        }

        $('#input_seq').val(de4bi_api.getResultData(api_result, 'seq'));
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
    },

    // 회원정보 수정 API
    api_changeMemberInfo : function() {
        const member_jwt = $.cookie('member_jwt');
        if (!!member_jwt) {
            if (!confirm('정말 변경하시겠습니까?')) {
                alert('취소되었습니다.');
                return;
            }

            $('#label_old_pw_error').html('');
            $('#label_new_pw_error').html('');
            $('#input_name').html('');
            $('#input_nickname').html('');

            // 요청 파라미터 검사
            var param_check_ok = true;
            var old_password = $('#input_old_password').val();
            var new_password = $('#input_new_password').val();
            var name = $('#input_name').val();
            var nickname = $('#input_nickname').val();

            if (!!old_password) {
                var len = old_password.length;
                if (len < 8 || len > 32) {
                    $('#label_old_pw_error').html('비밀번호는 8~32자 길이어야 합니다.');
                    param_check_ok = false;
                }
            }

            if (!!new_password) {
                var len = new_password.length;
                if (len < 8 || len > 32) {
                    $('#label_new_pw_error').html('비밀번호는 8~32자 길이어야 합니다.');
                    param_check_ok = false;
                }
            }

            var len = name.length;
            if (len < 1 || len > 64) {
                $('#label_name_error').html('이름은 1~64자 길이어야 합니다.');
                param_check_ok = false;
            }

            if (!!nickname) {
                var len = nickname.length;
                if (len < 2 || len > 16) {
                    $('#label_nickname_error').html('이름은 2~16자 길이어야 합니다.');
                    param_check_ok = false;
                }
            }

            if (param_check_ok == false) {
                return;
            }

            // API 호출
            var method = 'PUT';
            var api_url = (gb_apiurl_member_info + '/' + $('#input_seq').val());
            var header = {'member_jwt' : member_jwt};
            var body = {
                'old_password':de4bi_util.sha256(old_password),
                'new_password':de4bi_util.sha256(new_password),
                'name':name,
                'nickname':nickname,
            }

            de4bi_api.apiCall(method, api_url, header, body, 
                function() {
                    // Always
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Call!');
                },
                function(api_result, status, jq_XHR) {
                    // Success
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Success!');
                    console.log('api_result:' + api_result);
                    if (de4bi_api.isResultSuccess(api_result) == false) {
                        alert(de4bi_api.getResultMsg(api_result));
                        return;
                    }
                    location.reload();
                },
                function(api_result, jq_XHR, status, error) {
                    // Fail
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Fail!');
                    alert('정보 변경에 실패했습니다. (' + de4bi_api.getResultMsg(api_result) + ')');
                    location.replace(gb_pageurl_login);
                }
            );
        }
        else {
            // member_jwt미존재 시 로그인 페이지로 이동
            location.replace(gb_pageurl_login);
        }
    },

    // 회원 탈퇴
    api_deleteAccount : function() {
        const member_jwt = $.cookie('member_jwt');
        if (!!member_jwt) {
            if (!confirm('정말 탈퇴하시겠습니까?\n탈퇴 시 계정의 모든 정보를 복원할 수 없습니다!')) {
                alert('취소되었습니다.');
                return;
            }

            if (!confirm('또한, 탈퇴 후 1달간 재가입이 불가능합니다.')) {
                alert('취소되었습니다.');
                return;
            }

            $('#label_old_pw_error').html('');
            $('#label_new_pw_error').html('');
            $('#input_name').html('');
            $('#input_nickname').html('');

            // 요청 파라미터 검사
            var param_check_ok = true;
            var old_password = $('#input_old_password').val();
            var new_password = $('#input_new_password').val();
            var name = $('#input_name').val();
            var nickname = $('#input_nickname').val();

            if (param_check_ok == false) {
                return;
            }

            // API 호출
            var method = 'DELETE';
            var api_url = (gb_apiurl_member_info + '/' + $('#input_seq').val());
            var header = {'member_jwt' : member_jwt};
            var body = {
                'password':de4bi_util.sha256(old_password),
            }

            de4bi_api.apiCall(method, api_url, header, body, 
                function() {
                    // Always
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Call!');
                },
                function(api_result, status, jq_XHR) {
                    // Success
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Success!');
                    console.log('api_result:' + api_result);
                    if (de4bi_api.isResultSuccess(api_result) == false) {
                        alert(de4bi_api.getResultMsg(api_result));
                        return;
                    }
                    else {
                        // Fail
                    }
                    location.reload();
                },
                function(api_result, jq_XHR, status, error) {
                    // Fail
                    console.log('de4bi_apiCall(' + method + ' ' + api_url + ') Fail!');
                    alert('회원 탈퇴에 실패했습니다. (' + de4bi_api.getResultMsg(api_result) + ')');
                    location.replace(gb_pageurl_login);
                }
            );
        }
        else {
            // member_jwt미존재 시 로그인 페이지로 이동
            location.replace(gb_pageurl_login);
        }
    },

    // 로그아웃 버튼 클릭
    onClickLogout : function() {
        $.removeCookie('member_jwt');
        location.replace(gb_pageurl_login);
    }
}

// 페이지 초기화
$(document).ready(function(){try{info_js.initPage()}catch(e){console.log(e);alert('페이지 로딩 중 오류가 발생했습니다. 새로고침(F5)해주세요.')}});