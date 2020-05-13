package online.prostobank.clients.api.dto.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientGridRequest extends GridRequest {
	private PageFiltersDTO filters;
}
