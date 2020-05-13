package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client.ClientCardCreateDTO;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Валидация данных новой карточки клиента при создании её из интерфйеса CRM.
 */
public class ClientCardValidator {
	private static final String EMAIL_PATTERN = "^([a-zA-Z0-9_\\.\\-+])+@[a-zA-Z0-9-.]+\\.[a-zA-Z0-9-]{2,}$";
	private static final String TEXT_LIMIT = "^.{1,1000}$";
	private static final String PHONE_PATTERN = "[0-9]{10,15}";

	public static Pair<Boolean, List<String>> validate(ClientCardCreateDTO dto) {
		boolean result = true;
		List<String> messages = new ArrayList<>();

		if (dto == null) {
			messages.add("Данные полностью отсутствуют");
			result = false;
		} else {
			if (!StringUtils.isEmpty(dto.getInn()) && !TaxNumberUtils.isInnValid(dto.getInn())) {
				messages.add("Неверное значение ИНН");
				result = false;
			}
			if (!StringUtils.isEmpty(dto.getEmail()) && !dto.getEmail().trim().matches(EMAIL_PATTERN)) {
				messages.add("Неверный формат email");
				result = false;
			}
			if (StringUtils.isEmpty(dto.getPhone()) || !dto.getPhone().trim().matches(PHONE_PATTERN)) {
				messages.add("Поле телефон является обязательным и должно содержать не менее 10 цифр");
				result = false;
			}
			if (!StringUtils.isEmpty(dto.getComment()) && !dto.getComment().trim().matches(TEXT_LIMIT)) {
				messages.add("Сообщение в поле комментарий слишком велико");
				result = false;
			}
			if (dto.getCity() != null && dto.getCity() <= 0) {
				messages.add("Идентификатор города должен быть положительным");
				result = false;
			}
		}
		return Pair.of(result, messages);
	}
}