package online.prostobank.clients.domain.repository.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.client.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * Репозиторий объектов, связанных с клиентом (сотрудники, бенефициары и т.п.)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ClientDetailRepositoryImpl implements ClientDetailRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public boolean isClientExists(Long clientId) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", clientId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from account_application where id = :id", namedParameters,
				Integer.class);

		return exists != null && exists > 0;
	}

	@Override
	public boolean isHumanExists(Long humanId) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", humanId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from human where id = :id", namedParameters,
				Integer.class);

		return exists != null && exists > 0;
	}

	@Override
	public boolean isEmployeeExists(Long employeeId) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", employeeId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from employee where id = :id", namedParameters,
				Integer.class);

		return exists != null && exists > 0;
	}

	@Override
	public boolean isBeneficiaryExists(Long beneficiaryId) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", beneficiaryId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from beneficiary where id = :id", namedParameters,
				Integer.class);

		return exists != null && exists > 0;
	}

	@Override
	public List<Employee> getClientEmployees(Long clientId) {
		if (clientId == null || clientId <= 0) {
			log.error("параметр clientId не указан");
			return Collections.emptyList();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("clientId", clientId);
		String query = "SELECT id, position, human_id FROM employee WHERE client_id = :clientId";
		return jdbcTemplate.query(query, namedParameters, (res, i) -> {
			Long id = res.getLong("id");
			return new Employee(
					id,
					res.getString("position"),
					getHumanById(res.getLong("human_id")).orElse(null),
					getPhonesByEmployeeId(id),
					getEmailsByEmployeeId(id));
		});
	}

	public Optional<Employee> getClientEmployee(Long employeeId) throws IllegalArgumentException {
		if (employeeId == null || employeeId <= 0) {
			log.error("параметр employeeId не указан");
			throw new IllegalArgumentException("Отсутствует идентификатор сотрудника");
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", employeeId);
		String sql = "SELECT id, position, human_id FROM employee WHERE id = :id";
		return Optional.ofNullable(jdbcTemplate.queryForObject(sql, namedParameters, (res, i) -> {
			Long id = res.getLong("id");
			return new Employee(
					id,
					res.getString("position"),
					getHumanById(res.getLong("human_id")).orElse(null),
					getPhonesByEmployeeId(id),
					getEmailsByEmployeeId(id));
		}));
	}

	@Override
	public List<Beneficiary> getClientBeneficiaries(Long clientId) {
		if (clientId == null || clientId <= 0) {
			log.error("параметр clientId не указан");
			return Collections.emptyList();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("clientId", clientId);
		String query = "SELECT id, human_id, stake_absolute, stake_percent FROM beneficiary WHERE client_id = :clientId";
		return jdbcTemplate.query(query, namedParameters, (res, i) -> {
			Long id = res.getLong("id");
			return new Beneficiary(
					id,
					getHumanById(res.getLong("human_id")).orElse(null),
					getPhonesByBeneficiaryId(id),
					getEmailsByBeneficiaryId(id),
					res.getInt("stake_percent"),
					res.getLong("stake_absolute")
			);
		});
	}

	public Optional<Beneficiary> getClientBeneficiary(Long beneficiaryId) {
		if (beneficiaryId == null || beneficiaryId <= 0) {
			log.error("параметр beneficiaryId не указан");
			throw new IllegalArgumentException("Отсутствует идентификатор бенефициара");
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("beneficiaryId", beneficiaryId);
		String query = "SELECT id, human_id, stake_absolute, stake_percent FROM beneficiary WHERE id = :beneficiaryId";
		return Optional.ofNullable(jdbcTemplate.queryForObject(query, namedParameters, (res, i) -> {
			Long id = res.getLong("id");
			return new Beneficiary(
					id,
					getHumanById(res.getLong("human_id")).orElse(null),
					getPhonesByBeneficiaryId(id),
					getEmailsByBeneficiaryId(id),
					res.getInt("stake_percent"),
					res.getLong("stake_absolute")
			);
		}));
	}

	@Override
	public boolean deleteEmployee(Long employeeId) {
		if (employeeId == null || employeeId <= 0) {
			log.error("параметр employeeId не указан");
			return false;
		}
		try {
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("employeeId", employeeId);
			return jdbcTemplate.update("DELETE FROM employee WHERE id = :employeeId", namedParameters) == 1;
		} catch (DataAccessException ex) {
			log.error("При удалении сотрудника произошла ошибка", ex);
			return false;
		}
	}

	@Override
	public boolean deleteBeneficiary(Long beneficiaryId) {
		if (beneficiaryId == null || beneficiaryId <= 0) {
			log.error("параметр beneficiaryId не указан");
			return false;
		}
		try {
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("beneficiaryId", beneficiaryId);
			return jdbcTemplate.update("DELETE FROM beneficiary WHERE id = :beneficiaryId", namedParameters) == 1;
		} catch (DataAccessException ex) {
			log.error("При удалении бенефициара произошла ошибка", ex);
			return false;
		}
	}

	@Override
	@Transactional
	public Optional<Employee> addEmployee(Long clientId, Employee employee) {
		if (clientId == null || clientId <= 0) {
			log.error("параметр clientId не указан");
			return Optional.empty();
		}
		if (employee == null) {
			log.error("параметр employee не указан");
			return Optional.empty();
		}
		try {
			Long id = getNextSequenceValue();
			Long humanId = saveNewHuman(employee.getHuman());
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("id", id)
					.addValue("position", employee.getPosition())
					.addValue("dboLogin", "")
					.addValue("absCode", "")
					.addValue("humanId", humanId)
					.addValue("isDelegate", false)
					.addValue("clientId", clientId);
			String sql = "INSERT INTO employee (id, position, dbo_login, abs_code, human_id, is_delegate, client_id) " +
					"VALUES (:id, :position, :dboLogin, :absCode, :humanId, :isDelegate, :clientId)";
			jdbcTemplate.update(sql, namedParameters);
			if (employee.getEmails() != null) {
				employee.getEmails().forEach(email -> saveNewEmail(id, Employee.class, email));
			}
			if (employee.getPhones() != null) {
				employee.getPhones().forEach(phone -> saveNewPhone(id, Employee.class, phone));
			}
			return getClientEmployee(id);
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage());
			return Optional.empty();
		}
	}

	@Override
	public Optional<Beneficiary> addBeneficiary(Long clientId, Beneficiary beneficiary) {
		if (clientId == null || clientId <= 0) {
			log.error("параметр clientId не указан");
			return Optional.empty();
		}
		if (beneficiary == null) {
			log.error("параметр beneficiary не указан");
			return Optional.empty();
		}
		try {
			Long id = getNextSequenceValue();
			Long humanId = saveNewHuman(beneficiary.getHuman());
			SqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("id", id)
					.addValue("stakePercent", beneficiary.getStakePercent())
					.addValue("stakeAbsolute", beneficiary.getStakeAbsolute())
					.addValue("humanId", humanId)
					.addValue("clientId", clientId);
			String sql = "INSERT INTO beneficiary (id, stake_percent, stake_absolute, human_id, client_id) " +
					"VALUES (:id, :stakePercent, :stakeAbsolute, :humanId, :clientId)";
			jdbcTemplate.update(sql, namedParameters);
			if (beneficiary.getEmails() != null) {
				beneficiary.getEmails().forEach(email -> saveNewEmail(id, Beneficiary.class, email));
			}
			if (beneficiary.getPhones() != null) {
				beneficiary.getPhones().forEach(phone -> saveNewPhone(id, Beneficiary.class, phone));
			}
			return getClientBeneficiary(id);
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage());
			return Optional.empty();
		}
	}

	@Override
	public Optional<Employee> editEmployee(Employee employee) {
		if (employee == null || employee.getId() == null || employee.getId() <= 0) {
			return Optional.empty();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", employee.getId())
				.addValue("position", employee.getPosition())
				.addValue("dboLogin", "")
				.addValue("absCode", "")
				.addValue("isDelegate", false);
		String sql = "UPDATE employee SET position = :position, dbo_login = :dboLogin, abs_code = :absCode, is_delegate = :isDelegate WHERE id = :id";
		jdbcTemplate.update(sql, namedParameters);
		return getClientEmployee(employee.getId());
	}

	@Override
	public Optional<Beneficiary> editBeneficiary(Beneficiary beneficiary) {
		if (beneficiary == null || beneficiary.getId() == null || beneficiary.getId() <= 0) {
			return Optional.empty();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", beneficiary.getId())
				.addValue("stakePercent", beneficiary.getStakePercent())
				.addValue("stake_absolute", beneficiary.getStakeAbsolute());
		String sql = "UPDATE beneficiary SET stake_percent = :stakePercent, stake_absolute = :stake_absolute WHERE id = :id";
		jdbcTemplate.update(sql, namedParameters);
		return getClientBeneficiary(beneficiary.getId());
	}

	@Override
	public Optional<Human> editHuman(Human human) {
		if (human == null || human.getId() == null || human.getId() <= 0) {
			return Optional.empty();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", human.getId())
				.addValue("firstName", human.getFirstName())
				.addValue("middleName", human.getMiddleName())
				.addValue("lastName", human.getLastName())
				.addValue("snils", human.getSnils())
				.addValue("registrationAddress", human.getRegistrationAddress())
				.addValue("inn", human.getInn())
				.addValue("citizenship", human.getCitizenship());
		String sql = "UPDATE human SET first_name = :firstName, middle_name = :middleName, last_name = :lastName, snils = :snils, " +
				"registration_address = :registrationAddress, inn = :inn, citizenship = :citizenship WHERE id = :id";
		jdbcTemplate.update(sql, namedParameters);
		return getHumanById(human.getId());
	}

	@Override
	public Optional<Passport> addPassportToHuman(Long humanId, Passport passport) {
		//параметры проверяются в вызываемом методе
		Long id = saveNewPassport(humanId, passport);
		return getPassportsByHuman(humanId).stream().filter(it -> it.getId().equals(id)).findFirst();
	}

	@Override
	public boolean deletePassport(Long passportId) {
		if (passportId == null || passportId <= 0) {
			return false;
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", passportId);
		String sql = "DELETE FROM passport WHERE id = :id";
		try {
			return jdbcTemplate.update(sql, namedParameters) == 1;
		} catch (DataAccessException ex) {
			return false;
		}
	}

	@Override
	public Optional<Phone> addPhoneToEmployee(Long employeeId, Phone phone) {
		//параметры проверяются в вызываемом методе
		Long id = saveNewPhone(employeeId, Employee.class, phone);
		return getPhonesByEmployeeId(employeeId).stream().filter(it -> it.getId().equals(id)).findFirst();
	}

	@Override
	public boolean deletePhoneEmployee(Long phoneId) {
		//параметры проверяются в вызываемом методе
		return deletePhone(phoneId, Employee.class);
	}

	@Override
	public Optional<Email> addEmailToEmployee(Long employeeId, Email email) {
		//параметры проверяются в вызываемом методе
		Long id = saveNewEmail(employeeId, Employee.class, email);
		return getEmailsByEmployeeId(employeeId).stream().filter(it -> it.getId().equals(id)).findFirst();
	}

	@Override
	public boolean deleteEmailEmployee(Long emailId) {
		//параметры проверяются в вызываемом методе
		return deleteEmail(emailId, Employee.class);
	}

	@Override
	public Optional<Phone> addPhoneToBeneficiary(Long beneficiaryId, Phone phone) {
		//параметры проверяются в вызываемом методе
		Long id = saveNewPhone(beneficiaryId, Beneficiary.class, phone);
		return getPhonesByBeneficiaryId(beneficiaryId).stream().filter(it -> it.getId().equals(id)).findFirst();
	}

	@Override
	public boolean deletePhoneBeneficiary(Long phoneId) {
		//параметры проверяются в вызываемом методе
		return deletePhone(phoneId, Beneficiary.class);
	}

	@Override
	public Optional<Email> addEmailToBeneficiary(Long beneficiaryId, Email email) {
		//параметры проверяются в вызываемом методе
		Long id = saveNewEmail(beneficiaryId, Beneficiary.class, email);
		return getEmailsByBeneficiaryId(beneficiaryId).stream().filter(it -> it.getId().equals(id)).findFirst();
	}

	@Override
	public boolean deleteEmailBeneficiary(Long emailId) {
		//параметры проверяются в вызываемом методе
		return deleteEmail(emailId, Beneficiary.class);
	}

	@Override
	public Long getClientIdByEmployee(Long employeeId) {
		if (employeeId == null) {
			return 0L;
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("employeeId", employeeId);
		String query = "SELECT client_id FROM employee WHERE id = :employeeId";
		try {
			Long clientId = jdbcTemplate.queryForObject(query, namedParameters, Long.class);
			return clientId == null ? 0 : clientId;
		} catch (DataAccessException ex) {
			return 0L;
		}
	}

	@Override
	public Long getClientIdByBeneficiary(Long beneficiaryId) {
		if (beneficiaryId == null) {
			return 0L;
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("beneficiaryId", beneficiaryId);
		String query = "SELECT client_id FROM beneficiary WHERE id = :beneficiaryId";
		try {
			Long clientId = jdbcTemplate.queryForObject(query, namedParameters, Long.class);
			return clientId == null ? 0 : clientId;
		} catch (DataAccessException ex) {
			return 0L;
		}
	}

	@Override
	public List<Long> getClientsIdByHuman(Long humanId) {
		if (humanId == null) {
			return Collections.emptyList();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("humanId", humanId);
		String query = "SELECT em.client_id FROM employee em JOIN human h on em.human_id = h.id WHERE human_id = :humanId\n" +
				"UNION\n" +
				"SELECT bn.client_id FROM beneficiary bn JOIN human h on bn.human_id = h.id WHERE human_id = :humanId";
		try {
			return jdbcTemplate.query(query, namedParameters, (res, i) -> res.getLong("client_id"));
		} catch (DataAccessException ex) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<Long> getClientsIdByPassport(Long passportId) {
		if (passportId == null) {
			return Collections.emptyList();
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("passportId", passportId);
		String query = "SELECT em.client_id FROM employee em JOIN human h on em.human_id = h.id JOIN passport p on h.id = p.human_id WHERE p.id = :passportId\n" +
				"UNION\n" +
				"SELECT bn.client_id FROM beneficiary bn JOIN human h on bn.human_id = h.id JOIN passport p on h.id = p.human_id WHERE p.id = :passportId";
		try {
			return jdbcTemplate.query(query, namedParameters, (res, i) -> res.getLong("client_id"));
		} catch (DataAccessException ex) {
			return Collections.emptyList();
		}
	}

	//сохранение нового Human в БД
	private Long saveNewHuman(Human human) throws IllegalArgumentException {
		if (human == null) {
			log.error("Параметр human == null");
			throw new IllegalArgumentException("Отсутствуют данные о физлице");
		}
		Long id = getNextSequenceValue();
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("firstName", human.getFirstName())
				.addValue("middleName", human.getMiddleName())
				.addValue("lastName", human.getLastName())
				.addValue("snils", human.getSnils())
				.addValue("registrationAddress", human.getRegistrationAddress())
				.addValue("inn", human.getInn())
				.addValue("citizenship", human.getCitizenship());
		String sql = "INSERT INTO human (id, first_name, middle_name, last_name, snils, registration_address, inn, citizenship) VALUES " +
				"(:id, :firstName, :middleName, :lastName, :snils, :registrationAddress, :inn, :citizenship)";
		jdbcTemplate.update(sql, namedParameters);
		if (human.getPassport() != null) {
			human.getPassport().forEach(passport -> saveNewPassport(id, passport));
		}
		return id;
	}

	//добавление паспорта указанному человеку
	private Long saveNewPassport(Long humanId, Passport passport) throws IllegalArgumentException {
		if (humanId == null || humanId <= 0) {
			log.error("Параметр humanId == null");
			throw new IllegalArgumentException("Отсутствует указание на физлицо, для которого сохраняется паспорт");
		}
		if (passport == null) {
			log.error("Параметр passport == null");
			throw new IllegalArgumentException("Отсутствуют данные паспорта");
		}
		Long id = getNextSequenceValue();
		Timestamp issueDate = passport.getIssueDate() == null ? Timestamp.from(Instant.now()) : Timestamp.from(passport.getIssueDate());
		Timestamp dateOfBirth = passport.getDateOfBirth() == null ? Timestamp.from(Instant.now()) : Timestamp.from(passport.getDateOfBirth());
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("humanId", humanId)
				.addValue("number", passport.getNumber())
				.addValue("series", passport.getSeries())
				.addValue("issueDepartmentCode", passport.getIssueDepartmentCode())
				.addValue("issueDate", issueDate)
				.addValue("dateOfBirth", dateOfBirth)
				.addValue("placeOfBirth", passport.getPlaceOfBirth())
				.addValue("issueDepartmentName", passport.getIssueDepartmentName())
				.addValue("isValid", passport.isValid())
				.addValue("isMain", passport.isMain());
		String sql = "INSERT INTO passport (id, number, series, issue_department_code, issue_date, date_of_birth, place_of_birth, issue_department_name, is_valid, human_id, is_main) " +
				"VALUES (:id, :number, :series, :issueDepartmentCode, :issueDate, :dateOfBirth, :placeOfBirth, :issueDepartmentName, :isValid, :humanId, :isMain)";
		jdbcTemplate.update(sql, namedParameters);
		return id;
	}

	//добавление email указанному владельцу
	private Long saveNewEmail(Long emailOwnerId, Class<?> ownerClass, Email email) throws IllegalArgumentException {
		if (emailOwnerId == null || emailOwnerId <= 0) {
			log.error("Параметр emailOwnerId == null");
			throw new IllegalArgumentException("Отсутствует указание на идентификатор владельца email");
		}
		if (ownerClass == null || (!ownerClass.isAssignableFrom(Beneficiary.class) && !ownerClass.isAssignableFrom(Employee.class))) {
			log.error("Параметр ownerClass == null");
			throw new IllegalArgumentException("Отсутствует указание на тип владельца email");
		}
		if (email == null) {
			log.error("Параметр email == null");
			throw new IllegalArgumentException("Отсутствуют данные email");
		}
		Long id = getNextSequenceValue();
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("ownerId", emailOwnerId)
				.addValue("value", email.getValue())
				.addValue("isMain", email.isMain());
		String sql;
		if (ownerClass.isAssignableFrom(Beneficiary.class)) {
			sql = "INSERT INTO email_beneficiary (id, value, is_main, owner_id) VALUES (:id, :value, :isMain, :ownerId)";
		} else {
			sql = "INSERT INTO email_employee (id, value, is_main, owner_id) VALUES (:id, :value, :isMain, :ownerId)";
		}
		jdbcTemplate.update(sql, namedParameters);
		return id;
	}

	//добавление phone указанному владельцу
	private Long saveNewPhone(Long phoneOwnerId, Class<?> ownerClass, Phone phone) throws IllegalArgumentException {
		if (phoneOwnerId == null || phoneOwnerId <= 0) {
			log.error("Параметр phoneOwnerId == null");
			throw new IllegalArgumentException("Отсутствует указание на идентификатор владельца телефона");
		}
		if (ownerClass == null || (!ownerClass.isAssignableFrom(Beneficiary.class) && !ownerClass.isAssignableFrom(Employee.class))) {
			log.error("Параметр ownerClass == null");
			throw new IllegalArgumentException("Отсутствует указание на тип владельца телефона");
		}
		if (phone == null) {
			log.error("Параметр phone == null");
			throw new IllegalArgumentException("Отсутствуют данные телефона");
		}
		Long id = getNextSequenceValue();
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("ownerId", phoneOwnerId)
				.addValue("value", phone.getValue())
				.addValue("isMain", phone.isMain());
		String sql;
		if (ownerClass.isAssignableFrom(Beneficiary.class)) {
			sql = "INSERT INTO phone_beneficiary (id, value, is_main, owner_id) VALUES (:id, :value, :isMain, :ownerId)";
		} else {
			sql = "INSERT INTO phone_employee (id, value, is_main, owner_id) VALUES (:id, :value, :isMain, :ownerId)";
		}
		jdbcTemplate.update(sql, namedParameters);
		return id;
	}

	private Optional<Human> getHumanById(Long humanId) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", humanId);
		String query = "SELECT id, first_name, middle_name, last_name, snils, registration_address, inn, citizenship FROM human WHERE id = :id";
		Optional<Human> humanOptional = Optional.ofNullable(jdbcTemplate.queryForObject(query, namedParameters, (res, i) ->
				new Human(res.getLong("id"), res.getString("first_name"), res.getString("middle_name"),
						res.getString("last_name"), res.getString("snils"), res.getString("registration_address"),
						res.getString("inn"), res.getString("citizenship"))));
		humanOptional.ifPresent(human -> human.setPassport(getPassportsByHuman(human.getId())));
		return humanOptional;
	}

	//набор паспортов по указанному человеку
	private List<Passport> getPassportsByHuman(Long humanId) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", humanId);
		String query = "SELECT id, number, series, issue_department_code, issue_date, date_of_birth, place_of_birth, " +
				"issue_department_name, is_valid, is_main FROM passport WHERE human_id = :id";
		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		return jdbcTemplate.query(query, namedParameters, (res, i) ->
				new Passport(res.getLong("id"), res.getString("number"), res.getString("series"),
						res.getString("issue_department_code"),
						res.getTimestamp("issue_date", utcCalendar).toInstant(),
						res.getTimestamp("date_of_birth", utcCalendar).toInstant(),
						res.getString("place_of_birth"),
						res.getString("issue_department_name"), res.getBoolean("is_valid"), res.getBoolean("is_main")));
	}

	//набор телефонов сотрудника
	private List<Phone> getPhonesByEmployeeId(Long employeeId) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("employeeId", employeeId);
		String query = "SELECT id, value, is_main FROM phone_employee WHERE owner_id = :employeeId";
		return jdbcTemplate.query(query, namedParameters, (res, i) -> createPhone(res));
	}

	//набор телефонов бенефициара
	private List<Phone> getPhonesByBeneficiaryId(Long beneficiaryId) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("beneficiaryId", beneficiaryId);
		String query = "SELECT id, value, is_main FROM phone_beneficiary WHERE owner_id = :beneficiaryId";
		return jdbcTemplate.query(query, namedParameters, (res, i) -> createPhone(res));
	}

	//набор адресов почты сотрудника
	private List<Email> getEmailsByEmployeeId(Long employeeId) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("employeeId", employeeId);
		String query = "SELECT id, value, is_main FROM email_employee WHERE owner_id = :employeeId";
		return jdbcTemplate.query(query, namedParameters, (res, i) -> createEmail(res));
	}

	//набор адресов почты бенефициара
	private List<Email> getEmailsByBeneficiaryId(Long beneficiaryId) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("beneficiaryId", beneficiaryId);
		String query = "SELECT id, value, is_main FROM email_beneficiary WHERE owner_id = :beneficiaryId";
		return jdbcTemplate.query(query, namedParameters, (res, i) -> createEmail(res));
	}

	private Phone createPhone(ResultSet res) throws SQLException {
		return new Phone(res.getLong("id"), res.getString("value"), res.getBoolean("is_main"));
	}

	private Email createEmail(ResultSet res) throws SQLException {
		return new Email(res.getLong("id"), res.getString("value"), res.getBoolean("is_main"));
	}

	private boolean deleteEmail(Long emailId, Class<?> ownerClass) {
		if (emailId == null || emailId <= 0) {
			log.error("Параметр emailId == null");
			throw new IllegalArgumentException("Отсутствует указание на идентификатор email");
		}
		if (ownerClass == null || (!ownerClass.isAssignableFrom(Beneficiary.class) && !ownerClass.isAssignableFrom(Employee.class))) {
			log.error("Параметр ownerClass == null");
			throw new IllegalArgumentException("Отсутствует указание на тип владельца email");
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", emailId);
		String sql;
		if (ownerClass.isAssignableFrom(Employee.class)) {
			sql = "DELETE FROM email_employee WHERE id = :id";
		} else {
			sql = "DELETE FROM email_beneficiary WHERE id = :id";
		}
		try {
			return jdbcTemplate.update(sql, namedParameters) == 1;
		} catch (DataAccessException ex) {
			return false;
		}
	}

	private boolean deletePhone(Long phoneId, Class<?> ownerClass) {
		if (phoneId == null || phoneId <= 0) {
			log.error("Параметр phoneId == null");
			throw new IllegalArgumentException("Отсутствует указание на идентификатор телефона");
		}
		if (ownerClass == null || (!ownerClass.isAssignableFrom(Beneficiary.class) && !ownerClass.isAssignableFrom(Employee.class))) {
			log.error("Параметр ownerClass == null");
			throw new IllegalArgumentException("Отсутствует указание на тип владельца телефона");
		}
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", phoneId);
		String sql;
		if (ownerClass.isAssignableFrom(Employee.class)) {
			sql = "DELETE FROM phone_employee WHERE id = :id";
		} else {
			sql = "DELETE FROM phone_beneficiary WHERE id = :id";
		}
		try {
			return jdbcTemplate.update(sql, namedParameters) == 1;
		} catch (DataAccessException ex) {
			return false;
		}
	}

	private Long getNextSequenceValue() throws IllegalStateException {
		Long value = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence') as num", Collections.emptyMap(), Long.class);
		if (value == null) {
			throw new IllegalStateException("Генератор последовательностей БД не вернул валидные данные");
		}
		return value;
	}
}
