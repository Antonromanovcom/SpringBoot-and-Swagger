package online.prostobank.clients.domain.repository;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.rest.ClientValueDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ClientForExternalModuleRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public ClientForExternalModuleRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Получение ограниченных сведений о клиенте по номеру его счета (в интересах модуля №2)
	 * @param accountNumber
	 * @return
	 */
	public Optional<ClientValueDTO> getClientInfoByAccount(@NotEmpty(message = "Номер счета не может быть пуст") String accountNumber) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("accountNumber", accountNumber);

		try {
			String sql = "select client_name, head, ogrn, inn FROM account_application WHERE account_number = :accountNumber";
			List<ClientValueDTO> clients = jdbcTemplate.query(
					sql,
					namedParameters,
					(it, count) -> new ClientValueDTO(
							it.getString("client_name"),
							it.getString("head"),
							it.getString("ogrn"),
							it.getString("inn")));

			if (clients == null || clients.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(clients.get(0));
		} catch (DataAccessException ex) {
			log.error("Ошибка БД при поиске клиента по номеру счета", ex);
			return Optional.empty();
		}
	}
}
