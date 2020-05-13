package online.prostobank.clients.domain.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import online.prostobank.clients.utils.serializer.PathDeserializer;
import online.prostobank.clients.utils.serializer.PathSerializer;
import online.prostobank.clients.utils.serializer.UniqueIdDeserializer;
import online.prostobank.clients.utils.serializer.UniqueIdSerializer;

import java.time.Instant;
import java.util.List;

/**
 * Представление документа (технически - метаданные + контейнер для файлов {@link FileDTO})
 */
@NoArgsConstructor
public class DocumentDTO {

	public DocumentDTO(IUniqueId id) {
		this.id = id;
	}

	@Getter
	@JsonSerialize(using = UniqueIdSerializer.class)
	@JsonDeserialize(using = UniqueIdDeserializer.class)
	@JsonProperty(value = "id")
	private IUniqueId id;

	@Getter
	@JsonSerialize(using = PathSerializer.class)
	@JsonDeserialize(using = PathDeserializer.class)
	@JsonProperty(value = "path")
	private Path path;

	@Getter
	@Setter
	@JsonProperty(value = "title")
	private String title = "";

	@Getter
	@Setter
	@JsonProperty(value = "description")
	private String description = "";

	@Getter
	@Setter
	@JsonProperty(value = "type")
	private String documentType = "";

	@Getter
	@Setter
	@JsonProperty(value = "created_at")
	private Instant createdAt;

	@Getter
	@Setter
	@JsonProperty(value = "created_by")
	private String createdBy = "";

	@Getter
	@JsonProperty(value = "files")
	private List<FileDTO> files;

	@Getter
	@JsonProperty(value = "versions")
	private List<VersionDTO> versions;

	@Getter
	@Setter
	@JsonProperty(value = "aux_parameters")
	private JsonNode auxParameters;

	@Getter
	@Setter
	@JsonProperty(value = "version_creator")
	private String versionCreator;
}
