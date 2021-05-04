package com.liujie.emos.wx.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public ConnectionFactory getFactory(){
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("192.168.0.106");
        factory.setPort(5672);
        return factory;
    }
}
