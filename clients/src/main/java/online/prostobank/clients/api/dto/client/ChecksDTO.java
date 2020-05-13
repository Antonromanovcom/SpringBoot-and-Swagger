package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ChecksResultValue;

@Value
@Builder
public class ChecksDTO {
	private Double scoring;
	private String p550;
	private String arrests;
	private String passport;
	private String head;

	public static ChecksDTO createFrom(AccountApplication application) {
		ChecksResultValue checks = application.getChecks();
		return builder()
				.scoring(checks.getKonturCheck())
				.p550(checks.getP550check())
				.arrests(checks.getArrestsFns())
				.passport(checks.getPassportCheck())
				.head(checks.getP550checkHead())
				.build();
	}
}
