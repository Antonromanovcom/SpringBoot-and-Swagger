package online.prostobank.clients.config.properties;

import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
@ConditionalOnProperty(value = "inbound-mail.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "inbound-mail")
public class InboundEmailConfigProperties {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(InboundEmailConfigProperties.class);

    private final DbPropertiesServiceI dbPropertiesServiceI;

    private String url;
    private String successFolder;
    private String failedFolder;
    private int maxMessagesPerPoll;
    private int fixedDelay;
    private int timeout;

    public InboundEmailConfigProperties(@Nonnull DbPropertiesServiceI dbPropertiesServiceI) {
        this.dbPropertiesServiceI = dbPropertiesServiceI;
    }


    public @Nonnull String getUrl() {
        try {
            return dbPropertiesServiceI.getPropertyByKey("inbound-mail.url").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", url);
            return url;
        }
    }

    public @Nonnull String getSuccessFolder() {
        try {
            return dbPropertiesServiceI.getPropertyByKey("inbound-mail.success-folder").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", successFolder);
            return successFolder;
        }
    }

    public @Nonnull String getFailedFolder() {
        try {
            return dbPropertiesServiceI.getPropertyByKey("inbound-mail.failed-folder").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", failedFolder);
            return failedFolder;
        }
    }

    public int getMaxMessagesPerPoll() {
        try {
            return Integer.parseInt(
                    dbPropertiesServiceI
                            .getPropertyByKey("inbound-mail.maxMessagesPerPoll")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", maxMessagesPerPoll);
            return maxMessagesPerPoll;
        }
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public int getFixedDelay() {
        try {
            return Integer.parseInt(
                    dbPropertiesServiceI
                            .getPropertyByKey("inbound-mail.fixedDelay")
                            .getValue()
            );
        } catch ( PropertyServiceException e ) {
            log.trace("Unable to retrieve property from service, using default one :: {}", fixedDelay);
            return fixedDelay;
        }
    }

    public void setFixedDelay(int fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    public int getTimeout() {
        try {
            return Integer.parseInt(
                    dbPropertiesServiceI.getPropertyByKey("inbound-mail.timeout").getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from service, using default one :: {}", timeout);
            return timeout;
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSuccessFolder(String successFolder) {
        this.successFolder = successFolder;
    }

    public void setFailedFolder(String failedFolder) {
        this.failedFolder = failedFolder;
    }
}
