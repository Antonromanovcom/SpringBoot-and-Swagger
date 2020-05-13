package online.prostobank.clients.domain.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Метка версии документа (фактически является лишь именем-ссылкой, по которому можно получить сам документ)
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VersionDTO {
	@JsonProperty(value = "name")
	private String name;
	@JsonProperty(value = "created_at")
	private Instant createdAt;
	@JsonProperty(value = "is_deleted")
	private Boolean isDeleted;
	@JsonProperty(value = "versioned_document")
	private DocumentDTO versionedDocument;
}
