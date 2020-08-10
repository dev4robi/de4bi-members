package com.de4bi.members.spring;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 스프링 부트 실행 클래스입니다.
 * 'com.' 아래의 모든 클래스에서 스프링 컴포넌트를 스캔하여 빈으로 생성합니다.
 */
@ComponentScan("com.**")
@SpringBootApplication
public class BootApplication {

	public static final boolean IS_LOCAL_TEST = true;

	public static void main(String[] args) {
		SpringApplication.run(BootApplication.class, args);
	}

	// 키값 생성용 임시 클래스
	/*
	public static class Random {
		public static void main(String[] args) {
			System.out.println(RandomStringUtils.randomAlphanumeric(64));
		}
	}
	*/
}
