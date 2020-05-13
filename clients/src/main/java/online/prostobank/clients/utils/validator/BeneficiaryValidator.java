package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client_detail.BeneficiaryDTO;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryValidator {
	public static Pair<Boolean, List<String>> validate(BeneficiaryDTO beneficiaryDTO) {
		boolean result = true;
		List<String> messages = new ArrayList<>();

		if (beneficiaryDTO == null) {
			messages.add("Данные полностью отсутствуют");
			result = false;
		} else {
			if (beneficiaryDTO.getHuman() == null) {
				messages.add("Сведения о физическом лице отсутствуют");
				result = false;
			} else {
				Pair<Boolean, List<String>> humanValidation = HumanValidator.validate(beneficiaryDTO.getHuman());
				result = result && humanValidation.getFirst();
				messages.addAll(humanValidation.getSecond());
			}

			if (beneficiaryDTO.getPhones() != null) {
				Pair<Boolean, List<String>> phoneValidation = PhoneValidator.validate(beneficiaryDTO.getPhones());
				result = result && phoneValidation.getFirst();
				messages.addAll(phoneValidation.getSecond());
			}

			if (beneficiaryDTO.getEmails() != null) {
				Pair<Boolean, List<String>> emailValidation = EmailValidator.validate(beneficiaryDTO.getEmails());
				result = result && emailValidation.getFirst();
				messages.addAll(emailValidation.getSecond());
			}
		}
		return Pair.of(result, messages);
	}
}
