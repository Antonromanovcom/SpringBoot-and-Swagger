package online.prostobank.clients.services.tss;

import online.prostobank.clients.domain.tss.TssCheckResponse;
import online.prostobank.clients.domain.tss.TssDTO;
import online.prostobank.clients.domain.tss.TssResponse;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

public interface TssService {
	String POST_SIGN_URL = "api/tss/v1/sign-request";
	String GET_SIGN_RESULT_URL = "api/tss/v1/sign-request/{requestId}";
	String POST_CHECK_SIGN_URL = "api/tss/v1/check-signature";
	String POST_RESEND_SMS_URL = "api/tss/v1/resend-sms/{requestId}";

	/**
	 * Подписать сообщение
	 *
	 * @param token        - token пользователя
	 * @param clientId     - id пользователя
	 * @param attachmentId - id документа
	 * @return - результат
	 */
	Optional<UUID> sign(String token, Long clientId, Long attachmentId);

	/**
	 * Доставка подписи сервису потребителю
	 *
	 * @param dto - dto от tss
	 * @return - результат
	 */
	Optional<HttpStatus> callback(TssDTO dto);

	/**
	 * Получить результат подписания
	 *
	 * @param token     - token пользователя
	 * @param requestId - id запроса в tss
	 * @return - результат
	 */
	Optional<TssResponse> signResult(String token, UUID requestId);

	/**
	 * Повторная отправка СМС для подтверждения подписания сообщения
	 *
	 * @param token     - token пользователя
	 * @param requestId - id запроса в tss
	 * @return - результат
	 */
	Optional<HttpStatus> resendSms(String token, UUID requestId);

	/**
	 * Проверить подпись сообщения
	 *
	 * @param token     - token пользователя
	 * @param requestId - id запроса в tss
	 * @return - результат
	 */
	Optional<TssCheckResponse> signCheck(String token, UUID requestId);
}
