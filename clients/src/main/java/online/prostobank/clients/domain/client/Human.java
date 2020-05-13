package online.prostobank.clients.domain.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class Human {
	private final Long id;
	private final String firstName;
	private final String middleName;
	private final String lastName;
	private final String snils;
	private final String registrationAddress;
	private final String inn;
	private final String citizenship;
	@Setter
	private List<Passport> passport;

	public Human(Long id, String firstName, String middleName, String lastName, String snils, String registrationAddress,
				 String inn, String citizenship) {
		this.id = id;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.snils = snils;
		this.registrationAddress = registrationAddress;
		this.inn = inn;
		this.citizenship = citizenship;
	}
}
