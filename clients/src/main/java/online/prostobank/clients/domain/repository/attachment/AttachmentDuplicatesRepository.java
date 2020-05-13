package online.prostobank.clients.domain.repository.attachment;

import online.prostobank.clients.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;

@Repository
public class AttachmentDuplicatesRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private static final int POSTFIX_LENGTH = 6;

	@Autowired
	public AttachmentDuplicatesRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getUniqueName(@Nullable Long clientId, String name) {
		if (StringUtils.isEmpty(name)) {
			return Utils.getRandomString(POSTFIX_LENGTH);
		}
		MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("name", name);

		String query;
		if (clientId == null) {
			query = "select count(*) from attachment where attachment_name = :name";
		} else {
			namedParameters.addValue("clientId", clientId);
			query = "SELECT count(*) FROM (\n" +
					"SELECT id FROM attachment JOIN attachment_bank ON attachment.id = attachment_bank.bank_attachments_id\n" +
					"WHERE attachment_bank.account_application_id = :clientId AND attachment.attachment_name = :name\n" +
					"UNION ALL\n" +
					"SELECT id FROM attachment JOIN attachment_user ON attachment.id = attachment_user.attachments_id\n" +
					"WHERE attachment_user.account_application_id = :clientId AND attachment.attachment_name = :name\n" +
					"    ) as summary";
		}

		Integer exists = jdbcTemplate.queryForObject(query, namedParameters, Integer.class);

		if (exists != null && exists > 0) {
			String random = "_" + Utils.getRandomString(POSTFIX_LENGTH);
			if (name.contains(".")) {
				return name.replaceFirst("\\.", random + ".");
			}
			return name + random;
		}
		return name;
	}
}
