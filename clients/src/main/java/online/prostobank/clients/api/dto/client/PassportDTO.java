package online.prostobank.clients.api.dto.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.PersonValue;

import java.time.LocalDate;

@Value
@Builder
public class PassportDTO {
	private String snils;
	private String series;
	private String number;
	private LocalDate dateOfIssue;
	private String issuerCode;
	private String issuer;
	private LocalDate dateOfBirth;
	private String placeOfBirth;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public PassportDTO(@JsonProperty("snils") String snils,
					   @JsonProperty("series") String series,
					   @JsonProperty("number") String number,
					   @JsonProperty("dateOfIssue") LocalDate dateOfIssue,
					   @JsonProperty("issuerCode") String issuerCode,
					   @JsonProperty("issuer") String issuer,
					   @JsonProperty("dateOfBirth") LocalDate dateOfBirth,
					   @JsonProperty("placeOfBirth") String placeOfBirth) {
		this.snils = snils == null ? null : snils.trim();
		this.series = series == null ? null : series.replaceAll("\\s", "");
		this.number = number == null ? null : number.replaceAll("\\s", "");
		this.dateOfIssue = dateOfIssue;
		this.issuerCode = issuerCode == null ? null : issuerCode.replaceAll("\\s", "");
		this.issuer = issuer == null ? null : issuer.trim();
		this.dateOfBirth = dateOfBirth;
		this.placeOfBirth = placeOfBirth;
	}

	public static PassportDTO createFrom(AccountApplication application) {
		PersonValue person = application.getPerson();
		return builder()
				.snils(person.getSnils())
				.series(person.getSer())
				.number(person.getNum())
				.dateOfIssue(person.getDoi())
				.issuerCode(person.getIssuerCode())
				.issuer(person.getIssuer())
				.dateOfBirth(person.getDob())
				.placeOfBirth(person.getPob())
				.build();
	}
}
