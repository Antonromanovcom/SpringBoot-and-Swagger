package online.prostobank.clients.api.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValue;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class ClientGridDTO {
	private long clientId;
	private Instant createdAt;
	private String city;
	private String client;
	private String inn;
	private String phone;
	private String status;
	private String clientStatus;
	private Instant updateDateTime;
	private Double aiScore;
	private String assignedTo;

	public static ClientGridDTO createFrom(AccountApplication application) {
		ClientValue client = application.getClient();
		return new ClientGridDTO()
				.setClientId(application.getId())
				.setCreatedAt(application.getDateCreated())
				.setCity(application.getCity().getName())
				.setClient(client.getName())
				.setInn(client.getInn())
				.setPhone(client.getPhone())
				.setStatus(application.getClientState().getRuName())
				.setClientStatus(application.getClientState().name())
				.setUpdateDateTime(application.getUpdateDateTime() == null ? application.getDateCreated() : application.getUpdateDateTime())
				.setAiScore(application.getAiScore())
				.setAssignedTo(application.getAssignedTo());
	}
}
