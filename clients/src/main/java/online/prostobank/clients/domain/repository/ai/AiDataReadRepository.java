package online.prostobank.clients.domain.repository.ai;

import online.prostobank.clients.domain.AccountApplication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiDataReadRepository extends PagingAndSortingRepository<AccountApplication, Long> {

	/**
	 * Перечень id карточек клиента (с сортировкой и пейджингом для дальнейшего запроса в сервис AI-ранжирования)
	 * @param pageable
	 * @return
	 */
	@Query("SELECT id FROM AccountApplication aa WHERE aa.active = true")
	List<Long> getIdsForAiProcessing(Pageable pageable);

	/**
	 * Общее число карточек клиентов
	 * @return
	 */
	@Query("SELECT count(aa) FROM AccountApplication aa WHERE aa.active = true")
	Long getTotalClientsCount();
}
