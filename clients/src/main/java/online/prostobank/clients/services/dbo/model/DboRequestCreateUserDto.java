package online.prostobank.clients.services.dbo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.AccountValue;
import online.prostobank.clients.domain.ClientValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static online.prostobank.clients.utils.Utils.wrapProperty;

@Getter
@Setter
@Builder
@ToString
public class DboRequestCreateUserDto implements Serializable {
	private static final long serialVersionUID = -885566804814860247L;

	private String userId;
	private String bankBic;
	private String clientRef;

	private String surname;
	private String name;
	private String patronymic;
	private String email;
	private String phone;

	private String accountNumber;
	private String tariff;

	private String legEntityName;
	private String legEntityInn;
	private Map<String, String> legEntityKpp;
	private String legEntityPhone;
	private String legEntityEmail;

	public static DboRequestCreateUserDto createFrom(AccountApplication accountApplication, String keycloakId) {
		ClientValue client = accountApplication.getClient();
		AccountValue account = accountApplication.getAccount();

		String bankBik = account.getBankBik();
		String inn = client.getInn();

		String surname = client.getSurname();
		String name = client.getFirstName();
		String patronymic = client.getSecondName();
		String email = client.getEmail();
		String phone = client.getPhone();

		String accountNumber = account.getAccountNumber();
		String tariff = accountApplication.getClientTariffPlan();
		String legEntityName = client.getShortName();
		HashMap<String, String> legEntityKpp = new HashMap<>();
		legEntityKpp.put("string", wrapProperty(client.getKpp()));

		return DboRequestCreateUserDto.builder()
				.userId(wrapProperty(keycloakId))
				.bankBic(wrapProperty(bankBik))
				.clientRef(wrapProperty(inn))
				.surname(wrapProperty(surname))
				.name(wrapProperty(name))
				.patronymic(wrapProperty(patronymic))
				.email(wrapProperty(email))
				.phone(wrapProperty(phone))
				.accountNumber(wrapProperty(accountNumber))
				.tariff(wrapProperty(tariff))
				.legEntityName(wrapProperty(legEntityName))
				.legEntityInn(wrapProperty(inn))
				.legEntityKpp(legEntityKpp)
				.legEntityPhone(wrapProperty(phone))
				.legEntityEmail(wrapProperty(email))
				.build();
	}
}
