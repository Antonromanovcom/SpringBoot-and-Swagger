package online.prostobank.clients.api.dto.client;

import lombok.Value;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.services.client.ClientAttachmentClass;

@Value
public class ClientAttachmentHelpDTO {
	private Long clientId;
	private ClientAttachmentClass classId;
	private AttachmentFunctionalType typeId;
	private String name;
	private byte[] content;
	private String contentType;
}
