package online.prostobank.clients.config;

import online.prostobank.clients.config.properties.JmsTemplateProperties;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class SenderJmsConfig {

    @Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory(JmsTemplateProperties jmsTemplateProperties) {
        return new ActiveMQConnectionFactory(jmsTemplateProperties.getBrokerUrl(), jmsTemplateProperties.getLogin(), jmsTemplateProperties.getPassword());
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory(JmsTemplateProperties jmsTemplateProperties) {
        return new CachingConnectionFactory(
                senderActiveMQConnectionFactory(jmsTemplateProperties));
    }

    @Bean
    public JmsTemplate jmsTemplate(JmsTemplateProperties jmsTemplateProperties) {
        return new JmsTemplate(cachingConnectionFactory(jmsTemplateProperties));
    }

}
