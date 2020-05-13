package online.prostobank.clients.api.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.AccountApplication;

/**
 * Вспомогательные данные клиента -- наименование тарифа и времени перезвона
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ClientAuxInfoDTO {
	@JsonProperty(value = "recall_at")
	private Long recallTimeMs;
	@JsonProperty(value = "tariff")
	private String tariff;

	public static ClientAuxInfoDTO from(AccountApplication application) {
		return new ClientAuxInfoDTO(
				application.getClientCallback() != null ? application.getClientCallback().toEpochMilli() : null,
				application.getClientTariffPlan());
	}
}
