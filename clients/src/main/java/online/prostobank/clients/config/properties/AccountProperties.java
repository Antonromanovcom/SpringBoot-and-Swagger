package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class AccountProperties {

	private final DbPropertiesServiceI dbPropertiesServiceI;

	@Value("${app.lk.url}")  private String accountUrl;
	@Value("${app.url}")     private String accountApplicationUrl;
	@Value("${dboLink.url}") private String dboUrl;

	public AccountProperties(@Nonnull DbPropertiesServiceI dbPropertiesServiceI) {
		this.dbPropertiesServiceI = dbPropertiesServiceI;
	}

	public @Nonnull String getAccountUrl() {
		try {
			return dbPropertiesServiceI.getPropertyByKey("app.lk.url").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", accountUrl);
			return accountUrl;
		}
	}

	public @Nonnull String getAccountApplicationUrl() {
		try {
			return dbPropertiesServiceI.getPropertyByKey("app.url").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", accountApplicationUrl);
		}
		return accountApplicationUrl;
	}

	public @Nonnull String getDboUrl() {
		try {
			return dbPropertiesServiceI.getPropertyByKey("dboLink.url").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", dboUrl);
			return dboUrl;
		}
	}
}
