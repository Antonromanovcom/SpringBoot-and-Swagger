package online.prostobank.clients.domain.mappers;

import online.prostobank.clients.domain.EmailMessagesEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class EmailMessagesMapper implements RowMapper<EmailMessagesEntity> {
    @Override
    public EmailMessagesEntity mapRow(ResultSet resultSet, int i) throws SQLException {
        EmailMessagesEntity messagesEntity = new EmailMessagesEntity();

        Timestamp dateTime = resultSet.getTimestamp("date_time");
        Instant date = dateTime.toInstant();
        long applicationId = resultSet.getLong("account_application_id");
        String msgContent = resultSet.getString("msg_content");
        messagesEntity.setAccountApplicationId(applicationId)
                .setDate(date)
                .setMsgContent(msgContent);


        return messagesEntity;
    }
}
