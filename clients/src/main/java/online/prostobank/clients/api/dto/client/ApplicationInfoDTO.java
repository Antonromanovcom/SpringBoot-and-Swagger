package online.prostobank.clients.api.dto.client;

import lombok.Builder;
import lombok.Value;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.HistoryItem;

import static java.util.Comparator.comparing;

@Value
@Builder
public class ApplicationInfoDTO {
	private String comment;
	private String creator;
	private String source;
	private String manager;

	public static ApplicationInfoDTO createFrom(AccountApplication application) {
		String comment = application.getItems().stream()
				.filter(HistoryItem::isComment)
				.max(comparing(HistoryItem::getCreatedAt))
				.map(HistoryItem::getText)
				.orElse("");

		return builder()
				.comment(comment)
				.creator(application.getCreator())
				.source(application.getSource().getValue())
				.manager(application.getAssignedTo())
				.build();
	}
}
