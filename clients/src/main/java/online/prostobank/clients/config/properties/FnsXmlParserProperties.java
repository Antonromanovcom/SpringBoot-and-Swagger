package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class FnsXmlParserProperties {

    @Value("${ftp.baseUrl}")  private String baseUrl;
    @Value("${ftp.host}")     private String host;
    @Value("${ftp.port}")     private int    port;
    @Value("${ftp.userName}") private String userName;
    @Value("${ftp.password}") private String pass;

    private final DbPropertiesServiceI propertyService;

    public FnsXmlParserProperties(@Nonnull DbPropertiesServiceI propertyService) {
        this.propertyService = propertyService;
    }

    public @Nonnull String getBaseUrl() {
        try {
            return propertyService.getPropertyByKey("ftp.baseUrl").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", baseUrl);
            return baseUrl;
        }
    }

    public @Nonnull String getHost() {
        try {
            return propertyService.getPropertyByKey("ftp.host").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", host);
            return host;
        }
    }

    public int getPort() {
        try {
            return Integer.valueOf(
                    propertyService
                            .getPropertyByKey("ftp.port")
                            .getValue()
            );
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", port);
            return port;
        }
    }

    public @Nonnull String getUserName() {
        try {
            return propertyService.getPropertyByKey("ftp.userName").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", userName);
            return userName;
        }
    }

    public @Nonnull String getPass() {
        try {
            return propertyService.getPropertyByKey("ftp.password").getValue();
        } catch (PropertyServiceException e) {
            log.trace("Unable to retrieve property from propertyService, using default one :: {}", pass);
            return pass;
        }
    }
}
