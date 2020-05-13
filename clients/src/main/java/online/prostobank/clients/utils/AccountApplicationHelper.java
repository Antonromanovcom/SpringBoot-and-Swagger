package online.prostobank.clients.utils;

import club.apibank.connectors.smartengines.model.FieldNames;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.PersonValue;
import online.prostobank.clients.domain.recognition.interfaces.IRecognizedDocument;
import online.prostobank.clients.utils.validator.PassportValidator;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AccountApplicationHelper {

    /**
     * Заполнение данных аккаунта сведениями из отсканированного документа
     * @param aa
     * @param document
     * @return - комментарий к распознанию список произведенных изменений
     */
    public static Result fillFromDocument(AccountApplication aa, IRecognizedDocument document) {
        PersonValue pv = aa.getPerson();
        StringBuilder changesForHistory = new StringBuilder();
        StringBuilder resultMessage = new StringBuilder();

        if (pv != null) {
            switch (document.getFunctionalType()) {
                case PASSPORT_FIRST:
                    document.getFieldValue(FieldNames.PassportSeries).ifPresent(value -> {
                        if (PassportValidator.isSeriesNotValid(value)) {
                            addFailHistoryItem(changesForHistory, FieldNames.PassportSeries, pv.getSer(),
                                    "Серия паспорта не соответствует формату XX XX");
                        } else {
                            addHistoryItem(changesForHistory, FieldNames.PassportSeries, pv.getSer(), value);
                            pv.setSer(value);
                        }
                    });
                    document.getFieldValue(FieldNames.PassportNumber).ifPresent(value -> {
                        if (PassportValidator.isNumberNotValid(value)) {
                            addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                    "Номер паспорта не соответствует формату XXXXXX");
                        } else {
                            addHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(), value);
                            pv.setNum(value);
                        }
                    });

                    try {
                        document.getFieldValue(FieldNames.PassportBirthDate).ifPresent(value ->
                        {
                            String oldValue = pv.getDob() == null ? StringUtils.EMPTY : pv.getDob().toString();
                            pv.setDob(LocalDate.parse(value, Utils.DD_MM_YYYY_RU_FORMATTER));
                            addHistoryItem(changesForHistory, FieldNames.PassportBirthDate, oldValue, value);
                        });
                    } catch (DateTimeParseException ex) {
                        addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                "Дата рождения отсутствует или не соответствует формату");
                        resultMessage.append("Не удалось распознать дату рождения. ");
                    }
                    try {
                        document.getFieldValue(FieldNames.PassportIssueDate).ifPresent(value ->
                        {
                            String oldValue = pv.getDoi() == null ? StringUtils.EMPTY : pv.getDoi().toString();
                            pv.setDoi(LocalDate.parse(value, Utils.DD_MM_YYYY_RU_FORMATTER));
                            addHistoryItem(changesForHistory, FieldNames.PassportIssueDate, oldValue, value);
                        });
                    } catch (DateTimeParseException ex) {
                        addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                "Дата выдачи отсутствует или не соответствует формату");
                        resultMessage.append("Не удалось распознать дату выдачи паспорта. ");
                    }
                    document.getFieldValue(FieldNames.PassportAuthority).ifPresent(value -> {
                        if (PassportValidator.isIssuerNameNotValid(value)) {
                            addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                    "Наименование подразделения не указано или слишком велико");
                        } else {
                            addHistoryItem(changesForHistory, FieldNames.PassportAuthority, pv.getIssuer(), value);
                            pv.setIssuer(value);
                        }
                    });
                    document.getFieldValue(FieldNames.PassportAuthorityCode).ifPresent(value -> {
                        if (PassportValidator.isIssuerCodeNotValid(value)) {
                            addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                    "Код подразделения не соответствует формату XXX-XXX");
                        } else {
                            addHistoryItem(changesForHistory, FieldNames.PassportAuthorityCode, pv.getIssuerCode(), value);
                            pv.setIssuerCode(value);
                        }
                    });
                    document.getFieldValue(FieldNames.PassportBirthPlace).ifPresent(value -> {
                        if (PassportValidator.isPlaceOfBirthNotValid(value)) {
                            addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                    "Место рождения не указано");
                        } else {
                            addHistoryItem(changesForHistory, FieldNames.PassportBirthPlace, pv.getPob(), value);
                            pv.setPob(value);
                        }
                    });
                    break;
                case SNILS:
                    document.getFieldValue(FieldNames.SnilsNumber).ifPresent(value -> {
                        if (PassportValidator.isSnilsNotValid(value)) {
                            addFailHistoryItem(changesForHistory, FieldNames.PassportNumber, pv.getNum(),
                                    "СНИЛС не соответствует формату XXX-XXX-ХХХ ХХ");
                        } else {
                            addHistoryItem(changesForHistory, FieldNames.SnilsNumber, pv.getSnils(), value);
                            pv.setSnils(value);
                        }
                    });
                    break;
            }
        }
        return new Result(changesForHistory.toString(), resultMessage.toString());
    }

    public static class Result {
        private String historyMessage;
        private String comments;

        public Result(String historyMessage, String comments) {
            this.historyMessage = historyMessage;
            this.comments = comments;
        }

        public String getHistoryMessage() {
            return historyMessage;
        }

        public String getComments() {
            return comments;
        }
    }

    private static void addHistoryItem(StringBuilder changesForHistory, String fieldName, String oldValue, String newValue) {
        String historyFormat = "Изменено '%s'. Было: '%s', стало: '%s'.";
        oldValue = StringUtils.isBlank(oldValue) ? "пусто" : oldValue;
        newValue = StringUtils.isBlank(newValue) ? "пусто" : newValue;
        changesForHistory.append(String.format(historyFormat, fieldName, oldValue, newValue));
        changesForHistory.append("\n");
    }

    private static void addFailHistoryItem(StringBuilder changesForHistory, String fieldName, String oldValue, String newValue) {
        String historyFormat = "Не изменено '%s'. Было: '%s', причина отмены изменений: '%s'.";
        oldValue = StringUtils.isBlank(oldValue) ? "пусто" : oldValue;
        newValue = StringUtils.isBlank(newValue) ? "пусто" : newValue;
        changesForHistory.append(String.format(historyFormat, fieldName, oldValue, newValue));
        changesForHistory.append("\n");
    }
}
