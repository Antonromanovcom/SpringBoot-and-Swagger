package online.prostobank.clients.services.dbo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class AccountApplicationJdbcRepositoryImpl implements AccountApplicationJdbcRepository {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public String getEmail(long applicationId) {
		return jdbcTemplate.queryForObject(
				"SELECT client_email FROM account_application where id = ?", String.class, applicationId);
	}
}
