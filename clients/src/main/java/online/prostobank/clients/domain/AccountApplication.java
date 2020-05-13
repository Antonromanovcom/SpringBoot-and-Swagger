package online.prostobank.clients.domain;

import club.apibank.connectors.fns.model.dto.FounderDto;
import club.apibank.connectors.kontur.RiskDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jsoniter.JsonIterator;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.RosstatInfoDto;
import online.prostobank.clients.config.properties.AccountApplicationProperties;
import online.prostobank.clients.connectors.ExternalConnectors;
import online.prostobank.clients.connectors.abs.AbsResponseException;
import online.prostobank.clients.connectors.api.AbsService;
import online.prostobank.clients.connectors.api.KonturService;
import online.prostobank.clients.domain.attachment.DocumentClass;
import online.prostobank.clients.domain.enums.*;
import online.prostobank.clients.domain.events.*;
import online.prostobank.clients.domain.exceptions.EmptyTaxNumberException;
import online.prostobank.clients.domain.exceptions.KonturFailException;
import online.prostobank.clients.domain.exceptions.RiskyOkvedException;
import online.prostobank.clients.domain.repository.HistoryRepository;
import online.prostobank.clients.domain.state.mark.MarkEnum;
import online.prostobank.clients.domain.statuses.KycSystemForDeclineCode;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import online.prostobank.clients.services.KycService;
import online.prostobank.clients.services.StorageException;
import online.prostobank.clients.services.attacment.AttachmentService;
import online.prostobank.clients.services.passportCheck.PassportCheckServiceImpl;
import online.prostobank.clients.utils.TaxNumberUtils;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static online.prostobank.clients.domain.OkvedConstants.SPECIAL_RISKY_OKVED;
import static online.prostobank.clients.domain.enums.CustomKYCFactors.*;
import static online.prostobank.clients.domain.statuses.Status.ERR_AUTO_DECLINE;
import static online.prostobank.clients.utils.AttachmentUtils.getUniqueName;
import static online.prostobank.clients.utils.AutowireHelper.autowire;
import static online.prostobank.clients.utils.Utils.COMMA_DELIMITER;
import static online.prostobank.clients.utils.Utils.getDateFromStringEndAfterSpace;

@JsonLogger
@Slf4j
@Entity
@Table(name = "account_application")
public class AccountApplication extends AccountApplicationEntity {
	@Autowired
	transient private ExternalConnectors externalConnectors;
	@Autowired
	transient private AttachmentService attachmentService;
	@Autowired
	transient private ApplicationEventPublisher bus;
	@Autowired
	transient private AbsService reservator;
	@Autowired
	transient private KonturService kontur;
	@Autowired
	transient private KycService kycService;
	@Autowired
	transient private AccountApplicationProperties properties;
	@Autowired
	transient private HistoryRepository historyRepository;

	protected AccountApplication() {
	}

	public AccountApplication(@Nonnull City city,
							  @Nonnull ClientValue client,
							  @Nonnull Source source) {
		super(city, client, source);
	}

	public void setStatus(StatusValue status) {
		if (bus == null) {
			autowire(this); // :(
		}
		if (this.getStatus()== null || !this.getStatus().is(status.getValue())) {
			bus.publishEvent(new StatusChangedEvent(this, this.getStatus(), status));
		}
		super.setStatus(status);
	}

	////////////// Operations ///////////////////

	/**
	 * Резервирование счета в АБС
	 */
	public FailReason makeReservation() {
		try {
			this.setAccount(reservator.makeReservation(this));
		} catch (AbsResponseException ex) {
			String text = "AБС. Ошибка резервирования счета: '" + ex.getMessage() + "'";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			log.error("Ошибка резервирования счета " + ex.getMessage());
			if (ex.isDuplicateError()) {
				return FailReason.DUPLICATE;
			}
			return FailReason.ABS_ERROR;
		}
		return null;
	}

	private void moveToError(KycSystemForDeclineCode systemCode) {
		log.info("Moving application with id {} from '{}' to '{}'", this.getId(), getClientState(), ERR_AUTO_DECLINE);

		FailReason failReason = failReasonFrom(systemCode);

		if (failReason != null) {
			log.info("tryMove: Send ChecksDeclined event. Reason is {}", failReason);
			bus.publishEvent(new ChecksDeclined(this, failReason));
		}

		setStatus(new StatusValue(ERR_AUTO_DECLINE,  systemCode));

		String text = "Заявка переведена в статус 'Автоматический отказ'";
		log.info(text);
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
	}

	private FailReason failReasonFrom(KycSystemForDeclineCode code) {
		switch (code) {
			case P550: {
				return FailReason.P550;
			}
			case KONTUR_KYC: {
				return FailReason.KYC; // APIKUB-358 ПОС_BACKLOG - Проверка единичных факторов KYC
			}
			case KONTUR_SCORING: {
				return FailReason.SCORING; // APIKUB-359 ПОС_BACKLOG - Проверка общей оценки KYC
			}
			case OKVED: {
				return FailReason.OKVEDS;
			}
			default:
				return null;
		}
	}

	public void checkKonturAndProcess() throws KonturFailException, RiskyOkvedException {
		if (!checkKontur()) {
			return;
		}
		if (getClient().hasBlackListedCodes()) { // blacklisted okveds
			log.info("critical codes check was not successful for tax number " + this.getClient().getNumber() + ". Autodecline");
			String text = "Проведена проверка в Контур.Призма. Найдены ОКВЭД с которыми мы не работаем. Подробности в документе-отчёте";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			String errorMessage = "Автоотказ. Есть коды ОКВЭД, с которыми мы не работаем.";
			autoDecline(errorMessage, KycSystemForDeclineCode.OKVED);
			bus.publishEvent(InnCheckResult.failKyc(this, errorMessage));
			throw new KonturFailException(/*"OKVED"*/"autodecline", "Вы можете открыть счет в Банке «КУБ» (АО) на стандартных условиях (https://www.creditural.ru/index.php?lang=&link=sm_business/rko/sm_tarifs_rko/) в офисе банка, г.Магнитогорск ул.Гагарина д.17. Ваш Просто|Банк");
		}


		if (!StringUtils.isEmpty(this.getClient().getKonturFeature().getFailedFeatures())) {
			log.info("Единичная проверка признака компании не пройдена для ИНН " + this.getClient().getNumber() + ". Autodecline");
			String text = "Проведена проверка в Контур.Призма. Один из важных признаков компании не выдержал проверки. Подробности в документе-отчёте";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			autoDecline("Автоотказ. Один из важных признаков компании не выдержал проверки.", KycSystemForDeclineCode.KONTUR_KYC);
			throw new KonturFailException(/*"KONTUR_KYC"*/"autodecline", "Вы можете открыть счет в Банке «КУБ» (АО) на стандартных условиях (https://www.creditural.ru/index.php?lang=&link=sm_business/rko/sm_tarifs_rko/) в офисе банка, г.Магнитогорск ул.Гагарина д.17. Ваш Просто|Банк");
		}

		if (this.getChecks().getKonturCheck().compareTo(kontur.getAllowedTill().doubleValue()) >= 0) {
			log.info("Общая проверка KYC не пройдена для ИНН " + this.getClient().getNumber() + ". Autodecline");
			String text = "Проведена проверка в Контур.Призма. Компания набрала критичную сумму по проверкам. Подробности в документе-отчёте";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			autoDecline("Автоотказ. Компания набрала критичную сумму по проверкам.", KycSystemForDeclineCode.KONTUR_SCORING);
			throw new KonturFailException(/*"KONTUR_SCORING"*/"autodecline", "Вы можете открыть счет в Банке «КУБ» (АО) на стандартных условиях (https://www.creditural.ru/index.php?lang=&link=sm_business/rko/sm_tarifs_rko/) в офисе банка, г.Магнитогорск ул.Гагарина д.17. Ваш Просто|Банк");
		} else {
			log.info("Общая проверка KYC успешно пройдена для ИНН " + this.getClient().getNumber() + ".");
			String text = "Проведена проверка в Контур.Призма. Компания успешно прошла скоринг проверку. Подробности в документе-отчёте";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			bus.publishEvent(new ScoringApprovedEvent(this));
		}

		bus.publishEvent(InnCheckResult.successKyc(this));
		String text = "Проведена проверка в Контур.Призма с положительным результатом '" + this.getChecks().getKonturCheck() + "'";
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);

		if (StringUtils.isNotBlank(getClient().getRiskyCodes())) {
			throw new RiskyOkvedException("Заявка одобрена!<br />" +
					"Для вас зарезервирован счет.<br />" +
					"Прикрепите, пожалуйста, в Личном кабинете документы, необходимые для открытия счета. Мы отправили на вашу почту письмо со всеми подробностями!");
		}
	}

	@Override
	public Set<Attachment> getAttachments() {
		try {
			return attachmentService.getUserAttachments(this.getId());
		} catch (StorageException ex) {
			log.error("Ошибка при обращении к сервису вложений getAttachments()", ex);
		}
		return Collections.emptySet();
	}

	@Override
	public Set<Attachment> getBankAttachments() {
		try {
			return attachmentService.getBankAttachments(this.getId());
		} catch (StorageException ex) {
			log.error("Ошибка при обращении к сервису вложений getBankAttachments()", ex);
		}
		return Collections.emptySet();
	}

	/**
	 * Добавить файл, если имя не уникально, то добавить индекс
	 */
	public void addAttachmentUnique(String name, byte[] content, String mimeType) throws StorageException {
		addAttachmentUnique(name, content, mimeType, AttachmentFunctionalType.UNKNOWN);
	}

	public void addAttachmentUnique(@NotNull String name, @NotNull byte[] content, @NotNull String mimeType,
									@NotNull AttachmentFunctionalType functionalType) throws StorageException {

		Assert.notNull(name, "name can't be null");
		Assert.notNull(content, "content can't be null");
		Assert.notNull(mimeType, "file type can't be null");
		Assert.notNull(functionalType, "functional type can't be null");

		String fileName = getUniqueName(name, getAttachments());
		addAttachment(this.getId(), fileName, content, mimeType, functionalType);
	}

	/**
	 * Добавиль файл в аккаунт пользователя
	 *
	 * @param functionalType - функциональный тип документа - паспорт, СНИЛС и т.п.
	 */
	private void addAttachment(Long clientId, String name, byte[] content, String mimeType, AttachmentFunctionalType functionalType) throws StorageException {
		this.setLastAttachmentDatetime(Instant.now());
		attachmentService.createAttachment(clientId, name, this.getLastAttachmentDatetime(), content, mimeType, functionalType, DocumentClass.USER);
		String text = "Добавлен пользовательский документ '" + name + "'";
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
	}

	/**
	 * Добавить банковский файл
	 */
	public void addBankAttachment(@Nonnull String name, @Nonnull byte[] content, @Nonnull String fileType) throws StorageException {
		String fileName = getUniqueName(name, getBankAttachments());

		Instant now = Instant.now();
		attachmentService.createAttachment(this.getId(), fileName, now, content, fileType, DocumentClass.BANK);
		String text = "Добавлен банковский документ '" + fileName + "'";
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
	}

	public void setComment(String newComment) {
		newComment = ("" + newComment).trim();
		if (!("" + this.getComment()).equals(newComment)) {
			String text = "" + newComment;
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text, HistoryItemType.COMMENT);
			super.setComment(newComment);
		}
	}

	public void addHistoryRecord(String newRecord) {
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), newRecord);
	}

	public void addHistoryRecord(String newRecord, HistoryItemType itemType) {
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), newRecord, itemType);
	}

	/**
	 * перезапись паспорта
	 */
	public void setPerson(PersonValue person) {
		List<String> delta = this.getPerson().diff(person);
		if (!delta.isEmpty()) {
			super.setPerson(person);
			String text = "Паспорт отредактирован: " + String.join(", ", delta);
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
		}
	}

	@Override
	public String toString() {
		return String.format("%s[id=%d]", this.getClient().getNumber(), this.getId());
	}

	public void editCity(City newCity) {
		if (newCity == null) {
			throw new IllegalArgumentException("'newCity' must not be null");
		}
		if (!this.getCity().getId().equals(newCity.getId())) {
			@NotNull String text = "Город изменен на '" + newCity.getName() + "'";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			this.setCity(newCity);
		}
	}

	/**
	 * Перегенерация кода подтверждения заявки
	 */
	public boolean decideAndSendSms() {
		boolean toGenerate = decideToGenerateSms();

		if (!toGenerate) {
			this.setConfirmationCode(null);
			return false;
		}
		this.setConfirmationCode(
				properties.getDevConfirmationCodeSwitch() == 0
						? RandomStringUtils.randomNumeric(4)
						: properties.getDevConfirmationCode()
		);

		log.info("regenerateConfirmationCode: Send generated event.");
		bus.publishEvent(new ConfirmationCodeGeneratedEvent(this));
		return true;
	}

	private boolean decideToGenerateSms() {
		// Номер уже забанен (отправлено больше разрешённого кол-ва раз)
		String phone = getClient().getPhone();
		if (bannedSms.contains(phone)) {
			log.warn("Phone {} is banned but tries to send sms. Quit.", phone);
			return false;
		}

		Pair<Integer, Instant> integerInstantPair = smsCounter.get(phone);
		// На номер ещё не отправляли смс
		if (integerInstantPair == null) {
			log.warn("{} is not in list with phones we already sent sms. List size is {}", phone, smsCounter.size());
			smsCounter.put(phone, Pair.of(1, Instant.now()));
			log.info("New client. Sending regular sms to {}", phone);
			return true;
		} else { // На номер уже отправляли смс
			final long between = ChronoUnit.SECONDS.between(integerInstantPair.getSecond(), Instant.now());

			// Было выслано смс больше положенного кол-ва. В бан.
			if (integerInstantPair.getFirst() >= properties.getMaxSmsAttempts()) {
				bannedSms.add(phone);
				log.warn("{} banned. Sent more than {}", phone, properties.getMaxSmsAttempts());
				return false;
			}

			int next = integerInstantPair.getFirst() + 1;
			smsCounter.put(phone, Pair.of(next, Instant.now()));
			log.warn("{} attempts for {}", next, phone);
			// отправка происходит раньше, чем можно
			if (between < properties.getSmsTimeGap()) {
				log.warn("for number {} gap is {}", phone, between);
				return false;
			} else {
				log.info("Sending sms to {} because between {}", phone, between);
				return true;
			}
		}
	}

	private transient volatile static Map<String, Pair<Integer, Instant>> smsCounter = new HashMap<>();
	private transient volatile static List<String> bannedSms = new ArrayList<>();

	/**
	 * Проверка в контуре
	 */
	private boolean checkKontur() {
		final KonturService.CheckResult konturCheckResult = kontur.makeChecks(this);
		return Optional.ofNullable(konturCheckResult.scoring)
				.map(this::isAllRiskChecksValid)
				.orElseGet(() -> emptyKonturScoreResult(konturCheckResult.errorText));
	}

	private Boolean isAllRiskChecksValid(@Nonnull RiskDTO risk) {
		this.getChecks().setKonturCheck(risk.getRisk());

		this.getClient().setKonturFeature(new CompanyKonturFeature(risk));
		this.getClient().setCompanyKycScoring(new CompanyKycScoring(risk));

		doCustomChecks();

		this.getClient().setCodesFromKonturModel(
				String.join(
						COMMA_DELIMITER,
						risk.getBlackListedCodes() == null
								? Collections.emptyList()
								: risk.getBlackListedCodes()
				),
				String.join(
						COMMA_DELIMITER,
						risk.getTransportCodes() == null
								? Collections.emptyList()
								: risk.getTransportCodes()
				)
		);

		submitHistoryItems(risk);

		return true;
	}

	private void doCustomChecks() {
		if (checkRosstat()) increaseKycScoring(FINANCE_RESULT);
		if (checkHeadChangesCount()) increaseKycScoring(HEAD_CHANGES_COUNT);
		if (checkFounderChanged()) increaseKycScoring(FOUNDER_CHANGED);
		int infoEmployees = checkFns();
		if (infoEmployees == 0) {
			increaseKycScoring(EMPLOYEES_COUNT_ZERO);
		} else if (infoEmployees == 1) {
			increaseKycScoring(EMPLOYEES_COUNT_ONE);
		}
	}

	private void submitHistoryItems(@Nonnull RiskDTO risk) {
		final String msg = "Результат проверки в Контур.Фокус " + risk;
		log.info("Для ИНН " + getClient().getNumber() + " " + msg);
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), msg);

		if (CollectionUtils.isEmpty(risk.getBlackListedCodes())) {
			String text = "Проведена проверка ОКВЭД. Нежелательных ОКВЭД не выявлено";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
		}

		if (CollectionUtils.isEmpty(risk.getTransportCodes())) {
			String text = "Проведена проверка ОКВЭД. Рискованных ОКВЭД не выявлено";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
		}

		if (StringUtils.isNotBlank(getClient().getRiskyCodes())) {
			final Collection<String> intersection = CollectionUtils.intersection(Arrays.asList(getClient().getRiskyCodes().split(",")), SPECIAL_RISKY_OKVED);
			if (CollectionUtils.isNotEmpty(intersection)) {
				String text = String.format("Найден рискованный ОКВЭД, требующий предоставления дополнительных документов %s", StringUtils.join(intersection, ", "));
				historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			}
		}
	}

	private boolean emptyKonturScoreResult(@Nonnull String errorText) {
		this.getChecks().setKonturErrorText(errorText);
		log.error("Проверка в Контур вернула пустой скорринговый результат");
		String text = "Проведена проверка в Контур.Фокус, с ошибкой: '" + this.getChecks().getKonturErrorText() + "'";
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
		return false;
	}

	private boolean checkFounderChanged() {
		if (this.getClient().isSP()) return false;
		LocalDate regDate = this.getClient().getRegDate();
		if (regDate == null) return false;
		LocalDate now = LocalDate.now();
		for (Founder founder : this.getClient().getFounders()) {
			LocalDate grnRecordDate = founder.getGrnRecordDate();
			if (grnRecordDate == null) continue;
			if (regDate.equals(grnRecordDate)) continue;
			if (Period.between(grnRecordDate, now).getMonths() >= 1) continue;
			return true;
		}
		return false;
	}

	private boolean checkHeadChangesCount() {
		if (this.getClient().isSP()) return false;
		LocalDate regDate = this.getClient().getRegDate();
		LocalDate grnRecordDate = this.getClient().getGrnRecordDate();
		if (regDate == null || grnRecordDate == null) return false;
		if (regDate.equals(grnRecordDate)) return false;
		return Period.between(grnRecordDate, LocalDate.now()).getYears() < 1;
	}

	/**
	 * Если при скоринге в Контур.Призма мы получаем параметр "AccountingReportMiss" = true,
	 * то проверка пропускается и ставим 0 баллов.Если же параметр равен false, то идём ко второй проверке
	 */
	private boolean checkRosstat() {
		try {
			if (!this.getClient().getCompanyKycScoring().getFailedKycScoring().contains("AccountingReportMiss")) {
				String jsonData = externalConnectors.getRosstatInnService().getData(this.getClient().getInn());
				RosstatInfoDto[] rosstatInfo = JsonIterator.parse(jsonData).read(RosstatInfoDto[].class);
				for (RosstatInfoDto info : rosstatInfo) {
					if (info.getName().contains("Чистая прибыль (убыток) за отчетный год") && Integer.parseInt(info.getValue()) <= 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.warn("Произошла ошибка при парсинге страницы Росстата {}", e.getMessage());
		}
		return false;
	}

	private int checkFns() {
		if (this.getClient().isSP()) return -1;
		return externalConnectors.getFnsXmlParserService().getInfo(this.getClient().getInn());
	}

	private void increaseKycScoring(CustomKYCFactors financeResult) {
		CompanyKycScoring companyKycScoring = this.getClient().getCompanyKycScoring();

		this.getChecks().setKonturCheck(
				BigDecimal.valueOf(this.getChecks().getKonturCheck())
						.add(financeResult.getWeight())
		);
		companyKycScoring.setCalculatedTotalScore(
				companyKycScoring.getCalculatedTotalScore()
						.add(financeResult.getWeight())
		);

		String failedKycScoring = companyKycScoring.getFailedKycScoring();
		companyKycScoring.setFailedKycScoring(
				!failedKycScoring.isEmpty()
						? failedKycScoring + "," + financeResult.getKey()
						: financeResult.getKey()
		);
	}

	public KonturService.InfoResult clientInfoAndUpdate() {
		String innOrOgrn =
				StringUtils.isBlank(this.getClient().getInn())
						? this.getClient().getOgrn()
						: this.getClient().getInn();
		if (!TaxNumberUtils.isOgrnValid(innOrOgrn) && !TaxNumberUtils.isInnValid(innOrOgrn)) {
			log.error("Передан некорректный ИНН/ОГРН");
			return null;
		}

		KonturService.InfoResult res = kycService.getInfoResult(innOrOgrn);

		if (StringUtils.isNotBlank(res.errorText)) {
			log.error("Запрос информации из Контур.Фокус завершился С ОШИБКОЙ: '{}'", res.errorText);
			String text = String.format("Запрос информации из Контур.Фокус завершился С ОШИБКОЙ: '%s'", res.errorText);
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), text);
			return res;
		}

		updateClientInfo(res);
		return res;
	}

	private void updateClientInfo(KonturService.InfoResult res) {
		this.getClient().updateInfo(res.name, res.shortName, res.headName, res.address);
		this.getClient().setInnOgrn(res.inn, res.ogrn);
		this.getClient().setKpp(res.kpp);
		this.getClient().setHeadTaxNumber(res.headTaxNumber);
		Set<Founder> founders = new HashSet<>();
		if (res.founders != null) {
			for (FounderDto founder : res.founders) {
				founders.add(new Founder(founder.getFio(), founder.getInn(), founder.getOgrn(), getDateFromStringEndAfterSpace(founder.getGrnRecord())));
			}
		}
		this.getClient().setFounders(founders);
		this.getClient().setCodes(res.primaryCodes, res.secondaryCodes); // сохраняем коды чтобы были. теперь коды проверяет сама модель. метод getRisk
		this.getClient().setRegDate(getDateFromStringEndAfterSpace(res.regDate));
		this.getClient().setGrnRecordDate(getDateFromStringEndAfterSpace(res.grnRecord));

		addHistoryRecord(String.format("Обновлена информация о клиенте из внешнего источника.\n%s", this.getClient().toString()));
	}

	private void autoDecline(String historyRecordString, KycSystemForDeclineCode systemCode) {
		if (!this.getStatus().is(ERR_AUTO_DECLINE)) { // APIKUB-424
			log.info("Для ИНН " + this.getClient().getNumber() + " " + historyRecordString);
			moveToError(systemCode);
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), historyRecordString);
		}
	}

	/**
	 * Повторная проверка по 550П
	 */
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void recheckP550() throws EmptyTaxNumberException {
		log.info("Проверка 550-П начата");

		if (StringUtils.isBlank(getClient().getNumber())) {
			throw new EmptyTaxNumberException("Не заполнено поле ИНН либо ОГРН");
		}

		String errorMessage;
		ArrayList<String> resDesc = new ArrayList<>(Collections.singletonList(ChecksResultValue.OK)); // result string wapper
		if (callAndCheck(getClient().getNumber(), "Проверка компании", resDesc)) {
			this.getChecks().setP550check(resDesc.get(0));
		} else {
			errorMessage = "Компания не прошла проверку 550-П. Карточка переведена в автоотказ";
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), errorMessage);
			this.getChecks().setP550check(resDesc.get(0));
			bus.publishEvent(InnCheckResult.fail550(this, errorMessage));
			this.setStatus(new StatusValue(Status.ERR_AUTO_DECLINE));
			return;
		}

		if (StringUtils.isNotBlank(getClient().getHeadTaxNumber())) {
			if (callAndCheck(getClient().getHeadTaxNumber(), "Проверка руководителя компании", resDesc)) {
				this.getChecks().setP550checkHead(resDesc.get(0));
			} else {
				errorMessage = "Руководитель компании не прошёл проверку 550-П. Карточка переведена в автоотказ";
				historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), errorMessage);
				this.getChecks().setP550checkHead(resDesc.get(0));
				bus.publishEvent(InnCheckResult.fail550(this, errorMessage));
				this.setStatus(new StatusValue(Status.ERR_AUTO_DECLINE));
				return;
			}
		}

		if (!CollectionUtils.isEmpty(getClient().getFounders())) {
			String founderNumber;
			for (Founder founder : getClient().getFounders()) {
				founderNumber = StringUtils.isBlank(founder.getInn()) ? founder.getOgrn() : founder.getInn();
				if (callAndCheck(founderNumber, "Проверка учредителя " + founder.getFio(), resDesc)) {
					this.getChecks().setP550checkFounder(resDesc.get(0));
				} else {
					errorMessage = "Учредитель " + founder.getFio() + " не прошёл проверку 550-П. Карточка переведена в автоотказ";
					historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), errorMessage);
					this.getChecks().setP550checkFounder(resDesc.get(0));
					bus.publishEvent(InnCheckResult.fail550(this, errorMessage));
					this.setStatus(new StatusValue(Status.ERR_AUTO_DECLINE));
					return;
				}
			}
		}
		bus.publishEvent(InnCheckResult.success550(this));
	}

	private boolean callAndCheck(@Nonnull String number,
								 @Nonnull String specificMessage,
								 @Nonnull List<String> resDesc) {
		log.info(specificMessage);
		String msg;
		if (StringUtils.isBlank(number)) {
			msg = specificMessage + " в АБС КУБ (550-П) не состоялась. Так как отсутствует ИНН.";
			log.info(msg);
			historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), msg);
			return true;
		}
		AbsService.CheckResult res = externalConnectors.getAbsService().doCheck(number);
		if (res.getErrorReasons() != null && res.getErrorReasons().length > 0) {
			resDesc.set(0, res.getErrorReasons()[0]);
		}
		msg = specificMessage + " в АБС КУБ (550-П) проведена с результатом '" + resDesc.get(0) + "'";
		log.info(msg);
		historyRepository.insertChangeHistory(getId(), SecurityContextHelper.getCurrentUsername(), msg);

		if (!ChecksResultValue.OK.equals(resDesc.get(0)) && res.getIsSuspicious() != null && res.getIsSuspicious()) {
			autoDecline("Автоотказ. АБС КУБ (550-П) вернул результат '" + resDesc.get(0) + "'.", KycSystemForDeclineCode.P550);
		}

		return ChecksResultValue.OK.equals(resDesc.get(0)) && res.getIsSuspicious() != null && !res.getIsSuspicious();
	}

	@JsonIgnore
	public String getRiskyCodesString() {
		if (getClient().hasBlackListedCodes()) {
			return "Есть запрещённые";
		} else if (!getClient().hasRiskyCodes()) {
			return (getClient().isOkvedCodesEmpty() && getChecks().getKonturCheck() == null) ? "Не проверялся" : "Отсутствует";
		} else {
			List<String> intersection = new ArrayList<>();
			if (StringUtils.isNotBlank(getClient().getRiskyCodes())) {
				for (String code : getClient().getRiskyCodes().split(",")) {
					for (String riskOkved : SPECIAL_RISKY_OKVED) {
						if (code.startsWith(riskOkved)) {
							intersection.add(code);
						}
					}
				}
			}
			return CollectionUtils.isEmpty(intersection) ? "Есть рискованные" : StringUtils.join(intersection, ", ");
		}
	}

	//////////////////////////
	// infrastructure
	@PrePersist
	protected void onPrePersist() {
		this.setLoginURL(RandomStringUtils.randomAlphabetic(10));
	}

	@PostPersist
	protected void onPostPersist() {
		log.info("Application with id {} for phone {} persisted", getId(), getClient().getPhone());

		if (getStatus().is(Status.CONTACT_INFO_UNCONFIRMED) && (this.getSource()== Source.API_ANKETA)) {
			log.info("onPostPersist: Send generated event");
			bus.publishEvent(new ConfirmationCodeGeneratedEvent(this));
		}

		if (getStatus().is(Status.CONTACT_INFO_UNCONFIRMED) && (this.getSource()== Source.API_TM || this.getSource()== Source.API_TM_HOME || this.getSource()== Source.API_TM_HUNTER)) {
			log.info("onPostPersist: Cold application from {} created", this.getSource());
			log.info("Холодная заявка {} создана", getId());
		}
	}

	public void addStartWork(String manager) {
		this.getStartWorkSet().add(new StartWork(this, manager, this.getClientState().getRuName()));
	}

	@JsonIgnore
	public Map<Object, Object> getContext(){
		ClientValue clientValue = this.getClient();
		ChecksResultValue checks = this.getChecks();

		String inn = clientValue.getInn();
		boolean notBlankInn = StringUtils.isNotBlank(inn);

		boolean hasBlackListedCodes = clientValue.hasBlackListedCodes();
		boolean emptyFailedFeatures = StringUtils.isEmpty(
				Optional.of(clientValue)
						.map(ClientValueEntity::getKonturFeature)
						.map(CompanyKonturFeature::getFailedFeatures)
						.orElse("")
		);
		boolean allowedScore = Optional.of(checks)
				.map(ChecksResultValue::getKonturCheck)
				.map(check -> check.compareTo(kontur.getAllowedTill().doubleValue()) < 0)
				.orElse(false);
		boolean kyc = !hasBlackListedCodes && emptyFailedFeatures && allowedScore;

		String p550check = checks.getP550check();
		boolean p550 = StringUtils.isNotBlank(p550check) && p550check.equalsIgnoreCase(ChecksResultValue.OK);

		String arrestsFns = checks.getArrestsFns();
		boolean arrests = StringUtils.isNotBlank(arrestsFns) && arrestsFns.equalsIgnoreCase(ChecksResultValue.OK);

		boolean passportCheck = Optional.of(checks)
				.map(ChecksResultValue::getPassportCheck)
				.map(check -> check.equalsIgnoreCase(PassportCheckServiceImpl.CheckResult.OK.toString()))
				.orElse(false);

		Map<Object ,Object> contextClient = new HashMap<>();
		contextClient.put(MarkEnum.INN_MARK, notBlankInn);
		contextClient.put(MarkEnum.KYC_MARK, kyc);
		contextClient.put(MarkEnum.P550_MARK, p550);
		contextClient.put(MarkEnum.ARRESTS_MARK, arrests);
		contextClient.put(MarkEnum.PASSPORT_MARK, passportCheck);
		contextClient.put(MarkEnum.ACCOUNT_APPLICATION_ENTITY, this);
		contextClient.put(MarkEnum.SMS_CODE_MARK, !StringUtils.isBlank(this.getConfirmationCode()));

		return contextClient;
	}

	@JsonIgnore
	public Double getAllowedScoring() {
		return kontur.getAllowedTill().doubleValue();
	}

	public boolean isErrAutoDeclineOld() {
		return getStatus().getValue().equals(Status.ERR_AUTO_DECLINE);
	}
}
