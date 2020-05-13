package online.prostobank.clients.services.client;

import online.prostobank.clients.api.dto.client_detail.*;

import java.util.List;
import java.util.Optional;

public interface ClientDetailService {
	/**
	 * Получение списка сотрудников клиента
	 * @param clientId
	 * @return
	 */
	Optional<List<EmployeeDTO>> getClientEmployees(Long clientId);

	/**
	 * Получение списка бенефициаров клиента
	 * @param clientId
	 * @return
	 */
	Optional<List<BeneficiaryDTO>> getClientBeneficiaries(Long clientId);

	/**
	 * Создание нового сотрудника
	 * @param employeeDTO
	 * @return
	 */
	Optional<EmployeeDTO> createEmployee(Long clientId, EmployeeDTO employeeDTO) throws IllegalArgumentException;

	/**
	 * Создание нового бенефициара
	 * @param beneficiaryDTO
	 * @return
	 */
	Optional<BeneficiaryDTO> createBeneficiary(Long clientId, BeneficiaryDTO beneficiaryDTO) throws IllegalArgumentException;

	/**
	 * Изменить данные сотрудника (employee должен содержать валидный id)
	 * Редактируются только данные верхнего уровня (т.е. вложенные Human, Passport, Email, Phone не изменяются)
	 * @param employeeDTO
	 * @return
	 */
	Optional<EmployeeDTO> editEmployee(EmployeeDTO employeeDTO);

	/**
	 * Изменить данные бенефициара (beneficiary должен содержать валидный id)
	 * Редактируются только данные верхнего уровня (т.е. вложенные Human, Passport, Email, Phone не изменяются)
	 * @param beneficiaryDTO
	 * @return
	 */
	Optional<BeneficiaryDTO> editBeneficiary(BeneficiaryDTO beneficiaryDTO);

	/**
	 * Удаление сотрудника по указанному id
	 * @param employeeId
	 * @return
	 */
	boolean deleteEmployee(Long employeeId);

	/**
	 * Удаление бенефициара по указанному id
	 * @param beneficiaryId
	 * @return
	 */
	boolean deleteBeneficiary(Long beneficiaryId);

	/**
	 * Изменить данные физлица (human должен содержать валидный id)
	 * Редактируются только данные верхнего уровня (т.е. вложенные Passport не изменяются)
	 * @param humanDTO
	 * @return
	 */
	Optional<HumanDTO> editHuman(HumanDTO humanDTO);

	/**
	 * Добавить паспорт человеку
	 * @param humanId
	 * @param passportDTO
	 * @return
	 */
	Optional<HumanPassportDTO> addPassportToHuman(Long humanId, HumanPassportDTO passportDTO);

	/**
	 * Удалить указанный паспорт
	 * @param passportId
	 * @return
	 */
	boolean deletePassport(Long passportId);

	/**
	 * Добавить телефон сотруднику
	 * @param employeeId
	 * @param phoneDTO
	 * @return
	 */
	Optional<PhoneDTO> addPhoneToEmployee(Long employeeId, PhoneDTO phoneDTO);

	/**
	 * Удалить телефон сотрудника
	 * @param phoneId
	 * @return
	 */
	boolean deletePhoneEmployee(Long phoneId);

	/**
	 * Добавить почту сотруднику
	 * @param employeeId
	 * @param emailDTO
	 * @return
	 */
	Optional<EmailDTO> addEmailToEmployee(Long employeeId, EmailDTO emailDTO);

	/**
	 * Удаление почты сотрудника
	 * @param emailId
	 * @return
	 */
	boolean deleteEmailEmployee(Long emailId);

	/**
	 * Добавить телефон бенефициару
	 * @param beneficiaryId
	 * @param phoneDTO
	 * @return
	 */
	Optional<PhoneDTO> addPhoneToBeneficiary(Long beneficiaryId, PhoneDTO phoneDTO);

	/**
	 * Удалить телефон бенефициара
	 * @param phoneId
	 * @return
	 */
	boolean deletePhoneBeneficiary(Long phoneId);

	/**
	 * Добавить почту бенефициару
	 * @param beneficiaryId
	 * @param emailDTO
	 * @return
	 */
	Optional<EmailDTO> addEmailToBeneficiary(Long beneficiaryId, EmailDTO emailDTO);

	/**
	 * Удаление почты бенефициара
	 * @param emailId
	 * @return
	 */
	boolean deleteEmailBeneficiary(Long emailId);
}
