package online.prostobank.clients.api.dto.client;

import lombok.Value;

@Value
public class DuplicateDTO {
	int duplicateSize;
	int duplicateInactive;
	int emailDuplicateCount;
}
