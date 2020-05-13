package online.prostobank.clients.services.validation;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.client.ClientCardCreateDTO;
import online.prostobank.clients.api.dto.client.HistoryItemDTO;
import online.prostobank.clients.api.dto.client.PassportDTO;
import online.prostobank.clients.domain.repository.validation.InboundValidationRepository;
import online.prostobank.clients.utils.validator.ClientCardValidator;
import online.prostobank.clients.utils.validator.HistoryItemValidator;
import online.prostobank.clients.utils.validator.PassportValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InboundDtoValidatorImpl implements InboundDtoValidator{
	private final InboundValidationRepository validationRepository;

	/**
	 * Валидация входных данных при создании карточки клиента.
	 * @param clientDTO
	 * @return
	 */
	@Override
	public Pair<Boolean, List<String>> validate(ClientCardCreateDTO clientDTO) {
		boolean result;
		List<String> messages = new ArrayList<>();

		Pair<Boolean, List<String>> staticResult = ClientCardValidator.validate(clientDTO);
		result = staticResult.getFirst();
		messages.addAll(staticResult.getSecond());

		if (staticResult.getFirst()) {
			if (!StringUtils.isEmpty(clientDTO.getInn()) && validationRepository.isInnExists(clientDTO.getInn())) {
				result = false;
				messages.add("Невозможно создать карточку клиента т.к. уже существует карточка с указанным ИНН");
			}
			if (!StringUtils.isEmpty(clientDTO.getPhone()) && validationRepository.isPhoneExists(clientDTO.getPhone())) {
				result = false;
				messages.add("Невозможно создать карточку клиента т.к. уже существует карточка с указанным телефоном");
			}
		}

		return Pair.of(result, messages);
	}

	/**
	 * Валидация входных данных при создании лог-записи.
	 * @param itemDTO
	 * @return
	 */
	@Override
	public Pair<Boolean, List<String>> validate(HistoryItemDTO itemDTO) {
		return HistoryItemValidator.validate(itemDTO);
	}

	/**
	 * Валидация входных данных при редактировании паспорта.
	 * @param passportDTO
	 * @return
	 */
	@Override
	public Pair<Boolean, List<String>> validate(PassportDTO passportDTO) {
		return PassportValidator.validate(passportDTO);
	}
}
