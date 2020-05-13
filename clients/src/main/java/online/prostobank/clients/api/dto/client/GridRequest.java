package online.prostobank.clients.api.dto.client;

import lombok.Data;

@Data
public class GridRequest {
	private PageInfoDTO page;
	private PageSortDTO sort;
}
