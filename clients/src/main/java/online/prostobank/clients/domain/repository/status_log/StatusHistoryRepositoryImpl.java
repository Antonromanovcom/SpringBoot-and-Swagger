package online.prostobank.clients.domain.repository.status_log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.StatusHistoryItem;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StatusHistoryRepositoryImpl implements StatusHistoryRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public void insertStatusHistory(Long clientId, ClientStates previousStatus, ClientEvents event, ClientStates newStatus, String createdBy, String causeMessage) {
		Timestamp timestamp = Timestamp.from(Instant.now());
		if (clientId == null || previousStatus == null || event == null) {
			log.error("Не указан обязательный параметр");
			return;
		}
		if (!isClientExists(clientId)) {
			log.error("Клиент с указанным идентификатором не существует");
			return;
		}

		try {
			long id = getNextSequenceValue();
			MapSqlParameterSource namedParameters = new MapSqlParameterSource()
					.addValue("id", id)
					.addValue("client_id", clientId)
					.addValue("created_at", timestamp)
					.addValue("previous_state", previousStatus.name())
					.addValue("new_state", newStatus == null ? "null" : newStatus.name())
					.addValue("event", event.name())
					.addValue("created_by", createdBy)
					.addValue("cause_message", causeMessage);

			jdbcTemplate.update(
					"INSERT INTO sm_transition_log (id, client_id, created_at, previous_state, new_state, event, created_by, cause_message) " +
							"VALUES (:id, :client_id, :created_at, :previous_state, :new_state, :event, :created_by, :cause_message)",
					namedParameters);
		} catch (DataAccessException | IllegalArgumentException ex) {
			log.error("Не удалось сохранить данные об изменении статуса", ex);
		}
	}

	@Override
	public List<StatusHistoryItem> selectAllByClientIdOrdered(Long clientId) {
		return jdbcTemplate.query(
				"select * from sm_transition_log " +
						"where client_id = :client_id " +
						"order by created_at",
				new MapSqlParameterSource()
						.addValue("client_id", clientId),
				new BeanPropertyRowMapper<>(StatusHistoryItem.class));
	}

	private boolean isClientExists(long clientId) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", clientId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from account_application where id = :id", namedParameters,
				Integer.class);

		return exists != null && exists > 0;
	}

	private Long getNextSequenceValue() throws IllegalStateException {
		Long value = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence') as num", Collections.emptyMap(), Long.class);
		if (value == null) {
			throw new IllegalStateException("Генератор последовательностей БД не вернул валидные данные");
		}
		return value;
	}
}
