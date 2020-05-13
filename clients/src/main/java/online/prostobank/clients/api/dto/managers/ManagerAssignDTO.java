package online.prostobank.clients.api.dto.managers;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ManagerAssignDTO {
	private UUID managerUuid;
	private List<Long> clientIds;
}
