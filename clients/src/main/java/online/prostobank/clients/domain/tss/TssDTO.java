package online.prostobank.clients.domain.tss;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TssDTO {
	private UUID requestId;
	private String signature;
	private Instant signDate;
	private String signPublicId;
}
