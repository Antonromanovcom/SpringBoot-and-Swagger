package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class EgripServiceProperties {

    @Value("${egrul.file.parser.service.address}")
    private String parserUrl;

    private final DbPropertiesServiceI propertyService;

    public EgripServiceProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getParserUrl() {
        try {
            return propertyService.getPropertyByKey("egrul.file.parser.service.address").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", parserUrl);
            return parserUrl;
        }
    }
}
