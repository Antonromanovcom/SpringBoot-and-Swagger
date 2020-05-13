package online.prostobank.clients.connectors;

import club.apibank.connectors.EgrulServiceConnector;
import club.apibank.connectors.captcha.lab.CaptchaLABCaptchaSolverConnector;
import club.apibank.connectors.exceptions.EgrulServiceException;
import club.apibank.connectors.fns.model.dto.FounderDto;
import club.apibank.connectors.models.EgrulRecord;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.EgripServiceProperties;
import online.prostobank.clients.connectors.api.IEgripService;
import online.prostobank.clients.connectors.api.KonturService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static online.prostobank.clients.connectors.KonturServiceImpl.EMPTY_RESULT_BY_TAX_NUMBER;
import static online.prostobank.clients.utils.Utils.COMMA_DELIMITER_W_SPACE;

@Slf4j
public class EgripService implements IEgripService {
	private static final Duration SOLVE_INTERVAL = Duration.ofSeconds(3);
	private static final Duration RETRY_DELAY = Duration.ofSeconds(5);
	private static final String CAPTCHA_LAB_SERVICE_HOST = "http://service-captcha-lab.com";
	private static final String CAPTCHA_LAB_SERVICE_HOST_AUTH_KEY = "49y9043ls87b26ofjfejpyajvjzwpymt";
	private static final String EGRUL_SERVICE_HOST = "https://egrul.nalog.ru/";
	private static final Duration RETRY_INTERVAL = Duration.ofSeconds(1);
	private static final int RETRY_COUNT = 5;
	private static final int CAPTCHA_RETRY_COUNT = 5;
	private static final String CAPTCHA_VERSION = "2";

	@Autowired private EgripServiceProperties config;

	private EgrulServiceConnector egrulServiceConnector;

	public EgripService() {
		egrulServiceConnector = new EgrulServiceConnector(
				EGRUL_SERVICE_HOST,
				RETRY_INTERVAL,
				RETRY_COUNT,
				CAPTCHA_VERSION,
				new CaptchaLABCaptchaSolverConnector(
						SOLVE_INTERVAL,
						RETRY_DELAY,
						RETRY_COUNT,
						CAPTCHA_LAB_SERVICE_HOST,
						CAPTCHA_LAB_SERVICE_HOST_AUTH_KEY
				),
				CAPTCHA_RETRY_COUNT);
	}

	private byte[] getExcerpt(String inn) throws EgrulServiceException, InterruptedException {
		List<EgrulRecord> records;
		records = egrulServiceConnector.getEgripRecords(inn);
		if (CollectionUtils.isEmpty(records)) {
			return new byte[0];
		}
		return egrulServiceConnector.getExcerpt(records.get(0).getExcerptKey());
	}

	private HttpEntity execRequest(byte[] bytes, CloseableHttpClient httpClient) throws IOException {
		HttpPost uploadFile = new HttpPost(config.getParserUrl());
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		builder.addBinaryBody(
				"uploadedFile",
				new ByteArrayInputStream(bytes),
				ContentType.APPLICATION_OCTET_STREAM,
				"uploadFile.pdf"
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(uploadFile);
		return response.getEntity();
	}

	private String buildAddress(Map<String, Any> address) {
		StringBuilder stringBuilder = new StringBuilder();

		buildString(stringBuilder, address.get("zip_index") == null ? "" : address.get("zip_index").as(String.class));
		buildString(stringBuilder, address.get("region") == null ? "" : address.get("region").as(String.class));
		buildString(stringBuilder, address.get("area") == null ? "" : address.get("area").as(String.class));
		buildString(stringBuilder, address.get("locality") == null ? "" : address.get("locality").as(String.class));
		buildString(stringBuilder, address.get("city") == null ? "" : address.get("city").as(String.class));
		buildString(stringBuilder, address.get("street") == null ? "" : address.get("street").as(String.class));
		buildString(stringBuilder, address.get("house") == null ? "" : address.get("house").as(String.class));
		buildString(stringBuilder, address.get("building") == null ? "" : address.get("building").as(String.class));
		buildString(stringBuilder, address.get("flat") == null ? "" : address.get("flat").as(String.class));

		return StringUtils.substringBeforeLast(stringBuilder.toString(), COMMA_DELIMITER_W_SPACE);
	}

	private void buildString(StringBuilder stringBuilder, String strToAppend) {
		if (StringUtils.isNotBlank(strToAppend)) {
			stringBuilder.append(strToAppend).append(COMMA_DELIMITER_W_SPACE);
		}
	}

	private KonturService.InfoResult parseJson(String companyDTOAsJson) {
		KonturService.InfoResult infoResult = new KonturService.InfoResult();
		if (StringUtils.isBlank(companyDTOAsJson)) {
			infoResult.errorText = EMPTY_RESULT_BY_TAX_NUMBER;    //Не удалось найти зарегистрированную компанию с текущим ИНН
			return infoResult;
		}
		companyDTOAsJson = companyDTOAsJson.replace("&#34;", "\\\"");
//		companyDTOAsJson = StringEscapeUtils.escapeHtml4(companyDTOAsJson);
		Any deserialize = JsonIterator.deserialize(companyDTOAsJson);
		String type = Optional.ofNullable(deserialize.get("type").as(String.class)).orElse("");
		Set keys = deserialize.keys();

		if (type.equalsIgnoreCase("орг")) {
			infoResult.type = "LLC";
			if (keys.contains("common")) {
				Map<String, Any> common = deserialize.get("common").asMap();
				infoResult.name = common.get("full_name") == null ? "" : common.get("full_name").as(String.class);
				infoResult.shortName = common.get("short_name") == null ? "" : common.get("short_name").as(String.class);
			}

			if (keys.contains("address")) {
				Map<String, Any> address = deserialize.get("address").asMap();
				infoResult.address = buildAddress(address);
			}

			if (keys.contains("taxes")) {
				Map<String, Any> taxes = deserialize.get("taxes").asMap();
				infoResult.inn = taxes.get("inn") == null ? "" : taxes.get("inn").as(String.class);
				infoResult.kpp = taxes.get("kpp") == null ? "" : taxes.get("kpp").as(String.class);
			}

			if (keys.contains("registration_info")) {
				Map<String, Any> registrationInfo = deserialize.get("registration_info").asMap();
				infoResult.ogrn = registrationInfo.get("ogrn") == null ? "" : registrationInfo.get("ogrn").as(String.class);
				infoResult.regDate = registrationInfo.get("register_date") != null ? registrationInfo.get("register_date").as(String.class)
						: registrationInfo.get("register_date_before_2004") != null ? registrationInfo.get("register_date_before_2004").as(String.class)
						: registrationInfo.get("register_date_before_2002") != null ? registrationInfo.get("register_date_before_2002").as(String.class) : "";
			}

			if (keys.contains("confidant")) {
				Map<String, Any> confidant = deserialize.get("confidant").asMap();
				infoResult.headName = confidant.get("last_name") + " " + confidant.get("first_name") + " " + confidant.get("middle_name");
				infoResult.headTaxNumber = confidant.get("inn") == null ? "" : confidant.get("inn").as(String.class);
				infoResult.grnRecord = confidant.get("grn_record_current_person") == null ? "" : confidant.get("grn_record_current_person").as(String.class);
			}

			if (keys.contains("founders")) {
				Map<String, Any> founders = deserialize.get("founders").asMap();
				List<FounderDto> founderDtos = new ArrayList<>();
				founders.forEach((s, any) -> {
					try {
						Map<String, Any> founder = any.asMap();
						FounderDto founderDto = new FounderDto();
						founderDto.setFio((founder.get("last_name") == null ? "" : founder.get("last_name")) + " "
								+ (founder.get("first_name") == null ? "" : founder.get("first_name")) + " "
								+ (founder.get("middle_name") == null ? "" : founder.get("middle_name")));
						founderDto.setInn(founder.get("inn") == null ? "" : founder.get("inn").as(String.class));
						founderDto.setOgrn(founder.get("ogrn") == null ? "" : founder.get("ogrn").as(String.class));
						founderDto.setGrnRecord(founder.get("grn_record_current_person") == null ? "" : founder.get("grn_record_current_person").as(String.class));
						founderDtos.add(founderDto);
					} catch (ClassCastException e) {
						log.error("ClassCastException " + " at " + infoResult.inn + " for " + s + " - " + any.toString());
					}
				});
				infoResult.founders = founderDtos;
			}

			if (keys.contains("main_activity")) {
				Map<String, Any> mainActivity = deserialize.get("main_activity").asMap();
				if (mainActivity.get("code_name") != null) {
					infoResult.primaryCodes = new String[]{mainActivity.get("code_name").as(String.class).split(" ")[0]};
				} else {
					Map<String, Any> inner = mainActivity.get("1").asMap();
					infoResult.primaryCodes = new String[]{inner.get("code_name").as(String.class).split(" ")[0]};
				}
			}

			if (keys.contains("extra_activity")) {
				Map<String, Any> extraActivity = deserialize.get("extra_activity").asMap();
				List<String> secondaryCodes = new ArrayList<>();
				extraActivity.forEach((s, any) -> {
					try {
						Optional.ofNullable(any.asMap().get("code_name").as(String.class)).ifPresent(a -> secondaryCodes.add(a.split(" ")[0]));
					} catch (java.lang.ClassCastException e) {
						log.error("ClassCastException " + " at " + infoResult.inn + " for " + s + " - " + any.toString());
					}
				});
				infoResult.secondaryCodes = secondaryCodes.toArray(new String[0]);
			}

			if (keys.contains("register_taxes")) {
				Map<String, Any> registerTaxes = deserialize.get("register_taxes").asMap();
				infoResult.regPlace = registerTaxes.get("organization_name") == null ? "" : registerTaxes.get("organization_name").as(String.class);
			}

			//stop_org -> close_method, close_date, close_organization_name
//		confidant.get("position")
//		infoResult.type
		} else if (type.equalsIgnoreCase("ип")) {
			infoResult.type = "SP";

			if (keys.contains("common")) {
				Map<String, Any> common = deserialize.get("common").asMap();

				infoResult.headName = (common.get("last_name") == null ? "" : common.get("last_name").as(String.class)) + " " +
						(common.get("first_name") == null ? "" : common.get("first_name").as(String.class)) + " " +
						(common.get("middle_name") == null ? "" : common.get("middle_name").as(String.class));
				infoResult.name = "ИП " + infoResult.headName;
			}

			if (keys.contains("address")) {

			}

			if (keys.contains("taxes")) {
				Map<String, Any> taxes = deserialize.get("taxes").asMap();
				infoResult.inn = taxes.get("inn") == null ? "" : taxes.get("inn").as(String.class);
				infoResult.headTaxNumber = taxes.get("inn") == null ? "" : taxes.get("inn").as(String.class);
			}

			if (keys.contains("pe_register_info")) {
				Map<String, Any> registrationInfo = deserialize.get("pe_register_info").asMap();
				infoResult.ogrn = registrationInfo.get("ogrnip") == null ? "" : registrationInfo.get("ogrnip").as(String.class);
				infoResult.regDate = registrationInfo.get("register_date") == null ? "" : registrationInfo.get("register_date").as(String.class);
			}

			if (keys.contains("register_org_info")) {
				Map<String, Any> registerOrgInfo = deserialize.get("register_org_info").asMap();
				infoResult.regPlace = registerOrgInfo.get("organization_name") == null ? "" : registerOrgInfo.get("organization_name").as(String.class);
			}

			if (keys.contains("main_activity")) {
				Map<String, Any> mainActivity = deserialize.get("main_activity").asMap();
				infoResult.primaryCodes = new String[]{mainActivity.get("code_name").as(String.class).split(" ")[0]};
			}

			if (keys.contains("extra_activity")) {
				Map<String, Any> extraActivity = deserialize.get("extra_activity").asMap();
				List<String> secondaryCodes = new ArrayList<>();
				extraActivity.forEach((s, any) -> {
					try {
						secondaryCodes.add(any.asMap().get("code_name").as(String.class).split(" ")[0]);
					} catch (ClassCastException e) {
						log.error("ClassCastException " + " at " + infoResult.inn + " for " + s + " - " + any.toString());
					}
				});
				infoResult.secondaryCodes = secondaryCodes.toArray(new String[0]);
			}
		}
		return infoResult;
	}

	@Override
	public String getInfoRaw(String inn) throws InterruptedException, EgrulServiceException, IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpEntity httpEntity = execRequest(getExcerpt(inn), httpClient);
			if (httpEntity != null) {
				return getEntityAsString(httpEntity, null);
			}
			return null;
		}
	}

	@Override
	public KonturService.InfoResult getInfo(String inn) throws InterruptedException, EgrulServiceException, IOException {
		return parseJson(getInfoRaw(inn));
	}

	private String getEntityAsString(HttpEntity entity, HttpResponse httpResponse) throws IOException {
		InputStream content = entity.getContent();
		String res;
		if (httpResponse == null) {
			InputStream bufferedContent = new BufferedInputStream(content);
			String encoding = StandardCharsets.UTF_8.name();
			res = IOUtils.toString(bufferedContent, encoding);
		} else {
			byte[] response = IOUtils.toByteArray(content);
			httpResponse.setEntity(new InputStreamEntity(new ByteArrayInputStream(response)));
			res = new String(response);
		}
		log.info("Rsponse body from parser: " + res);
		return res;
	}
}
