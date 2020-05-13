package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.ImsiNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImsiNotificationRepository extends JpaRepository<ImsiNotification, Long> {
}
