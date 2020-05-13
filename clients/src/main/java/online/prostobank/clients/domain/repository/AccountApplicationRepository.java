package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AccountApplicationRepository extends PagingAndSortingRepository<AccountApplication, Long>, JpaRepository<AccountApplication, Long> {
	int countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCase(String inn, String ogrn);

	@Query("select count (aa) from AccountApplication aa where " +
			"(upper(:inn) like upper(aa.client.inn) or upper(:ogrn) like upper(aa.client.ogrn)) " +
			"and aa.active = false")
	int countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCaseAndActiveFalse(String inn, String ogrn);

	int countByClientEmailLikeIgnoreCase(String email);

	@Query("select aa from #{#entityName} aa " +
			"left join ClientKeycloak ck on aa.id = ck.clientId " +
			"where " +
			"aa.active = true " +
			"and " +
			"aa.client.email is not null " +
			"and " +
			"aa.client.email <> '' " +
			"and " +
			"ck.keycloakId is null " +
			"and " +
			"aa.clientState in (:statuses) ")
	List<AccountApplication> findNewAndNullKeycloak(@Param("statuses") Collection<ClientStates> statuses);

	@Query("select count (a) from AccountApplication a where (a.client.inn = ?1 or a.client.ogrn = ?1) and a.active = true")
	Long countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCaseAndActiveTrue(String taxNumber);

	/**
	 * Поиск с расширенной фильтрацией
	 */
	@Query("select aa from AccountApplication aa where aa.active = true "
			+ "and aa.city in (:cities) "
			+ "and aa.clientState in (:statuses) "
			+ "and (:filterText is null or (aa.client.inn like :filterText or aa.client.ogrn like :filterText or upper(aa.client.name) like upper(:filterText) or aa.client.phone like :filterText or upper(aa.client.head) like upper(:filterText) or upper(aa.assignedTo) like upper(:filterText))) "
			+ "and ((:sources) is null or aa.source in (:sources)) "
			+ "and ((:assignedTo) is null or aa.assignedTo in (:assignedTo))"
			+ "and ((aa.updateDateTime is not null and (aa.updateDateTime between :updateDateFrom and :updateDateTo)) or (aa.dateCreated between :updateDateFrom and :updateDateTo))"
			+ "and (aa.dateCreated between :createDateFrom and :createDateTo)")
	List<AccountApplication> findExtendedFiltered(
			@Param("cities") Collection<City> cities
			, @Param("statuses") Collection<ClientStates> statuses
			, @Param("filterText") String filterText
			, @Param("sources") Collection<Source> sources
			, @Param("assignedTo") Collection<String> assignedTo
			, @Param("updateDateFrom") Instant updateDateFrom
			, @Param("updateDateTo") Instant updateDateTo
			, @Param("createDateFrom") Instant createDateFrom
			, @Param("createDateTo") Instant createDateTo
			, Pageable pageable);

	/**
	 * Подсчёт кол-ва заявок с расширенной фильтрацией
	 */
	@Query("select count(aa) from AccountApplication aa where aa.active = true "
			+ "and aa.city in (:cities) "
			+ "and aa.clientState in (:statuses) "
			+ "and (:filterText is null or (aa.client.inn like :filterText or aa.client.ogrn like :filterText or upper(aa.client.name) like upper(:filterText) or aa.client.phone like :filterText or upper(aa.client.head) like upper(:filterText) or upper(aa.assignedTo) like upper(:filterText))) "
			+ "and ((:sources) is null or aa.source in (:sources)) "
			+ "and ((:assignedTo) is null or aa.assignedTo in (:assignedTo)) "
			+ "and ((aa.updateDateTime is not null and (aa.updateDateTime between :updateDateFrom and :updateDateTo)) or (aa.dateCreated between :updateDateFrom and :updateDateTo))"
			+ "and (aa.dateCreated between :createDateFrom and :createDateTo)"
	)
	Long countExtendFiltered(
			@Param("cities") Collection<City> cities
			, @Param("statuses") Collection<ClientStates> statuses
			, @Param("filterText") String filterText
			, @Param("sources") Collection<Source> sources
			, @Param("assignedTo") Collection<String> assignedTo
			, @Param("updateDateFrom") Instant updateDateFrom
			, @Param("updateDateTo") Instant updateDateTo
			, @Param("createDateFrom") Instant createDateFrom
			, @Param("createDateTo") Instant createDateTo);

	@Query("select aa from #{#entityName} aa where aa.client.phone = (?1) " +
			" and aa.clientState = online.prostobank.clients.domain.state.state.ClientStates.NEW_CLIENT " +
			" and aa.active = true " +
			" order by aa.id desc")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<AccountApplication> findUnconfirmedByClientPhoneAndActiveIsTrueOrderByIdDesc(String phone);

	@Query("select aa from #{#entityName} aa where (aa.client.inn = ?1 or aa.client.ogrn = ?1) and aa.active = true")
	List<AccountApplication> findAllByClientTaxNumberSameAsOgrnAndActiveIsTrue(String inn);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	AccountApplication findByAccountAccountNumber(String requisites);

	/**
	 * Поиск заявки по хешу для личного кабинета
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<AccountApplication> findByLoginURLAndActive(String loginURL, Boolean active);

	@Query("select a from AccountApplication a where a.active = true and a.clientState in ?1 and a.client.email = ?2")
	List<AccountApplication> findByStatusAndEmail(List<ClientStates> statuses, String clientEmail);

	@Query("select a from AccountApplication a where a.active = true and a.clientState in ?1 and (a.client.email = ?2 or a.account.accountNumber = ?3)")
	List<AccountApplication> findByStatusAndEmailOrAccountNumber(List<ClientStates> statuses, String clientEmail, String accountNumber);

	@Query("select a from AccountApplication a where a.client.phone = ?1 and (a.client.inn = null or a.client.inn = '') and (a.client.ogrn = null or a.client.ogrn = '') and a.active = true ")
	List<AccountApplication> findAllByClientPhoneAndClientInnIsEmptyAndOgrnIsEmptyAndActiveIsTrue(String phone);

	@Query("select a from AccountApplication a where a.dateCreated between ?1 and ?2 and a.active = true")
	List<AccountApplication> findAllByDateIntervalAndActiveIsTrue(Instant d1, Instant d2);

	/**
	 * Поиск карточек, назначенных указанному пользователю
	 */
	@Query(value = "select a from AccountApplication a "
			+ "where ((:user is null and a.assignedTo is null) or (:user is not null and a.assignedTo = :user)) ")
	List<AccountApplication> getAssignedAppsOnlyByUser(
			@Param("user") String user);

	/**
	 * Подсчёт заявок по адресу почты
	 */
	@Query("select count(a) " +
			"from AccountApplication a " +
			"where a.active = true and upper(a.client.email) = upper(?1)")
	long countAllByClientEmailAndActiveIsTrue(String clientEmail);

	@Query("select a from AccountApplication a" +
			" where a.dateCreated between ?1 and ?2 " +
			" and a.active = true and a.checks.konturCheck is not empty" +
			" and a.checks.konturCheck is not null")
	List<AccountApplication> findAllByDateIntervalAndIsChecked(Instant d1, Instant d2);

	Optional<AccountApplication> findByClientEmailLikeIgnoreCaseAndActiveIsTrue(String clientEmail);

	List<AccountApplication> findByClientInnIn(List<String> inns);

	Set<AccountApplication> findByIdIn(List<Long> ids);
}
