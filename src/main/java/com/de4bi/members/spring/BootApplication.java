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

/**
 * 고민거리 정리. @@
 * 1. DTO클래스의 필요성?
 *  1) 모두 필요하지 않다 - 응답 데이터를 안정적으로 채워넣는 방식으로 접근하자면 필요하다.
 *  2) 응답에 대해서만 필요하다 - 이부분은 동의하는 부분. 실수를 방지하기 위해서라도, 다중값 응답을 위해서라면 채워넣으면 좋다.
 *  다른 서비스의 '결과'를 가져와서 사용하기에도 매우 용이해 진다. 응답에 대해서 dto는 필수불가결이다.
 *  3) 요청에 대해서, 응답에 대해서 모두 필요하다 - 요청에 대한 커플링(결합)은 다양한 요청을 생성하는데 있어 걸림돌이 될 수 있다.
 *  비슷한 api에서의 재활용은 높아지지만, 혼선을 가할 확률이 높아진다.
 *  validator을 dto자체에 내장시킴으로써 코드를 간결하면서도 안정적으로 작성할 수 있는 장점은 생긴다.
 *  노출하느냐 숨기냐, 묶느냐 푸느냐의 문제. 어렵다. 내가 왜 dto를 객체로 묶기 시작했을까? 분명 여러번 시도한 기억이 있긴 하다.
 *  메서드 파라미터가 적으면 성능상 이점이 있긴 하다. 하지만 dto객체 자체를 gc처리하는 비용도 생각해봐야 한다.
 *  
 * 
 * 2. api_result 관련해서 data필드의 필요성?
 *  - 
 * 
 * 2. 서비스 메서드 구조정리
 * 
 * 
 * 3. 컨트롤러 RESTFUL 고민
 * 
 */