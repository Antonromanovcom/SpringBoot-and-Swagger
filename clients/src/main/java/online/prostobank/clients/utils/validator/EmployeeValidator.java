package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client_detail.EmployeeDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class EmployeeValidator {
	private static final String TEXT_LIMIT = "^.{1,255}$";

	public static Pair<Boolean, List<String>> validate(EmployeeDTO employeeDTO) {
		boolean result = true;
		List<String> messages = new ArrayList<>();

		if (employeeDTO == null) {
			messages.add("Данные полностью отсутствуют");
			result = false;
		} else {
			if (employeeDTO.getHuman() == null) {
				messages.add("Сведения о физическом лице отсутствуют");
				result = false;
			} else {
				Pair<Boolean, List<String>> humanValidation = HumanValidator.validate(employeeDTO.getHuman());
				result = result && humanValidation.getFirst();
				messages.addAll(humanValidation.getSecond());
			}

			if (!StringUtils.isEmpty(employeeDTO.getPosition()) && !employeeDTO.getPosition().matches(TEXT_LIMIT)) {
				messages.add("Значение поля должность слишком велико");
				result = false;
			}

			if (employeeDTO.getPhones() != null) {
				Pair<Boolean, List<String>> phoneValidation = PhoneValidator.validate(employeeDTO.getPhones());
				result = result && phoneValidation.getFirst();
				messages.addAll(phoneValidation.getSecond());
			}

			if (employeeDTO.getEmails() != null) {
				Pair<Boolean, List<String>> emailValidation = EmailValidator.validate(employeeDTO.getEmails());
				result = result && emailValidation.getFirst();
				messages.addAll(emailValidation.getSecond());
			}
		}
		return Pair.of(result, messages);
	}
}
