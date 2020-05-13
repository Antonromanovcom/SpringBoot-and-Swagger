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
public class MainAnketaEndpointProperties {
	@Value("${is.utm.active}")
	private int isUtmActive;

	@Value("${kontur.retreive.info.on}")
	private int isKontur;

	@Value("${sms.partner}")
	private String partner;

	@Value("${app.lk.url}")
	private String lkUrl;

	private final DbPropertiesServiceI propertyService;

	public int getIsUtmActive() {
		return Integer.parseInt(getString("is.utm.active", String.valueOf(isUtmActive)));
	}

	public int getIsKontur() {
		return Integer.parseInt(getString("kontur.retreive.info.on", String.valueOf(isKontur)));
	}

	@Nonnull
	public String getPartner() {
		return getString("sms.partner", partner);
	}

	@Nonnull
	public String getLkUrl() {
		return getString("app.lk.url", lkUrl);
	}

	private String getString(String valueName, String defaultProperty) {
		try {
			return propertyService.getPropertyByKey(valueName).getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", defaultProperty);
			return defaultProperty;
		}
	}
}
