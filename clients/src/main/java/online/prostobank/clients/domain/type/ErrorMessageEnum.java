package online.prostobank.clients.domain.type;

public enum ErrorMessageEnum {

    ONLY_RUSSIAN_CHAR("Можно вводить только кириллицу!"),
    INVALID_NUMBER("Введен невалидный номер телефона!"),
    CARD_IS_FILLED_INCORRECT("Карточка заполнена неверно!"),
    IS_REQUIRED("Поле обязательно!");


    private String errorText;

    ErrorMessageEnum(String errorText) {
        this.errorText = errorText;
    }

    public String getErrorText() {
        return errorText;
    }
}
