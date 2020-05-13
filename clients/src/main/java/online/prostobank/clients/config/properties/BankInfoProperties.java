package online.prostobank.clients.config.properties;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.BankInformation;
import online.prostobank.clients.domain.enums.BankId;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Nonnull;

/**
 * Простой загрузчик информации о банках из специального проперти-файла
 *
 * @author yurij
 */
@Slf4j
@Configuration
@PropertySource(value = "kub.properties")
@ConfigurationProperties(prefix = "kub", ignoreInvalidFields = false, ignoreUnknownFields = false)
public class BankInfoProperties implements BankInformation {

	private final DbPropertiesServiceI dbPropertiesServiceI;

	private String bik;
	private String inn;
	private String name;

	public BankInfoProperties(@Nonnull DbPropertiesServiceI dbPropertiesServiceI) {
		this.dbPropertiesServiceI = dbPropertiesServiceI;
	}

	public void setBik(String v) {
		this.bik = v;
	}

	public void setInn(String v) {
		this.inn = v;
	}

	public void setName(String v) {
		this.name = v;
	}

	@Override
	public String getBik(BankId bank) {
		if (!BankId.KUB.equals(bank)) {
			throw new IllegalArgumentException("KUB supported only");
		}
		try {
			return dbPropertiesServiceI.getPropertyByKey("kub.properties.bik").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", bik);
			return bik;
		}
	}

	@Override
	public String getInn(BankId bank) {
		if (!BankId.KUB.equals(bank)) {
			throw new IllegalArgumentException("KUB supported only");
		}

		try {
			return dbPropertiesServiceI.getPropertyByKey("kub.properties.inn").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", inn);
			return inn;
		}
	}

	@Override
	public String getName(BankId bank) {
		if (!BankId.KUB.equals(bank)) {
			throw new IllegalArgumentException("KUB supported only");
		}
		try {
			return dbPropertiesServiceI.getPropertyByKey("kub.properties.name").getValue();
		} catch (PropertyServiceException e) {
			log.trace("Unable to retrieve property from propertyService, using default one :: {}", name);
			return name;
		}
	}
}
