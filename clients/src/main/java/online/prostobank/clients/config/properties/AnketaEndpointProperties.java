package online.prostobank.clients.config.properties;

import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class AnketaEndpointProperties {
    private static final Logger log = LoggerFactory.getLogger(AnketaEndpointProperties.class);

    @Value("${app.lk.url}")
    private String lkUrl;

    @Value("${app.url}")
    private String appUrl;

    private final DbPropertiesServiceI propertyService;

    public AnketaEndpointProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getLkUrl() {
        try {
            return propertyService.getPropertyByKey("app.lk.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", lkUrl);
            return lkUrl;
        }
    }

    public @Nonnull String getAppUrl() {
        try {
            return propertyService.getPropertyByKey("app.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", appUrl);
            return appUrl;
        }
    }
}
