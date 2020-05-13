package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.DbPropertiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
@Slf4j
public class JmsTemplateProperties {

    @Value("${artemis.broker-url}") private String brokerUrl;
    @Value("${artemis.login}")      private String login;
    @Value("${artemis.password}")   private String password;


    private final DbPropertiesService propertiesService;

    public JmsTemplateProperties(DbPropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    public @Nonnull
    String getBrokerUrl() {
        try {
            return propertiesService.getPropertyByKey("artemis.broker-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", brokerUrl);
            return brokerUrl;
        }
    }


    @Nonnull
    public String getLogin() {
        try {
            return propertiesService.getPropertyByKey("artemis.login").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", brokerUrl);
            return login;
        }
    }

    @Nonnull
    public String getPassword() {
        try {
            return propertiesService.getPropertyByKey("artemis.password").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", brokerUrl);
            return password;
        }
    }
}
