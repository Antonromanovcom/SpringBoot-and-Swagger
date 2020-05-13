package online.prostobank.clients.services.validation;

import online.prostobank.clients.api.dto.client.ClientCardCreateDTO;
import online.prostobank.clients.api.dto.client.HistoryItemDTO;
import online.prostobank.clients.api.dto.client.PassportDTO;
import org.springframework.data.util.Pair;

import java.util.List;

public interface InboundDtoValidator {
	/**
	 * Валидация входных данных при создании карточки клиента.
	 * @param dto
	 * @return
	 */
	Pair<Boolean, List<String>> validate(ClientCardCreateDTO dto);

	/**
	 * Валидация входных данных при создании лог-записи.
	 * @param itemDTO
	 * @return
	 */
	Pair<Boolean, List<String>> validate(HistoryItemDTO itemDTO);

	/**
	 * Валидация входных данных при редактировании паспорта.
	 * @param passportDTO
	 * @return
	 */
	Pair<Boolean, List<String>> validate(PassportDTO passportDTO);
}
