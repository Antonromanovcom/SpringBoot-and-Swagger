package online.prostobank.clients.domain.tss;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TssParam {
	private String name;
	private String value;
}
