package online.prostobank.clients.domain.repository;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.connectors.mail_receiver.IncomingMailMessage;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.EmailMessagesEntity;
import online.prostobank.clients.domain.mappers.EmailMessagesMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.mail.MessagingException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class EmailMessagesRepository {
	private final JdbcTemplate jdbcTemplate;

	public List<EmailMessagesEntity> getEmailMessages(Long id) {
		return jdbcTemplate.query("select * from email_messages where account_application_id = ?",
				new Object[]{id},
				new EmailMessagesMapper());
	}

	public void insert(AccountApplication accountApplication, IncomingMailMessage message) throws MessagingException {
		jdbcTemplate.update(
				"INSERT INTO email_messages (account_application_id, msg_content, date_time) VALUES (?, ?, ?)",
				accountApplication.getId(), message.getContentMsg(), new Timestamp(message.getSource().getSentDate().getTime()));
	}
}
