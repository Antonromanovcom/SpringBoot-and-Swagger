package online.prostobank.clients.config.properties;

import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class AbsProperties {
    private static final Logger log = LoggerFactory.getLogger(AbsProperties.class);

    @Value("${abs.url}")
    private String absUrl;
    @Value("${abs.client-id}")
    private int clientId;
    @Value("${abs.client-code}")
    private String clientCode;
    @Value("${abs.client-source}")
    private String clientSource;
    @Value("${abs.partner_id}")
    private int partnerId;
    @Value("${abs.partner_user}")
    private String partnerUser;
    @Value("${abs.partner_pass}")
    private String partnerPass;

    private final DbPropertiesServiceI propertyService;

    public AbsProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getAbsUrl() {
        try {
            return propertyService.getPropertyByKey("abs.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", absUrl);
            return absUrl;
        }
    }

    public int getClientId() {
        try {
            return Integer.parseInt(
                    propertyService
                            .getPropertyByKey("abs.client-id")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", clientId);
            return clientId;
        }
    }

    public @Nonnull String getClientCode() {
        try {
            return propertyService.getPropertyByKey("abs.client-code").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", clientCode);
            return clientCode;
        }
    }

    public @Nonnull String getClientSource() {
        try {
            return propertyService.getPropertyByKey("abs.client-source").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", clientSource);
            return clientSource;
        }
    }

    public int getPartnerId() {
        try {
            return Integer.parseInt(propertyService.getPropertyByKey("abs.partner_id").getValue());
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", partnerId);
            return partnerId;
        }
    }

    public String getPartnerUser() {
        try {
            return propertyService.getPropertyByKey("abs.partner_user").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", partnerUser);
            return partnerUser;
        }
    }

    public String getPartnerPass() {
        try {
            return propertyService.getPropertyByKey("abs.partner_pass").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", partnerPass);
            return partnerPass;
        }
    }
}
