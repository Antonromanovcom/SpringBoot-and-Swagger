package online.prostobank.clients.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Запись о владельце номера телефона с подозрительными действиями с IMSI
 */
@Entity
public class ImsiNotificationPhoneNumberOwner {

	@Id
	@GeneratedValue
	private Long id;

	private String phoneNumber;

	private String oldImsi;

	private String currentImsi;

	private String taxNumber;

	private String fullName;

	public ImsiNotificationPhoneNumberOwner() {
	}

	public ImsiNotificationPhoneNumberOwner(@NotNull String phoneNumber, String oldImsi, String currentImsi, String taxNumber,
	                                        String fullName) {
		this.phoneNumber = phoneNumber;
		this.oldImsi = oldImsi;
		this.currentImsi = currentImsi;
		this.taxNumber = taxNumber;
		this.fullName = fullName;
	}

	public Long getId() {
		return id;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getTaxNumber() {
		return taxNumber;
	}

	public String getFullName() {
		return fullName;
	}
}
