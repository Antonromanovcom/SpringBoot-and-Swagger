package online.prostobank.clients.services.forui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.AccountProperties;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.Contracts;
import online.prostobank.clients.domain.HistoryItem;
import online.prostobank.clients.domain.QuestionnaireValue;
import online.prostobank.clients.domain.enums.AccountantNoSignPermission;
import online.prostobank.clients.domain.enums.ContractTypes;
import online.prostobank.clients.domain.events.SmsReminderEvent;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.services.PdfGenerator;
import online.prostobank.clients.utils.Utils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;
import static online.prostobank.clients.domain.enums.HistoryItemType.SMS_REMINDER;
import static online.prostobank.clients.domain.statuses.Status.ERR_AUTO_DECLINE;
import static online.prostobank.clients.utils.Utils.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountApplicationViewServiceImpl implements AccountApplicationViewService {
	private static final String SMS_CODE_SUCCESS = "Пользователь подтвердил код в разговоре с менеджером, код :: ";
	public static final String PREFIX_TEXT_ASSIGNED_TO = "переназначена c пользователя ";
	public static final String TEXT_ASSIGNED_TO = PREFIX_TEXT_ASSIGNED_TO + "%s на пользователя %s";
	private final ApplicationEventPublisher bus;
	private final AccountProperties accountConfig;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final TemplateEngine tt;
	private final PdfGenerator generator;

	@Override
	public Pair<String, AccountApplication> smsRemind(AccountApplication currentApp) {
		String phone = currentApp.getClient().getPhone();
		String lastReminderTime = lastReminderTime(phone, currentApp);
		if (!isBlank(lastReminderTime)) {
			return Pair.of(lastReminderTime, currentApp);
		} else {
			bus.publishEvent(new SmsReminderEvent(currentApp, getAccountLink(accountConfig.getAccountUrl(), currentApp.getLoginURL())));
			currentApp.addHistoryRecord(String.format("Отправка смс-напоминания на номер %s", phone), SMS_REMINDER);
			Pair<Boolean, AccountApplication> application = saveAccountApplication(currentApp);
			return Pair.of(StringUtils.EMPTY, application.getSecond());
		}
	}

	@Override
	public Pair<Boolean, AccountApplication> saveAccountApplication(@Nonnull AccountApplication currentApp) {
		return repositoryWrapper.saveAccountApplication(currentApp);
	}

	@Override
	public byte[] pdfListener(AccountApplication currentApp) {
		Map<String, Object> map = new HashMap<>();
		map.put("app", currentApp);
		map.put("status", currentApp.getClientState().getRuName());
		map.put("regDate", currentApp.getDateCreated() == null ? "" : DD_MM_YYYY_RU_FORMATTER.format(currentApp.getDateCreated()));

		Set<HistoryItem> historyItems = currentApp.getItems();
		map.put("hystory", historyItems.stream()
				.map(HistoryItem::getText)
				.collect(toList()));
		if (isBlank(currentApp.getClient().getHead())) {
			map.put("head", "Неизвестно");
		} else {
			String headState = StringUtils.isNotBlank(currentApp.getChecks().getP550checkHead())
					? " (550-П " + currentApp.getChecks().getP550checkHead() + ")"
					: (currentApp.getStatus().getValue().equals(ERR_AUTO_DECLINE)
					? ""
					: " (Нужна проверка 550-П)");
			map.put("head", currentApp.getClient().getHead() + headState);
		}

		map.put("okved", currentApp.getRiskyCodesString());

		map.put("hystory", historyItems.stream()
				.filter(HistoryItem::isComment)
				.sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
				.map(it -> DD_MM_YYYY_RU_FORMATTER.format(it.getCreatedAt()) + " " + it.getText())
				.collect(toList()));

		map.put("passportSer", currentApp.getPerson().getSer());
		map.put("passportNumber", currentApp.getPerson().getNum());
		map.put("passportDate", currentApp.getPerson().getDoi() == null
				? ""
				: DD_MM_YYYY_RU_FORMATTER.format(currentApp.getPerson().getDoi()));

		map.put("clientDateOfBirth", currentApp.getPerson().getDob() == null
				? ""
				: DD_MM_YYYY_RU_FORMATTER.format(currentApp.getPerson().getDob()));

		map.put("clientPlaceOfBirth", currentApp.getPerson().getPob());
		map.put("passportCode", currentApp.getPerson().getIssuerCode());
		map.put("passportIssuer", currentApp.getPerson().getIssuer());
		map.put("snils", currentApp.getPerson().getSnils());
		map.put("tax", currentApp.getTaxForm());
		map.put("contracts", currentApp.getContractTypes() == null ? "" : currentApp.getContractTypes().getOtherText());

		if (currentApp.getContractTypes() != null) {

			Contracts contractTypes = currentApp.getContractTypes();
			List<String> contractList = Arrays.stream(ContractTypes.values())
					.collect(Collectors.toMap(
							Function.identity(),
							contractTypes1 -> contractTypes1.getGetter().apply(contractTypes))
					).entrySet().stream()
					.filter(Map.Entry::getValue)
					.map(x->x.getKey().getRuName())
					.collect(Collectors.toList());

			String result = contractList.stream()
					.map(n -> String.valueOf(n))
					.collect(Collectors.joining(". ", "", ""));

			map.put("contractTypes", result);
		}


		// --------------- Наличие главного бухгалтера ---------------------------
		QuestionnaireValue questionnaireValue = currentApp.getQuestionnaireValue();
		if ("Да".equals(questionnaireValue.getIsChiefAccountantPresent())) {
			map.put("isChiefAccountant", "присутствует");
		} else if ("Нет".equals(questionnaireValue.getIsChiefAccountantPresent())) {
			map.put("isChiefAccountant", "отсутствует");
		} else {
			map.put("isChiefAccountant", "нет данных");
		}

		// ------------------- Обязанности по ведению бухгалтерского учета БЕЗ ПРАВА ПОДПИСИ переданы ------------------------------
		map.put("accountantNoSignPermission", AccountantNoSignPermission.valueBy(questionnaireValue.getAccountNoSignPermission()));
		map.put("realCompanySize", questionnaireValue.getRealCompanySize()); // Реальный размер компании
		map.put("offSite", questionnaireValue.getOfficialSite()); // Оф.Сайт
		map.put("businessType", questionnaireValue.getBusinessType());  // Чем занимается компания
		map.put("regAddress", currentApp.getClient().getAddress());  // Адрес регистрации
		map.put("realLocationAddress", currentApp.getClient().getResidentAddress());  // Адрес фактического проживания
		map.put("konturCheck", currentApp.getChecks().getKonturCheck());
		map.put("passportCheck", currentApp.getChecks().getPassportCheck());
		map.put("P550CheckHead", currentApp.getChecks().getP550checkHead());
		map.put("P550Check", currentApp.getChecks().getP550check());
		map.put("konturErrorText", currentApp.getChecks().getKonturErrorText());
		map.put("P550CheckFounder", currentApp.getChecks().getP550checkFounder());
		map.put("smevCheck", currentApp.getChecks().getSmevCheck());
		map.put("arrests", currentApp.getChecks().getArrestsFns());
		map.put("billingPlan", currentApp.getClientTariffPlan());
		map.put("contragents", currentApp.getContragents());
		map.put("contragentsInn", currentApp.getContragentsRecip());
		map.put("supplierContragentsInn", currentApp.getContragents());

		map.put("income", currentApp.getIncome());
		map.put("comment", currentApp.getComment());

		Instant clientCallback = currentApp.getClientCallback();
		if (clientCallback != null) {
			String dateFormat = DD_MM_YYYY_RU_FORMATTER.format(clientCallback);
			String timeFormat = TIME_FORMAT_MM_F.format(clientCallback);
			map.put("callBackDate", dateFormat);
			map.put("callBackTime", timeFormat);
			map.put("callBackComment", dateFormat + " в " + timeFormat);
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			generator.createPdf(tt, "anketa.pdf", map, outputStream);
			return outputStream.toByteArray();
		} catch (Exception ex) {
			log.error("error on pdf stream", ex);
			return new byte[0];
		}
	}

	@Override
	public Pair<Boolean, AccountApplication> startWork(AccountApplication currentApp, String name) {
		Instant now = Instant.now();
		currentApp.addStartWork(name);
		log.info("Заявка " + currentApp.getId() + " взята в работу " + Utils.dateFormat(now, Utils.DATE_TIME_FORMAT));
		return saveAccountApplication(currentApp);
	}

	@Override
	public Pair<Boolean, AccountApplication> setAssignedTo(AccountApplication application, @Nonnull String username) {
		String oldManager = application.getAssignedTo();
		application.setAssignedTo(username);
		application.addHistoryRecord(String.format(TEXT_ASSIGNED_TO, oldManager, username));
		return saveAccountApplication(application);
	}

	@Override
	public Pair<Boolean, AccountApplication> saveComment(AccountApplication currentApp, String text, String username) {
		currentApp.setComment(text);
		Pair<Boolean, AccountApplication> pair = saveAccountApplication(currentApp);
		log.info("User " + username + " just left comment '" + text + "'");
		return pair;
	}

	@Override
	public Pair<Boolean, AccountApplication> resetConfirmationCode(AccountApplication currentApp) {
		currentApp.setConfirmationCode(null);
		return saveAccountApplication(currentApp);
	}

	@Override
	public Pair<Boolean, AccountApplication> decideAndSendSms(AccountApplication currentApp) {
		String phone = currentApp.getClient().getPhone();
		log.info(String.format("Менеджер отправляет смс c кодом подтверждения клиенту на номер %s", phone));
		boolean decideAndSendSms = currentApp.decideAndSendSms();
		if (decideAndSendSms) {
			currentApp.addHistoryRecord(String.format("Отправка смс на номер %s с кодом", phone));
			Pair<Boolean, AccountApplication> pair = saveAccountApplication(currentApp);
			currentApp = pair.getSecond();
		} else {
			log.info(String.format("Номер клиента %s забанен", phone));
		}
		return Pair.of(decideAndSendSms, currentApp);
	}

	@Override
	public Pair<Boolean, AccountApplication> smsConfirmationAddHistory(AccountApplication currentApp, String code) {
		final String SMS_CONFIRMATION_MESSAGE = SMS_CODE_SUCCESS + code;
		currentApp.addHistoryRecord(SMS_CONFIRMATION_MESSAGE);
		return saveAccountApplication(currentApp);
	}

	private String lastReminderTime(String phone, AccountApplication currentApp) {
		HistoryItem historyItemLast = currentApp.getItems().stream()
				.filter(historyItem -> {
					int beginIndex = historyItem.getText().length() - phone.length();
					if (beginIndex < 0) return false;
					return SMS_REMINDER == historyItem.getItemType() && historyItem.getText().substring(beginIndex).equals(phone);
				})
				.max(Comparator.comparing(HistoryItem::getCreatedAt)).orElse(null);
		if (historyItemLast == null) return null;
		Duration duration = Duration.between(Instant.now(), historyItemLast.getCreatedAt().plus(1, DAYS));
		if (duration.toMillis() <= 0) return null;
		return DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm", true);
	}
}
