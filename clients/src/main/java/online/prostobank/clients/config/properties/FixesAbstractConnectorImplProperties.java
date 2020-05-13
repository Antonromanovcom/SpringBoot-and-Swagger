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
public class FixesAbstractConnectorImplProperties {

    @Value("${simple.prosto-crypto-id:0}")     private long   prostoCryptoId;
    @Value("${simple.prosto-group-name:null}") private String prostoGroupName;

    private final DbPropertiesServiceI propertyService;

    public FixesAbstractConnectorImplProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public long getProstoCryptoId() {
        try {
            return Long.valueOf(
                    propertyService
                            .getPropertyByKey("simple.prosto-crypto-id")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", prostoCryptoId);
            return prostoCryptoId;
        }
    }

    public @Nullable String getProstoGroupName() {
        try {
            return propertyService.getPropertyByKey("").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", prostoGroupName);
            return prostoGroupName;
        }
    }
}
