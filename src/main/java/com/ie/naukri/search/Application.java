package com.ie.naukri.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.ie.naukri.search", "com.ie.naukri.search.commons"})
// Disable Spring default datasource configuration to use Naukri one
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
        RabbitAutoConfiguration.class})
@EnableAsync
@EnableConfigurationProperties
@EnableScheduling
public class Application {

//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//
//    @Bean
//    public TestRestTemplate testRestTemplate() {
//        RestTemplateBuilder temp = new RestTemplateBuilder();
//        return new TestRestTemplate(temp);
//    }

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

