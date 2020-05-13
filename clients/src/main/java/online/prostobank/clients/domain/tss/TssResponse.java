package online.prostobank.clients.domain.tss;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TssResponse {
	private UUID requestId;
	private TssStatus status;
	private ConfirmType confirmType;
	private String error;
	private String signature;
	private Instant signDate;
	private String signPublicId;
}
