package online.prostobank.clients.api.dto.client_detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.HistoryField;
import online.prostobank.clients.domain.client.Email;
import online.prostobank.clients.domain.client.Employee;
import online.prostobank.clients.domain.client.Human;
import online.prostobank.clients.domain.client.Phone;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter(onMethod = @__( @JsonIgnore))
public class EmployeeDTO {
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "position")
	@HistoryField(title = "Должность")
	private String position;
	@JsonProperty(value = "human")
	@HistoryField(title = "Сведения о физлице")
	private HumanDTO human;
	@JsonProperty(value = "phones")
	@HistoryField(title = "Список телефонов")
	private List<PhoneDTO> phones;
	@JsonProperty(value = "emails")
	@HistoryField(title = "Список e-mail")
	private List<EmailDTO> emails;

	public EmployeeDTO(Employee employee) {
		this.id = employee.getId();
		this.position = employee.getPosition();
		this.human = new HumanDTO(employee.getHuman());
		//телефоны и почты для упрощения работы фронту (отказались от списков) отдают только самую свежую запись
		if (employee.getPhones() != null) {
			this.phones = employee.getPhones().stream()
					.max(Comparator.comparingLong(Phone::getId))
					.map(PhoneDTO::new)
					.map(Collections::singletonList).orElse(Collections.emptyList());
		}
		if (employee.getEmails() != null) {
			this.emails = employee.getEmails().stream()
					.max(Comparator.comparingLong(Email::getId))
					.map(EmailDTO::new)
					.map(Collections::singletonList).orElse(Collections.emptyList());
		}
	}

	public Employee toBusiness() {
		Human businessHuman =  human == null ? null : human.toBusiness();
		List<Phone> businessPhones = phones == null ? Collections.emptyList() : phones.stream().map(PhoneDTO::toBusiness).collect(Collectors.toList());
		List<Email> businessEmails = emails == null ? Collections.emptyList() : emails.stream().map(EmailDTO::toBusiness).collect(Collectors.toList());
		return new Employee(id, position, businessHuman, businessPhones, businessEmails);
	}
}
