// Main Application Class - entry point for Spring Boot app
package com.abnalliance.journalapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication // Marks this as a Spring Boot application (auto-config + component scan)
@EnableTransactionManagement
public class JournalappApplication {

    public static void main(String[] args) {
        SpringApplication.run(JournalappApplication.class, args); // Bootstraps the app
    }

    @Bean
    public PlatformTransactionManager returnBean(MongoDatabaseFactory mongoDatabaseFactory){
        return new MongoTransactionManager(mongoDatabaseFactory);
    }
}
