package online.prostobank.clients.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ClientDocumentsDTO {
	@JsonProperty(value = "client_id")
	private Long clientId;
	@JsonProperty(value = "user_documents")
	private Map<Long, Boolean> userDocuments = new HashMap<>();
	@JsonProperty(value = "bank_documents")
	private Map<Long, Boolean> bankDocuments = new HashMap<>();

	public ClientDocumentsDTO(Long clientId) {
		this.clientId = clientId;
	}
}
