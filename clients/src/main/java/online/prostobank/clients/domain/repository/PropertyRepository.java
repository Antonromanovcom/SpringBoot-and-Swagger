package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, Long> {

    @Query("select pe " +
            "from PropertyEntity pe " +
            "where pe.key = ?1")
    PropertyEntity getPropertyByKey(@Nonnull String key);

    @Query("select count(pe) " +
            "from PropertyEntity pe " +
            "where pe.key = ?1")
    Long isExists(@Nonnull String key);

    @Query("select pe " +
            "from PropertyEntity pe")
    List<PropertyEntity> getAllProperties();
}
