package online.prostobank.clients.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.exceptions.EmailDuplicateException;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientValueService {
	private final AccountApplicationRepository accountApplicationRepository;

	/**
	 * Правки email
	 */
	boolean isPossibleToChangeEmail(String oldEmail, String newEmail, boolean needToValidateEmptyEmail, boolean needToValidateEmailDuplicates)
			throws EmailDuplicateException, IllegalArgumentException {
		newEmail = StringUtils.trim(newEmail);
		if (needToValidateEmptyEmail && StringUtils.isBlank(newEmail)) {
			throw new IllegalArgumentException("Email не может быть пустым");
		}

		boolean isEmailChanged = !((StringUtils.isEmpty(oldEmail) && StringUtils.isEmpty(newEmail)) || (oldEmail != null && oldEmail.equals(newEmail)));
		if (isEmailChanged && needToValidateEmailDuplicates && accountApplicationRepository.countAllByClientEmailAndActiveIsTrue(newEmail) > 0) {
			String message = String.format("Не удалось присвоить email \"%s\" текущей заявке, так как, такой email уже присвоен другой заявке", newEmail);
			log.warn(message);
			throw new EmailDuplicateException(message);
		}

		return !newEmail.equals(oldEmail);
	}
}
