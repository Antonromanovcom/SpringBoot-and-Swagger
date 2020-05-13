package online.prostobank.clients.domain.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.statistics.ReportColumn;
import online.prostobank.clients.domain.statistics.ReportColumnType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
public class AllAttributes extends AnalysisAttributes {
	@ReportColumn(title = "телефон")
	public String phone = "";
	@ReportColumn(title = "город")
	public String city = "";
	@ReportColumn(title = "email")
	public String email = "";
	@ReportColumn(title = "ОГРН")
	public String ogrn = "";
	@ReportColumn(title = "адрес проживания")
	public String address = "";
	@ReportColumn(title = "руководитель")
	public String head = "";
	@ReportColumn(title = "ИНН руководителя")
	public String headInn = "";

	@ReportColumn(title = "Дата встречи", type = ReportColumnType.DATE)
	public Instant appointmentDate;
	@ReportColumn(title = "Время встречи", type = ReportColumnType.TIME)
	public Instant appointmentTime;
	@ReportColumn(title = "последний комментарий")
	public String lastComment = "";
	@ReportColumn(title = "автор комментария")
	public String lastCommentInitiator = "";
	@ReportColumn(title = "дата комментария", type = ReportColumnType.DATE_TIME)
	public Instant lastCommentDate;

	@ReportColumn(title = "Скоринг")
	public String scoringErrorText = "";
	@ReportColumn(title = "СМЭВ")
	public String smev = "";
	@ReportColumn(title = "недействительный паспорт")
	public String passportCheck = "";
	@ReportColumn(title = "аресты")
	public String arrests = "";

	@ReportColumn(title = "выручка")
	public String income = "";
	@ReportColumn(title = "статус создания")
	public String createdStatus = "";

	@ReportColumn(title = "дата перевода в статус 'в работе менеджера'", type = ReportColumnType.DATE)
	public Instant lastManagerProcessingDate;
	@ReportColumn(title = "автор перевода в статус 'в работе менеджера'")
	public String lastManagerProcessingInitiator = "";

	public AllAttributes(ResultSet it) throws SQLException {
		super(it);
		setAllFields(it);
	}

	private void setAllFields(ResultSet it) throws SQLException {
		setPhone(it.getString("phone"));
		setCity(it.getString("city_name"));
		setEmail(it.getString("client_email"));
		setAddress(it.getString("resident_address"));
		setHeadInn(it.getString("head_tax_number"));
		setLastComment(it.getString("last_comment_text"));

		Optional.ofNullable(it.getTimestamp("last_comment_time"))
				.map(Timestamp::toInstant)
				.ifPresent(this::setLastCommentDate);

		setLastCommentInitiator(it.getString("last_comment_initiator"));
		setScoringErrorText(it.getString("kontur_error_text"));
		setSmev(it.getString("smev_check"));
		setPassportCheck(it.getString("passport_check"));
		setIncome(it.getString("income"));
		setCreatedStatus(it.getString("start_work_status"));

		Optional.ofNullable(it.getTimestamp("manager_processing_time"))
				.map(Timestamp::toInstant)
				.ifPresent(this::setLastManagerProcessingDate);

		setLastManagerProcessingInitiator(it.getString("manager_processing_initiator"));
		setArrests(it.getString("arrests_fns"));
	}
}
