package online.prostobank.clients.api.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Сигнал со стороны ПОС об отказе в открытии счета (деактивация заявки) с указанием причины.
 */
@AllArgsConstructor
@Getter
public class ClientDeclineDTO {
	@JsonProperty(value = "client_id")
	private Long clientId;
	@JsonProperty(value = "decline_cause")
	private String declineCause;
}
