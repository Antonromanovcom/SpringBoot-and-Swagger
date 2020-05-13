package online.prostobank.clients.api.dto.managers;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.api.dto.client.ClientGridDTO;

import java.util.List;

@Data
@Accessors(chain = true)
public class ClientForManagerGridDTO {
	private long clientId;
	private String city;
	private String client;
	private String inn;
	private String status;
	private String assignedTo;
	private List<String> roles;

	public static ClientForManagerGridDTO createFrom(ClientGridDTO application, List<String> roles) {
		return new ClientForManagerGridDTO()
				.setClientId(application.getClientId())
				.setCity(application.getCity())
				.setClient(application.getClient())
				.setInn(application.getInn())
				.setStatus(application.getStatus())
				.setAssignedTo(application.getAssignedTo())
				.setRoles(roles);
	}
}
