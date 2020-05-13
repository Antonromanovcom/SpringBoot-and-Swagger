package online.prostobank.clients.services.dbo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.DboProperties;
import online.prostobank.clients.services.dbo.model.DboRequestCreateUserDto;
import online.prostobank.clients.services.dbo.model.DboResponseCreateUserDto;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static online.prostobank.clients.domain.enums.EventName.DBO_NAME;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getTokenString;
import static online.prostobank.clients.services.dbo.service.DboHelper.getHttpHeaders;

@JsonLogger
@Slf4j
@RequiredArgsConstructor
@Service
public class DboServiceImpl implements DboService {
	private final RestTemplate restTemplate;
	private final DboProperties dboProperties;
	private final JmsTemplate sender;

	@Async
	@Override
	public void sentNewUser(DboRequestCreateUserDto dto) {
		if (!Boolean.TRUE.equals(dboProperties.getEnable())) {
			return;
		}
		String host = dboProperties.getHost();
		String dboApi = dboProperties.getDboApi();
		String users = dboProperties.getUsers();

		log.info("sent request to dbo, {}", dboApi + users);
		log.info(dto.toString());

		if (dboProperties.getQueueEnable()) {
			sender.convertAndSend(DBO_NAME, dto);
		} else {
			HttpEntity<DboRequestCreateUserDto> request = new HttpEntity<>(dto, getHttpHeaders(getTokenString().orElse(StringUtils.EMPTY)));
			try {
				ResponseEntity<DboResponseCreateUserDto> response = restTemplate.exchange(host + dboApi + users,
						HttpMethod.POST, request, DboResponseCreateUserDto.class);
				Optional.ofNullable(response.getBody())
						.ifPresent(body -> log.info(body.toString()));
			} catch (HttpClientErrorException e) {
				log.error("statusCode: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}
}
