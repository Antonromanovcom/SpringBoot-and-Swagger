package online.prostobank.clients.config.properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TssProperties {
	private final DbPropertiesServiceI dbPropertiesServiceI;

	@Value("${app.tss-url}")
	private String tssUrl;
	@Value("${app.base-url}")
	private String baseUrl;

	@Nonnull
	public String getTssUrl() {
		return getProperty("app.tss-url", tssUrl);
	}

	@Nonnull
	public String getBaseUrl() {
		return getProperty("app.base-url", baseUrl);
	}

	private String getProperty(String valueName, String defaultProperty) {
		try {
			return dbPropertiesServiceI.getPropertyByKey(valueName).getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", defaultProperty);
		}
		return defaultProperty;
	}
}
