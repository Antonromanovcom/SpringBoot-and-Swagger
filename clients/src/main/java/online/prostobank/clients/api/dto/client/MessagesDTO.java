package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.EmailMessagesEntity;
import online.prostobank.clients.domain.HistoryItem;
import online.prostobank.clients.domain.enums.HistoryItemType;
import online.prostobank.clients.domain.messages.MessageToClient;

import java.util.*;
import java.util.stream.Collectors;

import static online.prostobank.clients.utils.Utils.ROBOT;

@Value
@Builder
public class MessagesDTO {
	private List<MessageDTO> comments;
	private List<MessageDTO> historyMessages;
	private List<MessageDTO> systemMessages;
	private List<MessageDTO> emailMessages;
	private List<MessageDTO> managerMessages;

	public static MessagesDTO createFrom(AccountApplication application,
										 List<EmailMessagesEntity> messagesFromClients,
										 List<MessageToClient> messagesToClients) {
		List<MessageDTO> comments = new ArrayList<>();
		List<MessageDTO> historyMessages = new ArrayList<>();
		List<MessageDTO> systemMessages = new ArrayList<>();

		application.getItems().stream()
				.sorted(Comparator.comparing(HistoryItem::getCreatedAt).reversed())
				.forEach(historyItem -> {
					if (historyItem.getItemType() == HistoryItemType.COMMENT) {
						comments.add(MessageDTO.createFrom(historyItem));
					} else if (historyItem.getItemType() == HistoryItemType.DATA_CHANGE) {
						if (Objects.equals(ROBOT, historyItem.getEventInitiator())) {
							systemMessages.add(MessageDTO.createFrom(historyItem));
						} else {
							historyMessages.add(MessageDTO.createFrom(historyItem));
						}
					}
				});

		List<MessageDTO> clientMessages = Optional.ofNullable(messagesFromClients)
				.map(messages -> messages.stream()
						.map(MessageDTO::createFrom)
						.collect(Collectors.toList()))
				.orElse(Collections.emptyList());
		List<MessageDTO> managerMessages = Optional.ofNullable(messagesToClients)
				.map(messages -> messages.stream()
						.map(MessageDTO::createFrom)
						.collect(Collectors.toList()))
				.orElse(Collections.emptyList());
		return builder()
				.comments(comments)
				.historyMessages(historyMessages)
				.systemMessages(systemMessages)
				.emailMessages(clientMessages)
				.managerMessages(managerMessages)
				.build();
	}
}
