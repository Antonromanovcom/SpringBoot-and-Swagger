package online.prostobank.clients.domain.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Phone {
	private Long id;
	private String value;
	private boolean isMain;
}
