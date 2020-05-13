package online.prostobank.clients.services.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClientAttachmentClass {
	USER("attachment_user"),
	BANK("attachment_bank"),
	;

	private final String table;
}
