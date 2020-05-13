package online.prostobank.clients.api.dto;


import lombok.AllArgsConstructor;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;

import java.time.Instant;

/**
 * Вспомогательный DTO для выгрузки blob-документов во внешнее хранилище
 */
@AllArgsConstructor
public class AttachmentDTO {
	private Long id;
	private Integer type;
	private String name;
	private Instant createdAt;
	private String functionalType;
	private Boolean quality;
	private Boolean verified;

	public Long getId() {
		return id;
	}

	public Integer getType() {
		return type == null ? 0 : type;
	}

	public String getName() {
		return name == null ? "" : name;
	}

	public Instant getCreatedAt() {
		return createdAt == null ? Instant.now() : createdAt;
	}

	public String getFunctionalType() {
		if (functionalType == null || functionalType.equals(AttachmentFunctionalType.UNKNOWN.name())) {
			return AttachmentFunctionalType.OTHER.name();
		}
		return functionalType;
	}

	public Boolean getQuality() {
		return Boolean.TRUE.equals(quality);
	}

	public Boolean getVerified() {
		return Boolean.TRUE.equals(verified);
	}
}
