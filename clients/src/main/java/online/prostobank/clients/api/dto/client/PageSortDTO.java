package online.prostobank.clients.api.dto.client;

import lombok.Data;

import java.util.List;

@Data
public class PageSortDTO {
	private String by;
	private boolean reverse;

	private List<PageSortDTO> sortFields;
}
