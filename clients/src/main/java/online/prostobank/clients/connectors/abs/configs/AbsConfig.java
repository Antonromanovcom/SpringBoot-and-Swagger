package online.prostobank.clients.connectors.abs.configs;

import club.apibank.connectors.kub.AbsConnector;
import club.apibank.connectors.kub.AbsConnector.AbsRestOperations;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.config.properties.AbsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AbsConfig {
	private final AbsProperties properties;

	@Bean
	public AbsConnector absConnector(AbsRestOperations absRestOperations) {
		return new AbsConnector(
				properties.getAbsUrl(),
				absRestOperations,
				properties.getClientId(),
				properties.getClientCode(),
				properties.getClientSource(),
				properties.getPartnerId(),
				properties.getPartnerUser(),
				properties.getPartnerPass()
		);
	}
}
