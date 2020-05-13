package online.prostobank.clients.domain.tss;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TssCheckMessage {
	private String message;
	private String signature;
}
