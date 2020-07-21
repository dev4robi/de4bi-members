package com.de4bi.members.spring;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.AllArgsConstructor;

/**
 * 사용자 정의 DataSource/SqlSession을 생성하는 클래스입니다.
 * mybatis연동을 위해 apllication.properties에서 설정값을
 * 읽는 코드와, DataSource를 생성하기 위해 SecureProperties로부터
 * DB접속 정보들을 읽어오는 부분으로 구성되어 있습니다.
 */
@AllArgsConstructor
@Configuration
@MapperScan(basePackages = "com.de4bi.members.db.mapper")
@PropertySource("application.properties")
@EnableTransactionManagement
public class DataSourceConfig {

    private final SecureProperties secureProperties;
    private final Environment applicationProperties;

    @Bean
    @Primary
    public DataSource customDataSource() {
        return DataSourceBuilder.create()
            .driverClassName(secureProperties.getDataSourceDriverClassName())
            .url(secureProperties.getDataSoruceUrl())
            .username(secureProperties.getDataSoruceUserName())
            .password(secureProperties.getDataSorucePassword())
            .build();
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource customDataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(customDataSource);

        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(
            resolver.getResources(applicationProperties.getProperty("mybatis.mapper-locations")));

        final Properties mybatisProp = new Properties();
        mybatisProp.setProperty(
            "mybatis.configuration.map-underscore-to-camel-case",
            applicationProperties.getProperty("mybatis.configuration.map-underscore-to-camel-case"));
        sessionFactory.setConfigurationProperties(mybatisProp);
        
        return sessionFactory.getObject();
    }
    
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
      return new SqlSessionTemplate(sqlSessionFactory);
    }
}