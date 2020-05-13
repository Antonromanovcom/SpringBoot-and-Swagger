package online.prostobank.clients.domain;

import lombok.Data;

import java.time.Instant;

@Data
public class StatusHistoryItem {
	private Long id;
	private Long clientId;
	private Instant createdAt;
	private String previousState;
	private String newState;
	private String createdBy;
	private String causeMessage;
	private String event;
}
