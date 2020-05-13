package online.prostobank.clients.api.dto.managers;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.services.GridUtils;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class ClientForManagerGridResponse {
	private long total;
	private List<ClientForManagerGridDTO> clients;
	private Set<String> availableSortingFields = GridUtils.CLIENT_ALIAS_MAP.keySet();
}
