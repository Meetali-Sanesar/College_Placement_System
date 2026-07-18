package com.yourname.campusplacement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the application.
 *
 * @SpringBootApplication combines three annotations:
 *  - @Configuration        -> this class can define Spring beans
 *  - @EnableAutoConfiguration -> Spring Boot configures sensible defaults
 *                                based on what's on the classpath (e.g. sees
 *                                MySQL driver + JPA -> configures a DataSource)
 *  - @ComponentScan        -> Spring scans this package and all sub-packages
 *                                for @Component, @Service, @Repository,
 *                                @Controller classes and registers them as beans
 *
 * IMPORTANT: all your code must live inside or below
 * com.yourname.campusplacement, or Spring will never find it.
 */
@SpringBootApplication
public class CampusplacementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusplacementApplication.class, args);
    }

}
