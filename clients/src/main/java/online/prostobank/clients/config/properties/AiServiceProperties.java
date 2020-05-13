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
public class AiServiceProperties {
	private final DbPropertiesServiceI dbPropertiesServiceI;

	@Value("${ai.url-init-scoring:}")
	private String urlInitPrediction;

	@Value("${ai.url-get-result:}")
	private String urlGetPrediction;

	@Value("${ai.page-size:1}")
	private Integer pageSize;

	@Nonnull
	public String getUrlInitPrediction() {
		return getProperty("ai.url-init-scoring", urlInitPrediction == null ? "" : urlInitPrediction);
	}

	@Nonnull
	public String getUrlObtainingPrediction() {
		return getProperty("ai.url-get-result", urlGetPrediction == null ? "" : urlGetPrediction);
	}

	public int getPageSize() {
		try {
			return Math.max(1, Integer.parseInt(getProperty("ai.page-size", pageSize == null ? "1" : pageSize.toString())));
		} catch (NumberFormatException ex) {
			return 0;
		}
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
