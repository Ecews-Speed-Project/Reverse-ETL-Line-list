//package com.etlservice.schedular.confuguration;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class CommonDatasourceConfig {
//    @Value("${spring.jpa.hibernate.naming.physical-strategy}")
//    private String hibernatePhysicalNamingStrategy;
//
//    @Value("${spring.jpa.hibernate.naming.implicit-strategy}")
//    private String hibernateImplicitNamingStrategy;
//
//    @Bean(name = "commonJpaProperties")
//    public Map<String, String> commonJpaProperties() {
//        Map<String, String> props = new HashMap<>();
//        props.put("hibernate.naming.physical-strategy", hibernatePhysicalNamingStrategy);
//        props.put("hibernate.naming.implicit-strategy", hibernateImplicitNamingStrategy);
//        return props;
//    }
//}
