package com.jjl.shortlink.project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaInitialConfiguration {
    @Bean
    public NewTopic initialTopic(){
        return new NewTopic("test",1,(short) 0);
    }
}
