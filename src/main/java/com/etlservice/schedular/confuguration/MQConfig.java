package com.etlservice.schedular.confuguration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MQConfig {
    @Value("${queue.exchange}")
    private String queueExchange;
    @Value("${etl.routing.key}")
    private String etlRoutingKey;
    @Value("${etl.queue}")
    private String etlQueue;
    @Value("${daily.etl.routing.key}")
    private String dailyEtlRoutingKey;
    @Value("${daily.etl.queue}")
    private String dailyEtlQueue;

    @Value("${spring.rabbitmq.host}")
    private String rabbitMQHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitMQPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitMQUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitMQPassword;

    @Bean
    public Queue etlQueue() {
        return new Queue(etlQueue);
    }

    @Bean
    public Queue dailyEtlQueue() {
        return new Queue(dailyEtlQueue);
    }

    @Bean
    @Primary
    public TopicExchange exchangeQueues() {
        return new TopicExchange(queueExchange);
    }

    @Bean
    @Primary
    public Binding bindingEtl(Queue etlQueue, TopicExchange exchangeQueues) {
        return BindingBuilder
                .bind(etlQueue)
                .to(exchangeQueues)
                .with(etlRoutingKey);
    }

    @Bean
    @Primary
    public Binding bindingDailyEtl(Queue dailyEtlQueue, TopicExchange exchangeQueues) {
        return BindingBuilder
                .bind(dailyEtlQueue)
                .to(exchangeQueues)
                .with(dailyEtlRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate (){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMQHost);
        connectionFactory.setPort(rabbitMQPort);
        connectionFactory.setUsername(rabbitMQUsername);
        connectionFactory.setPassword(rabbitMQPassword);
        connectionFactory.setChannelCacheSize(10); // set channel cache size
//        connectionFactory.setConnectionCacheSize(10); // set connection cache size
        return connectionFactory;
    }
}