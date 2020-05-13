package online.prostobank.clients.api.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.state.state.ClientStates;

@Data
@Accessors(chain = true)
public class StatusDTO {
	private int id;
	private String name;
	private String internalName;
	private Integer subcode;
	@JsonProperty(value = "client_state")
	private StateMachineStatus clientState;

	public static StatusDTO createFrom(ClientStates status) {
		return new StatusDTO()
				.setId(status.getIndex())
				.setName(status.getRuName())
				.setInternalName(status.name());
	}

	@AllArgsConstructor
	private static class StateMachineStatus {
		@JsonProperty(value = "state")
		private String state;
		@JsonProperty(value = "readable_name")
		private String name;
	}
}
