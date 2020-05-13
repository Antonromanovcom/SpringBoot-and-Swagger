package online.prostobank.clients.domain.repository;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.enums.HistoryItemType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Repository
public class HistoryRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final MapSqlParameterSource emptyParameters = new MapSqlParameterSource();

	public HistoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Сохранение записи типа  HistoryItemType.DATA_CHANGE в историю
	 *
	 * @param accountId - номер заявки
	 * @param initiator - логин инициатора операции
	 * @param message   - произвольное сообщение
	 */
	public void insertChangeHistory(long accountId, String initiator, String message) {
		try {
			insertChangeHistory(accountId, initiator, message, HistoryItemType.DATA_CHANGE);
		} catch (Exception ex) {
			log.error("Не удалось вставить запись в историю", ex);
		}
	}

	/**
	 * Сохранение записи в историю
	 * @param accountId
	 * @param initiator
	 * @param message
	 * @param itemType
	 */
	public void insertChangeHistory(long accountId, String initiator, String message, HistoryItemType itemType) {
		Instant timestamp = Instant.now();

		if (StringUtils.isEmpty(initiator) || StringUtils.isEmpty(message)) {
			throw new IllegalArgumentException("Не указан инициатор или сообщение");
		}

		if (!isClientExists(accountId)) {
			throw new IllegalArgumentException("Клиент с указанным идентификатором не существует");
		}

		long id = getNextSequenceValue();
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("account_id", accountId)
				.addValue("initiator", initiator)
				.addValue("type", itemType.ordinal())
				.addValue("message", message)
				.addValue("created_at", Timestamp.from(timestamp));
		jdbcTemplate.update(
				"INSERT INTO history_item (id, app_id, created_at, item_type, text, event_initiator) " +
						"VALUES (:id, :account_id, :created_at, :type, :message, :initiator)",
				namedParameters);
	}

	private boolean isClientExists(long accountId) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", accountId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from account_application where id = :id", namedParameters,
				Integer.class);

		return exists != null && exists > 0;
	}

	private Long getNextSequenceValue() throws IllegalStateException {
		Long value = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence') as num", emptyParameters, Long.class);
		if (value == null) {
			throw new IllegalStateException("Генератор последовательностей БД не вернул валидные данные");
		}
		return value;
	}
}
