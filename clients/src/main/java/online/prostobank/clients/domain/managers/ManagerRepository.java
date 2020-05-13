package online.prostobank.clients.domain.managers;

import online.prostobank.clients.domain.City;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, UUID> {
	Optional<Manager> findByLogin(String login);

	@Query("select manager from Manager manager " +
			" where " +
			" (:filterText = '' or ( " +
			"    upper(manager.login) like upper(:filterText) " +
			" or upper(manager.firstName) like upper(:filterText) " +
			" or upper(manager.secondName) like upper(:filterText) " +
			" or upper(manager.lastName) like upper(:filterText) " +
			" )) " +
			" and (:online = false or (manager.lastOnline >= :dateFrom)) " +
			" and ((:cities) is not null and manager.city in (:cities)) "
	)
	List<Manager> findAllByFilter(
			@Param("online") boolean online,
			@Param("dateFrom") Instant dateFrom,
			@Param("filterText") String filterText,
			@Param("cities") Collection<City> cities,
			Sort sorting
	);
}
