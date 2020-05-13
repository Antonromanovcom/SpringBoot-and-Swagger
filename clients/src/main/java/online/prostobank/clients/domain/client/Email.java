package online.prostobank.clients.domain.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Email {
	private final Long id;
	private final String value;
	private final boolean isMain;
}
