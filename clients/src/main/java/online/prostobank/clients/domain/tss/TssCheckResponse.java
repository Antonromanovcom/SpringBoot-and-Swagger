package online.prostobank.clients.domain.tss;

import lombok.Data;

@Data
public class TssCheckResponse {
	private boolean success;
	private Object signer; // todo?
	private String serviceId;
	private String userId;
}
