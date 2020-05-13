package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.ImsiNotificationPhoneNumberOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImsiNotificationPhoneNumberOwnerRepository extends JpaRepository<ImsiNotificationPhoneNumberOwner, Long> {

	@Query("select ph from ImsiNotificationPhoneNumberOwner ph where ph.phoneNumber = ?1 and ph.oldImsi = ?2 and ph.currentImsi = ?3")
	ImsiNotificationPhoneNumberOwner findByPhoneNumberAndOldImsiAndCurrentImsi(String phoneNumber, String oldImsi, String currentImsi);
}
