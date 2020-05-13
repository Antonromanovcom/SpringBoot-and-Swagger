package online.prostobank.clients.api.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValue;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEditDTO {
	// for now AccountApplication id
	private long clientId;
	private String email;
	private String inn;
	private String phone;
	private CityDTO city;
	private String clientTariffPlan;
	private Instant clientCallback;

	public static ClientEditDTO createFrom(AccountApplication application) {
		ClientValue client = application.getClient();
		return builder()
				.clientId(application.getId())
				.email(client.getEmail())
				.inn(client.getInn())
				.phone(client.getPhone())
				.city(CityDTO.createFrom(application.getCity()))
				.clientTariffPlan(application.getClientTariffPlan())
				.clientCallback(application.getClientCallback())
				.build();
	}
}
