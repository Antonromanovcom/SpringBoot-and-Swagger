package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class ApplicationProperties {

    @Value("${emarsys.api.user}")     private String emarsysUser;
    @Value("${emarsys.api.key}")      private String emarsysKey;
    @Value("${emarsys.api.url}")      private String emarsysUrl;
    @Value("${token.encryptor.seed}") private String seed;

    private final DbPropertiesServiceI propertyService;

    public ApplicationProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getEmarsysUser() {
        try {
            return propertyService.getPropertyByKey("emarsys.api.user").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", emarsysUser);
            return emarsysUser;
        }
    }

    public @Nonnull String getEmarsysKey() {
        try {
            return propertyService.getPropertyByKey("emarsys.api.key").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", emarsysKey);
            return emarsysKey;
        }
    }

    public @Nonnull String getEmarsysUrl() {
        try {
            return propertyService.getPropertyByKey("emarsys.api.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", emarsysUrl);
            return emarsysUrl;
        }
    }

    public @Nonnull String getSeed() {
        try {
            return propertyService.getPropertyByKey("token.encryptor.seed").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", seed);
            return seed;
        }
    }
}
