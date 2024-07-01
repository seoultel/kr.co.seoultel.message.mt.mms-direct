package kr.co.seoultel.message.mt.mms.direct.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;


/* RABBIT-MQ 관련 상수 및 Bean */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties("rabbitmq")
public class RabbitMQConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultRabbitMQConfig {

    @Override
    @PostConstruct
    public void check() {
        super.check();
    }

    @Bean
    @Primary
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        if (primaryCluster) {
            cachingConnectionFactory.setAddresses(primaryHost);
        } else {
            cachingConnectionFactory.setHost(primaryHost);
            cachingConnectionFactory.setPort(primaryPort);
        }

        cachingConnectionFactory.setVirtualHost(primaryVirtualHost);
        cachingConnectionFactory.setUsername(primaryUsername);
        cachingConnectionFactory.setPassword(primaryPassword);

        //연결이 유효한지 체크
        Connection connection = cachingConnectionFactory.createConnection();
        connection.close();

        return cachingConnectionFactory;
    }


//    public Channel getNonClusteredChannel(CachingConnectionFactory cachingConnectionFactory) {
//        try {
//            com.rabbitmq.client.Connection conn = cachingConnectionFactory.getRabbitConnectionFactory().newConnection();
//            return conn.createChannel();
//        } catch (IOException | TimeoutException e) {
//            return null;
//        }
//    }
//
//    public Channel getClusteredChannel(CachingConnectionFactory cachingConnectionFactory) {
//        org.springframework.amqp.rabbit.connection.Connection amqpConn = cachingConnectionFactory.getPublisherConnectionFactory().createConnection();
//        return amqpConn.createChannel(false);
//    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setPrefetchCount(1);
        return factory;
    }

    @Bean
    public CachingConnectionFactory connectionFactory2() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        if (secondaryCluster) {
            cachingConnectionFactory.setAddresses(secondaryHost);
        } else {
            cachingConnectionFactory.setHost(secondaryHost);
            cachingConnectionFactory.setPort(secondaryPort);
        }
        cachingConnectionFactory.setVirtualHost(secondaryVirtualHost);
        cachingConnectionFactory.setUsername(secondaryUsername);
        cachingConnectionFactory.setPassword(secondaryPassword);
        cachingConnectionFactory.setRequestedHeartBeat(60);

        //연결이 유효한지 체크
        Connection connection = cachingConnectionFactory.createConnection();
        connection.close();

        return cachingConnectionFactory;
    }


    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setExchange(mrExchange);
        rabbitTemplate.setRoutingKey(mrQueue);
        return rabbitTemplate;
    }

    @Bean
    public RabbitTemplate secondaryTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory2());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        rabbitTemplate.setExchange(portOutExchange);
//        rabbitTemplate.setRoutingKey(portOutQueue);
        return rabbitTemplate;
    }

    @Bean
    public RabbitTemplate testTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setExchange(mtExchange);
        rabbitTemplate.setRoutingKey(mtQueue);
        return rabbitTemplate;
    }
}
