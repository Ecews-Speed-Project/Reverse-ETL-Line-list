package com.etlservice.schedular.confuguration;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Slf4j
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate getRestTemplate () {
        return new RestTemplate();
    }

    @Bean
    public ExecutorService executor() {
        return newFixedThreadPool(10);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

//    @Bean
//    public MongoTemplate mongoTemplate() {
//        return new MongoTemplate();
//    }
//
//    @Bean
//    public MongoClient mongoClient() {
//        MongoClientURI uri = new MongoClientUri("mongodb://localhost:27017");
//    }
//    Connection dbConn ;
//
//    public  Connection getConnection() {
//
//        try {
//            // Create Properties object.
//            Properties props = new Properties();
//
//            InputStream dbSettingsPropertyFile= AppConfig.class.getClassLoader().getResourceAsStream("application.properties");
//
//            // Load jdbc related properties in above file.
//            props.load(dbSettingsPropertyFile);
//
//            // Get each property value.
//            String dbDriverClass = props.getProperty("spring.datasource.driver-class-name");
//
//            String dbConnUrl = props.getProperty("spring.datasource.url");
//
//            String dbUserName = props.getProperty("spring.datasource.username");
//
//            String dbPassword = props.getProperty("spring.datasource.password");
//
//            if (!"" .equals(dbDriverClass) && !"" .equals(dbConnUrl)) {
//                /* Register jdbc driver class. */
//                Class.forName(dbDriverClass);
//
//                // Get database connection object.
//                try (Connection dbConn2 = DriverManager.getConnection(dbConnUrl, dbUserName, dbPassword)){
//                    // Get dtabase meta data.
//                    DatabaseMetaData dbMetaData = dbConn2.getMetaData();
//
//                    // Get database name.
//                    String dbName = dbMetaData.getDatabaseProductName();
//
//                    // Get database version.
//                    String dbVersion = dbMetaData.getDatabaseProductVersion();
//
//                    log.info("Database Name : " + dbName);
//
//                    log.info("Database Version : " + dbVersion);
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("context", ex);
//        }
//        return dbConn;
//    }
}
