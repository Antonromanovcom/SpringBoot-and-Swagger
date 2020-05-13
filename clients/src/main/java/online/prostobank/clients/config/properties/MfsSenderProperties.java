package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class MfsSenderProperties {

    @Value("${sms.partner}")  private String partner;
    @Value("${sms.uri}")      private String uri;
    @Value("${sms.login}")    private String login;
    @Value("${sms.password}") private String password;

    private final DbPropertiesServiceI propertyService;

    public MfsSenderProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getPartner() {
        try {
            return propertyService.getPropertyByKey("sms.partner").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", partner);
            return partner;
        }
    }

    public @Nonnull String getUri() {
        try {
            return propertyService.getPropertyByKey("sms.uri").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", uri);
            return uri;
        }
    }

    public @Nonnull String getLogin() {
        try {
            return propertyService.getPropertyByKey("sms.login").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", login);
            return login;
        }
    }

    public @Nonnull String getPassword() {
        try {
            return propertyService.getPropertyByKey("sms.password").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", password);
            return password;
        }
    }
}
