package online.prostobank.clients.api.anketa;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.anketa.AccountApplicationDTO;
import online.prostobank.clients.api.dto.anketa.ApplicationAcceptedDTO;
import online.prostobank.clients.api.dto.anketa.OrganizationDto;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.services.anketa.AnketaService;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static online.prostobank.clients.api.ApiConstants.CHATBOT_CONTROLLER;

@Slf4j
@Benchmark
@JsonLogger
@RestController
@RequestMapping(
		value = CHATBOT_CONTROLLER,
		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class ChatbotEndpoint extends MainAnketaEndpoint {
	public ChatbotEndpoint(AnketaService anketaService) {
		super(anketaService);
	}

	@Override
	protected Source getSource() {
		return Source.CHATBOT;
	}

	@Override
	protected String[] getAllowedMethods() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseEntity<OrganizationDto> getOrganizationInfo(
			String csrf,
			String inn,
			String phone
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseEntity<ApplicationAcceptedDTO> createApplicationConfirmed(
			String csrf,
			AccountApplicationDTO dto
	) {
		throw new UnsupportedOperationException();
	}
}
