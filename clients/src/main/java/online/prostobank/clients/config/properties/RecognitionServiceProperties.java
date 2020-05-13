package online.prostobank.clients.config.properties;

import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class RecognitionServiceProperties {
    @Value("${smartengines.trial:false}")
    private boolean isTrialMode;

    private final DbPropertiesServiceI propertyService;

    public RecognitionServiceProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public boolean isTrialMode() {
        try {
            return Boolean.valueOf(
                    propertyService
                            .getPropertyByKey("smartengines.trial")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            return isTrialMode;
        }
    }
}
