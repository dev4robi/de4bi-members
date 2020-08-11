package com.de4bi.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class RestHttpUtil {

    private static final HttpComponentsClientHttpRequestFactory HTTP_FACTORY;
    
    public static final int METHOD_GET      = 0;
    public static final int METHOD_POST     = 1;
    public static final int METHOD_PUT      = 2;
    public static final int METHOD_DELETE   = 3;

    static {
        HTTP_FACTORY = new HttpComponentsClientHttpRequestFactory();
        HTTP_FACTORY.setReadTimeout(5000);
        HTTP_FACTORY.setConnectTimeout(5000);
        HTTP_FACTORY.setHttpClient(HttpClientBuilder.create()
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(20)
            .build());
    }

    /**
     * <p>HTTP GET 통신을 수행합니다.</p>
     * @param url : 목적지 URL.
     * @param reqHeadMap : 요청 헤더를 담은 Map.
     * @param reqParamMap : 요청 파라미터를 담은 Map.
     * @param resHeadMap : 응답 헤더를 답은 Map.
     * @param resBodyList : 응답 문자열을 담은 List. {@code (resBodyList.get(0) 메서드로 요청 획득)}
     * @return HTTP status code값.
     */
    public static int httpGet(String url, Map<String, String> reqHeadMap, Map<String, String> reqParamMap,
        Map<String, String> resHeadMap, List<String> resBodyList)
    {
        final HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.setContentType(MediaType.APPLICATION_JSON);

        try {
            // 헤더 생성
            if (reqHeadMap != null) {
                for (String header : reqHeadMap.keySet()) {
                    final String value = reqHeadMap.get(header);
                    httpHeader.add(header, value);
                }
            }
            
            // URL 쿼리 파라미터 생성
            if (reqParamMap != null) {
                final StringBuilder urlSb = new StringBuilder(url);
                int appendCnt = (url.lastIndexOf('?') == -1 ? 0 : 1);
                for (String param : reqParamMap.keySet()) {
                    final String value = reqParamMap.get(param);
                    urlSb.append((appendCnt++) == 0 ? '?' : '&');
                    urlSb.append(param).append('=').append(value);
                }
                url = urlSb.toString();
            }

            // 요청 및 응답 파싱
            final RestTemplate restTemplate = new RestTemplate(HTTP_FACTORY);
            final ResponseEntity<String> resEntity = restTemplate.getForEntity(url, String.class);

            // 응답 헤더 생성
            if (resHeadMap != null) {
                final HttpHeaders headers = resEntity.getHeaders();
                for (String header : headers.keySet()) {
                    final String value = headers.getFirst(header);
                    if (value != null) { resHeadMap.put(header, value); }
                }
            }

            // 응답 바디 생성
            if (resBodyList != null) {
                resBodyList.add(resEntity.getBody());
            }

            return resEntity.getStatusCode().value();
        }
        catch (RestClientException e) {
            throw new IllegalStateException("Exception while 'httpGet'!", e);
        }
    }

        /**
     * <p>HTTP GET 통신을 수행합니다.</p>
     * @param url : 목적지 URL.
     * @param reqHeadMap : 요청 헤더를 담은 Map.
     * @param reqDataMap : 요청 데이터를 담은 Map.
     * @param resHeadMap : 응답 헤더를 답은 Map.
     * @param resBodyList : 응답 문자열을 담은 List. {@code (resBodyList.get(0) 메서드로 요청 획득)}
     * @return HTTP status code값.
     */
    public static int httpPost(String url, Map<String, String> reqHeadMap, Map<String, String> reqDataMap,
        Map<String, String> resHeadMap, List<String> resBodyList)
    {
        final HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.setContentType(MediaType.APPLICATION_JSON);

        try {
            // 헤더 생성
            if (reqHeadMap != null) {
                for (String header : reqHeadMap.keySet()) {
                    final String value = reqHeadMap.get(header);
                    httpHeader.add(header, value);
                }
            }
            
            // 요청 바디 생성
            if (reqDataMap != null) {
                /*
                JSONObject 
                                    bodyJsonObj = new JSONObject(reqBodyMap);
                }
                catch (JSONException e) {
                    logger.error("Exception!", e);
                    return null;
                }

                httpEntity = new HttpEntity<String>(bodyJsonObj.toString(), httpHeader);
                */ 
                // @@ 여기부터 시작. 요청을 JSON Map으로 바꿔서 처럼 담자... 아래 urlConnection 레거시 코드 참고.
            }

            // 요청 및 응답 파싱
            final RestTemplate restTemplate = new RestTemplate(HTTP_FACTORY);
            final ResponseEntity<String> resEntity = restTemplate.getForEntity(url, String.class);

            // 응답 헤더 생성
            if (resHeadMap != null) {
                final HttpHeaders headers = resEntity.getHeaders();
                for (String header : headers.keySet()) {
                    final String value = headers.getFirst(header);
                    if (value != null) { resHeadMap.put(header, value); }
                }
            }

            // 응답 바디 생성
            if (resBodyList != null) {
                resBodyList.add(resEntity.getBody());
            }

            return resEntity.getStatusCode().value();
        }
        catch (RestClientException e) {
            throw new IllegalStateException("Exception while 'httpGet'!", e);
        }
    }

    public static String urlConnection(String url, int method, Map<String, Object> reqHeadMap, Map<String, Object> reqBodyMap) throws RestClientException {
        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
        if (reqHeadMap != null) {
            for (String key : reqHeadMap.keySet()) {
                Object value = reqHeadMap.get(key);
                if (value != null) {
                    httpHeader.add(key, value.toString());
                }
            }
        }

        boolean isReqBodyExist = (reqBodyMap != null && reqBodyMap.size() > 0);
        JSONObject bodyJsonObj = null;
        HttpEntity<String> httpEntity = null;

        if (method == RestHttpUtil.METHOD_GET) {
            if (isReqBodyExist) {
                try {
                    StringBuilder bodyParamSb = new StringBuilder("?");
                    for (String key : reqBodyMap.keySet()) {
                        Object value = reqBodyMap.get(key);
                        if (value != null) {
                            bodyParamSb.append(URLEncoder.encode(key, "utf-8")).append("=")
                                       .append(URLEncoder.encode(value.toString(), "utf-8")).append("&");
                        }
                    }
                    bodyParamSb.setLength(bodyParamSb.length() - 1);
                    url += bodyParamSb.toString();
                }
                catch (UnsupportedEncodingException e) {
                    logger.error("Exception!", e);
                    return null;
                }
            }
        }
        else {
            if (isReqBodyExist) {
                try {
                    bodyJsonObj = new JSONObject(reqBodyMap);
                }
                catch (JSONException e) {
                    logger.error("Exception!", e);
                    return null;
                }

                httpEntity = new HttpEntity<String>(bodyJsonObj.toString(), httpHeader);
            }
        }

        RestTemplate restTemplate = RestHttpUtil.getInstance();
        ResponseEntity<String> rpyStr = null;

        switch (method) {
            case RestHttpUtil.METHOD_GET:
                rpyStr = restTemplate.getForEntity(url, String.class);
                break;
            case RestHttpUtil.METHOD_POST:
            default:
                rpyStr = restTemplate.postForEntity(url, httpEntity, String.class);
                break;
            /*
                [Note] put(), delete()는 응답결과를 받아올 수 없는 듯 하다.
                철저히 REST의 본연의 특징을 살려서 설계된 클래스인듯...
                추후 메서드 설계 변경해야 함.
            case RestHttpUtil.METHOD_PUT:
                restTemplate.put(url, 업데이트될 객체, paramMap);
                break;
            case RestHttpUtil.METHOD_DELETE:
                restTemplate.delete(url, reqBodyMap);
                break;
            */
        }

        return (rpyStr != null ? rpyStr.getBody() : null);
    }
}