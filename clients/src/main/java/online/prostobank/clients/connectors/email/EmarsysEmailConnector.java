package online.prostobank.clients.connectors.email;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.connectors.api.EmailConnector;
import online.prostobank.clients.domain.Email;
import online.prostobank.clients.domain.EmarsysSentStatus;
import online.prostobank.clients.domain.repository.EmarsysSentStatusRepository;
import online.prostobank.clients.domain.statuses.ApplicationEmarsysStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import static online.prostobank.clients.connectors.email.EmarsysEvents.*;
import static online.prostobank.clients.domain.type.ParamsKeys.*;
import static online.prostobank.clients.utils.Utils.YYYY_MM_DD_T_HH_MM_SS_Z;
import static online.prostobank.clients.utils.Utils.toStringOrNull;

/**
 * Коннектор для рассылки уведомлений (почты) через движок EMARSYS
 *
 * @author yv
 */
@Slf4j
public class EmarsysEmailConnector implements EmailConnector {

	@Autowired
	private Environment env;

	@Autowired
	private EmarsysSentStatusRepository sentStatusRepository;


	@PostConstruct
	public void init() {
		EmarsysEmailProcessor.setEnv(env);
	}

	private String apiUsername;
	private String apiSecretKey;
	private String emarsysUri;
	private RestTemplate http;

	private static final EmarsysEmailProcessor PROCESSOR = new EmarsysEmailProcessor();

	/**
	 * Коннектор к емарсис. Не проверяет возможность соединения. Все аргументы обязательны
	 *
	 * @param http
	 * @param apiUsername
	 * @param apiSecretKey
	 * @param emarsysUri
	 */
	public EmarsysEmailConnector(RestTemplate http, String apiUsername, String apiSecretKey, String emarsysUri) {
		Assert.notNull(http, "`http` can't be null");
		Assert.notNull(apiUsername, "`apiUsername` can't be null");
		Assert.notNull(apiSecretKey, "`apiSecretKey` can't be null");
		Assert.notNull(emarsysUri, "`emarsysUri` can't be null");

		this.apiUsername = apiUsername;
		this.apiSecretKey = apiSecretKey;
		this.emarsysUri = emarsysUri;
		this.http = new RestTemplate();
	}

	/**
	 * Выполняет фактическую отправку письма
	 *
	 * @param p
	 */
	@Override
	public void send(@Nonnull Email p) {
		try {
			String digest = getSignature(); // Рассчитать аутентификационную подпись
			RequestEntity<String> re = RequestEntity.put(new URI(emarsysUri + "/v2/contact/?create_if_not_exists=1"))
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.header("X-WSSE", digest)
					.body(PROCESSOR.prepare(p));
			ResponseEntity<String> resp = http.exchange(re, String.class); // Отправить запрос на emarsys
			log.info("Received message from Emarsys on mail sent. Code: {}, Message: '{}'", resp.getStatusCodeValue(), resp.toString());
			log.info(resp.getBody());

			Optional<EmarsysSentStatus> byAccountApplication = sentStatusRepository.findByAccountApplication(p.applicationId);
			EmarsysSentStatus emarsysSentStatus;
			if(byAccountApplication.isPresent()){
				emarsysSentStatus= byAccountApplication.get();
			}
			else {
				emarsysSentStatus = new EmarsysSentStatus();
				emarsysSentStatus.setAccountApplicationId(p.applicationId);
				}
			emarsysSentStatus.setEmarsysStatus(ApplicationEmarsysStatus.CREATED);
			sentStatusRepository.save(emarsysSentStatus);
		} catch (URISyntaxException | RuntimeException ex) {
			log.error("Error occurred while sending e-mail to Emarsys: {}", ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}

	private String getEventId(Email email) {
		String eventId = (String) email.obj.get(EVENT_ID.getKey());
		if (eventId != null) return eventId;

		switch (email.id) {
			case ACCOUNT_RESERVED: {
				eventId = ACCOUNT_RESERVED.getEmarsysEventNumber();
				break;
			}

			case ACCOUNT_RESERVED_FOR_TRANSPORTATION: {
				eventId = ACCOUNT_RESERVED_FOR_TRANSPORTATION.getEmarsysEventNumber();
				break;
			}

			case FULFILLED_MANUAL_NOTIFY: {
				eventId = FULFILLED_MANUAL_NOTIFY.getEmarsysEventNumber();
				break;
			}

			case FULFILLED_AUTO_NOTIFY:
				eventId = FULFILLED_AUTO_NOTIFY.getEmarsysEventNumber();
				break;

			case APPOINTMENT_MADE_MANUAL_NOTIFY: {
				eventId = APPOINTMENT_MADE_MANUAL_NOTIFY.getEmarsysEventNumber();
				break;
			}

			case APPOINTMENT_MADE_AUTO_NOTIFY:
				eventId = APPOINTMENT_MADE_AUTO_NOTIFY.getEmarsysEventNumber();
				break;

			case OKVEDS:
				eventId = DENIED_OKVED.getEmarsysEventNumber();
				break;
			case ARREST_DECLINE:
				eventId = DENIED_ARREST.getEmarsysEventNumber();
				break;
			case P550:
			case KYC:
			case SCORING:
			case ERR_SECURITY_DECLINE:
			case BANK_REFUSED:
				eventId = DENIED_OTHER_CONDITION.getEmarsysEventNumber();
				break;
			case POS_BACK_REFUSED:
				eventId = DENIED_BACK.getEmarsysEventNumber();
				break;

			case ATTACHMENT_DOCUMENTS_NEW:
			case ATTACHMENT_DOCUMENTS_NEW_T:
				eventId = ATTACHMENT_DOCUMENTS_NEW.getEmarsysEventNumber();
				break;

			default: {
				eventId = "";
				break;
			}
		}
		return eventId;
	}

	public void sendTrigger(Email email) {
		Assert.notNull(email, "email can't be null");

		final String eventId = getEventId(email);

		if (StringUtils.isNotBlank(eventId)) {
			try {
				String digest = getSignature(); // Рассчитать аутентификационную подпись
				RequestEntity<String> re = RequestEntity.post(new URI(emarsysUri + "/v2/event/" + eventId + "/trigger"))
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.header("X-WSSE", digest)
						.body(PROCESSOR.prepareTrigger(email));
				ResponseEntity<String> resp = http.exchange(re, String.class); // Отправить запрос на emarsys
				log.info("Received message from Emarsys on trigger sent. Code: {}, Message: '{}'", resp.getStatusCodeValue(), resp.getBody());
			} catch (URISyntaxException e) {
				log.error("Error occurred while sending trigger to Emarsys: {}", e.getLocalizedMessage());
				throw new RuntimeException(e);
			}
		} else {
			log.warn("Попытка отправить триггерное событие для неизвестного email id " + email.id);
		}
	}

	private String getSignature() {
		String timestamp = getUTCTimestamp();
		String nonce = getNonce();
		String digest = getPasswordDigest(nonce, timestamp);

		return String.format(
				"UsernameToken Username=\"%s\", PasswordDigest=\"%s\", Nonce=\"%s\", Created=\"%s\"",
				apiUsername,
				digest,
				nonce,
				timestamp);
	}

	private String getUTCTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_Z);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		return sdf.format(new Date());
	}

	private String getNonce() {
		byte[] nonceBytes = new byte[16];
		new java.util.Random().nextBytes(nonceBytes);

		return bytesToHex(nonceBytes);
	}

	private String getPasswordDigest(String nonce, String timestamp) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.reset();
			String hashedString = String.format("%s%s%s", nonce, timestamp, apiSecretKey);
			messageDigest.update(hashedString.getBytes(StandardCharsets.UTF_8));
			String sha1Sum = bytesToHex(messageDigest.digest());

			return DatatypeConverter.printBase64Binary(sha1Sum.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	final private static char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

	private String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static final class EmarsysEmailProcessor {

		private static Environment env;

		public static void setEnv(Environment env) {
			EmarsysEmailProcessor.env = env;
		}

		private static final ObjectMapper MAPPER = new ObjectMapper();

		/**
		 * Подготовка нагрузки запроса, отправляемого в emarsys
		 */
		public String prepare(Email p) {
			Assert.notNull(p, "`p` must not be null");
			EmarsysContactDto.Contact c = createContact(p.obj);
			Email.EmailId id = p.id;
			if (id != null) c.status = id.getEmarsysStatusName();
			EmarsysContactDto dto = new EmarsysContactDto();
			dto.contacts.add(c);
			try {
				return MAPPER.writeValueAsString(dto);
			} catch (JsonProcessingException ex) {
				throw new RuntimeException(ex);
			}
		}

		public String prepareTrigger(Email email) {
			Assert.notNull(email, "email must not be null");
			EmarsysTransactionalEventDto.Global global = createTriggerContact(email.obj);
			Email.EmailId id = email.id;
			if (id != null) global.status = id.getEmarsysStatusName();
			EmarsysTransactionalEventDto dto = new EmarsysTransactionalEventDto();
			if (CollectionUtils.isEmpty(dto.contacts)) {
				dto.contacts = Collections.singletonList(new EmarsysTransactionalEventDto.Contact());
			}
			dto.contacts.get(0).external_id = global.crmId; // ключевое поле для емарсиса, 5083 - id заявки а бд. Раньше был 37, номер телефона
			dto.contacts.get(0).data.global = global; // предполагается наличие одного контакта дял отправки. наверно, так плохо делать
			try {
				return MAPPER.writeValueAsString(dto);
			} catch (JsonProcessingException ex) {
				throw new RuntimeException(ex);
			}
		}

		private static EmarsysTransactionalEventDto.Global createTriggerContact(HashMap<String, Object> paramz) {
			EmarsysTransactionalEventDto.Global result = new EmarsysTransactionalEventDto.Global();
			result.emailField = toStringOrNull(paramz.getOrDefault(EMAIL.getKey(), null));
			result.phoneField = toStringOrNull(paramz.getOrDefault(PHONE.getKey(), null));
			result.crmId = toStringOrNull(paramz.getOrDefault(CRM_ID.getKey(), null));
			result.crmId = env.acceptsProfiles("prod") ? result.crmId = "P" + result.crmId : "S" + result.crmId;
			result.city = toStringOrNull(paramz.getOrDefault(CITY.getKey(), null));
			result.firstName = toStringOrNull(paramz.getOrDefault(FIRST_NAME.getKey(), null));
			result.accNumber = toStringOrNull(paramz.getOrDefault(ACCOUNT_NUMBER.getKey(), null));
			result.secondName = toStringOrNull(paramz.getOrDefault(SECOND_NAME.getKey(), null));
			result.appointmentDate = toStringOrNull(paramz.getOrDefault(APPOINTMENT_DATE.getKey(), null));
			result.appointmentTime = toStringOrNull(paramz.getOrDefault(APPOINTMENT_TIME.getKey(), null));
			result.pcHref = toStringOrNull(paramz.getOrDefault(ACCOUNT_LINK.getKey(), null));
			result.clientDeniedComment = toStringOrNull(paramz.getOrDefault(CLIENT_DENIED_COMMENT.getKey(), null));
			result.companyName = toStringOrNull(paramz.getOrDefault(COMPANY_NAME.getKey(), null));
			result.originSource = toStringOrNull(paramz.getOrDefault(ORIGIN_SOURCE.getKey(), null));
            result.kodOrgnomer = toStringOrNull(paramz.getOrDefault(KOD_ORG_NUMBER.getKey(), null));
            result.kppOgrnip = toStringOrNull(paramz.getOrDefault(KPP_OGRNIP.getKey(), null));
            result.message = toStringOrNull(paramz.getOrDefault(MESSAGE.getKey(), null));

            return result;
		}

		/**
		 * Заполнение всех данных для шаблона
		 */
		private static EmarsysContactDto.Contact createContact(Map<String, Object> paramz) {
			EmarsysContactDto.Contact result = new EmarsysContactDto.Contact();
			result.emailField = toStringOrNull(paramz.getOrDefault(EMAIL.getKey(), null));
			result.phoneField = toStringOrNull(paramz.getOrDefault(PHONE.getKey(), null));
			result.crmId = toStringOrNull(paramz.getOrDefault(CRM_ID.getKey(), null));
			result.crmId = env.acceptsProfiles("prod") ? result.crmId = "P" + result.crmId : "S" + result.crmId;
			result.city = toStringOrNull(paramz.getOrDefault(CITY.getKey(), null));
			result.firstName = toStringOrNull(paramz.getOrDefault(FIRST_NAME.getKey(), null));
			result.accNumber = toStringOrNull(paramz.getOrDefault(ACCOUNT_NUMBER.getKey(), null));
			result.secondName = toStringOrNull(paramz.getOrDefault(SECOND_NAME.getKey(), null));
			result.appointmentDate = toStringOrNull(paramz.getOrDefault(APPOINTMENT_DATE.getKey(), null));
			result.appointmentTime = toStringOrNull(paramz.getOrDefault(APPOINTMENT_TIME.getKey(), null));
			result.pcHref = toStringOrNull(paramz.getOrDefault(ACCOUNT_LINK.getKey(), null));
			result.clientDeniedComment = toStringOrNull(paramz.getOrDefault(CLIENT_DENIED_COMMENT.getKey(), null));
			result.companyName = toStringOrNull(paramz.getOrDefault(COMPANY_NAME.getKey(), null));
			result.originSource = toStringOrNull(paramz.getOrDefault(ORIGIN_SOURCE.getKey(), null));
			result.acceptance = toStringOrNull(paramz.getOrDefault(ACCEPTANCE.getKey(), "1"));
			result.inn = toStringOrNull(paramz.getOrDefault(INN.getKey(), null));
			result.kodOrgnomer = toStringOrNull(paramz.getOrDefault(KOD_ORG_NUMBER.getKey(), null));
			result.kppOgrnip = toStringOrNull(paramz.getOrDefault(KPP_OGRNIP.getKey(), null));
			result.message = toStringOrNull(paramz.getOrDefault(MESSAGE.getKey(), null));
			return result;
		}

		/**
		 * Триггерит отправку письма с указанным наполнением.
		 */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static final class EmarsysTransactionalEventDto {

			public String key_id = "5083";
			public List<Contact> contacts = new ArrayList<Contact>();

			public static final class Contact {

				public String external_id;
				public Data data = new Data();
			}

			public static final class Data {

				public Global global = new Global();
			}

			public static final class Global {
				@JsonProperty(value = "mobil")
				public String phoneField;
				@JsonProperty(value = "email")
				public String emailField;
				@JsonProperty(value = "first_name")
				public String firstName;
				@JsonProperty(value = "otchestvo")
				public String secondName;
				@JsonProperty(value = "city")
				public String city;
				@JsonProperty(value = "status_zayavki")
				public String status;
				@JsonProperty(value = "raschetnyj_schet ")
				public String accNumber;
				@JsonProperty(value = "optin")
				public String acceptance = "1"; // always send accept to receive emails APIKUB-328
				@JsonProperty(value = "data_vstrechi_tm")
				public String appointmentDate;
				@JsonProperty(value = "vremya_vstrechi")
				public String appointmentTime;
				@JsonProperty(value = "ssylka_lk_zagruzka_dok")
				public String pcHref;
				@JsonProperty(value = "komment_otkaza_klienta")
				public String clientDeniedComment;
				@JsonProperty(value = "company_name")
				public String companyName;
				@JsonProperty(value = "origin_source")
				public String originSource;
				@JsonProperty(value = "vnut_id_crm")
				public String crmId;
				@JsonProperty(value = "kod_orgnomer")
				public String kodOrgnomer;
				@JsonProperty(value = "kpp_ogrnip")
				public String kppOgrnip;
				@JsonProperty(value = "kommentarij")
				public String message;
			}
		}

		/**
		 * Триггерит отправку письма с указанным наполнением
		 */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static final class EmarsysContactDto {

			public String key_id = "5083";
			public List<Contact> contacts = new ArrayList<Contact>();

			@JsonInclude(JsonInclude.Include.NON_NULL)
			public static final class Contact {
				@JsonProperty(value = "5083")
				public String crmId;

				@JsonProperty(value = "37")
				public String phoneField;

				@JsonProperty(value = "3")
				public String emailField;

				@JsonProperty(value = "1")
				public String firstName;

				@JsonProperty(value = "2002")
				public String secondName;

				@JsonProperty(value = "11")
				public String city;

				@JsonProperty(value = "1904")
				public String status;

				@JsonProperty(value = "1997")
				public String accNumber;

				@JsonProperty(value = "31")
				public String acceptance = "1"; // always send accept to receive emails APIKUB-328

				@JsonProperty(value = "2204")
				public String appointmentDate;

				@JsonProperty(value = "2000")
				public String appointmentTime;

				@JsonProperty(value = "2008")
				public String pcHref;

				@JsonProperty(value = "1905")
				public String clientDeniedComment;

				@JsonProperty(value = "18")
				public String companyName;

				@JsonProperty(value = "2042")
				public String originSource;

				@JsonProperty(value = "2421")
				public String inn;
				@JsonProperty(value = "12505")
				public String kodOrgnomer;
				@JsonProperty(value = "12504")
				public String kppOgrnip;
				@JsonProperty(value = "15065")
				public String message;

			}
		}
	}
}
