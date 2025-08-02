package com.medplus.aiagent.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = { 
    MongoAutoConfiguration.class, 
    MongoDataAutoConfiguration.class,
    DataSourceAutoConfiguration.class, 
    TransactionAutoConfiguration.class, 
    JmxAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class, 
    JdbcTemplateAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    JmsAutoConfiguration.class,
    DataSourceHealthContributorAutoConfiguration.class
})
@ComponentScan(lazyInit = true, basePackages = { "com.medplus", "com.optival", "db.test" })
@ImportResource(locations = { "classpath:applicationContext.xml" })
@EnableTransactionManagement
public class AIAgentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIAgentServiceApplication.class, args);
    }
}