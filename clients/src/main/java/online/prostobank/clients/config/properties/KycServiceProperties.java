package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class KycServiceProperties {

    @Value("${kontur.retreive.info.on}")
    private int isKontur;

    private final DbPropertiesServiceI propertyService;

    public KycServiceProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public int getIsKontur() {
        try {
            return Integer.valueOf(
                    propertyService
                            .getPropertyByKey("kontur.retreive.info.on")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", isKontur);
            return isKontur;
        }
    }
}
