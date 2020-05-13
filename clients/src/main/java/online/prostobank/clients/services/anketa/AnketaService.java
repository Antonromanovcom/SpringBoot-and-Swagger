package online.prostobank.clients.services.anketa;

import online.prostobank.clients.api.dto.anketa.*;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.domain.enums.Source;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AnketaService {
	/**
	 * Проверка e-mail на дубликат
	 *
	 * @param email
	 * @return - результат
	 */
	ResponseEntity<String> checkEmail(String email);

	/**
	 * Список городов для анкеты
	 *
	 * @param nextMethodName
	 * @return - результат
	 */
	ResponseEntity<List<CityDTO>> getCitiesDictionary(
			String... nextMethodName
	);

	/**
	 * Оставляем заявку в статусе 'Создана'
	 *
	 * @param csrf
	 * @param dto
	 * @param source
	 * @param forwardedForHeader
	 * @param nextMethod
	 * @return - результат
	 */
	ResponseEntity<ApplicationAcceptedDTO> commonBookApplication(
			String csrf,
			ContactInfoVerifyDTO dto,
			Source source,
			String forwardedForHeader,
			String... nextMethod);

	/**
	 * @param csrf
	 * @param inn
	 * @param phone
	 * @param nextMethodName
	 * @return - результат
	 */
	ResponseEntity<OrganizationDto> getOrganizationInfo(
			String csrf,
			String inn,
			String phone,
			String... nextMethodName);

	/**
	 * Информация об организации
	 *
	 * @param csrf
	 * @param dto
	 * @return - результат
	 */
	ResponseEntity<ApplicationAcceptedDTO> createApplication(
			String csrf,
			AccountApplicationDTO dto);

	/**
	 * Проверка финального создания карточки
	 *
	 * @param csrf
	 * @param dto
	 * @return - результат
	 */
	ResponseEntity<ApplicationAcceptedDTO> finalCheck(String csrf, AccountApplicationDTO dto);

	/**
	 * Сохранение промокода
	 *
	 * @param csrf
	 * @param dto
	 * @param allowedMethods
	 * @return - результат
	 */
	ResponseEntity<PromocodeInfoResponseDTO> getPromocodeInfoResponseDTO(
			String csrf,
			PromocodeInfoDTO dto,
			String... allowedMethods);
}
