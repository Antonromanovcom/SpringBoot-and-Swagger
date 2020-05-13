package online.prostobank.clients.api.dto.client_detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.HistoryField;
import online.prostobank.clients.domain.client.Beneficiary;
import online.prostobank.clients.domain.client.Email;
import online.prostobank.clients.domain.client.Human;
import online.prostobank.clients.domain.client.Phone;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class BeneficiaryDTO {
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "human")
	@HistoryField(title = "Сведения о физлице")
	private HumanDTO human;
	@JsonProperty(value = "phones")
	@HistoryField(title = "Список телефонов")
	private List<PhoneDTO> phones;
	@JsonProperty(value = "emails")
	@HistoryField(title = "Список e-mail")
	private List<EmailDTO> emails;
	@JsonProperty(value = "stake_percent")
	@HistoryField(title = "Доля капитала в процентах")
	private Integer stakePercent;
	@JsonProperty(value = "stake_absolute")
	@HistoryField(title = "Доля капитала в копейках")
	private Long stakeAbsolute; //в копейках

	public BeneficiaryDTO(Beneficiary beneficiary) {
		this.id = beneficiary.getId();
		this.human = new HumanDTO(beneficiary.getHuman());
		this.stakePercent = beneficiary.getStakePercent();
		this.stakeAbsolute = beneficiary.getStakeAbsolute();
		if (beneficiary.getPhones() != null) {
			this.phones = beneficiary.getPhones().stream()
					.max(Comparator.comparingLong(Phone::getId))
					.map(PhoneDTO::new)
					.map(Collections::singletonList).orElse(Collections.emptyList());
		}
		if (beneficiary.getEmails() != null) {
			this.emails = beneficiary.getEmails().stream()
					.max(Comparator.comparingLong(Email::getId))
					.map(EmailDTO::new)
					.map(Collections::singletonList).orElse(Collections.emptyList());
		}
	}

	public Beneficiary toBusiness() {
		Human businessHuman =  human == null ? null : human.toBusiness();
		List<Phone> businessPhones = phones == null ? Collections.emptyList() : phones.stream().map(PhoneDTO::toBusiness).collect(Collectors.toList());
		List<Email> businessEmails = emails == null ? Collections.emptyList() : emails.stream().map(EmailDTO::toBusiness).collect(Collectors.toList());
		return new Beneficiary(id, businessHuman, businessPhones, businessEmails, stakePercent, stakeAbsolute);
	}
}