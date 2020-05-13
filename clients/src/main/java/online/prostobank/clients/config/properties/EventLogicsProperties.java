package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.DbPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class EventLogicsProperties {

    @Value("${app.email-debug:null}")            private String  emailDebugger;
    @Value("${app.email-front:null}")            private String  emailFront;
    @Value("${app.email-back:null}")             private String  emailBack;
    @Value("${app.email-delivery:null}")         private String  emailDelivery;
    @Value("${analytics.prefix:}")               private String  eventPrefix;
    @Value("${app.lk.url}")                      private String  lkUrl;
    @Value("${app.url}")                         private String  appUrl;
    @Value("${dboLink.url}")                     private String  dboUrl;
    @Value("${google.analytics.url}")            private String  googleAnalyticsUrl;
    @Value("${google.analytics.version}")        private String  googleAnalyticsVersion;
    @Value("${google.analytics.id}")             private String  googleAnalyticsId;
    @Value("${google.analytics.type}")           private String  googleAnalyticsType;
    @Value("${google.analytics.event.category}") private String  googleAnalyticsEventCategory;
    @Value("${dev.confiramtion.code}")           private String  devConfirmationCode;
    @Value("${dev.confiramtion.code.switch}")    private Integer devConfirmationCodeSwitch;

    @Autowired private DbPropertiesService dbPropertiesService;

    public @Nonnull String getEmailDebugger() {
        try {
            return dbPropertiesService.getPropertyByKey("app.email-debug").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", emailDebugger);
            return emailDebugger;
        }
    }

    public @Nonnull String getEmailFront() {
        try {
            return dbPropertiesService.getPropertyByKey("app.email-front").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", emailFront);
            return emailFront;
        }
    }

    public @Nonnull String getEmailBack() {
        try {
            return dbPropertiesService.getPropertyByKey("app.email-back").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", emailBack);
            return emailBack;
        }
    }

    public @Nonnull String getEmailDelivery() {
        try {
            return dbPropertiesService.getPropertyByKey("app.email-delivery").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", emailDelivery);
            return emailDelivery;
        }
    }

    public @Nonnull String getEventPrefix() {
        try {
            return dbPropertiesService.getPropertyByKey("analytics.prefix").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", eventPrefix);
            return eventPrefix;
        }
    }

    public @Nonnull String getLkUrl() {
        try {
            return dbPropertiesService.getPropertyByKey("app.lk.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", lkUrl);
            return lkUrl;
        }
    }

    public @Nonnull String getAppUrl() {
        try {
            return dbPropertiesService.getPropertyByKey("app.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", appUrl);
            return appUrl;
        }
    }

    public @Nonnull String getDboUrl() {
        try {
            return dbPropertiesService.getPropertyByKey("dboLink.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", dboUrl);
            return dboUrl;
        }
    }

    public @Nonnull String getGoogleAnalyticsUrl() {
        try {
            return dbPropertiesService.getPropertyByKey("google.analytics.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", googleAnalyticsUrl);
            return googleAnalyticsUrl;
        }
    }

    public @Nonnull String getGoogleAnalyticsVersion() {
        try {
            return dbPropertiesService.getPropertyByKey("google.analytics.version").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", googleAnalyticsVersion);
            return googleAnalyticsVersion;
        }
    }

    public @Nonnull String getGoogleAnalyticsId() {
        try {
            return dbPropertiesService.getPropertyByKey("google.analytics.id").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", googleAnalyticsId);
            return googleAnalyticsId;
        }
    }

    public @Nonnull String getGoogleAnalyticsType() {
        try {
            return dbPropertiesService.getPropertyByKey("google.analytics.type").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", googleAnalyticsType);
            return googleAnalyticsType;
        }
    }

    public @Nonnull String getGoogleAnalyticsEventCategory() {
        try {
            return dbPropertiesService.getPropertyByKey("google.analytics.event.category").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", googleAnalyticsEventCategory);
            return googleAnalyticsEventCategory;
        }
    }

    public @Nonnull String getDevConfirmationCode() {
        try {
            return dbPropertiesService.getPropertyByKey("dev.confiramtion.code").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", devConfirmationCode);
            return devConfirmationCode;
        }
    }

    public @Nonnull Integer getDevConfirmationCodeSwitch() {
        try {
            return Integer.valueOf(
                    dbPropertiesService
                            .getPropertyByKey("dev.confiramtion.code.switch")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", devConfirmationCodeSwitch);
            return devConfirmationCodeSwitch;
        }
    }
}
