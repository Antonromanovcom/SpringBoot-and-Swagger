package online.prostobank.clients.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.enums.HistoryItemType;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getUserInfoDto;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Entity
public class HistoryItem {
	@Id
	@GeneratedValue
	private Long id;
	@Getter(onMethod = @__(@JsonIgnore))
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	private AccountApplication app;
	@Type(type = "org.hibernate.type.TextType")
	private String text;
	private Instant createdAt;
	private HistoryItemType itemType;
	private String eventInitiator;

	/**
	 * Основной способ создания записи в историю заявки.
	 */
	public HistoryItem(AccountApplication acc, String text, Instant createdAt) {
		this(acc, text, createdAt, HistoryItemType.DATA_CHANGE);
	}

	/**
	 * Создание с указанием типа записи в историю
	 */
	public HistoryItem(AccountApplication acc, String text, Instant createdAt, HistoryItemType itemType) {
		Assert.notNull(acc, "acc can't be null");
		Assert.notNull(text, "text can't be null");
		Assert.notNull(createdAt, "createdAt can't be null");
		Assert.notNull(itemType, "itemType can't be null");

		this.text = text;
		this.createdAt = createdAt;
		this.app = acc;
		this.itemType = itemType;
		this.eventInitiator = SecurityContextHelper.getCurrentUsername();
		log.info("Made" + (itemType.equals(HistoryItemType.DATA_CHANGE) ? " history " : " comment ") + "record for application " + acc.getId() + ": '" + text + "'. Initiator " + getUserInfoDto().toString());
	}

	public boolean isComment() {
		return HistoryItemType.COMMENT.equals(this.itemType);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HistoryItem that = (HistoryItem) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(app.getId(), that.app.getId()) &&
				Objects.equals(text, that.text) &&
				Objects.equals(createdAt, that.createdAt) &&
				itemType == that.itemType &&
				Objects.equals(eventInitiator, that.eventInitiator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, app.getId(), text, createdAt, itemType, eventInitiator);
	}

	@Override
	public String toString() {
		return "HistoryItem " + hashCode() + " {" +
				"id=" + id +
				", app=" + app.getId() +
				", text='" + text + '\'' +
				", createdAt=" + createdAt +
				", itemType=" + itemType +
				", eventInitiator='" + eventInitiator + '\'' +
				'}';
	}
}
