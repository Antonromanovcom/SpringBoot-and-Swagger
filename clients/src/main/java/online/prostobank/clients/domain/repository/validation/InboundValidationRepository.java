package online.prostobank.clients.domain.repository.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для обслуживания процесса валидации входных данных.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InboundValidationRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	/**
	 * Проверка на наличие карточки с указанным ИНН.
	 * @param inn
	 * @return
	 */
	public boolean isInnExists(String inn) {
		if (StringUtils.isEmpty(inn)) {
			return false;
		}
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("inn", inn);

		Integer exists = jdbcTemplate.queryForObject("SELECT count(*) FROM account_application WHERE inn = :inn", namedParameters, Integer.class);
		return exists != null && exists > 0;
	}

	/**
	 * Проверка на наличие карточки с указанным телефоном.
	 * @param phone
	 * @return
	 */
	public boolean isPhoneExists(String phone) {
		if (StringUtils.isEmpty(phone)) {
			return false;
		}
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("phone", phone);

		Integer exists = jdbcTemplate.queryForObject("SELECT count(*) FROM account_application WHERE phone = :phone", namedParameters, Integer.class);
		return exists != null && exists > 0;
	}
}
