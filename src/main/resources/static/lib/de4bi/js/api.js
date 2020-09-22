const de4bi_api = {
    /**
     * AJAX 요청을 보냅니다.
     * @param {*} httpMethod HTTP메서드 (GET/POST/PUT/DELETE)
     * @param {*} url 목적지 URL
     * @param {*} header 요청 헤더 (JSON)
     * @param {*} body 요청 바디 (JSON)
     * @param {*} alwaysFunc 항상 수행할 메서드
     * @param {*} doneFunc 성공 시 수행할 메서드
     * @param {*} failFunc 실패 시 수행할 메서드
     * @return 성공 시 true, 실패 시 false
     */
    apiCall : function(httpMethod, apiUrl, header, body, alwaysFunc, doneFunc, failFunc) {
        if (!httpMethod) {
            console.log('Invaild method! (httpMethod: ' + httpMethod + ')');
            return false;
        }

        if (!apiUrl) {
            console.log('Invalid apiUrl! (apiUrl: ' + apiUrl + ')');
            return false;
        }

        $.ajax({
            method : httpMethod,
            url : apiUrl,
            headers : header,
            contentType : 'application/json',
            data : (!!body ? JSON.stringify(body) : '')
        })
        .always(function(data_jqXHR, textStatus, jqXHR_errorThrown) {
            if (!!alwaysFunc) alwaysFunc(data_jqXHR, textStatus, jqXHR_errorThrown);
        })
        .done(function(data, textStatus, jqXHR) {
            if (!!doneFunc) doneFunc(JSON.parse(data), textStatus, jqXHR);
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            if (!!failFunc) failFunc(jqXHR, textStatus, errorThrown);
        });

        return true;
    },

    /**
     * API성공 여부를 반환합니다.
     * @param {*} result apiCall()응답 결과
     * @return 성공 시 true, 실패 시 false
     */
    isResultSuccess : function(result) {
        if (!result) {
            console.log('Invaild result! (result: ' + result + ')');
            return false;
        }

        return result.result;
    },

    /**
     * Result에서 key값에 해당하는 데이터를 반환합니다.
     * @param {*} result apiCall()응답 결과
     * @param {*} key 데이터 키
     * @return 성공 시 데이터값, 실패 시 null
     */
    getResultData : function(result, key) {
        if (!result) {
            console.log('Invaild result! (result: ' + result + ')');
            return null;
        }

        if (!key) {
            console.log('Invaild key! (key: ' + key + ')');
            return false;
        }

        var rt = '';
        try { rt = result.data[key]; }
        catch (e) { console.log('Exception while getResultData(' + key + ')' + e); }
        
        return rt;
    },

    /**
     * Result에서 message를 반환합니다.
     * @param {*} result apiCall()응답 결과
     * @return 성공 시 메시지값, 실패 시 null
     */
    getResultMsg : function(result) {
        return result.message;
    },

    /**
     * 
     */
};