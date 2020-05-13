package online.prostobank.clients.services.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.rest.AmlClientDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.client.ClientKeycloak;
import online.prostobank.clients.domain.client.ClientKeycloakRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientAmlModuleService {

	private final AccountApplicationRepository accountApplicationRepository;
	private final ClientKeycloakRepository clientKeycloakRepository;
	private final ClientDetailServiceImpl clientDetailService;

	/**
	 * Получение информации клиента по его номеру счёта
	 *
	 * @param accountNumber - номер счёта
	 * @return - инфо по клиенту
	 */
	@Transactional
	public Optional<AmlClientDTO> findByAccountNumber(String accountNumber) {
		final AccountApplication accountApplication = accountApplicationRepository
				.findByAccountAccountNumber(accountNumber);
		return Optional.ofNullable(accountApplication)
				.map(AmlClientDTO::createFrom)
				.map(amlClientDTOBuilder -> {
					clientDetailService.getClientBeneficiaries(accountApplication.getId()).map(amlClientDTOBuilder::beneficiary);
					return amlClientDTOBuilder.build();
				});
	}

	/**
	 * Получение информации клиента по его идектификатору в киклоке
	 *
	 * @param keycloakId - id клиента
	 * @return - инфо по клиенту
	 */
	@Transactional
	public Optional<AmlClientDTO> findByKeycloakId(UUID keycloakId) {
		final Optional<ClientKeycloak> keycloakClient = clientKeycloakRepository.findByKeycloakId(keycloakId);
		if (!keycloakClient.isPresent()) {
			return Optional.empty();
		}
		Long clientId = keycloakClient.get().getClientId();
		Optional<AccountApplication> client = accountApplicationRepository.findById(clientId);
		return client
				.map(AmlClientDTO::createFrom)
				.map(amlClientDTOBuilder -> {
					clientDetailService.getClientBeneficiaries(clientId).map(amlClientDTOBuilder::beneficiary);
					return amlClientDTOBuilder.build();
				});
	}
}
