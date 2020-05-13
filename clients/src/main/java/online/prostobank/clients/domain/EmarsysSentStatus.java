package online.prostobank.clients.domain;

import online.prostobank.clients.domain.statuses.ApplicationEmarsysStatus;

import javax.persistence.*;

@Entity
@Table(name = "emarsys_sent_status")
public class EmarsysSentStatus {

	@Id
	@GeneratedValue
	private Long id;

	private Long accountApplicationId;

	@Enumerated(EnumType.STRING)
	private ApplicationEmarsysStatus emarsysStatus;

	public Long getAccountApplicationId() {
		return accountApplicationId;
	}

	public void setAccountApplicationId(Long accountApplicationId) {
		this.accountApplicationId = accountApplicationId;
	}

	public ApplicationEmarsysStatus getEmarsysStatus() {
		return emarsysStatus;
	}

	public void setEmarsysStatus(ApplicationEmarsysStatus emarsysStatus) {
		this.emarsysStatus = emarsysStatus;
	}
}
