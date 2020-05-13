package online.prostobank.clients.utils.validator;

import online.prostobank.clients.api.dto.client.PassportDTO;
import online.prostobank.clients.api.dto.client_detail.HumanPassportDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PassportValidator {
    private static final String SERIES_PATTERN = "^[0-9]{2}\\s?[0-9]{2}$"; // 12 34
    private static final String NUMBER_PATTERN = "^([0-9]{6})?$"; // 123456
    private static final String ISSUER_CODE_PATTERN = "^([0-9]{3}[-]{1}[0-9]{3})?$"; // 123-123
    private static final String SNILS_PATTERN = "^([0-9]{3}[-]{1}[0-9]{3}[-]{1}[0-9]{3}[\\s]{1}[0-9]{2})?$"; //123-123-123 12
    private static final String TEXT_LIMIT = "^.{1,255}$";

    public static Pair<Boolean, List<String>> validate(PassportDTO passportDTO) {
        boolean result = true;
        List<String> messages = new ArrayList<>();

        if (passportDTO == null) {
            messages.add("Данные о паспорте полностью отсутствуют");
            return Pair.of(false, messages);
        }

        if (isSeriesNotValid(passportDTO.getSeries())) {
            messages.add("Серия паспорта не соответствует формату XX XX");
            result = false;
        }

        if (isNumberNotValid(passportDTO.getNumber())) {
            messages.add("Номер паспорта не соответствует формату XXXXXX");
            result = false;
        }

        if (isIssuerCodeNotValid(passportDTO.getIssuerCode())) {
            messages.add("Код подразделения не соответствует формату XXX-XXX");
            result = false;
        }

        if (StringUtils.isNotBlank(passportDTO.getSnils()) && isSnilsNotValid(passportDTO.getSnils())) { // необязательное поле
            messages.add("СНИЛС не соответствует формату XXX-XXX-ХХХ ХХ");
            result = false;
        }

        if (isIssuerNameNotValid(passportDTO.getIssuer())) {
            messages.add("Наименование подразделения не указано или слишком велико");
            result = false;
        }

        if (isIssueDateNotValid(passportDTO.getDateOfIssue())) {
            messages.add("Дата выдачи не указана");
            result = false;
        }

        if (isPlaceOfBirthNotValid(passportDTO.getPlaceOfBirth())) {
            messages.add("Место рождения не указано");
            result = false;
        }

        if (isDateOfBirthNotValid(passportDTO.getDateOfBirth())) {
            messages.add("Дата рождения не указана");
            result = false;
        }
        return Pair.of(result, messages);
    }

    public static Pair<Boolean, List<String>> validate(HumanPassportDTO passportDTO) {
        boolean result = true;
        List<String> messages = new ArrayList<>();

        if (passportDTO == null) {
            messages.add("Данные о паспорте полностью отсутствуют");
            return Pair.of(false, messages);
        }

        if (isSeriesNotValid(passportDTO.getSeries())) {
            messages.add("Серия паспорта не соответствует формату XX XX");
            result = false;
        }

        if (isNumberNotValid(passportDTO.getNumber())) {
            messages.add("Номер паспорта не соответствует формату XXXXXX");
            result = false;
        }

        if (isIssuerCodeNotValid(passportDTO.getIssueDepartmentCode())) {
            messages.add("Код подразделения не соответствует формату XXX-XXX");
            result = false;
        }

        if (isIssuerNameNotValid(passportDTO.getIssueDepartmentName())) {
            messages.add("Наименование подразделения не указано или слишком велико");
            result = false;
        }

        if (isIssueDateNotValid(passportDTO.getIssueDate())) {
            messages.add("Дата выдачи не указана");
            result = false;
        }

        if (isPlaceOfBirthNotValid(passportDTO.getPlaceOfBirth())) {
            messages.add("Место рождения не указано");
            result = false;
        }

        if (isDateOfBirthNotValid(passportDTO.getDateOfBirth())) {
            messages.add("Дата рождения не указана");
            result = false;
        }
        return Pair.of(result, messages);
    }

    public static boolean isSeriesNotValid(String series) {
        return series == null || !series.trim().matches(SERIES_PATTERN);
    }

    public static boolean isNumberNotValid(String number) {
        return number == null || !number.trim().matches(NUMBER_PATTERN);
    }

    public static boolean isIssuerCodeNotValid(String code) {
        return code == null || !code.trim().matches(ISSUER_CODE_PATTERN);
    }

    public static boolean isIssuerNameNotValid(String name) {
        return StringUtils.isEmpty(name) || !name.trim().matches(TEXT_LIMIT);
    }

    public static boolean isPlaceOfBirthNotValid(String place) {
        return StringUtils.isEmpty(place) || !place.trim().matches(TEXT_LIMIT);
    }

    public static boolean isIssueDateNotValid(LocalDate date) {
        return date == null;
    }

    public static boolean isDateOfBirthNotValid(LocalDate date) {
        return date == null;
    }

    public static boolean isSnilsNotValid(String snils) {
        return !snils.trim().matches(SNILS_PATTERN);
    }
}
