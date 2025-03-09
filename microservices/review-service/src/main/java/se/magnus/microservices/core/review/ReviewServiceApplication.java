package se.magnus.microservices.core.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan("se.magnus.microservices")
public class ReviewServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);
        String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
        log.info("Connected to MySQL: " + mysqlUri);
    }

}
