package online.prostobank.clients.domain.repository.client;

import online.prostobank.clients.domain.client.*;

import java.util.List;
import java.util.Optional;

public interface ClientDetailRepository {
	/**
	 * Проверка существования карточки с указанным id
	 * @param clientId
	 * @return
	 */
	boolean isClientExists(Long clientId);

	/**
	 * Проверка существования физлица с указанным id
	 * @param humanId
	 * @return
	 */
	boolean isHumanExists(Long humanId);

	/**
	 * Проверка существования сотрудника с указанным id
	 * @param employeeId
	 * @return
	 */
	boolean isEmployeeExists(Long employeeId);

	/**
	 * Проверка существования бенефициара с указанным id
	 * @param beneficiaryId
	 * @return
	 */
	boolean isBeneficiaryExists(Long beneficiaryId);

	/**
	 * Список сотрудников указанного клиента
	 * @param clientId
	 * @return
	 */
	List<Employee> getClientEmployees(Long clientId);

	/**
	 * Список бенефициаров указанного клиента
	 * @param clientId
	 * @return
	 */
	List<Beneficiary> getClientBeneficiaries(Long clientId);

	/**
	 * Удаление сотрудника
	 * @param employeeId
	 * @return
	 */
	boolean deleteEmployee(Long employeeId);

	/**
	 * Удаление бенефициара
	 * @param beneficiaryId
	 * @return
	 */
	boolean deleteBeneficiary(Long beneficiaryId);

	/**
	 * Добавить сотрудника указанному клиенту
	 * @param employee
	 * @return
	 */
	Optional<Employee> addEmployee(Long clientId, Employee employee);

	/**
	 * Добавить бенефициара указанному клиенту
	 * @param clientId
	 * @param beneficiary
	 * @return
	 */
	Optional<Beneficiary> addBeneficiary(Long clientId, Beneficiary beneficiary);

	/**
	 * Изменить данные сотрудника (employee должен содержать валидный id)
	 * Редактируются только данные верхнего уровня (т.е. вложенные Human, Passport, Email, Phone не изменяются)
	 * @param employee
	 * @return
	 */
	Optional<Employee> editEmployee(Employee employee);

	/**
	 * Изменить данные бенефициара (beneficiary должен содержать валидный id)
	 * Редактируются только данные верхнего уровня (т.е. вложенные Human, Passport, Email, Phone не изменяются)
	 * @param beneficiary
	 * @return
	 */
	Optional<Beneficiary> editBeneficiary(Beneficiary beneficiary);

	/**
	 * Изменить данные физлица (human должен содержать валидный id)
	 * Редактируются только данные верхнего уровня (т.е. вложенные Passport не изменяются)
	 * @param human
	 * @return
	 */
	Optional<Human> editHuman(Human human);

	/**
	 * Добавить паспорт человеку
	 * @param humanId
	 * @param passport
	 * @return
	 */
	Optional<Passport> addPassportToHuman(Long humanId, Passport passport);

	/**
	 * Удалить указанный паспорт
	 * @param passportId
	 * @return
	 */
	boolean deletePassport(Long passportId);

	/**
	 * Добавить телефон сотруднику
	 * @param employeeId
	 * @param phone
	 * @return
	 */
	Optional<Phone> addPhoneToEmployee(Long employeeId, Phone phone);

	/**
	 * Удалить телефон сотрудника
	 * @param phoneId
	 * @return
	 */
	boolean deletePhoneEmployee(Long phoneId);

	/**
	 * Добавить почту сотруднику
	 * @param employeeId
	 * @param email
	 * @return
	 */
	Optional<Email> addEmailToEmployee(Long employeeId, Email email);

	/**
	 * Удаление почты сотрудника
	 * @param emailId
	 * @return
	 */
	boolean deleteEmailEmployee(Long emailId);

	/**
	 * Добавить телефон бенефициару
	 * @param beneficiaryId
	 * @param phone
	 * @return
	 */
	Optional<Phone> addPhoneToBeneficiary(Long beneficiaryId, Phone phone);

	/**
	 * Удалить телефон бенефициара
	 * @param phoneId
	 * @return
	 */
	boolean deletePhoneBeneficiary(Long phoneId);

	/**
	 * Добавить почту бенефициару
	 * @param beneficiaryId
	 * @param email
	 * @return
	 */
	Optional<Email> addEmailToBeneficiary(Long beneficiaryId, Email email);

	/**
	 * Удаление почты бенефициара
	 * @param emailId
	 * @return
	 */
	boolean deleteEmailBeneficiary(Long emailId);

	/**
	 * Получить идентификатор родительского клиента по известному сотруднику
	 * @return
	 */
	Long getClientIdByEmployee(Long employeeId);

	/**
	 * Получить идентификатор родительского клиента по известному бенефициару
	 * @param beneficiaryId
	 * @return
	 */
	Long getClientIdByBeneficiary(Long beneficiaryId);

	/**
	 * Получить перечень id клиентов, у которых в качестве сотрудников (и пр.) имеется указанный человек
	 * @param humanId
	 * @return
	 */
	List<Long> getClientsIdByHuman(Long humanId);

	/**
	 * Получить перечень id клиентов, у которых среди физлиц (сотрудники и пр.) имеется владелец указанного паспорта
	 * @param passportId
	 * @return
	 */
	List<Long> getClientsIdByPassport(Long passportId);
}
