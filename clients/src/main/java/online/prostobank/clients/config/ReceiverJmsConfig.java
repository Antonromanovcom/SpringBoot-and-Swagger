package online.prostobank.clients.config;

import online.prostobank.clients.config.properties.JmsTemplateProperties;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
@EnableJms
public class ReceiverJmsConfig {

    @Bean
    public ActiveMQConnectionFactory receiverActiveMQConnectionFactory(JmsTemplateProperties properties) {
        return new ActiveMQConnectionFactory(properties.getBrokerUrl(), properties.getLogin(), properties.getPassword());
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(JmsTemplateProperties properties) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(receiverActiveMQConnectionFactory(properties));
        factory.setConcurrency("3-10");

        return factory;
    }

}
