package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.DbPropertiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class JmsEventsProperties {

    @Value("#{'${sms.blacklisted}'.split(',')}")
    private List<String> blacklistedSms;

    private final DbPropertiesService dbPropertiesService;

    public JmsEventsProperties(DbPropertiesService dbPropertiesService) {
        this.dbPropertiesService = dbPropertiesService;
    }

    public @Nonnull List<String> getBlacklistedSms() {
        try {
            return Arrays.stream(
                    dbPropertiesService
                            .getPropertyByKey("sms.blacklisted")
                            .getValue()
                            .split(",")
            ).collect(Collectors.toList());
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from dbPropertiesService, using default one :: {}", blacklistedSms);
            return blacklistedSms;
        }
    }
}
