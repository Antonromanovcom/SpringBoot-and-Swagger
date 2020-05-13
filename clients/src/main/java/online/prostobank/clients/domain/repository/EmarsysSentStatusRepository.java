package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.EmarsysSentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmarsysSentStatusRepository extends JpaRepository<EmarsysSentStatus, Long> {

	@Query("select e from EmarsysSentStatus e where e.accountApplicationId = ?1")
	Optional<EmarsysSentStatus> findByAccountApplication(Long applicationId);
}
