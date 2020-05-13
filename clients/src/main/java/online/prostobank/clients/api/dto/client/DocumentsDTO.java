package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.api.dto.rest.AttachmentDTO;
import online.prostobank.clients.domain.AccountApplication;

import java.util.List;

@Value
@Builder
public class DocumentsDTO {
	private List<AttachmentDTO> userAttachments;
	private List<AttachmentDTO> bankAttachments;

	public static DocumentsDTO createFrom(AccountApplication application) {
		List<AttachmentDTO> userAttachments = AttachmentDTO.createListFrom(application.getAttachments());
		List<AttachmentDTO> bankAttachments = AttachmentDTO.createListFrom(application.getBankAttachments());
		return builder()
				.userAttachments(userAttachments)
				.bankAttachments(bankAttachments)
				.build();
	}
}
