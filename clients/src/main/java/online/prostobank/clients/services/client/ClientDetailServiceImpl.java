package online.prostobank.clients.services.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client_detail.*;
import online.prostobank.clients.domain.client.*;
import online.prostobank.clients.domain.enums.HistoryItemType;
import online.prostobank.clients.domain.repository.HistoryRepository;
import online.prostobank.clients.domain.repository.client.ClientDetailRepository;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import online.prostobank.clients.utils.ToHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientDetailServiceImpl implements ClientDetailService {
	private final ClientDetailRepository clientDetailRepository;
	private final HistoryRepository historyRepository;

	/**
	 * Получение списка сотрудников клиента
	 *
	 * @param clientId
	 * @return
	 */
	@Override
	public Optional<List<EmployeeDTO>> getClientEmployees(Long clientId) {
		if (clientId == null || !clientDetailRepository.isClientExists(clientId)) {
			return Optional.empty();
		}
		return Optional.of(clientDetailRepository.getClientEmployees(clientId).stream().map(EmployeeDTO::new).collect(Collectors.toList()));
	}

	/**
	 * Получение списка бенефициаров клиента
	 *
	 * @param clientId
	 * @return
	 */
	@Override
	public Optional<List<BeneficiaryDTO>> getClientBeneficiaries(Long clientId) {
		if (clientId == null || !clientDetailRepository.isClientExists(clientId)) {
			return Optional.empty();
		}
		return Optional.of(clientDetailRepository.getClientBeneficiaries(clientId).stream().map(BeneficiaryDTO::new).collect(Collectors.toList()));
	}

	/**
	 * Создание нового сотрудника
	 * @param clientId
	 * @param employeeDTO
	 * @return
	 */
	@Override
	public Optional<EmployeeDTO> createEmployee(Long clientId, EmployeeDTO employeeDTO) throws IllegalArgumentException {
		if (clientId == null || !clientDetailRepository.isClientExists(clientId)) {
			return Optional.empty();
		}
		HumanDTO humanDTO = employeeDTO.getHuman();
		if (humanDTO == null) {
			throw new IllegalArgumentException("Отсутствуют данные о физическом лице");
		}
		Human human = new Human(null, humanDTO.getFirstName(), humanDTO.getMiddleName(), humanDTO.getLastName(),
				humanDTO.getSnils(), humanDTO.getRegistrationAddress(), humanDTO.getInn(), humanDTO.getCitizenship());
		if (humanDTO.getPassports() != null) {
			human.setPassport(
					humanDTO.getPassports().stream().map(HumanPassportDTO::toBusiness).collect(Collectors.toList())
			);
		}

		List<Email> emails = employeeDTO.getEmails() != null ?
				employeeDTO.getEmails().stream().map(EmailDTO::toBusiness).collect(Collectors.toList())
				:
				Collections.emptyList();
		List<Phone> phones = employeeDTO.getPhones() != null ?
				employeeDTO.getPhones().stream().map(PhoneDTO::toBusiness).collect(Collectors.toList())
				:
				Collections.emptyList();
		Employee employee = new Employee(null, employeeDTO.getPosition(), human, phones, emails);
		ObjectMapper mapper = new ObjectMapper();
		try {
			EmployeeDTO resultDTO = new EmployeeDTO(employee);
			String message = "Добавлен сотрудник " + ToHistoryMapper.toHistory(resultDTO) + " " + mapper.writeValueAsString(resultDTO);
			historyRepository.insertChangeHistory(clientId, SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}

		return clientDetailRepository.addEmployee(clientId, employee)
				.map(EmployeeDTO::new);
	}

	/**
	 * Создание нового бенефициара
	 *
	 * @param clientId
	 * @param beneficiaryDTO
	 * @return
	 */
	@Override
	public Optional<BeneficiaryDTO> createBeneficiary(Long clientId, BeneficiaryDTO beneficiaryDTO) throws IllegalArgumentException {
		if (clientId == null || !clientDetailRepository.isClientExists(clientId)) {
			return Optional.empty();
		}
		HumanDTO humanDTO = beneficiaryDTO.getHuman();
		if (humanDTO == null) {
			throw new IllegalArgumentException("Отсутствуют данные о физическом лице");
		}
		Human human = new Human(null, humanDTO.getFirstName(), humanDTO.getMiddleName(), humanDTO.getLastName(),
				humanDTO.getSnils(), humanDTO.getRegistrationAddress(), humanDTO.getInn(), humanDTO.getCitizenship());
		if (humanDTO.getPassports() != null) {
			human.setPassport(
					humanDTO.getPassports().stream().map(HumanPassportDTO::toBusiness).collect(Collectors.toList())
			);
		}

		List<Email> emails = beneficiaryDTO.getEmails() != null ?
				beneficiaryDTO.getEmails().stream().map(EmailDTO::toBusiness).collect(Collectors.toList())
				:
				Collections.emptyList();
		List<Phone> phones = beneficiaryDTO.getPhones() != null ?
				beneficiaryDTO.getPhones().stream().map(PhoneDTO::toBusiness).collect(Collectors.toList())
				:
				Collections.emptyList();
		Beneficiary beneficiary = new Beneficiary(null, human, phones, emails, beneficiaryDTO.getStakePercent(), beneficiaryDTO.getStakeAbsolute());
		ObjectMapper mapper = new ObjectMapper();
		try {
			BeneficiaryDTO resultDTO = new BeneficiaryDTO(beneficiary);
			String message = "Добавлен бенефициар " + ToHistoryMapper.toHistory(resultDTO) + " " + mapper.writeValueAsString(resultDTO);
			historyRepository.insertChangeHistory(clientId, SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}

		return clientDetailRepository.addBeneficiary(clientId, beneficiary)
				.map(BeneficiaryDTO::new);
	}

	@Override
	public Optional<EmployeeDTO> editEmployee(EmployeeDTO employeeDTO) {
		if (employeeDTO == null || employeeDTO.getId() == null || !clientDetailRepository.isEmployeeExists(employeeDTO.getId())) {
			return Optional.empty();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String message = "Изменены сведения о сотруднике, новые данные: " + ToHistoryMapper.toHistory(employeeDTO)
					+ " " + mapper.writeValueAsString(employeeDTO);
			historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByEmployee(employeeDTO.getId()),
					SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}
		return clientDetailRepository.editEmployee(employeeDTO.toBusiness()).map(EmployeeDTO::new);
	}

	@Override
	public Optional<BeneficiaryDTO> editBeneficiary(BeneficiaryDTO beneficiaryDTO) {
		if (beneficiaryDTO == null || beneficiaryDTO.getId() == null || !clientDetailRepository.isBeneficiaryExists(beneficiaryDTO.getId())) {
			return Optional.empty();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String message = "Изменены сведения о бенефициаре, новые данные: " + ToHistoryMapper.toHistory(beneficiaryDTO)
					+ " " + mapper.writeValueAsString(beneficiaryDTO);
			historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByBeneficiary(beneficiaryDTO.getId()),
					SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}
		return clientDetailRepository.editBeneficiary(beneficiaryDTO.toBusiness()).map(BeneficiaryDTO::new);
	}

	@Override
	public Optional<HumanDTO> editHuman(HumanDTO humanDTO) {
		if (humanDTO == null || humanDTO.getId() == null || !clientDetailRepository.isHumanExists(humanDTO.getId())) {
			return Optional.empty();
		}
		ObjectMapper mapper = new ObjectMapper();
		String message = "Изменены сведения о физлице, новые данные";
		try {
			message += ToHistoryMapper.toHistory(humanDTO) + " " + mapper.writeValueAsString(humanDTO);
		} catch (JsonProcessingException ex) {
			log.error("Не удалось сериализовать данные о физлице", ex);
		}
		String historyMessage = message;
		clientDetailRepository.getClientsIdByHuman(humanDTO.getId()).forEach(clientId -> {
					try {
						historyRepository.insertChangeHistory(clientId,
								SecurityContextHelper.getCurrentUsername(), historyMessage, HistoryItemType.DATA_CHANGE);
					} catch (Exception ex) {
						log.error("Не удалось записать историю", ex);
					}
				}
		);
		return clientDetailRepository.editHuman(humanDTO.toBusiness()).map(HumanDTO::new);
	}

	@Override
	public boolean deleteEmployee(Long employeeId) {
		if (employeeId == null) {
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		Long clientId = clientDetailRepository.getClientIdByEmployee(employeeId);
		clientDetailRepository.getClientEmployees(clientId).stream()
				.filter(it -> it.getId().equals(employeeId))
				.findFirst().ifPresent(it -> {
			try {
				EmployeeDTO previousDTO = new EmployeeDTO(it);
				String message = "Удалены данные о сотруднике " + ToHistoryMapper.toHistory(previousDTO) + " " + mapper.writeValueAsString(previousDTO);
				historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByEmployee(it.getId()),
						SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
			} catch (Exception ex) {
				log.error("Не удалось записать историю", ex);
			}
		});

		return clientDetailRepository.deleteEmployee(employeeId);
	}

	@Override
	public boolean deleteBeneficiary(Long beneficiaryId) {
		if (beneficiaryId == null) {
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		Long clientId = clientDetailRepository.getClientIdByBeneficiary(beneficiaryId);
		clientDetailRepository.getClientBeneficiaries(clientId).stream()
				.filter(it -> it.getId().equals(beneficiaryId))
				.findFirst().ifPresent(it -> {
			try {
				BeneficiaryDTO previousDTO = new BeneficiaryDTO(it);
				String message = "Удалены данные о бенефициаре " + ToHistoryMapper.toHistory(previousDTO) + " " + mapper.writeValueAsString(previousDTO);
				historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByBeneficiary(it.getId()),
						SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
			} catch (Exception ex) {
				log.error("Не удалось записать историю", ex);
			}
		});
		return clientDetailRepository.deleteBeneficiary(beneficiaryId);
	}

	/**
	 * Добавить паспорт человеку
	 *
	 * @param humanId
	 * @param passportDTO
	 * @return
	 */
	@Override
	public Optional<HumanPassportDTO> addPassportToHuman(Long humanId, HumanPassportDTO passportDTO) {
		if (humanId == null || !clientDetailRepository.isHumanExists(humanId)) {
			return Optional.empty();
		}
		ObjectMapper mapper = new ObjectMapper();
		String message = "Добавлен паспорт физлицу ";
		try {
			message += ToHistoryMapper.toHistory(passportDTO) + " " +mapper.writeValueAsString(passportDTO);
		} catch (JsonProcessingException ex) {
			log.error("Не удалось сериализовать данные о паспорте", ex);
		}
		String historyMessage = message;
		clientDetailRepository.getClientsIdByHuman(humanId).forEach(clientId -> {
					try {
						historyRepository.insertChangeHistory(clientId,
								SecurityContextHelper.getCurrentUsername(), historyMessage, HistoryItemType.DATA_CHANGE);
					} catch (Exception ex) {
						log.error("Не удалось записать историю", ex);
					}
				}
		);

		return clientDetailRepository.addPassportToHuman(humanId, passportDTO.toBusiness()).map(HumanPassportDTO::new);
	}

	/**
	 * Удалить указанный паспорт
	 *
	 * @param passportId
	 * @return
	 */
	@Override
	public boolean deletePassport(Long passportId) {
		if (passportId == null) {
			return false;
		}
		String historyMessage = "Удален паспорт физлица ";
		clientDetailRepository.getClientsIdByPassport(passportId).forEach(clientId -> {
					try {
						historyRepository.insertChangeHistory(clientId,
								SecurityContextHelper.getCurrentUsername(), historyMessage, HistoryItemType.DATA_CHANGE);
					} catch (Exception ex) {
						log.error("Не удалось записать историю", ex);
					}
				}
		);

		return clientDetailRepository.deletePassport(passportId);
	}

	/**
	 * Добавить телефон сотруднику
	 *
	 * @param employeeId
	 * @param phoneDTO
	 * @return
	 */
	@Override
	public Optional<PhoneDTO> addPhoneToEmployee(Long employeeId, PhoneDTO phoneDTO) {
		if (employeeId == null || phoneDTO == null || !clientDetailRepository.isEmployeeExists(employeeId)) {
			return Optional.empty();
		}
		try {
			String message = String.format("Добавлен телефон %s сотруднику employeeId = %s", phoneDTO.getValue(), employeeId);
			historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByEmployee(employeeId),
					SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}
		return clientDetailRepository.addPhoneToEmployee(employeeId, phoneDTO.toBusiness()).map(PhoneDTO::new);
	}

	/**
	 * Удалить телефон сотрудника
	 *
	 * @param phoneId
	 * @return
	 */
	@Override
	public boolean deletePhoneEmployee(Long phoneId) {
		if (phoneId == null) {
			return false;
		}
		return clientDetailRepository.deletePhoneEmployee(phoneId);
	}

	/**
	 * Добавить почту сотруднику
	 *
	 * @param employeeId
	 * @param emailDTO
	 * @return
	 */
	@Override
	public Optional<EmailDTO> addEmailToEmployee(Long employeeId, EmailDTO emailDTO) {
		if (employeeId == null || emailDTO == null || !clientDetailRepository.isEmployeeExists(employeeId)) {
			return Optional.empty();
		}
		try {
			String message = String.format("Добавлена почта %s сотруднику employeeId = %s", emailDTO.getValue(), employeeId);
			historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByEmployee(employeeId),
					SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}
		return clientDetailRepository.addEmailToEmployee(employeeId, emailDTO.toBusiness()).map(EmailDTO::new);
	}

	/**
	 * Удаление почты сотрудника
	 *
	 * @param emailId
	 * @return
	 */
	@Override
	public boolean deleteEmailEmployee(Long emailId) {
		if (emailId == null) {
			return false;
		}
		return clientDetailRepository.deleteEmailEmployee(emailId);
	}

	/**
	 * Добавить телефон бенефициару
	 *
	 * @param beneficiaryId
	 * @param phoneDTO
	 * @return
	 */
	@Override
	public Optional<PhoneDTO> addPhoneToBeneficiary(Long beneficiaryId, PhoneDTO phoneDTO) {
		if (beneficiaryId == null || phoneDTO == null || !clientDetailRepository.isBeneficiaryExists(beneficiaryId)) {
			return Optional.empty();
		}
		try {
			String message = String.format("Добавлен телефон %s бенефициару beneficiaryId = %s", phoneDTO.getValue(), beneficiaryId);
			historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByBeneficiary(beneficiaryId),
					SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}
		return clientDetailRepository.addPhoneToBeneficiary(beneficiaryId, phoneDTO.toBusiness()).map(PhoneDTO::new);
	}

	/**
	 * Удалить телефон бенефициара
	 *
	 * @param phoneId
	 * @return
	 */
	@Override
	public boolean deletePhoneBeneficiary(Long phoneId) {
		if (phoneId == null) {
			return false;
		}
		return clientDetailRepository.deletePhoneBeneficiary(phoneId);
	}

	/**
	 * Добавить почту бенефициару
	 *
	 * @param beneficiaryId
	 * @param emailDTO
	 * @return
	 */
	@Override
	public Optional<EmailDTO> addEmailToBeneficiary(Long beneficiaryId, EmailDTO emailDTO) {
		if (beneficiaryId == null || emailDTO == null || !clientDetailRepository.isBeneficiaryExists(beneficiaryId)) {
			return Optional.empty();
		}
		try {
			String message = String.format("Добавлена почта %s бенефициару beneficiaryId = %s", emailDTO.getValue(), beneficiaryId);
			historyRepository.insertChangeHistory(clientDetailRepository.getClientIdByBeneficiary(beneficiaryId),
					SecurityContextHelper.getCurrentUsername(), message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось записать историю", ex);
		}
		return clientDetailRepository.addEmailToBeneficiary(beneficiaryId, emailDTO.toBusiness()).map(EmailDTO::new);
	}

	/**
	 * Удаление почты бенефициара
	 *
	 * @param emailId
	 * @return
	 */
	@Override
	public boolean deleteEmailBeneficiary(Long emailId) {
		if (emailId == null) {
			return false;
		}
		return clientDetailRepository.deleteEmailBeneficiary(emailId);
	}
}
