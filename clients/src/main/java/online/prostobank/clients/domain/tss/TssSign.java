package online.prostobank.clients.domain.tss;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class TssSign {
	@Id
	private UUID requestId;
	@Enumerated(EnumType.STRING)
	private TssStatus status;
	@Enumerated(EnumType.STRING)
	private ConfirmType confirmType;
	@CreatedDate
	private Instant createdDate;

	private Long clientId;
	private Long attachmentId;

	@Type(type = "org.hibernate.type.TextType")
	private String signatureBase64;
	private Instant signDate;
	private String signPublicId;
	@Type(type = "org.hibernate.type.TextType")
	private String error;

	public TssSign(UUID requestId) {
		this.requestId = requestId;
	}
}
