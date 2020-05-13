package online.prostobank.clients.services.tss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.TssProperties;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.tss.*;
import online.prostobank.clients.services.attacment.AttachmentService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static online.prostobank.clients.api.TssController.API_TSS;
import static online.prostobank.clients.api.TssController.CALLBACK;
import static online.prostobank.clients.utils.HttpUtils.setBearerAuth;

@Slf4j
@RequiredArgsConstructor
@Service
public class TssServiceImpl implements TssService {
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate;
	private final TssProperties tssProperties;
	private final AttachmentService attachmentService;
	private final TssEntityRepository tssEntityRepository;
	private final AccountApplicationRepository accountApplicationRepository;

	private HttpHeaders headers;
	private String postSignUrl;
	private String callbackUrl;
	private String getSignResultUrl;
	private String postResendSmsUrl;
	private String postCheckSignUrl;

	@PostConstruct
	private void init() {
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setCacheControl("no-cache");

		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

		postSignUrl = tssProperties.getTssUrl() + POST_SIGN_URL;
		callbackUrl = tssProperties.getBaseUrl() + API_TSS + CALLBACK;
		getSignResultUrl = tssProperties.getTssUrl() + GET_SIGN_RESULT_URL;
		postResendSmsUrl = tssProperties.getTssUrl() + POST_RESEND_SMS_URL;
		postCheckSignUrl = tssProperties.getTssUrl() + POST_CHECK_SIGN_URL;
	}

	@Override
	public Optional<UUID> sign(String token, Long clientId, Long attachmentId) {
		return attachmentService.findById(clientId, attachmentId)
				.map(attachment -> {
					byte[] content = attachmentService.getBinaryContent(attachment);
					TssData tssData = TssData.builder()
							.name(attachment.getAttachmentName())
							.build();
					String phone = accountApplicationRepository
							.findById(clientId)
							.map(application -> application.getClient().getPhone())
							.orElse("");
					String signResponse = sign(token, content, tssData, phone);
					UUID requestId = UUID.fromString(signResponse);
					TssSign tssSign = new TssSign(requestId);
					tssSign.setClientId(clientId);
					tssSign.setAttachmentId(attachmentId);

					tssSign.setStatus(TssStatus.NEW);
					tssSign.setConfirmType(ConfirmType.sms);
					tssEntityRepository.save(tssSign);
					return requestId;
				});
	}

	private String sign(String token,
						byte[] message,
						TssData tssData,
						String phone) {

		TssMessage tssMessage = null;
		try {
			tssMessage = TssMessage.builder()
					.message(Base64.getEncoder().encodeToString(message))
					.displayText(objectMapper.writeValueAsString(tssData))
					.callbackUrl(callbackUrl)
					.needConfirm(true)
					.confirmType(ConfirmType.sms)
					.phone(phone)
					.build();
		} catch (JsonProcessingException e) {
			log.error(e.getLocalizedMessage(), e);
		}

		log.info("sign request: " + tssMessage);
		return restTemplate
				.exchange(postSignUrl, HttpMethod.POST, new HttpEntity<>(tssMessage, setBearerAuth(headers, token)), String.class)
				.getBody();
	}

	@Override
	public Optional<HttpStatus> callback(TssDTO dto) {
		return Optional.of(dto.getRequestId())
				.map(requestId -> tssEntityRepository
						.findById(requestId)
						.orElseGet(() -> new TssSign(requestId)))
				.map(tssSign -> {
					tssSign.setSignatureBase64(dto.getSignature());
					tssSign.setSignDate(dto.getSignDate());
					tssSign.setSignPublicId(dto.getSignPublicId());
					tssEntityRepository.save(tssSign);
					return HttpStatus.OK;
				});
	}

	@Override
	public Optional<TssResponse> signResult(String token, UUID requestId) {
		TssResponse sign = restTemplate
				.exchange(getSignResultUrl, HttpMethod.GET, new HttpEntity<>(setBearerAuth(headers, token)), TssResponse.class, requestId.toString())
				.getBody();

		return Optional.ofNullable(sign)
				.map(it -> tssEntityRepository
						.findById(requestId)
						.orElseGet(() -> new TssSign(requestId)))
				.map(tssSign -> {
					tssSign.setStatus(sign.getStatus());
					tssSign.setConfirmType(sign.getConfirmType());
					tssSign.setSignatureBase64(sign.getSignature());
					tssSign.setSignDate(sign.getSignDate());
					tssSign.setSignPublicId(sign.getSignPublicId());
					tssSign.setError(sign.getError());
					tssEntityRepository.save(tssSign);
					return sign;
				});
	}

	@Override
	public Optional<HttpStatus> resendSms(String token, UUID requestId) {
		return Optional.of(
				restTemplate
						.exchange(postResendSmsUrl, HttpMethod.POST, new HttpEntity<>(setBearerAuth(headers, token)), String.class, requestId)
						.getStatusCode()
		);
	}

	@Override
	public Optional<TssCheckResponse> signCheck(String token, UUID requestId) {
		return tssEntityRepository.findById(requestId)
				.flatMap(tssSign -> attachmentService
						.findById(tssSign.getClientId(), tssSign.getAttachmentId())
						.map(attachment -> {
							byte[] content = attachmentService.getBinaryContent(attachment);
							TssCheckMessage tssCheck = TssCheckMessage.builder()
									.message(Base64.getEncoder().encodeToString(content))
									.signature(tssSign.getSignatureBase64())
									.build();

							log.info("sign check request, requestId: {}", requestId);
							return restTemplate
									.exchange(postCheckSignUrl, HttpMethod.POST, new HttpEntity<>(tssCheck, setBearerAuth(headers, token)), TssCheckResponse.class, requestId)
									.getBody();
						}));
	}
}
