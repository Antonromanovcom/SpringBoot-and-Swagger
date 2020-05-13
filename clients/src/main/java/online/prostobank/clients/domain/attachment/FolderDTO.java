package online.prostobank.clients.domain.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.utils.serializer.PathDeserializer;
import online.prostobank.clients.utils.serializer.PathSerializer;

import java.util.List;

/**
 * Представление папки (контейнера документов {@link DocumentDTO})
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FolderDTO {

	@JsonSerialize(using = PathSerializer.class)
	@JsonDeserialize(using = PathDeserializer.class)
	@JsonProperty(value = "id")
	private Path path;

	@JsonProperty(value = "title")
	private String title;

	@JsonProperty(value = "description")
	private String description;

	@JsonProperty(value = "documents")
	private List<DocumentDTO> documents;

	@JsonProperty(value = "folders")
	private List<FolderDTO> folders;
}
