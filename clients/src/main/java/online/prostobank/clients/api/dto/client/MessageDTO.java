package online.prostobank.clients.api.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.EmailMessagesEntity;
import online.prostobank.clients.domain.HistoryItem;
import online.prostobank.clients.domain.messages.MessageToClient;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
	private Long clientId;
	private String text;
	private Instant createdAt;
	private String eventInitiator;

	public static MessageDTO createFrom(HistoryItem historyItem) {
		return builder()
				.text(historyItem.getText())
				.createdAt(historyItem.getCreatedAt())
				.eventInitiator(historyItem.getEventInitiator())
				.build();
	}

	public static MessageDTO createFrom(EmailMessagesEntity entity) {
		return builder()
				.text(entity.getMsgContent())
				.createdAt(entity.getDate())
				.build();
	}

	public static MessageDTO createFrom(MessageToClient entity) {
		return builder()
				.text(entity.getText())
				.createdAt(entity.getCreatedAt())
				.createdAt(entity.getCreatedAt())
				.eventInitiator(entity.getManagerLogin())
				.build();
	}
}
