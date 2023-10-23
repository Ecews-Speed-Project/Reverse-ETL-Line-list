//package com.etlservice.schedular.confuguration;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.persistence.EntityManagerFactory;
//import javax.sql.DataSource;
//import java.util.Map;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        entityManagerFactoryRef = "readEntityManagerFactory",
//        transactionManagerRef = "readTransactionManager",
//        basePackages = {"com.etlservice.schedular.repository.jpa_repository.read"}
//)
//public class DataSourceConfigRead {
//
//    @Primary
//    @Bean(name="readProperties")
//    @ConfigurationProperties("spring.datasource.read")
//    public DataSourceProperties dataSourceProperties() {
//
//        return new DataSourceProperties();
//    }
//
//    @Primary
//    @Bean(name="readDatasource")
//    public DataSource datasource(@Qualifier("readProperties") DataSourceProperties properties){
//
//        return properties.initializeDataSourceBuilder().build();
//    }
//
//    @Primary
//    @Bean(name="readEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("readDatasource") DataSource dataSource,
//            @Qualifier("commonJpaProperties")Map<String, String> jpaProperties
//    ){
//        return builder.dataSource(dataSource)
//                .packages("com.etlservice.schedular.entities")
//                .persistenceUnit("art_linelist")
//                .properties(jpaProperties)
//                .build();
//    }
//
//    @Primary
//    @Bean(name = "readTransactionManager")
//    @ConfigurationProperties("spring.datasource.read.jpa")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("readEntityManagerFactory") EntityManagerFactory entityManagerFactory
//    ) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//
//}
