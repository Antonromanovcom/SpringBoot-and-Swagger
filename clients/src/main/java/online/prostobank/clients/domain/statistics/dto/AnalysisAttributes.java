package online.prostobank.clients.domain.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statistics.ReportColumn;
import online.prostobank.clients.domain.statistics.ReportColumnType;
import online.prostobank.clients.utils.HumanNamesFromCompany;
import online.prostobank.clients.utils.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static online.prostobank.clients.domain.statistics.ReportUtils.getRiskyCodes;
import static online.prostobank.clients.domain.statistics.ReportUtils.getRiskyCodesString;

@Getter
@Setter
public class AnalysisAttributes {
	@ReportColumn(title = "ID клиента")
	public String clientId = "";
	@ReportColumn(title = "дата создания клиента", type = ReportColumnType.DATE_TIME)
	public Instant createdAt;
	@ReportColumn(title = "текущий статус")
	public String clientState = "";
	@ReportColumn(title = "компания")
	public String companyName = "";

	@ReportColumn(title = "фамилия")
	public String _lastName = "";
	@ReportColumn(title = "имя")
	public String _firstName = "";
	@ReportColumn(title = "отчество")
	public String _middleName = "";
	@ReportColumn(title = "ИНН")
	public String inn = "";
	//	@ReportColumn(title = "ОГРН")
	private String ogrn = "";
	//	@ReportColumn(title = "руководитель")
	private String head = "";

	@ReportColumn(title = "номер счёта")
	public String accountNumber = "";
	@ReportColumn(title = "тариф")
	public String billingPlan = "";

	@ReportColumn(title = "Основные ОКВЭД")
	public String mainOkved = "";
	@ReportColumn(title = "Рискованные основные ОКВЭД")
	public String _riskyMainOkved = "";
	@ReportColumn(title = "Рискованные дополнительные ОКВЭД")
	public String _riskySecondaryOkved = "";
	@ReportColumn(title = "Риск по ОКВЭД")
	public String _riskByOkved = "";

	@ReportColumn(title = "550-П")
	public String p550 = "";
	@ReportColumn(title = "550-П руководителя")
	public String p550head = "";
	@ReportColumn(title = "550-П учредителей")
	public String p550founder = "";
	@ReportColumn(title = "Скоринг балл")
	public String scoringValue = "";

	@ReportColumn(title = "налогообложение")
	public String taxForm = "";
	@ReportColumn(title = "источник клиента")
	public String source = "";
	@ReportColumn(title = "создатель карточки")
	public String creator = "";

	@ReportColumn(title = "utm source")
	public String utmSource = "";
	@ReportColumn(title = "utm medium")
	public String utmMedium = "";
	@ReportColumn(title = "utm term")
	public String utmTerm = "";
	@ReportColumn(title = "utm campaign")
	public String utmCampaign = "";
	@ReportColumn(title = "URL")
	public String utmUrl = "";

	private String secondaryOkved = "";
	private String blackOkved = "";
	private String riskyOkved = "";
	private Double scoringValueSource;

	public AnalysisAttributes(ResultSet it) throws SQLException {
		setAnalysisFields(it);
	}

	private void setAnalysisFields(ResultSet it) throws SQLException {
		setClientId(String.valueOf(it.getLong("id")));
		setCompanyName(it.getString("client_name"));
		setInn(it.getString("inn"));
		setOgrn(it.getString("ogrn"));
		setHead(it.getString("head"));
		setMainOkved(it.getString("primary_codes"));
		setSecondaryOkved(it.getString("secondary_codes"));
		setBlackOkved(it.getString("black_listed_codes"));
		setRiskyOkved(it.getString("risky_codes"));
		setBillingPlan(it.getString("billing_plan"));

		setP550(it.getString("p550check"));
		setP550head(it.getString("p550check_head"));
		setP550founder(it.getString("p550check_founder"));
		setScoringValueSource(it.getDouble("kontur_check"));
		setScoringValue(String.format("%.2f", getScoringValueSource()));
		setTaxForm(it.getString("tax_form"));
		setAccountNumber(it.getString("account_number"));
		setClientState(it.getString("client_state"));

		Optional.ofNullable(it.getTimestamp("date_created"))
				.map(Timestamp::toInstant)
				.ifPresent(this::setCreatedAt);

		setSource(it.getString("source"));
		setCreator(it.getString("creator"));
		setUtmSource(it.getString("utm_source"));
		setUtmMedium(it.getString("utm_medium"));
		setUtmTerm(it.getString("utm_term"));
		setUtmCampaign(it.getString("utm_campaign"));
		setUtmUrl(it.getString("url"));

		calculateFields();
	}

	private void calculateFields() {
		boolean isLegalEntity = Utils.isLegalEntity(getInn(), getOgrn());
		HumanNamesFromCompany.Names names = HumanNamesFromCompany.createNames(getCompanyName(), getHead(), isLegalEntity);
		set_firstName(names.firstName());
		set_middleName(names.middleName());
		set_lastName(names.lastName());

		String blackOkved = getBlackOkved();
		String riskyOkved = getRiskyOkved();
		set_riskyMainOkved(getRiskyCodes(getMainOkved(), blackOkved, riskyOkved));
		set_riskySecondaryOkved(getRiskyCodes(getSecondaryOkved(), blackOkved, riskyOkved));
		set_riskByOkved(getRiskyCodesString(getScoringValueSource(), blackOkved, riskyOkved));
		try {
			setClientState(ClientStates.valueOf(getClientState()).getRuName());
		} catch (IllegalArgumentException ex) {
			//do nothing
		}
	}
}
