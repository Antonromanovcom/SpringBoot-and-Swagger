package online.prostobank.clients.domain.repository.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.AttachmentDTO;
import online.prostobank.clients.api.dto.ClientDocumentsDTO;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.attachment.DocumentClass;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.services.StorageException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.*;

/**
 * Функциональная замена embedded полям аттачей в AccountApplication для манипуляции blob-ами.
 * Также используется при обращениях со стороны сервиса миграции файлового хранилища.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AttachmentMigrationRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	/**
	 * Массив объектов, содержащих идентификатор клиента и списки идентификаторов принадлежащих клиенту документов
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<ClientDocumentsDTO> getClientsDocuments(long offset, long limit) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("offset", offset)
				.addValue("limit", limit);

		String query = "(SELECT au.account_application_id as client_id, att.id as attach_id, 'user' as type, att.migrated FROM attachment as att JOIN attachment_user au on att.id = au.attachments_id\n" +
				"WHERE au.account_application_id IN (SELECT id FROM account_application LIMIT :limit OFFSET :offset) GROUP BY au.account_application_id, att.id ORDER BY au.account_application_id)\n" +
				"UNION ALL\n" +
				"(SELECT ab.account_application_id as client_id, att.id as attach_id, 'bank' as type, att.migrated FROM attachment as att JOIN attachment_bank ab on att.id = ab.bank_attachments_id\n" +
				" WHERE ab.account_application_id IN (SELECT id FROM account_application LIMIT :limit OFFSET :offset) GROUP BY ab.account_application_id, att.id ORDER BY ab.account_application_id\n" +
				")";

		Map<Long, ClientDocumentsDTO> documentsDTOMap = new HashMap<>();

		List<AttachRow> rows = jdbcTemplate.query(query, namedParameters, (resultSet, i) ->
				new AttachRow(resultSet.getLong("client_id"), resultSet.getLong("attach_id"),
						resultSet.getString("type"), resultSet.getBoolean("migrated")));

		for (AttachRow row : rows) {
			Long clientId = row.getClientId();
			if (clientId == null) {
				continue;
			}
			documentsDTOMap.putIfAbsent(clientId, new ClientDocumentsDTO(clientId));
			ClientDocumentsDTO dto = documentsDTOMap.get(clientId);
			if (row.getType().equals("user")) {
				dto.getUserDocuments().putIfAbsent(row.getAttachId(), row.isMigrated());
			}
			if (row.getType().equals("bank")) {
				dto.getBankDocuments().putIfAbsent(row.getAttachId(), row.isMigrated());
			}
		}
		return new ArrayList<>(documentsDTOMap.values());
	}

	/**
	 * Метаданные документа
	 * @param id
	 * @return
	 */
	public AttachmentDTO getAttachmentMeta(Long id) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id);

		return jdbcTemplate.queryForObject("SELECT * FROM attachment WHERE id = :id",
				namedParameters, (resultSet, i) ->
						new AttachmentDTO(
								resultSet.getLong("id"), resultSet.getInt("att_type"), resultSet.getString("attachment_name"),
								resultSet.getTimestamp("created_at").toInstant(),
								resultSet.getString("functional_type"), resultSet.getBoolean("quality"), resultSet.getBoolean("verified")));
	}

	/**
	 * Получение двоичных данных документа
	 * @param id
	 * @return
	 */
	public byte[] getAttachmentContent(Long id) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id);

		try {
			return jdbcTemplate.queryForObject("select content from attachment where id = :id",
					namedParameters,
					(it, count) -> it.getBytes("content"));
		} catch (DataAccessException ex) {
			log.error("не удалось получить бинарные данные документа", ex);
			return new byte[0];
		}
	}

	/**
	 * Промаркировать вложение, как мигрировавшее
	 * @param id
	 */
	public void setAttachmentAsMigrated(Long id) {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("id", id);

		jdbcTemplate.update("UPDATE attachment SET migrated = true WHERE id = :id", namedParameters);
	}

	/**
	 * Количество вложений, помеченных как не мигрировавшие
	 * @return
	 */
	public long getNotMigratedCount() {
		Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM attachment WHERE migrated = false",
				Collections.emptyMap(), Integer.class);
		return count == null ? 0 : count;
	}

	/**
	 * Общее количество записей о клиентах
	 * @return
	 */
	public long getClientTotalCount() {
		Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM account_application",
				Collections.emptyMap(), Integer.class);
		return count == null ? 0 : count;
	}

	/**
	 * Общее количество вложений
	 * @return
	 */
	public long getAttachTotalCount() {
		Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM attachment",
				Collections.emptyMap(), Integer.class);
		return count == null ? 0 : count;
	}

	/**
	 * Количество пользовательских вложений
	 * @return
	 */
	public int getUserAttachCount(long clientId) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("clientId", clientId);

		String sql ="SELECT count(*) FROM attachment as att  JOIN attachment_user au on att.id = au.attachments_id WHERE au.account_application_id = :clientId";
		Integer count;
		try {
			count = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
		} catch (DataAccessException ex) {
			log.error("Не удалось получить кол-во пользовательских документов", ex);
			throw new StorageException("Не удалось получить кол-во пользовательских документов");
		}
		return count == null ? 0 : count;
	}

	/**
	 * Количество банковских вложений
	 * @return
	 */
	public int getBankAttachCount(Long clientId) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("clientId", clientId);

		String sql ="SELECT count(*) FROM attachment as att  JOIN attachment_bank au on att.id = au.bank_attachments_id WHERE au.account_application_id = :clientId";
		Integer count;
		try {
			count = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
		} catch (DataAccessException ex) {
			log.error("Не удалось получить кол-во банковских документов ", ex);
			throw new StorageException("Не удалось получить кол-во банковских документов");
		}
		return count == null ? 0 : count;
	}

	/**
	 * Удалить файл из БД
	 * @param attachmentId
	 */
	@Transactional
	public void deleteAttachment(Long attachmentId) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("attachmentId", attachmentId);
		String deleteAttach ="DELETE FROM attachment WHERE id = :attachmentId";
		String deleteFromUser ="DELETE FROM attachment_user WHERE attachments_id = :attachmentId";
		String deleteFromBank ="DELETE FROM attachment_bank WHERE bank_attachments_id = :attachmentId";
		try {
			jdbcTemplate.update(deleteFromUser, namedParameters);
			jdbcTemplate.update(deleteFromBank, namedParameters);
			jdbcTemplate.update(deleteAttach, namedParameters);
		} catch (DataAccessException ex) {
			log.error("Не удалось удалить документ", ex);
			throw new StorageException("Не удалось удалить документ");
		}
	}

	/**
	 * Изменить наименование файла в БД
	 * @param attachmentId
	 * @param newName
	 */
	public void editAttachmentName(Long attachmentId, String newName) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("attachmentId", attachmentId)
				.addValue("newName", newName);

		String sql ="UPDATE attachment SET attachment_name = :newName WHERE id = :attachmentId";
		try {
			jdbcTemplate.update(sql, namedParameters);
		} catch (DataAccessException ex) {
			log.error("Не удалось изменить имя документа", ex);
			throw new StorageException("Не удалось изменить имя документа");
		}
	}

	/**
	 * Изменить флаг качества скана в БД
	 * @param attachmentId
	 * @param isQuality
	 */
	public void editAttachmentQuality(Long attachmentId, boolean isQuality) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("attachmentId", attachmentId)
				.addValue("isQuality", isQuality);

		String sql ="UPDATE attachment SET quality = :isQuality WHERE id = :attachmentId";
		try {
			jdbcTemplate.update(sql, namedParameters);
		} catch (DataAccessException ex) {
			log.error("Не удалось изменить флаг качества документа", ex);
			throw new StorageException("Не удалось изменить флаг качества документа");
		}
	}

	/**
	 * Изменить флаг верификации документа в БД
	 * @param attachmentId
	 * @param isVerify
	 */
	public void editAttachmentVerify(Long attachmentId, boolean isVerify) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("attachmentId", attachmentId)
				.addValue("isVerify", isVerify);

		String sql ="UPDATE attachment SET verified = :isVerify WHERE id = :attachmentId";
		try {
			jdbcTemplate.update(sql, namedParameters);
		} catch (DataAccessException ex) {
			log.error("Не удалось изменить флаг заверения документа", ex);
			throw new StorageException("Не удалось изменить флаг заверения документа");
		}
	}

	/**
	 * Получить список вложений указанного типа из БД (исключая бинарное поле, которое запрашивается отдельно)
	 * @param clientId
	 * @param documentClass
	 * @return
	 */
	public List<Attachment> getAttachments(Long clientId, DocumentClass documentClass) throws StorageException {
		SqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("clientId", clientId);
		String sql;
		if (documentClass == DocumentClass.USER) {
			sql = "SELECT id, attachment_name, created_at, att_type, functional_type, quality, verified, (quality IS NULL) as q_null, migrated FROM attachment JOIN attachment_user au on attachment.id = au.attachments_id WHERE au.account_application_id = :clientId";
		} else if (documentClass == DocumentClass.BANK) {
			sql = "SELECT id, attachment_name, created_at, att_type, functional_type, quality, verified, (quality IS NULL) as q_null, migrated FROM attachment JOIN attachment_bank au on attachment.id = au.bank_attachments_id WHERE au.account_application_id = :clientId";
		} else {
			return Collections.emptyList();
		}
		try {
			return jdbcTemplate.query(sql, namedParameters, (it, i) -> {
				Long id = it.getLong("id");
				String name = it.getString("attachment_name");
				Timestamp timestamp = it.getTimestamp("created_at");
				Instant createdAt;
				if (timestamp != null) {
					createdAt = timestamp.toInstant();
				} else {
					createdAt = Instant.now();
				}
				String functionalType = it.getString("functional_type");
				boolean quality = it.getBoolean("quality");
				boolean verified = it.getBoolean("verified");
				boolean isQualityNull = it.getBoolean("q_null");
				Attachment attachment = createInputAttachment(id, name, createdAt, "", AttachmentFunctionalType.getByStringKey(functionalType));
				attachment.setVerified(verified);
				attachment.setQuality(isQualityNull ? null : quality);
				attachment.setMigrated(it.getBoolean("migrated"));
				return attachment;
			});
		} catch (DataAccessException ex) {
			log.error("Не удалось получить список документов", ex);
			throw new StorageException("Не удалось получить список документов");
		}
	}

	/**
	 * Сохранить вложение в БД и вернуть его же, но уже с установленным id
	 * @param clientId
	 * @param attachment
	 * @param documentClass
	 * @return
	 */
	@Transactional
	public Attachment persistenceAndGetAttachment(Long clientId, Attachment attachment, DocumentClass documentClass) throws StorageException {
		Long id;
		try {
			id = getNextSequenceValue();
		} catch (IllegalStateException ex) {
			throw new StorageException("Не удалось сохранить новый документ (не получен идентификатор)");
		}
		SqlParameterSource attachParameters = new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("name", attachment.getAttachmentName())
				.addValue("createdAt", Timestamp.from(attachment.getCreatedAt()))
				.addValue("type", attachment.getFunctionalType().name())
				.addValue("content",  new SqlLobValue(new ByteArrayInputStream(attachment.getContent()),
						attachment.getContent().length, new DefaultLobHandler()), Types.BLOB);

		SqlParameterSource linkParameters = new MapSqlParameterSource()
				.addValue("clientId", clientId)
				.addValue("attachmentId", id);
		String saveAttach = "INSERT INTO attachment (id, attachment_name, created_at, functional_type, content) VALUES (:id, :name, :createdAt, :type, :content)";
		String saveLink = "";

		if (documentClass == DocumentClass.USER) {
			saveLink = "INSERT INTO attachment_user (account_application_id, attachments_id) VALUES (:clientId, :attachmentId)";
		} else if (documentClass == DocumentClass.BANK) {
			saveLink = "INSERT INTO attachment_bank (account_application_id, bank_attachments_id) VALUES (:clientId, :attachmentId)";
		}
		try {
			jdbcTemplate.update(saveAttach, attachParameters);
			jdbcTemplate.update(saveLink, linkParameters);
		} catch (DataAccessException ex) {
			log.error("Не удалось сохранить новый документ", ex);
			throw new StorageException("Не удалось сохранить новый документ");
		}
		attachment.setId(id);
		return attachment;
	}

	private Attachment createInputAttachment(Long id, String attachmentName, Instant when, String mimeType,
											 AttachmentFunctionalType functionalType){

		when = when == null ? Instant.now() : when;
		mimeType = mimeType == null ? "" : mimeType;
		functionalType = functionalType == null ? AttachmentFunctionalType.UNKNOWN : functionalType;

		try {
			Attachment newAttachment =  new Attachment(attachmentName, when, mimeType, functionalType);
			newAttachment.setId(id);
			return newAttachment;
		} catch (StorageException ex) {
			log.error("Не удалось сконструировать документ", ex);
			return new Attachment();
		}
	}

	private Long getNextSequenceValue() throws IllegalStateException {
		Long value = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence') as num", Collections.emptyMap(), Long.class);
		if (value == null) {
			throw new IllegalStateException("Генератор последовательностей БД не вернул валидные данные");
		}
		return value;
	}

	@AllArgsConstructor
	@Getter
	private class AttachRow {
		private Long clientId;
		private Long attachId;
		private String type;
		private Boolean isMigrated;

		public boolean isMigrated() {
			return Boolean.TRUE.equals(isMigrated);
		}
	}
}
