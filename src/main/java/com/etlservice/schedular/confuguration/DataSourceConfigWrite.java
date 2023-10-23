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
//        entityManagerFactoryRef = "writeEntityManagerFactory",
//        transactionManagerRef = "writeTransactionManager",
//        basePackages = {"com.etlservice.schedular.repository.jpa_repository.linelist_repository"}
//)
//public class DataSourceConfigWrite {
//
////    @Primary
//    @Bean(name="writeProperties")
//    @ConfigurationProperties(value = "spring.datasource")
//    public DataSourceProperties dataSourceProperties() {
//
//        return new DataSourceProperties();
//    }
//
////    @Primary
//    @Bean(name="writeDatasource")
////    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSource datasource(@Qualifier("writeProperties") DataSourceProperties properties){
//
//        return properties.initializeDataSourceBuilder().build();
//    }
//
////    @Primary
//    @Bean(name="writeEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("writeDatasource") DataSource dataSource,
//            @Qualifier("commonJpaProperties") Map<String, String> jpaProperties
//    ) {
//
//        return builder.dataSource(dataSource)
//                .packages("com.etlservice.schedular.entities.linelists")
//                .persistenceUnit("write")
//                .properties(jpaProperties)
//                .build();
//    }
//
////    @Primary
//    @Bean(name = "writeTransactionManager")
//    @ConfigurationProperties("spring.jpa")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("writeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
//
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//
//}
