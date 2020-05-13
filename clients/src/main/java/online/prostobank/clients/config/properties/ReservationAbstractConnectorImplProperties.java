package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Slf4j
@Configuration
public class ReservationAbstractConnectorImplProperties {

    @Value("${simple.url}")       private String simpleUrl;
    @Value("${simple.login}")     private String login;
    @Value("${simple.password}")  private String password;
    @Value("${simple.branch}")    private String branchCodeRef;
    @Value("${simple.host:null}") private String host;

    private final DbPropertiesServiceI propertyService;

    public ReservationAbstractConnectorImplProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getSimpleUrl() {
        try {
            return propertyService.getPropertyByKey("simple.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", simpleUrl);
            return simpleUrl;
        }
    }

    public @Nonnull String getLogin() {
        try {
            return propertyService.getPropertyByKey("simple.login").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", login);
            return login;
        }
    }

    public @Nonnull String getPassword() {
        try {
            return propertyService.getPropertyByKey("simple.password").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", password);
            return password;
        }
    }

    public @Nonnull String getBranchCodeRef() {
        try {
            return propertyService.getPropertyByKey("simple.branch").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", branchCodeRef);
            return branchCodeRef;
        }
    }

    public @Nullable String getHost() {
        try {
            return propertyService.getPropertyByKey("simple.host").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", host);
            return host;
        }
    }
}
