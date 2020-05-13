package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.City;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends CrudRepository<City, Long> {
	Optional<City> findByNameIgnoreCase(String name);

	@Query(value = "(select * from city " +
			"where name in ('Москва','Санкт-Петербург') " +
			"order by name) " +
			"union all " +
			"(select * from city " +
			"where name not in ('Москва','Санкт-Петербург') " +
			"and to_list is true " +
			"and is_serviced is true " + // APIKUB-1608 убрать по готовности фронта
			"order by name)",
			nativeQuery = true)
	List<City> findInSpecialOrderToList();

	@Query(value = "(select * from city " +
			"where name in ('Москва','Санкт-Петербург') " +
			"order by name) " +
			"union all " +
			"(select * from city " +
			"where name not in ('Москва','Санкт-Петербург') " +
			"and is_serviced is true " +
			"order by name)",
			nativeQuery = true)
	List<City> findInSpecialOrderIsServiced();

	List<City> findAllByIdIn(Collection<Long> ids);
}
