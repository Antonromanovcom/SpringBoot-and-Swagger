package online.prostobank.clients.domain.tss;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TssEntityRepository extends CrudRepository<TssSign, UUID> {
}
