package com.de4bi.members.spring;

import java.util.List;

import com.de4bi.common.data.ApiResult;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurationSupport {

    // https://heowc.tistory.com/22
    // https://yoojh9.github.io/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-HttpMessageConverter/

    @Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		super.addDefaultHttpMessageConverters(converters); // SpringBoot 기본 MessageConverter
		converters.add(de4biApiResultMessageConverter());
	}

    /**
     * {@link com.de4bi.common.data.ApiResult}를 관리하는 MesssageConverter입니다.
     * 
     */
	@Bean
	public HttpMessageConverter<?> de4biApiResultMessageConverter() {
		ObjectMapper objectMapper = new ObjectMapper();
		MappingJackson2HttpMessageConverter htmlEscapingConverter =
				new MappingJackson2HttpMessageConverter();
        htmlEscapingConverter.setObjectMapper(objectMapper);

		return htmlEscapingConverter;
    }
    
    public static class ApiResultMapper extends ObjectMapper {
        public ApiResultMapper() {
            SimpleModule module = new SimpleModule();
            module.addSerializer(type, ser)
        }
    }
}