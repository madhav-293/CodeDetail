package com.codepost.CodePost.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

//    @Bean
//    public NewTopic topic(){
//        return TopicBuilder.name(AppConstant.CODE_TOPIC_NAME).build();
//    }

    @Bean
    public NewTopic topic1(){
        return TopicBuilder.name(AppConstant.DETAIL_TOPIC_NAME).build();
    }

}
