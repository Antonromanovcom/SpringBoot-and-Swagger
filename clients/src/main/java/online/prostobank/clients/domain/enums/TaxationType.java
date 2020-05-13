package online.prostobank.clients.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TaxationType {
	OSNO("ОСНО"),
	USN("УСН"),
	PATENT("Патент"),
	ENVD("ЕНВД"),
	ESHN("ЕСХН"),
	;

	private final String ruName;

	public static TaxationType valueBy(String caption) {
		return Arrays.stream(values())
				.filter(contractTypes -> contractTypes.getRuName().equals(caption))
				.findFirst()
				.orElse(null);
	}
}
