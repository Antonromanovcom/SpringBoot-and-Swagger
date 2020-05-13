package online.prostobank.clients.domain;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;

/**
 * Запись о подозрительных действиях с IMSI
 */
@Entity
public class ImsiNotification {

	@Id
	@GeneratedValue
	private Long id;

	@Transient
	private Set<String> imsiNotificationRecipientUser;

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinTable(name = "imsi_notification_imsi_notification_phone_number_owner")
	private Set<ImsiNotificationPhoneNumberOwner> imsiNotificationPhoneNumberOwners;

	@Basic
	private Instant created;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<ImsiNotificationPhoneNumberOwner> getImsiNotificationPhoneNumberOwners() {
		return imsiNotificationPhoneNumberOwners;
	}

	public void setImsiNotificationPhoneNumberOwners(Set<ImsiNotificationPhoneNumberOwner> imsiNotificationPhoneNumberOwners) {
		this.imsiNotificationPhoneNumberOwners = imsiNotificationPhoneNumberOwners;
	}

	public Set<String> getImsiNotificationRecipientUser() {
		return imsiNotificationRecipientUser;
	}

	public void setImsiNotificationRecipientUser(Set<String> imsiNotificationRecipientUser) {
		this.imsiNotificationRecipientUser = imsiNotificationRecipientUser;
	}

	public Instant getCreated() {
		return created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}
}
