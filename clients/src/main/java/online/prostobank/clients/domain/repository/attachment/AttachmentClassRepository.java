package online.prostobank.clients.domain.repository.attachment;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.services.client.ClientAttachmentClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AttachmentClassRepository {
	private final JdbcTemplate jdbcTemplate;

	public int getUserAttachmentCount(Long id) {
		Integer count = jdbcTemplate.queryForObject("select count(a) " +
				"from account_application as a " +
				"inner join attachment_user as attu on a.id = attu.account_application_id " +
				"where a.id = ?", new Object[]{id}, Integer.class);

		return count == null ? 0 : count;
	}

	public int getBankAttachmentCount(Long id) {
		Integer count = jdbcTemplate.queryForObject("select count(a) " +
				"from account_application as a " +
				"inner join attachment_bank as attb on a.id = attb.account_application_id " +
				"where a.id = ?", new Object[]{id}, Integer.class);

		return count == null ? 0 : count;
	}

	public int insert(Long clientId, Long attachmentId, ClientAttachmentClass classId) {
		return jdbcTemplate.update("insert into " + classId.getTable() + " values (?, ?)", clientId, attachmentId);
	}
}
