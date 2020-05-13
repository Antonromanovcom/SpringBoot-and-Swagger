package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client_detail.PhoneDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhoneValidator {
	private static final String PHONE_PATTERN = "[0-9]{10,15}";
	public static Pair<Boolean, List<String>> validate(PhoneDTO phoneDTO) {
		boolean result = true;
		List<String> messages = new ArrayList<>();

		if (phoneDTO == null) {
			messages.add("Данные полностью отсутствуют");
			result = false;
		} else {
			if (StringUtils.isEmpty(phoneDTO.getValue()) || !phoneDTO.getValue().trim().matches(PHONE_PATTERN)) {
				messages.add("Поле телефон является обязательным и должно содержать не менее 10 цифр");
				result = false;
			}
		}
		return Pair.of(result, messages);
	}

	public static Pair<Boolean, List<String>> validate(List<PhoneDTO> phoneDTOs) {
		return phoneDTOs.stream()
				.map(PhoneValidator::validate)
				.filter(it -> !it.getFirst())
				.findFirst()
				.orElse(Pair.of(true, Collections.emptyList()));
	}
}
