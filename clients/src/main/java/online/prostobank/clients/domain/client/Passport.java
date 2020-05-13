package online.prostobank.clients.domain.client;

import lombok.Getter;

import java.time.Instant;
@Getter
public class Passport {
	private final Long id;
	private final String number;
	private final String series;
	private final String issueDepartmentCode;
	private final Instant issueDate;
	private final Instant dateOfBirth;
	private final String placeOfBirth;
	private final String issueDepartmentName;
	private final boolean isValid;
	private final boolean isMain;

	//намеренно без lombok виду наличия множественных параметров одного типа
	public Passport(Long id, String number, String series, String issueDepartmentCode, Instant issueDate,
					Instant dateOfBirth, String placeOfBirth, String issueDepartmentName, boolean isValid, boolean isMain) {
		this.id = id;
		this.number = number;
		this.series = series;
		this.issueDepartmentCode = issueDepartmentCode;
		this.issueDate = issueDate;
		this.dateOfBirth = dateOfBirth;
		this.placeOfBirth = placeOfBirth;
		this.issueDepartmentName = issueDepartmentName;
		this.isValid = isValid;
		this.isMain = isMain;
	}
}
