package online.prostobank.clients.config.properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DboProperties {
	@Value("${dbo.enable}")
	private String enable;
	@Value("${dbo.queue.enabled}")
	private String queueEnable;
	@Value("${dbo.checkOld.enabled}")
	private String checkOld;
	@Value("${dbo.host}")
	private String host;
	@Value("${dbo.apiUrl}")
	private String dboApi;
	@Value("${dbo.usersUrl}")
	private String users;

	private final DbPropertiesServiceI propertyService;

	public Boolean getEnable() {
		try {
			return Boolean.valueOf(propertyService.getPropertyByKey("dbo.enable").getValue());
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", enable);
			return Boolean.valueOf(enable);
		}
	}

	public Boolean getQueueEnable() {
		try {
			return Boolean.valueOf(propertyService.getPropertyByKey("dbo.queue.enabled").getValue());
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", queueEnable);
			return Boolean.valueOf(queueEnable);
		}
	}

	public Boolean getCheckOld() {
		try {
			return Boolean.valueOf(propertyService.getPropertyByKey("dbo.checkOld").getValue());
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", checkOld);
			return Boolean.valueOf(checkOld);
		}
	}

	public String getHost() {
		try {
			return propertyService.getPropertyByKey("dbo.host").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", host);
			return host;
		}
	}

	public String getDboApi() {
		try {
			return propertyService.getPropertyByKey("dbo.apiUrl").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", dboApi);
			return dboApi;
		}
	}

	public String getUsers() {
		try {
			return propertyService.getPropertyByKey("dbo.usersUrl").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", users);
			return users;
		}
	}
}
