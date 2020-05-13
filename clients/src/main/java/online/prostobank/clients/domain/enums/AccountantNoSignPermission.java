package online.prostobank.clients.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum AccountantNoSignPermission {
	ACCOUNTANT_SERVICE("Бухгалтерской службе"),
	AUDIT_FIRM("Специализированной организации (аудиторской фирме)"),
	ACCOUNTANT_SPECIALIST("Бухгалтеру-специалисту (индивидуальному аудитору)"),
	OTHER("Прочее (иные соглашения, заключенные на ведение учета)"),
	;

	private final String caption;

	public static AccountantNoSignPermission valueBy(String caption) {
		return Arrays.stream(values())
				.filter(contractTypes -> Objects.equals(contractTypes.getCaption(), caption))
				.findFirst()
				.orElse(null);
	}
}
