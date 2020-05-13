package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class IsimpleAbstractConnectorProperties {

    @Value("${simple.jdbc-url}")               private String dsUrl;
    @Value("${simple.jdbc-username}")          private String dsUsername;
    @Value("${simple.jdbc-password}")          private String dsPassword;
    @Value("${simple.jdbc-driver-class-name}") private String dsDriverClassName;

    private final DbPropertiesServiceI propertyService;

    public IsimpleAbstractConnectorProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getDsUrl() {
        try {
            return propertyService.getPropertyByKey("simple.jdbc-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", dsUrl);
            return dsUrl;
        }
    }

    public @Nonnull String getDsUsername() {
        try {
            return propertyService.getPropertyByKey("simple.jdbc-username").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", dsUsername);
            return dsUsername;
        }
    }

    public @Nonnull String getDsPassword() {
        try {
            return propertyService.getPropertyByKey("simple.jdbc-password").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", dsPassword);
            return dsPassword;
        }
    }

    public @Nonnull String getDsDriverClassName() {
        try {
            return propertyService.getPropertyByKey("simple.jdbc-driver-class-name").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", dsDriverClassName);
            return dsDriverClassName;
        }
    }
}
