package online.prostobank.clients.domain.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.utils.serializer.PathDeserializer;
import online.prostobank.clients.utils.serializer.PathSerializer;

/**
 * Представление файла (контейнер для непосредственно бинарника), являющегося частью документа {@link DocumentDTO}
 */
@NoArgsConstructor
@Getter
public class FileDTO {
	@JsonProperty(value = "reference")
	private String reference;
	@JsonSerialize(using = PathSerializer.class)
	@JsonDeserialize(using = PathDeserializer.class)
	@JsonProperty(value = "path")
	private Path path;
	@JsonProperty(value = "title")
	private String title = "";
	@JsonProperty(value = "description")
	private String description = "";
	@JsonProperty(value = "mime_type")
	private String mimeType = "";
	@JsonProperty(value = "size")
	private long size;
}
