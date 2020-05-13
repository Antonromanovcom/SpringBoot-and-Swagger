package online.prostobank.clients.api.dto.client;

import lombok.Data;
import online.prostobank.clients.services.GridUtils;

import java.util.List;
import java.util.Set;

@Data
public class ClientGridResponse {
	private long total;
	private List<ClientGridDTO> clients;
	public Set<String> availableSortingFields = GridUtils.CLIENT_ALIAS_MAP.keySet();

	public ClientGridResponse(long total, List<ClientGridDTO> clients) {
		this.total = total;
		this.clients = clients;
	}
}
