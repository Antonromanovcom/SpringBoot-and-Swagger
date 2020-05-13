package online.prostobank.clients.api.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeStatusDTO {
	// for now AccountApplication id
	private long clientId;
	private int statusId;
}
