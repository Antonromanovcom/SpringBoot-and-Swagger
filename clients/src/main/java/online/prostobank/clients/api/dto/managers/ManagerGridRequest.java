package online.prostobank.clients.api.dto.managers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import online.prostobank.clients.api.dto.client.GridRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerGridRequest extends GridRequest {
	private ManagerPageFiltersDTO filters;
}
