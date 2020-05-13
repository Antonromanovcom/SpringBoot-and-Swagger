package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client_detail.EmailDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailValidator {
	private static final String EMAIL_PATTERN = "^([a-zA-Z0-9_\\.\\-+])+@[a-zA-Z0-9-.]+\\.[a-zA-Z0-9-]{2,}$";

	public static Pair<Boolean, List<String>> validate(EmailDTO emailDTO) {
		boolean result = true;
		List<String> messages = new ArrayList<>();

		if (emailDTO == null) {
			messages.add("Данные полностью отсутствуют");
			result = false;
		} else {
			if (!StringUtils.isEmpty(emailDTO.getValue()) && !emailDTO.getValue().trim().matches(EMAIL_PATTERN)) {
				messages.add("Неверный формат email");
				result = false;
			}
		}
		return Pair.of(result, messages);
	}

	public static Pair<Boolean, List<String>> validate(List<EmailDTO> emailDTOs) {
		return emailDTOs.stream()
				.map(EmailValidator::validate)
				.filter(it -> !it.getFirst())
				.findFirst()
				.orElse(Pair.of(true, Collections.emptyList()));

	}
}
