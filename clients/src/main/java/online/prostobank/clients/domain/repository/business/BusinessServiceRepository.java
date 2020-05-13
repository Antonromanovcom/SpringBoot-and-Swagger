package online.prostobank.clients.domain.repository.business;

import online.prostobank.clients.domain.business_service.BusinessServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessServiceRepository extends JpaRepository<BusinessServiceEntity, Long> {

}
