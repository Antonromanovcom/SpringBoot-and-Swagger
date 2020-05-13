package online.prostobank.clients.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RoleServiceProperties {

    @Value("${keycloak.auth-server-url}")    private String keyCloakServerUrl;
    @Value("${keycloak.realm}")              private String keyCloakRealm;
    @Value("${keycloak.resource}")           private String login;
    @Value("${keycloak.credentials.secret}") private String secret;
    @Value("${cache.users.timeout}")         private Long   refreshRate;

    private final DbPropertiesServiceI propertyService;

    public @Nonnull String getKeyCloakServerUrl() {
        try {
            return propertyService.getPropertyByKey("keycloak.auth-server-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", keyCloakServerUrl);
            return keyCloakServerUrl;
        }
    }

    public @Nonnull String getKeyCloakRealm() {
        try {
            return propertyService.getPropertyByKey("keycloak.realm").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", keyCloakRealm);
            return keyCloakRealm;
        }
    }

    public @Nonnull String getLogin() {
        try {
            return propertyService.getPropertyByKey("keycloak.resource").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", login);
            return login;
        }
    }

    public @Nonnull String getSecret() {
        try {
            return propertyService.getPropertyByKey("keycloak.credentials.secret").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", secret);
            return secret;
        }
    }

    public @Nonnull Long getRefreshRate() {
        try {
            return Long.valueOf(
                    propertyService
                            .getPropertyByKey("cache.users.timeout")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", refreshRate);
            return refreshRate;
        }
    }
}
