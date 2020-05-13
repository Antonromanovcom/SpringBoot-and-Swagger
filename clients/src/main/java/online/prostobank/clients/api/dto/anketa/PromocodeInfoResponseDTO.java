package online.prostobank.clients.api.dto.anketa;

import lombok.Data;

@Data
public class PromocodeInfoResponseDTO {
	private boolean ok;
	private String errorMessage;
	private DeveloperPayloadDTO developerPayload;
}
