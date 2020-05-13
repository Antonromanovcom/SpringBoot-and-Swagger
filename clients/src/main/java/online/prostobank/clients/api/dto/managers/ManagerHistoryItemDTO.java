package online.prostobank.clients.api.dto.managers;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.domain.managers.ManagerHistoryItem;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class ManagerHistoryItemDTO {
	private Long id;
	private String text;
	private Instant createdAt;
	private String eventInitiator;

	public static ManagerHistoryItemDTO createFrom(ManagerHistoryItem item) {
		return new ManagerHistoryItemDTO()
				.setId(item.getId())
				.setText(item.getText())
				.setCreatedAt(item.getCreatedAt())
				.setEventInitiator(item.getEventInitiator());
	}
}
