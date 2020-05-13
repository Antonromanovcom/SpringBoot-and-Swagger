package online.prostobank.clients.api.dto.client_detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.HistoryField;
import online.prostobank.clients.domain.client.Human;
import online.prostobank.clients.domain.client.Passport;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class HumanDTO {
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "first_name")
	@HistoryField(title = "Имя")
	private String firstName;
	@JsonProperty(value = "middle_name")
	@HistoryField(title = "Отчество")
	private String middleName;
	@JsonProperty(value = "last_name")
	@HistoryField(title = "Фамилия")
	private String lastName;
	@JsonProperty(value = "snils")
	@HistoryField(title = "СНИЛС")
	private String snils;
	@JsonProperty(value = "registration_address")
	@HistoryField(title = "Адрес регистрации")
	private String registrationAddress;
	@JsonProperty(value = "inn")
	@HistoryField(title = "ИНН")
	private String inn;
	@JsonProperty(value = "citizenship")
	@HistoryField(title = "Гражданство")
	private String citizenship;
	@JsonProperty(value = "passports")
	@HistoryField(title = "Паспорта")
	private List<HumanPassportDTO> passports;

	public HumanDTO(Human human) {
		this.id = human.getId();
		this.firstName = human.getFirstName();
		this.middleName = human.getMiddleName();
		this.lastName = human.getLastName();
		this.snils = human.getSnils();
		this.registrationAddress = human.getRegistrationAddress();
		this.inn = human.getInn();
		this.citizenship = human.getCitizenship();
		if (human.getPassport() != null) {
			this.passports = human.getPassport().stream()
					.max(Comparator.comparingLong(Passport::getId))
					.map(HumanPassportDTO::new)
					.map(Collections::singletonList).orElse(Collections.emptyList());
		}
	}

	public Human toBusiness() {
		Human human = new Human(id, firstName, middleName, lastName, snils, registrationAddress, inn, citizenship);
		if (passports != null) {
			human.setPassport(passports.stream().map(HumanPassportDTO::toBusiness).collect(Collectors.toList()));
		}
		return human;
	}
}
