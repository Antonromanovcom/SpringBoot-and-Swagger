package online.prostobank.clients.config.properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

@Slf4j
@Configuration
public class KonturServiceProperties {

    @Value("${kontur.api-key}")               private String     apiKey;
    @Value("${kontur.base-url}")              private String     baseUrl;
    @Value("${kontur.auth-url}")              private String     authUrl;
    @Value("${kontur.auth-login}")            private String     authLogin;
    @Value("${kontur.auth-password}")         private String     authPassword;
    @Value("${kontur.allowed.till.val}")      private BigDecimal allowedTill;
    @Value("${kontur.company.info.base-url}") private String     companyInfoBaseUrl;
    @Value("${bigdata.kyc.base-url}")         private String     bigDataBaseUrl;
    @Value("${bigdata.kyc.auth-url}")         private String     bigDataAuthUrl;
    @Value("${bigdata.kyc.on}")               private String     isBigDataKycOn;

    //todo: implement custom getters
    @Getter @Value("${fns.db}")                       private String     fnsBaseUrl;
    @Getter @Value("${fns.username}")                 private String     fnsBaseUserName;
    @Getter @Value("${fns.password}")                 private String     fnsBasePassword;

    private final DbPropertiesServiceI propertyService;

    public KonturServiceProperties(DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getApiKey() {
        try {
            return propertyService.getPropertyByKey("kontur.api-key").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", apiKey);
            return apiKey;
        }
    }

    public @Nonnull String getBaseUrl() {
        try {
            return propertyService.getPropertyByKey("kontur.base-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", baseUrl);
            return baseUrl;
        }
    }

    public @Nonnull String getAuthUrl() {
        try {
            return propertyService.getPropertyByKey("kontur.auth-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", authUrl);
            return authUrl;
        }
    }

    public @Nonnull String getAuthLogin() {
        try {
            return propertyService.getPropertyByKey("kontur.auth-login").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", authLogin);
            return authLogin;

        }
    }

    public @Nonnull String getAuthPassword() {
        try {
            return propertyService.getPropertyByKey("kontur.auth-password").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", authPassword);
            return authPassword;
        }
    }

    public @Nonnull BigDecimal getAllowedTill() {
        try {
            return new BigDecimal(
                    propertyService
                            .getPropertyByKey("kontur.allowed.till.val")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", allowedTill);
            return allowedTill;
        }
    }

    public @Nonnull String getCompanyInfoBaseUrl() {
        try {
            return propertyService.getPropertyByKey("kontur.company.info.base-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", companyInfoBaseUrl);
            return companyInfoBaseUrl;
        }
    }

    public @Nonnull String getBigDataBaseUrl() {
        try {
            return propertyService.getPropertyByKey("bigdata.kyc.base-url}").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", bigDataBaseUrl);
            return bigDataBaseUrl;
        }
    }

    public @Nonnull String getBigDataAuthUrl() {
        try {
            return propertyService.getPropertyByKey("bigdata.kyc.auth-url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", bigDataAuthUrl);
            return bigDataAuthUrl;
        }
    }

    public @Nonnull String getIsBigDataKycOn() {
        try {
            return propertyService.getPropertyByKey("bigdata.kyc.on").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", isBigDataKycOn);
            return isBigDataKycOn;
        }
    }
}
