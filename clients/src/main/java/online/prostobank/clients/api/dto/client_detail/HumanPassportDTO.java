package online.prostobank.clients.api.dto.client_detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.HistoryField;
import online.prostobank.clients.domain.client.Passport;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class HumanPassportDTO {
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "number")
	@HistoryField(title = "Номер паспорта")
	private String number;
	@JsonProperty(value = "series")
	@HistoryField(title = "Серия паспорта")
	private String series;
	@JsonProperty(value = "issue_department_code")
	@HistoryField(title = "Код подразделения выдавшего паспорт")
	private String issueDepartmentCode;
	@JsonProperty(value = "issue_date")
	@HistoryField(title = "Дата выдачи")
	private LocalDate issueDate;
	@JsonProperty(value = "date_of_birth")
	@HistoryField(title = "Дата рождения")
	private LocalDate dateOfBirth;
	@JsonProperty(value = "place_of_birth")
	@HistoryField(title = "Место рождения")
	private String placeOfBirth;
	@JsonProperty(value = "issue_department_name")
	@HistoryField(title = "Наименование подразделения выдавшего паспорт")
	private String issueDepartmentName;
	@JsonProperty(value = "is_valid")
	private boolean isValid;
	@JsonProperty(value = "is_main")
	private boolean isMain;

	public HumanPassportDTO (Passport passport) {
		this.id = passport.getId();
		this.number = passport.getNumber();
		this.series = passport.getSeries();
		this.issueDepartmentCode = passport.getIssueDepartmentCode();
		this.issueDate = passport.getIssueDate().atZone(ZoneOffset.UTC).toLocalDate();
		this.dateOfBirth = passport.getDateOfBirth().atZone(ZoneOffset.UTC).toLocalDate();
		this.placeOfBirth = passport.getPlaceOfBirth();
		this.issueDepartmentName = passport.getIssueDepartmentName();
		this.isValid = passport.isValid();
		this.isMain = passport.isMain();
	}

	public Passport toBusiness() {
		Instant localIssueDate = issueDate == null ? LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)
				: issueDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant localDateOfBirth = dateOfBirth == null ? LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)
				: dateOfBirth.atStartOfDay().toInstant(ZoneOffset.UTC);
		return new Passport(id, number, series, issueDepartmentCode, localIssueDate,
				localDateOfBirth, placeOfBirth, issueDepartmentName, isValid, isMain);
	}
}
