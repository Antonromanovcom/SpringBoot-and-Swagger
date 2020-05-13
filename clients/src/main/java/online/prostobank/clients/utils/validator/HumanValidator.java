package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client_detail.HumanDTO;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HumanValidator {
    private static final String SNILS_PATTERN = "^([0-9]{3}[-]{1}[0-9]{3}[-]{1}[0-9]{3}[\\s]{1}[0-9]{2})?$"; //123-123-123 12
    private static final String TEXT_LIMIT = "^.{1,255}$";
    private static final String LONG_TEXT_LIMIT = "^.{1,1000}$";

    public static Pair<Boolean, List<String>> validate(HumanDTO humanDTO) {
        boolean result = true;
        List<String> messages = new ArrayList<>();

        if (humanDTO == null) {
            messages.add("Данные о физлице полностью отсутствуют");
            return Pair.of(false, messages);
        }

        if (!StringUtils.isEmpty(humanDTO.getFirstName()) && !humanDTO.getFirstName().matches(TEXT_LIMIT)) {
            messages.add("Имя слишком велико");
            result = false;
        }

        if (!StringUtils.isEmpty(humanDTO.getMiddleName()) && !humanDTO.getMiddleName().matches(TEXT_LIMIT)) {
            messages.add("Отчество слишком велико");
            result = false;
        }

        if (!StringUtils.isEmpty(humanDTO.getLastName()) && !humanDTO.getLastName().matches(TEXT_LIMIT)) {
            messages.add("Фамилия слишком велика");
            result = false;
        }

        if (!StringUtils.isEmpty(humanDTO.getSnils()) && !humanDTO.getSnils().trim().matches(SNILS_PATTERN)) {
            messages.add("СНИЛС не соответствует формату XXX-XXX-ХХХ ХХ");
            result = false;
        }

        if (!StringUtils.isEmpty(humanDTO.getRegistrationAddress()) && !humanDTO.getRegistrationAddress().matches(LONG_TEXT_LIMIT)) {
            messages.add("Адрес регистрации слишком велик");
            result = false;
        }

        if (!StringUtils.isEmpty(humanDTO.getInn()) && !TaxNumberUtils.isInnValid(humanDTO.getInn())) {
            messages.add("ИНН не соответствует стандартам");
            result = false;
        }

        if (!StringUtils.isEmpty(humanDTO.getCitizenship()) && !humanDTO.getCitizenship().matches(TEXT_LIMIT)) {
            messages.add("Значение поле гражданство слишком велико");
            result = false;
        }

        if (humanDTO.getPassports() != null) {
            Pair<Boolean, List<String>> passportValidation = humanDTO.getPassports().stream()
                    .map(PassportValidator::validate)
                    .filter(it -> !it.getFirst())
                    .findFirst()
                    .orElse(Pair.of(true, Collections.emptyList()));
            result = result && passportValidation.getFirst();
            messages.addAll(passportValidation.getSecond());
        }

        return Pair.of(result, messages);
    }
}
