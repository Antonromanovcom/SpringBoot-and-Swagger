package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class AccountApplicationProperties {

    @Value("${dev.confiramtion.code}")        private String  devConfirmationCode;
    @Value("${dev.confiramtion.code.switch}") private Integer devConfirmationCodeSwitch;
    @Value("${sms.time.gap}")                 private int     smsTimeGap;
    @Value("${sms.attempts.max}")             private int     maxSmsAttempts;
    @Value("${kontur.retreive.info.on}")      private int     isKontur;

    private final DbPropertiesServiceI propertyService;

    public AccountApplicationProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public String getDevConfirmationCode() {
        try {
            return propertyService.getPropertyByKey("dev.confiramtion.code").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", devConfirmationCode);
            return devConfirmationCode;
        }
    }

    public Integer getDevConfirmationCodeSwitch() {
        try {
            return Integer.valueOf(
                    propertyService
                            .getPropertyByKey("dev.confiramtion.code.switch")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", devConfirmationCodeSwitch);
            return devConfirmationCodeSwitch;
        }
    }

    public int getSmsTimeGap() {
        try {
            return Integer.valueOf(
                    propertyService
                            .getPropertyByKey("sms.time.gap")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", smsTimeGap);
            return smsTimeGap;
        }
    }

    public int getMaxSmsAttempts() {
        try {
            return Integer.valueOf(
                    propertyService
                            .getPropertyByKey("sms.attempts.max")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", maxSmsAttempts);
            return maxSmsAttempts;
        }
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
